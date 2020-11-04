/*
 * Copyright 2020-2021 Better Ltd (www.better.care)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.openehr.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nedap.archie.rm.support.identification.HierObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehr.data.OpenEhrContributionAudit;
import org.openehr.data.OpenEhrContributionRequest;
import org.openehr.data.OpenEhrContributionVersion;
import org.openehr.data.OpenEhrErrorResponse;
import org.openehr.rest.conf.WebClientConfiguration;
import org.openehr.utils.LocatableUid;
import org.openehr.utils.Utils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;

/**
 * @author Dusan Markovic
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class})
@TestPropertySource(value = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {WebClientConfiguration.class})
public class OpenEhrContributionRestTest extends AbstractRestTest {

    @BeforeEach
    @Override
    public void setUp() throws IOException {
        super.setUp();
    }

    @Test
    public void createContribution() throws JsonProcessingException {

        HttpHeaders headers = fullRepresentationHeaders();
        // 200
        String request = createRequestData("CREATION", "CREATION", "MODIFICATION", "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        ResponseEntity<JsonNode> response = exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, JsonNode.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        validateLocationAndETag(response);
        JsonNode contribution = response.getBody();
        assertThat(contribution).isNotNull();
        assertThat(contribution.get("versions")).hasSize(3);
        assertThat(contribution.get("versions").get(0).get("_type").asText()).isEqualTo("OBJECT_REF");
        assertThat(contribution.get("versions").get(1).get("_type").asText()).isEqualTo("OBJECT_REF");
        assertThat(contribution.get("versions").get(2).get("_type").asText()).isEqualTo("OBJECT_REF");
        assertThat(stream(spliteratorUnknownSize(contribution.get("versions").elements(), ORDERED), false)
                           .map(el -> el.get("type").asText())
                           .collect(Collectors.toList()))
                .containsOnly("FOLDER", "COMPOSITION", "EHR_STATUS");
        validateLocationHeader("CONTRIBUTION", response.getHeaders().getLocation(), getUid(contribution), this::getUid);
    }

    @Test
    public void createContributionResolveRefs() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Prefer", "return=representation, resolve_refs");
        // 200
        String request = createRequestData("CREATION", "CREATION", "MODIFICATION", "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        ResponseEntity<JsonNode> response = exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, JsonNode.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        validateLocationAndETag(response);
        JsonNode contribution = response.getBody();
        assertThat(contribution).isNotNull();
        assertThat(contribution.get("versions")).hasSize(3);
        assertThat(contribution.get("versions").get(0).get("_type").asText()).isEqualTo("COMPOSITION");
        assertThat(contribution.get("versions").get(1).get("_type").asText()).isEqualTo("FOLDER");
        assertThat(contribution.get("versions").get(2).get("_type").asText()).isEqualTo("EHR_STATUS");

        validateLocationHeader(
                "CONTRIBUTION",
                response.getHeaders().getLocation(),
                getUid(contribution),
                this::getUid);
    }

    @Test
    public void createContribution400() throws JsonProcessingException {
        HttpHeaders headers = fullRepresentationHeaders();

        // 400 - EhrStatus can't be created or deleted
        String request = createRequestData("CREATION", "CREATION", "CREATION", "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException.getResponseBodyAsString()).isNotEmpty();

        String request1 = createRequestData("CREATION", "CREATION", "DELETED", "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request1, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException1.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException1.getResponseBodyAsString()).isNotEmpty();

        // 400 - nonexisting EhrStatus can't be modified
        String request2 = createRequestData("CREATION", "CREATION", "MODIFICATION", "JanezBananez", "NotJanezBananez",
                                            "NotNotJanezBananez");
        JsonNode requestNode = objectMapper.readTree(request2);
        Optional<JsonNode> ehrStatusVersion = stream(spliteratorUnknownSize(requestNode.get("versions").elements(), ORDERED), false)
                .filter(v -> "EHR_STATUS".equals(v.get("data").get("_type").asText()))
                .findFirst();
        ehrStatusVersion.ifPresent(es -> {
            HierObjectId hierObjectId = new HierObjectId();
            hierObjectId.setValue(nonExistingUid);
            ((ObjectNode)es.get("data")).set("uid", objectMapper.convertValue(hierObjectId, JsonNode.class));
        });
        HttpStatusCodeException httpException2 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, requestNode, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException2.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException2.getResponseBodyAsString()).isNotEmpty();

        // 400 - nonexisting Folder can't be deleted or modified
        String request3 = createRequestData("CREATION", "DELETED", "MODIFICATION", "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        HttpStatusCodeException httpException3 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request3, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException3.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException3.getResponseBodyAsString()).isNotEmpty();

        String request4 = createRequestData("CREATION", "MODIFICATION", "MODIFICATION", "JanezBananez", "NotJanezBananez",
                                            "NotNotJanezBananez");
        HttpStatusCodeException httpException4 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request4, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException4.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException4.getResponseBodyAsString()).isNotEmpty();

        // 400 - nonexisting Composition can't be deleted or modified
        String request5 = createRequestData("DELETED", "CREATION", "MODIFICATION", "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        JsonNode requestNode1 = objectMapper.readTree(request5);
        Optional<JsonNode> ehrStatusVersion1 = stream(spliteratorUnknownSize(requestNode1.get("versions").elements(), ORDERED), false)
                .filter(v -> "COMPOSITION".equals(v.get("data").get("_type").asText()))
                .findFirst();
        ehrStatusVersion1.ifPresent(c -> setCompositionUid(c.get("data"), new LocatableUid(nonExistingUid)));
        HttpStatusCodeException httpException5 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, requestNode1, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException5.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException5.getResponseBodyAsString()).isNotEmpty();

        String request6 = createRequestData("MODIFICATION", "CREATION", "MODIFICATION", "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        JsonNode requestNode2 = objectMapper.readTree(request6);
        Optional<JsonNode> ehrStatusVersion2 = stream(spliteratorUnknownSize(requestNode2.get("versions").elements(), ORDERED), false)
                .filter(v -> "COMPOSITION".equals(v.get("data").get("_type").asText()))
                .findFirst();
        ehrStatusVersion2.ifPresent(c -> setCompositionUid(c.get("data"), new LocatableUid(nonExistingUid)));
        HttpStatusCodeException httpException6 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, requestNode2, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException6.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException6.getResponseBodyAsString()).isNotEmpty();
    }

    @Test
    public void createContribution404() throws JsonProcessingException {
        String request = createRequestData(
                "CREATION", "CREATION", "MODIFICATION", "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(
                        getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, OpenEhrErrorResponse.class, fullRepresentationHeaders(), "blablabla"));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void createContributionFailedValidation() throws JsonProcessingException {
        String request = createRequestData(
                "CREATION", "CREATION", "MODIFICATION", "JanezBananez", "NotJanezBananez", "NotNotJanezBananez", unProcessableComposition);
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, String.class, fullRepresentationHeaders(), ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotEmpty();
        assertThat(body).contains("validation failed");
    }

    @Test
    public void retrieveContribution() throws JsonProcessingException {
        HttpHeaders headers = fullRepresentationHeaders();

        String request = createRequestData(
                "CREATION", "CREATION", "MODIFICATION", "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        ResponseEntity<JsonNode> response = exchange(
                getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, JsonNode.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        JsonNode contribution = response.getBody();
        String contributionUid = getUid(contribution);

        // 200
        response = getResponse(getTargetPath() + "/ehr/{ehr_id}/contribution/{contribution_uid}", JsonNode.class, ehrId, contributionUid);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        JsonNode contribution1 = response.getBody();
        assertThat(contribution1).isNotNull();
        assertThat(contribution1.has("uid")).isTrue();
        assertThat(getUid(contribution1)).isEqualTo(contributionUid);

        // 404 nonexistant ehrUid or contributionUid
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(getTargetPath() + "/ehr/{ehr_id}/contribution/{contribution_uid}", String.class, "blablabla", contributionUid));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException, false, false);

        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(getTargetPath() + "/ehr/{ehr_id}/contribution/{contribution_uid}", String.class, ehrId, nonExistingUid));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException1, false, false);
    }

    private String createRequestData(
            String compositionAuditChangeType,
            String folderAuditChangeType,
            String ehrStatusAuditChangeType,
            String outerCommiterName,
            String compositionCommiterName,
            String folderCommiterName) throws JsonProcessingException {
        return this.createRequestData(
                compositionAuditChangeType,
                folderAuditChangeType,
                ehrStatusAuditChangeType,
                outerCommiterName, compositionCommiterName, folderCommiterName,
                composition);
    }

    private String createRequestData(
            String compositionAuditChangeType,
            String folderAuditChangeType,
            String ehrStatusAuditChangeType,
            String outerCommiterName,
            String compositionCommiterName,
            String folderCommiterName,
            JsonNode composition) throws JsonProcessingException {
        OpenEhrContributionRequest request = new OpenEhrContributionRequest();
        OpenEhrContributionAudit audit = new OpenEhrContributionAudit();
        audit.setCommitter(Utils.getPartyIdentified(outerCommiterName));
        request.setAudit(audit);

        List<OpenEhrContributionVersion> versions = new ArrayList<>();

        OpenEhrContributionVersion compositionVersion = new OpenEhrContributionVersion();
        compositionVersion.setData(composition);
        OpenEhrContributionVersion.OpenEhrCommitAudit contributionCommitAudit = new OpenEhrContributionVersion.OpenEhrCommitAudit();
        contributionCommitAudit.setChangeType(compositionAuditChangeType);
        contributionCommitAudit.setDescription("Composition CREATE contribution");
        contributionCommitAudit.setCommitter(Utils.getPartyIdentified(compositionCommiterName));
        compositionVersion.setCommitAudit(contributionCommitAudit);
        versions.add(compositionVersion);

        OpenEhrContributionVersion folderVersion = new OpenEhrContributionVersion();
        folderVersion.setData(createFolderWithSubfolder("parentFolderName", "subfolderName"));
        OpenEhrContributionVersion.OpenEhrCommitAudit folderCommitAudit = new OpenEhrContributionVersion.OpenEhrCommitAudit();
        folderCommitAudit.setChangeType(folderAuditChangeType);
        folderCommitAudit.setDescription("Folder CREATE contribution");
        folderCommitAudit.setCommitter(Utils.getPartyIdentified(folderCommiterName));
        folderVersion.setCommitAudit(folderCommitAudit);
        versions.add(folderVersion);

        OpenEhrContributionVersion ehrStatusVersion = new OpenEhrContributionVersion();
        ehrStatusVersion.setData(
                objectMapper.readTree("{\"_type\":\"EHR_STATUS\",\"subject\":{\"_type\":\"PARTY_SELF\"},\"is_queryable\":false,\"is_modifiable\":true}"));
        OpenEhrContributionVersion.OpenEhrCommitAudit ehrStatusCommitAudit = new OpenEhrContributionVersion.OpenEhrCommitAudit();
        ehrStatusCommitAudit.setChangeType(ehrStatusAuditChangeType);
        ehrStatusCommitAudit.setDescription("EhrStatus CREATE contribution");
        ehrStatusVersion.setCommitAudit(ehrStatusCommitAudit);
        versions.add(ehrStatusVersion);

        request.setVersions(versions);
        return objectMapper.writeValueAsString(request);
    }
}
