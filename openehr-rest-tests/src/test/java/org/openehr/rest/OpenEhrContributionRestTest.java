package org.openehr.rest;

import care.better.platform.locatable.LocatableUid;
import care.better.platform.model.EhrStatus;
import care.better.platform.service.AuditChangeType;
import care.better.platform.util.ConversionUtils;
import com.fasterxml.jackson.databind.JsonNode;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static care.better.platform.service.AuditChangeType.*;
import static org.assertj.core.api.Assertions.assertThat;
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
        ResponseEntity<OpenEhrErrorResponse> response = exchange(getTargetPath() + "/ehr/{ehr_id}/contribution",
                                                                 POST,
                                                                 request,
                                                                 OpenEhrErrorResponse.class,
                                                                 headers,
                                                                 ehrId);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        request = createRequestData(CREATION, CREATION, DELETED, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        response = exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, OpenEhrErrorResponse.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        // 400 - nonexisting EhrStatus can't be modified
        request = createRequestData(CREATION, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        Optional<OpenEhrContributionVersion> ehrStatusVersion = request.getVersions().stream().filter(v -> v.getData() instanceof EhrStatus).findFirst();
        ehrStatusVersion.ifPresent(es -> {
            HierObjectId hierObjectId = new HierObjectId();
            hierObjectId.setValue(nonExistingUid);
            ((EhrStatus)es.getData()).setUid(hierObjectId);
        });
        response = exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, OpenEhrErrorResponse.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        // 400 - nonexisting Folder can't be deleted or modified
        request = createRequestData(CREATION, DELETED, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        response = exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, OpenEhrErrorResponse.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        request = createRequestData(CREATION, MODIFICATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        response = exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, OpenEhrErrorResponse.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        // 400 - nonexisting Composition can't be deleted or modified
        request = createRequestData(DELETED, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        ehrStatusVersion = request.getVersions().stream().filter(v -> v.getData() instanceof Composition).findFirst();
        ehrStatusVersion.ifPresent(c -> setCompositionUid((Composition)c.getData(), new LocatableUid(nonExistingUid)));
        response = exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, OpenEhrErrorResponse.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        request = createRequestData(MODIFICATION, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        ehrStatusVersion = request.getVersions().stream().filter(v -> v.getData() instanceof Composition).findFirst();
        ehrStatusVersion.ifPresent(c -> setCompositionUid((Composition)c.getData(), new LocatableUid(nonExistingUid)));
        response = exchange(getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, OpenEhrErrorResponse.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void createContribution404() {
        OpenEhrContributionRequest request = createRequestData(
                CREATION, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez");
        ResponseEntity<OpenEhrErrorResponse> response = exchange(
                getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, OpenEhrErrorResponse.class, fullRepresentationHeaders(), "blablabla");
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void createContributionFailedValidation() {
        OpenEhrContributionRequest request = createRequestData(
                CREATION, CREATION, MODIFICATION, "JanezBananez", "NotJanezBananez", "NotNotJanezBananez", unProcessableComposition);
        ResponseEntity<JsonNode> response = exchange(
                getTargetPath() + "/ehr/{ehr_id}/contribution", POST, request, JsonNode.class, fullRepresentationHeaders(), ehrId);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message").asText()).isEqualTo("Composition validation failed (there are missing or invalid values).");
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
        ResponseEntity<JsonNode> notFoundResponse = getResponse(getTargetPath() + "/ehr/{ehr_id}/contribution/{contribution_uid}",
                                                                JsonNode.class,
                                                                "blablabla",
                                                                contributionUid);
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(notFoundResponse, false, false);
        notFoundResponse = getResponse(getTargetPath() + "/ehr/{ehr_id}/contribution/{contribution_uid}", JsonNode.class, ehrId, nonExistingUid);
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(notFoundResponse, false, false);
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
