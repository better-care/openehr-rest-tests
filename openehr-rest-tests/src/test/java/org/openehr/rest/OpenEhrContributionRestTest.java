/*
 * Copyright (C) 2020 Better d.o.o.
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

import care.better.platform.locatable.LocatableUid;
import care.better.platform.model.EhrStatus;
import care.better.platform.service.AuditChangeType;
import care.better.platform.util.ConversionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehr.data.OpenEhrContributionAudit;
import org.openehr.data.OpenEhrContributionRequest;
import org.openehr.data.OpenEhrContributionVersion;
import org.openehr.data.OpenEhrErrorResponse;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.Contribution;
import org.openehr.jaxb.rm.Folder;
import org.openehr.jaxb.rm.HierObjectId;
import org.openehr.jaxb.rm.ObjectRef;
import org.openehr.jaxb.rm.PartySelf;
import org.openehr.rest.conf.WebClientConfiguration;
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

import static care.better.platform.service.AuditChangeType.*;
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
    public void createContribution() {

        HttpHeaders headers = fullRepresentationHeaders();
        // 200
        OpenEhrContributionRequest request = createRequestData(
                CREATION, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        ResponseEntity<Contribution> response = exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, Contribution.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        validateLocationAndETag(response);
        Contribution contribution = response.getBody();
        assertThat(contribution).isNotNull();
        assertThat(contribution.getVersions()).hasSize(3);
        assertThat(contribution.getVersions().get(0)).isOfAnyClassIn(ObjectRef.class);
        assertThat(contribution.getVersions().get(1)).isOfAnyClassIn(ObjectRef.class);
        assertThat(contribution.getVersions().get(2)).isOfAnyClassIn(ObjectRef.class);
        assertThat(contribution.getVersions().stream()
                           .map(v -> (ObjectRef)v)
                           .map(ObjectRef::getType)
                           .collect(Collectors.toList()))
                .containsOnly("FOLDER", "COMPOSITION", "EHR_STATUS");
        validateLocationHeader(
                Contribution.class,
                response.getHeaders().getLocation(),
                contribution.getUid().getValue(),
                (Contribution f) -> f.getUid().getValue());
    }

    @Test
    public void createContributionResolveRefs() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Prefer", "return=representation, resolve_refs");
        // 200
        OpenEhrContributionRequest request = createRequestData(
                CREATION, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        ResponseEntity<Contribution> response = exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, Contribution.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        validateLocationAndETag(response);
        Contribution contribution = response.getBody();
        assertThat(contribution).isNotNull();
        assertThat(contribution.getVersions()).hasSize(3);
        assertThat(contribution.getVersions().get(0)).isOfAnyClassIn(Composition.class);
        assertThat(contribution.getVersions().get(1)).isOfAnyClassIn(Folder.class);
        assertThat(contribution.getVersions().get(2)).isOfAnyClassIn(EhrStatus.class);

        validateLocationHeader(
                Contribution.class,
                response.getHeaders().getLocation(),
                contribution.getUid().getValue(),
                (Contribution f) -> f.getUid().getValue());
    }

    @Test
    public void createContribution400() {
        HttpHeaders headers = fullRepresentationHeaders();

        // 400 - EhrStatus can't be created or deleted
        OpenEhrContributionRequest request = createRequestData(CREATION, CREATION, CREATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException.getResponseBodyAsString()).isNotEmpty();

        OpenEhrContributionRequest request1 = createRequestData(CREATION, CREATION, DELETED, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request1, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException1.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException1.getResponseBodyAsString()).isNotEmpty();

        // 400 - nonexisting EhrStatus can't be modified
        OpenEhrContributionRequest request2 = createRequestData(CREATION, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        Optional<OpenEhrContributionVersion> ehrStatusVersion = request2.getVersions().stream().filter(v -> v.getData() instanceof EhrStatus).findFirst();
        ehrStatusVersion.ifPresent(es -> {
            HierObjectId hierObjectId = new HierObjectId();
            hierObjectId.setValue(nonExistingUid);
            ((EhrStatus)es.getData()).setUid(hierObjectId);
        });
        HttpStatusCodeException httpException2 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request2, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException2.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException2.getResponseBodyAsString()).isNotEmpty();

        // 400 - nonexisting Folder can't be deleted or modified
        OpenEhrContributionRequest request3 = createRequestData(CREATION, DELETED, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        HttpStatusCodeException httpException3 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request3, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException3.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException3.getResponseBodyAsString()).isNotEmpty();

        OpenEhrContributionRequest request4 = createRequestData(CREATION, MODIFICATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        HttpStatusCodeException httpException4 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request4, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException4.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException4.getResponseBodyAsString()).isNotEmpty();

        // 400 - nonexisting Composition can't be deleted or modified
        OpenEhrContributionRequest request5 = createRequestData(DELETED, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        ehrStatusVersion = request5.getVersions().stream().filter(v -> v.getData() instanceof Composition).findFirst();
        ehrStatusVersion.ifPresent(c -> setCompositionUid((Composition)c.getData(), new LocatableUid(nonExistingUid)));
        HttpStatusCodeException httpException5 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request5, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException5.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException5.getResponseBodyAsString()).isNotEmpty();

        OpenEhrContributionRequest request6 = createRequestData(MODIFICATION, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        ehrStatusVersion = request6.getVersions().stream().filter(v -> v.getData() instanceof Composition).findFirst();
        ehrStatusVersion.ifPresent(c -> setCompositionUid((Composition)c.getData(), new LocatableUid(nonExistingUid)));
        HttpStatusCodeException httpException6 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request6, OpenEhrErrorResponse.class, headers, ehrId));
        assertThat(httpException6.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException6.getResponseBodyAsString()).isNotEmpty();
    }

    @Test
    public void createContribution404() {
        OpenEhrContributionRequest request = createRequestData(
                CREATION, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(
                        getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, OpenEhrErrorResponse.class, fullRepresentationHeaders(), "blablabla"));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void createContributionFailedValidation() {
        OpenEhrContributionRequest request = createRequestData(
                CREATION, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez", unProcessableComposition);
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, String.class, fullRepresentationHeaders(), ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotEmpty();
        assertThat(body).contains("validation failed");
    }

    @Test
    public void retrieveContribution() {
        HttpHeaders headers = fullRepresentationHeaders();

        OpenEhrContributionRequest request = createRequestData(
                CREATION, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        ResponseEntity<Contribution> response = exchange(
                getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, Contribution.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        Contribution contribution = response.getBody();
        String contributionUid = contribution.getUid().getValue();

        // 200
        response = getResponse(getTargetPath() + "/ehr/{ehr_id}/contribution/{contribution_uid}", Contribution.class, ehrId, contributionUid);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        Contribution contribution1 = response.getBody();
        assertThat(contribution1).isNotNull();
        assertThat(contribution1.getUid()).isNotNull();
        assertThat(contribution1.getUid().getValue()).isEqualTo(contributionUid);

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

    private OpenEhrContributionRequest createRequestData(
            AuditChangeType compositionAuditChangeType,
            AuditChangeType folderAuditChangeType,
            AuditChangeType ehrStatusAuditChangeType,
            String outerCommiterName,
            String compositionCommiterName,
            String folderCommiterName) {
        return this.createRequestData(
                compositionAuditChangeType, folderAuditChangeType, ehrStatusAuditChangeType, outerCommiterName, compositionCommiterName, folderCommiterName,
                composition);
    }

    private OpenEhrContributionRequest createRequestData(
            AuditChangeType compositionAuditChangeType,
            AuditChangeType folderAuditChangeType,
            AuditChangeType ehrStatusAuditChangeType,
            String outerCommiterName,
            String compositionCommiterName,
            String folderCommiterName,
            Composition composition) {
        OpenEhrContributionRequest request = new OpenEhrContributionRequest();
        OpenEhrContributionAudit audit = new OpenEhrContributionAudit();
        audit.setCommitter(ConversionUtils.getPartyIdentified(outerCommiterName));
        request.setAudit(audit);

        List<OpenEhrContributionVersion> versions = new ArrayList<>();

        OpenEhrContributionVersion<Composition> compositionVersion = new OpenEhrContributionVersion<>();
        compositionVersion.setData(composition);
        OpenEhrContributionVersion.OpenEhrCommitAudit contributionCommitAudit = new OpenEhrContributionVersion.OpenEhrCommitAudit();
        contributionCommitAudit.setChangeType(compositionAuditChangeType);
        contributionCommitAudit.setDescription("Composition CREATE contribution");
        contributionCommitAudit.setCommitter(ConversionUtils.getPartyIdentified(compositionCommiterName));
        compositionVersion.setCommitAudit(contributionCommitAudit);
        versions.add(compositionVersion);

        OpenEhrContributionVersion<Folder> folderVersion = new OpenEhrContributionVersion<>();
        folderVersion.setData(createFolderWithSubfolder("parentFolderName", "subfolderName"));
        OpenEhrContributionVersion.OpenEhrCommitAudit folderCommitAudit = new OpenEhrContributionVersion.OpenEhrCommitAudit();
        folderCommitAudit.setChangeType(folderAuditChangeType);
        folderCommitAudit.setDescription("Folder CREATE contribution");
        folderCommitAudit.setCommitter(ConversionUtils.getPartyIdentified(folderCommiterName));
        folderVersion.setCommitAudit(folderCommitAudit);
        versions.add(folderVersion);

        OpenEhrContributionVersion<EhrStatus> ehrStatusVersion = new OpenEhrContributionVersion<>();
        EhrStatus status = new EhrStatus();
        status.setSubject(new PartySelf());
        status.setQueryable(false);
        ehrStatusVersion.setData(status);
        OpenEhrContributionVersion.OpenEhrCommitAudit ehrStatusCommitAudit = new OpenEhrContributionVersion.OpenEhrCommitAudit();
        ehrStatusCommitAudit.setChangeType(ehrStatusAuditChangeType);
        ehrStatusCommitAudit.setDescription("EhrStatus CREATE contribution");
        ehrStatusVersion.setCommitAudit(ehrStatusCommitAudit);
        versions.add(ehrStatusVersion);

        request.setVersions(versions);
        return request;
    }
}
