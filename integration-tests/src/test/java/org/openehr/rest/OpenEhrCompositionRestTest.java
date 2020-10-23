package org.openehr.rest;

import care.better.platform.locatable.LocatableUid;
import care.better.platform.model.Ehr;
import care.better.platform.model.VersionedObjectDto;
import care.better.platform.service.VersionLifecycleState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehr.data.OpenEhrConstants;
import org.openehr.data.OpenEhrErrorResponse;
import org.openehr.jaxb.am.Template;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.HierObjectId;
import org.openehr.jaxb.rm.Locatable;
import org.openehr.jaxb.rm.OriginalVersion;
import org.openehr.jaxb.rm.PartyIdentified;
import org.openehr.rest.conf.WebClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openehr.data.OpenEhrConstants.*;
import static org.springframework.http.HttpHeaders.IF_MATCH;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_XML;

/**
 * @author Dusan Markovic
 */
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class})
@TestPropertySource(value = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {WebClientConfiguration.class})
public class OpenEhrCompositionRestTest extends AbstractRestTest {

    @Autowired
    private ObjectMapper objectMapper;

    protected String compositionUid;
    protected String compositionUid5;
    protected String nonExistingUid;
    private Composition composition;
    private Composition compositionUpdated;
    private String compositionWrongType;
    protected String ehrId;

    @PostConstruct
    @Override
    public void setUp() throws IOException {
        super.setUp();
        String atemfrequenzTemplate = IOUtils.toString(
                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/AtemfrequenzTemplate.opt"),
                StandardCharsets.UTF_8);

        String medikationLoopTemplate = IOUtils.toString(
                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/MedikationLoop.opt"),
                StandardCharsets.UTF_8);

        String jsonCompositionWithPlaceholder = IOUtils.toString(
                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/AtemfrequenzTemplate-composition.json"),
                StandardCharsets.UTF_8);
        composition = objectMapper.readValue(jsonCompositionWithPlaceholder.replace("{{REPLACE_THIS}}", "Jane Nurse"), Composition.class);
        assertThat(composition).isNotNull();
        assertThat(composition.getUid()).isNull();
        compositionUpdated = objectMapper.readValue(jsonCompositionWithPlaceholder.replace("{{REPLACE_THIS}}", "John Nurse"), Composition.class);
        compositionWrongType = IOUtils.toString(
                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/AtemfrequenzTemplate-composition-wrong-type.json"),
                StandardCharsets.UTF_8);

        ResponseEntity<Ehr> ehrResponseEntity = exchange(getTargetPath() + "/ehr", POST, null, Ehr.class, fullRepresentationHeaders());
        ehrId = Objects.requireNonNull(ehrResponseEntity.getBody()).getEhrId().getValue();
        HttpHeaders headers = fullRepresentationHeaders();
        headers.setContentType(APPLICATION_XML);
        headers.setAccept(Collections.singletonList(APPLICATION_XML));
        ResponseEntity<Template> templateResponseEntity = exchange(
                getTargetPath() + "/definition/template/adl1.4", POST, atemfrequenzTemplate, Template.class, headers);
        assertThat(templateResponseEntity.getStatusCode()).isEqualTo(CREATED);
        ResponseEntity<Template> medikationLoopEntity = exchange(
                getTargetPath() + "/definition/template/adl1.4", POST, medikationLoopTemplate, Template.class, headers);
        assertThat(medikationLoopEntity.getStatusCode()).isEqualTo(CREATED);
        Composition medikationLoopComposition = objectMapper.readValue(IOUtils.toString(
                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/MedikationLoop.json"),
                StandardCharsets.UTF_8), Composition.class);
        try {
            ResponseEntity<Composition> response = exchange(
                    getTargetPath() + POST_COMPOSITION_PATH, POST, medikationLoopComposition, Composition.class, fullRepresentationHeaders(), ehrId);
            assertThat(response.getStatusCode()).isEqualTo(CREATED);
            compositionUid = Objects.requireNonNull(response.getBody()).getUid().getValue();
            compositionUid5 = compositionUid;
            nonExistingUid = UUID.randomUUID() + "::domain3::1";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createComposition() {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<Composition> response = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, Composition.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        Composition body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getUid()).isNotNull();
        validateLocationAndETag(response);

        ResponseEntity<Composition> response1 = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, Composition.class, null, ehrId);
        assertThat(response1.getStatusCode()).isEqualTo(CREATED);
        Composition body1 = response1.getBody();
        assertThat(body1).isNull();
        validateLocationAndETag(response1);
    }

    @Test
    public void createComposition400() {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<Object> response = exchange(getTargetPath() + POST_COMPOSITION_PATH, POST, compositionWrongType, Object.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);

        Object body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).isInstanceOf(Map.class);
        assertThat((Map<String, String>)body).containsEntry("message", "Cannot parse request data.");

        validateLocationAndETag(response, false, false);
    }

    @Test
    public void createCompositionValidationErrors() {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<JsonNode> response = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, JsonNode.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(UNPROCESSABLE_ENTITY);
        validateLocationAndETag(response, false, false);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message").asText()).isEqualTo("Composition validation failed (there are missing or invalid values).");
    }

    @Test
    public void createIncompleteCompositionValidationErrors() {
        HttpHeaders headers = fullRepresentationHeaders();
        headers.add(OpenEhrConstants.VERSION_LIFECYCLE_STATE, "code_string=\"" + VersionLifecycleState.INCOMPLETE.code() + "\"");
        ResponseEntity<JsonNode> response = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, JsonNode.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(UNPROCESSABLE_ENTITY);
        validateLocationAndETag(response, false, false);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message").asText()).isEqualTo("Incomplete composition validation failed (there are missing or invalid values).");
    }

    @Test
    public void createComposition404() {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<OpenEhrErrorResponse> response = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, OpenEhrErrorResponse.class, headers, "blablabla");
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        validateLocationAndETag(response, false, false);
    }

    @Test
    public void updateComposition() {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<Composition> response = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, Composition.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        Composition body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getUid()).isNotNull();
        assertThat(body.getComposer()).isInstanceOf(PartyIdentified.class);
        assertThat(((PartyIdentified)body.getComposer()).getName()).isEqualTo("Jane Nurse");

        String versionUid = body.getUid().getValue();
        headers.set(IF_MATCH, versionUid);
        String compositionUid = new LocatableUid(versionUid).getUid();

        ResponseEntity<Composition> response1 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, PUT, compositionUpdated, Composition.class, headers, ehrId, compositionUid);
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response1);
        Composition body1 = response1.getBody();
        assertThat(((PartyIdentified)body1.getComposer()).getName()).isEqualTo("John Nurse");

        // 400 input Composition is invalid
        ResponseEntity<JsonNode> response2 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, PUT, compositionWrongType, JsonNode.class, headers, ehrId,
                body1.getUid().getValue());
        assertThat(response2.getStatusCode()).isEqualTo(BAD_REQUEST);
        validateLocationAndETag(response2, false, false);

        // 404 ehrUid not found
        ResponseEntity<JsonNode> response3 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, PUT, composition, JsonNode.class, headers, "blablabla",
                body1.getUid().getValue());
        assertThat(response3.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(response3, false, false);

        // 412 Precondition failed with wrong {version_uid}
        ResponseEntity<JsonNode> response4 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, PUT, composition, JsonNode.class, headers, ehrId, versionUid);
        assertThat(response4.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(response4);
        String eTag = response4.getHeaders().getETag();
        assertThat("\"" + new LocatableUid(versionUid).next() + '"').isEqualTo(eTag);

        // 412 Precondition failed with wrong Composition Uid
        String versionUid1 = body1.getUid().getValue();
        HierObjectId hierObjectId = new HierObjectId();
        hierObjectId.setValue(UUID.randomUUID().toString());
        composition.setUid(hierObjectId);
        ResponseEntity<JsonNode> response5 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, PUT, composition, JsonNode.class, headers, ehrId, versionUid1);
        assertThat(response5.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(response5);

        // 204
        composition.setUid(null);
        HttpHeaders simpleHeaders = new HttpHeaders();
        simpleHeaders.set(IF_MATCH, versionUid1);
        String compositionVersionUid1 = new LocatableUid(versionUid1).getUid();
        ResponseEntity<JsonNode> response6 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, PUT, composition, JsonNode.class, simpleHeaders, ehrId, compositionVersionUid1);
        assertThat(response6.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response6);
        assertThat(response6.getBody()).isNull();
    }

    @Test
    public void updateCompositionValidationErrors() {
        HttpHeaders headers = fullRepresentationHeaders();
        headers.set(IF_MATCH, compositionUid);
        String compositionSimpleUid = new LocatableUid(compositionUid).getUid();

        ResponseEntity<JsonNode> response = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, PUT, composition, JsonNode.class, headers, ehrId, compositionSimpleUid);
        assertThat(response.getStatusCode()).isEqualTo(UNPROCESSABLE_ENTITY);
        validateLocationAndETag(response, false, false);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message").asText()).isEqualTo("Composition validation failed (there are missing or invalid values).");
    }

    @Test
    public void deleteComposition() {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<Composition> response = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, Composition.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        Composition body = response.getBody();
        assertThat(body).isNotNull();

        ResponseEntity<String> response1 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, headers, "blablabla",
                body.getUid().getValue());
        assertThat(response1.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(response1, false, false);

        ResponseEntity<String> response2 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, headers, ehrId, "blablabla");
        assertThat(response2.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(response2, false, false);

        ResponseEntity<String> response3 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, headers, ehrId, body.getUid().getValue());
        assertThat(response3.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response3);
        assertThat(response3.getBody()).isNull();

        String versionUid = body.getUid().getValue();

        ResponseEntity<String> response4 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH,
                DELETE,
                null,
                String.class,
                headers,
                ehrId,
                versionUid);
        assertThat(response4.getStatusCode()).isEqualTo(CONFLICT);
        validateLocationAndETag(response4);

        ResponseEntity<String> response5 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH,
                DELETE,
                null,
                String.class,
                headers,
                ehrId,
                new LocatableUid(body.getUid().getValue()).next().toString());
        assertThat(response5.getStatusCode()).isEqualTo(BAD_REQUEST);
        validateLocationAndETag(response4);
    }

    @Test
    public void retrieveComposition() {
        // 200
        ResponseEntity<Composition> response = getResponse(getTargetPath() + GET_COMPOSITION_PATH, Composition.class, ehrId, compositionUid);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        Composition body = response.getBody();
        assertThat(body).isNotNull();
    }

    @Test
    public void retrieveComposition204() {
        ResponseEntity<String> response1 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, null, ehrId, compositionUid);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response1);

        // 204 get deleted compostiion
        String deletedVersionUid = response1.getHeaders().getETag().replaceAll("\"", "");
        ResponseEntity<Composition> response = getResponse(getTargetPath() + GET_COMPOSITION_PATH, Composition.class, ehrId, deletedVersionUid);
        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        assertThat(response1.getHeaders().getETag()).isEqualTo('"' + deletedVersionUid + '"');
    }

    @Test
    public void retrieveComposition404() {
        // 404 nonexistent ehr
        ResponseEntity<OpenEhrErrorResponse> response = getResponse(
                getTargetPath() + GET_COMPOSITION_PATH, OpenEhrErrorResponse.class, "blablabla", compositionUid);
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        // 404 nonexistent composition uid
        ResponseEntity<JsonNode> response1 = getResponse(getTargetPath() + GET_COMPOSITION_PATH, JsonNode.class, ehrId, nonExistingUid);
        assertThat(response1.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void retrieveCompositionByVersionAtTime() {
        HttpHeaders headers = fullRepresentationHeaders();

        ResponseEntity<Composition> response = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, Composition.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        DateTime before = DateTime.now();

        Composition body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getUid()).isNotNull();
        assertThat(body.getComposer()).isInstanceOf(PartyIdentified.class);
        assertThat(((PartyIdentified)body.getComposer()).getName()).isEqualTo("Jane Nurse");

        String versionUid = body.getUid().getValue();
        headers.set(IF_MATCH, versionUid);
        String compositionUid = new LocatableUid(versionUid).getUid();

        ResponseEntity<Composition> response1 = exchange(getTargetPath() + GET_COMPOSITION_PATH, PUT, compositionUpdated, Composition.class,
                                                         headers, ehrId, compositionUid);
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response1);
        Composition body1 = response1.getBody();
        assertThat(body1).isNotNull();
        assertThat(body1.getUid()).isNotNull();
        assertThat(((PartyIdentified)body1.getComposer()).getName()).isEqualTo("John Nurse");
        LocatableUid locatableUid = new LocatableUid(body1.getUid().getValue());

        ResponseEntity<Composition> response2 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                Composition.class,
                ehrId,
                locatableUid.getUid(),
                DATE_TIME_FORMATTER.print(before));
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response2, false, false);
        Composition body2 = response2.getBody();
        assertThat(body2).isNotNull();
        assertThat(body2.getUid()).isNotNull();
        assertThat(body2.getComposer()).isInstanceOf(PartyIdentified.class);
        assertThat(((PartyIdentified)body2.getComposer()).getName()).isEqualTo("Jane Nurse");

        ResponseEntity<Composition> response3 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                Composition.class,
                ehrId,
                locatableUid.getUid(),
                DATE_TIME_FORMATTER.print(DateTime.now()));
        assertThat(response3.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response3, false, false);
        Composition body3 = response3.getBody();
        assertThat(body3).isNotNull();
        assertThat(body3.getUid()).isNotNull();
        assertThat(body3.getComposer()).isInstanceOf(PartyIdentified.class);
        assertThat(((PartyIdentified)body3.getComposer()).getName()).isEqualTo("John Nurse");
    }

    @Test
    public void retrieveCompositionByVersionAtTime204() {
        ResponseEntity<String> response1 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, null, ehrId, compositionUid5);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response1);
        LocatableUid locatableUid = new LocatableUid(compositionUid5);
        // 204 get deleted composition
        ResponseEntity<Composition> response = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                Composition.class,
                ehrId,
                locatableUid.getUid(),
                DATE_TIME_FORMATTER.print(DateTime.now()));
        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response, false, false);
    }

    @Test
    public void retrieveCompositionByVersionAtTime404() {
        LocatableUid locatableUid = new LocatableUid(compositionUid5);
        // 404 nonexistent ehr
        ResponseEntity<OpenEhrErrorResponse> response = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                OpenEhrErrorResponse.class,
                "blablabla",
                locatableUid.getUid(),
                DATE_TIME_FORMATTER.print(DateTime.now()));
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(response, false, false);

        // 404 nonexistent composition uid
        ResponseEntity<OpenEhrErrorResponse> response1 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                OpenEhrErrorResponse.class,
                ehrId,
                UUID.randomUUID().toString(),
                DATE_TIME_FORMATTER.print(DateTime.now()));
        assertThat(response1.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(response1, false, false);

        // 404 nonexistent composition at version_at_time
        String invalidDateTimeString = "1018-12-29T12:40:57.995+02:00";
        ResponseEntity<OpenEhrErrorResponse> response2 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                OpenEhrErrorResponse.class,
                ehrId,
                locatableUid.getUid(),
                invalidDateTimeString);
        assertThat(response2.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(response2, false, false);
    }

    @Test
    public void retrieveVersionedComposition() {
        LocatableUid locatableUid = new LocatableUid(compositionUid5);
        ResponseEntity<VersionedObjectDto> response = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_composition/{versioned_object_uid}",
                VersionedObjectDto.class,
                ehrId,
                locatableUid.getUid());
        assertThat(response.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response, false, false);
    }

    @Test
    public void retrieveVersionedComposition404() {
        LocatableUid locatableUid = new LocatableUid(compositionUid5);
        // 404 nonexistent ehr
        ResponseEntity<OpenEhrErrorResponse> response = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_composition/{versioned_object_uid}",
                OpenEhrErrorResponse.class,
                "blablablabla",
                locatableUid.getUid());
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 nonexistent versionedObjectUid
        ResponseEntity<OpenEhrErrorResponse> response1 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_composition/{versioned_object_uid}",
                OpenEhrErrorResponse.class,
                ehrId,
                UUID.randomUUID().toString());
        assertThat(response1.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void retrieveCompositionVersion() {

        LocatableUid locatableUid = new LocatableUid(compositionUid5);
        ResponseEntity<OriginalVersion> response = getResponse(
                getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                OriginalVersion.class,
                ehrId,
                locatableUid.getUid(),
                locatableUid.toString());
        assertThat(response.getStatusCode()).isEqualTo(OK);
        OriginalVersion body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getPrecedingVersionUid()).isNull();
        assertThat(body.getData()).isNotNull();
        assertThat(body.getData()).isOfAnyClassIn(Composition.class);
        assertThat(((Locatable)body.getData()).getUid().getValue()).isEqualTo(locatableUid.toString());
        validateLocationAndETag(response);
    }

    @Test
    public void retrieveCompositionVersion404() {
        LocatableUid locatableUid = new LocatableUid(compositionUid5);
        // 404 nonexistent ehr
        ResponseEntity<OpenEhrErrorResponse> response = getResponse(
                getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                OpenEhrErrorResponse.class,
                "blablablabla",
                locatableUid.getUid(),
                locatableUid.toString());
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 nonexistent versionedObjectUid
        ResponseEntity<OpenEhrErrorResponse> response1 = getResponse(
                getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                OpenEhrErrorResponse.class,
                ehrId,
                UUID.randomUUID().toString(),
                locatableUid.toString());
        assertThat(response1.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 different versionedObjectUid and version_uid
        ResponseEntity<OpenEhrErrorResponse> response2 = getResponse(
                getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                OpenEhrErrorResponse.class,
                ehrId,
                locatableUid.getUid(),
                new LocatableUid("unknown", locatableUid.getSystemId(), 1));
        assertThat(response2.getStatusCode()).isEqualTo(NOT_FOUND);

        // 400 different system id
        ResponseEntity<OpenEhrErrorResponse> response4 = getResponse(
                getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                OpenEhrErrorResponse.class,
                ehrId,
                locatableUid.getUid(),
                nonExistingUid);
        assertThat(response4.getStatusCode()).isEqualTo(BAD_REQUEST);

        // 404 nonexistent version
        LocatableUid locatableUid1 = new LocatableUid(nonExistingUid);
        ResponseEntity<OpenEhrErrorResponse> response3 = getResponse(
                getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                OpenEhrErrorResponse.class,
                ehrId,
                locatableUid1.getUid(),
                locatableUid1.toString());
        assertThat(response3.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void retrieveCompositionVersionAtTime() {
        LocatableUid locatableUid = new LocatableUid(compositionUid5);
        DateTime before = DateTime.now();

        ResponseEntity<String> response1 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, null, ehrId, compositionUid5);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response1);

        ResponseEntity<OriginalVersion> response = getResponse(
                getTargetPath() + GET_VERSIONED_COMPOSITION_PATH + "?version_at_time={version_at_time}",
                OriginalVersion.class,
                ehrId,
                locatableUid.getUid(),
                DATE_TIME_FORMATTER.print(before));
        assertThat(response.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response);
        OriginalVersion body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData()).isNotNull();
        assertThat(body.getLifecycleState().getValue()).isEqualTo(VersionLifecycleState.COMPLETE.value());

        DateTime after = DateTime.now();
        ResponseEntity<OriginalVersion> response2 = getResponse(
                getTargetPath() + GET_VERSIONED_COMPOSITION_PATH + "?version_at_time={version_at_time}",
                OriginalVersion.class,
                ehrId,
                locatableUid.getUid(),
                DATE_TIME_FORMATTER.print(after));
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response2);
        OriginalVersion body1 = response2.getBody();
        assertThat(body1).isNotNull();
        assertThat(body1.getData()).isNull();
        assertThat(body1.getLifecycleState().getValue()).isEqualTo(VersionLifecycleState.DELETED.value());
    }
}
