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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehr.data.OpenEhrConstants;
import org.openehr.data.OpenEhrErrorResponse;
import org.openehr.rest.conf.WebClientConfiguration;
import org.openehr.utils.LocatableUid;
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
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openehr.data.OpenEhrConstants.*;
import static org.springframework.http.HttpHeaders.IF_MATCH;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;

/**
 * @author Dusan Markovic
 */
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class})
@TestPropertySource(value = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {WebClientConfiguration.class})
public class OpenEhrCompositionRestTest extends AbstractRestTest {

    private String compositionUpdated;
    private String compositionWrongType;
    private DateTime before;

    @BeforeAll
    @Override
    public void setUp() throws IOException {
        super.setUp();

        String jsonCompositionWithPlaceholder = IOUtils.toString(
                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/AtemfrequenzTemplate-composition.json"),
                StandardCharsets.UTF_8);
        compositionUpdated = jsonCompositionWithPlaceholder.replace("{{REPLACE_THIS}}", "John Nurse");
        compositionWrongType = IOUtils.toString(
                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/AtemfrequenzTemplate-composition-wrong-type.json"),
                StandardCharsets.UTF_8);

        uploadTemplate("/rest/AtemfrequenzTemplate.opt");
        uploadTemplate("/rest/MedikationLoop.opt");

        String anotherComposition = jsonCompositionWithPlaceholder.replace("{{REPLACE_THIS}}", "Just Someone");
        compositionUid2 = postComposition(ehrId, anotherComposition);
        before = DateTime.now();
    }

    @Test
    public void createComposition() {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<JsonNode> response = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, JsonNode.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(getUid(body)).isNotNull();
        validateLocationAndETag(response);

        ResponseEntity<JsonNode> response1 = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, JsonNode.class, null, ehrId);
        assertThat(response1.getStatusCode()).isEqualTo(CREATED);
        JsonNode body1 = response1.getBody();
        assertThat(body1).isNull();
        validateLocationAndETag(response1);
    }

    @Test
    public void createComposition400() {
        HttpHeaders headers = fullRepresentationHeaders();
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + POST_COMPOSITION_PATH, POST, compositionWrongType, Object.class, headers, ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        validateLocationAndETag(httpException, false, false);

        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotEmpty();
        assertThat(body).contains("Could not resolve type");
    }

    @Test
    public void createCompositionValidationErrors() {
        HttpHeaders headers = fullRepresentationHeaders();
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + POST_COMPOSITION_PATH, POST, unProcessableComposition, String.class, headers, ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(UNPROCESSABLE_ENTITY);
        validateLocationAndETag(httpException, false, false);
        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotEmpty();
        assertThat(body).contains("validation failed");
    }

    @Test
    public void createIncompleteCompositionValidationErrors() {
        HttpHeaders headers = fullRepresentationHeaders();
        headers.add(OpenEhrConstants.VERSION_LIFECYCLE_STATE, "code_string=\"553\"");   // incomplete
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + POST_COMPOSITION_PATH, POST, unProcessableComposition, String.class, headers, ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(UNPROCESSABLE_ENTITY);
        validateLocationAndETag(httpException, false, false);
        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotEmpty();
        assertThat(body).contains("validation failed");
    }

    @Test
    public void createComposition404() {
        HttpHeaders headers = fullRepresentationHeaders();
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + POST_COMPOSITION_PATH, POST, composition, OpenEhrErrorResponse.class, headers, "blablabla"));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException, false, false);
        assertThat(httpException.getResponseBodyAsString()).isNotEmpty();
    }

    @Test
    public void updateComposition() {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<JsonNode> response = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, JsonNode.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
//        assertThat(body.getUid()).isNotNull();
//        assertThat(body.getComposer()).isInstanceOf(PartyIdentified.class);
//        assertThat(((PartyIdentified)body.getComposer()).getName()).isEqualTo("Jane Nurse");

        String versionUid = getUid(body);
        headers.set(IF_MATCH, versionUid);
        String compositionUid = new LocatableUid(versionUid).getUid();

        ResponseEntity<JsonNode> response1 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, PUT, compositionUpdated, JsonNode.class, headers, ehrId, compositionUid);
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response1);
        JsonNode body1 = response1.getBody();
//        assertThat(((PartyIdentified)body1.getComposer()).getName()).isEqualTo("John Nurse");
        String compositionUid1 = getUid(body1);
        // 400 input Composition is invalid
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + GET_COMPOSITION_PATH, PUT, compositionWrongType, String.class, headers, ehrId, compositionUid1));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        validateLocationAndETag(httpException, false, false);

        // 404 ehrUid not found
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + GET_COMPOSITION_PATH, PUT, composition, String.class, headers, "blablabla", compositionUid1));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException1, false, false);

        // 412 Precondition failed with wrong {version_uid}
        HttpStatusCodeException httpException2 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + GET_COMPOSITION_PATH, PUT, composition, String.class, headers, ehrId, versionUid));
        assertThat(httpException2.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(httpException2);
        String eTag = Objects.requireNonNull(httpException2.getResponseHeaders()).getETag();
        assertThat("\"" + new LocatableUid(versionUid).next() + '"').isEqualTo(eTag);

        // 412 Precondition failed with wrong Composition Uid
//        HierObjectId hierObjectId = new HierObjectId();
//        hierObjectId.setValue(UUID.randomUUID().toString());
//        composition.setUid(hierObjectId);
        HttpStatusCodeException httpException3 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + GET_COMPOSITION_PATH, PUT, composition, String.class, headers, ehrId, compositionUid1));
        assertThat(httpException3.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(httpException3);

        // 204
//        composition.setUid(null);
//        HttpHeaders simpleHeaders = new HttpHeaders();
//        simpleHeaders.set(IF_MATCH, versionUid1);
//        String compositionVersionUid1 = new LocatableUid(versionUid1).getUid();
//        ResponseEntity<String> response6 = exchange(
//                getTargetPath() + GET_COMPOSITION_PATH, PUT, composition, String.class, simpleHeaders, ehrId, compositionVersionUid1);
//        assertThat(response6.getStatusCode()).isEqualTo(NO_CONTENT);
//        validateLocationAndETag(response6);
//        assertThat(response6.getBody()).isNull();
    }

    @Test
    public void updateCompositionValidationErrors() {
        HttpHeaders headers = fullRepresentationHeaders();
        headers.set(IF_MATCH, compositionUid);
        String compositionSimpleUid = new LocatableUid(compositionUid).getUid();
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + GET_COMPOSITION_PATH, PUT, unProcessableComposition, String.class, headers, ehrId, compositionSimpleUid));
        assertThat(httpException.getStatusCode()).isEqualTo(UNPROCESSABLE_ENTITY);
        validateLocationAndETag(httpException, false, false);
        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotEmpty();
        assertThat(body).contains("validation failed");
    }

    @Test
    public void deleteComposition() {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<JsonNode> response = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, JsonNode.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        String versionUid = getUid(body);

        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, headers, "blablabla", versionUid));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException, false, false);

        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, headers, ehrId, "blablabla"));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException1, false, false);

        ResponseEntity<String> response3 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, headers, ehrId, versionUid);
        assertThat(response3.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response3);
        assertThat(response3.getBody()).isNull();


        HttpStatusCodeException httpException2 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, headers, ehrId, versionUid));
        assertThat(httpException2.getStatusCode()).isEqualTo(CONFLICT);
        validateLocationAndETag(httpException2);

        String compositionUid = new LocatableUid(versionUid).next().toString();
        HttpStatusCodeException httpException3 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, headers, ehrId, compositionUid));
        assertThat(httpException3.getStatusCode()).isEqualTo(BAD_REQUEST);
        validateLocationAndETag(httpException3);
    }

    @Test
    public void retrieveComposition() {
        // 200
        ResponseEntity<JsonNode> response = getResponse(getTargetPath() + GET_COMPOSITION_PATH, JsonNode.class, ehrId, compositionUid);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        JsonNode body = response.getBody();
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
        ResponseEntity<JsonNode> response = getResponse(getTargetPath() + GET_COMPOSITION_PATH, JsonNode.class, ehrId, deletedVersionUid);
        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        assertThat(response1.getHeaders().getETag()).isEqualTo('"' + deletedVersionUid + '"');
    }

    @Test
    public void retrieveComposition404() {
        // 404 nonexistent ehr
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(getTargetPath() + GET_COMPOSITION_PATH, OpenEhrErrorResponse.class, "blablabla", compositionUid));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
        // 404 nonexistent composition uid
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(getTargetPath() + GET_COMPOSITION_PATH, String.class, ehrId, nonExistingUid));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void retrieveCompositionByVersionAtTime() {
        HttpHeaders headers = fullRepresentationHeaders();

        ResponseEntity<JsonNode> response = exchange(
                getTargetPath() + POST_COMPOSITION_PATH, POST, composition, JsonNode.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        DateTime before = DateTime.now();

        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(getUid(body)).isNotNull();
        assertThat(body.get("composer").get("_type").asText()).isEqualTo("PARTY_IDENTIFIED");
        assertThat(body.get("composer").get("name").asText()).isEqualTo("Jane Nurse");

        String versionUid = getUid(body);
        headers.set(IF_MATCH, versionUid);
        String compositionUid = new LocatableUid(versionUid).getUid();

        ResponseEntity<JsonNode> response1 = exchange(getTargetPath() + GET_COMPOSITION_PATH, PUT, compositionUpdated, JsonNode.class,
                                                         headers, ehrId, compositionUid);
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response1);
        JsonNode body1 = response1.getBody();
        assertThat(body1).isNotNull();
        String uid = getUid(body1);
        assertThat(uid).isNotNull();
        assertThat(body1.get("composer").get("name").asText()).isEqualTo("John Nurse");
        LocatableUid locatableUid = new LocatableUid(uid);

        ResponseEntity<JsonNode> response2 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                locatableUid.getUid(),
                DATE_TIME_FORMATTER.print(before));
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response2, false, false);
        JsonNode body2 = response2.getBody();
        assertThat(body2).isNotNull();
        assertThat(getUid(body2)).isNotNull();
        assertThat(body2.get("composer").get("_type").asText()).isEqualTo("PARTY_IDENTIFIED");
        assertThat(body2.get("composer").get("name").asText()).isEqualTo("Jane Nurse");

        ResponseEntity<JsonNode> response3 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                locatableUid.getUid(),
                DATE_TIME_FORMATTER.print(DateTime.now()));
        assertThat(response3.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response3, false, false);
        JsonNode body3 = response3.getBody();
        assertThat(body3).isNotNull();
        assertThat(getUid(body3)).isNotNull();
        assertThat(body3.get("composer").get("_type").asText()).isEqualTo("PARTY_IDENTIFIED");
        assertThat(body3.get("composer").get("name").asText()).isEqualTo("John Nurse");
    }

    @Test
    public void retrieveCompositionByVersionAtTime204() {
        ResponseEntity<String> response1 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, null, ehrId, compositionUid2);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response1);
        LocatableUid locatableUid = new LocatableUid(compositionUid2);
        // 204 get deleted composition
        ResponseEntity<JsonNode> response = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                locatableUid.getUid(),
                DATE_TIME_FORMATTER.print(DateTime.now()));
        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response, false, false);
    }

    @Test
    public void retrieveCompositionByVersionAtTime404() {
        LocatableUid locatableUid = new LocatableUid(compositionUid2);
        // 404 nonexistent ehr
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                        OpenEhrErrorResponse.class,
                        "blablabla",
                        locatableUid.getUid(),
                        DATE_TIME_FORMATTER.print(DateTime.now())));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException, false, false);

        // 404 nonexistent composition uid
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                        OpenEhrErrorResponse.class,
                        ehrId,
                        UUID.randomUUID().toString(),
                        DATE_TIME_FORMATTER.print(DateTime.now())));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException1, false, false);

        // 404 nonexistent composition at version_at_time
        String invalidDateTimeString = "1018-12-29T12:40:57.995+02:00";
        HttpStatusCodeException httpException2 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/composition/{versioned_object_uid}?version_at_time={version_at_time}",
                        OpenEhrErrorResponse.class,
                        ehrId,
                        locatableUid.getUid(),
                        invalidDateTimeString));
        assertThat(httpException2.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException2, false, false);
    }

    @Test
    public void retrieveVersionedComposition() {
        LocatableUid locatableUid = new LocatableUid(compositionUid2);
        ResponseEntity<JsonNode> response = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_composition/{versioned_object_uid}",
                JsonNode.class,
                ehrId,
                locatableUid.getUid());
        assertThat(response.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response, false, false);
    }

    @Test
    public void retrieveVersionedComposition404() {
        LocatableUid locatableUid = new LocatableUid(compositionUid2);
        // 404 nonexistent ehr
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/versioned_composition/{versioned_object_uid}",
                        OpenEhrErrorResponse.class,
                        "blablablabla",
                        locatableUid.getUid()));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 nonexistent versionedObjectUid
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/versioned_composition/{versioned_object_uid}",
                        OpenEhrErrorResponse.class,
                        ehrId,
                        UUID.randomUUID().toString()));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void retrieveCompositionVersion() {

        LocatableUid locatableUid = new LocatableUid(compositionUid2);
        ResponseEntity<JsonNode> response = getResponse(
                getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                JsonNode.class,
                ehrId,
                locatableUid.getUid(),
                locatableUid.toString());
        assertThat(response.getStatusCode()).isEqualTo(OK);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.has("preceding_version_uid")).isFalse();
        assertThat(body.get("data")).isNotNull();
        assertThat(body.get("data").get("_type").asText()).isEqualTo("COMPOSITION");
        assertThat(getUid(body.get("data"))).isEqualTo(locatableUid.toString());
        validateLocationAndETag(response);
    }

    @Test
    public void retrieveCompositionVersion404() {
        LocatableUid locatableUid = new LocatableUid(compositionUid2);
        // 404 nonexistent ehr
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                        OpenEhrErrorResponse.class,
                        "blablablabla",
                        locatableUid.getUid(),
                        locatableUid.toString()));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 nonexistent versionedObjectUid
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                        OpenEhrErrorResponse.class,
                        ehrId,
                        UUID.randomUUID().toString(),
                        locatableUid.toString()));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 different versionedObjectUid and version_uid
        HttpStatusCodeException httpException2 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                        OpenEhrErrorResponse.class,
                        ehrId,
                        locatableUid.getUid(),
                        new LocatableUid("unknown", locatableUid.getSystemId(), 1)));
        assertThat(httpException2.getStatusCode()).isEqualTo(NOT_FOUND);

        // 400 different system id
        LocatableUid locatableUid1 = new LocatableUid(nonExistingUid);
        HttpStatusCodeException httpException3 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                        OpenEhrErrorResponse.class,
                        ehrId,
                        locatableUid1.getUid(),
                        nonExistingUid));
        assertThat(httpException3.getStatusCode()).isEqualTo(BAD_REQUEST);

        // 404 uid not matching
        HttpStatusCodeException httpException4 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                        OpenEhrErrorResponse.class,
                        ehrId,
                        locatableUid.getUid(),
                        nonExistingUid));
        assertThat(httpException4.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 nonexistent version
        String uid = locatableUid.getUid();
        LocatableUid locatableUid2 = new LocatableUid(uid, "some.other.system", locatableUid.getVersion());
        HttpStatusCodeException httpException5 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + GET_COMPOSITION_VERSION_PATH,
                        OpenEhrErrorResponse.class,
                        ehrId,
                        uid,
                        locatableUid2.toString()));
        assertThat(httpException5.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void retrieveCompositionVersionAtTime() throws InterruptedException {
        LocatableUid locatableUid = new LocatableUid(compositionUid2);
        Thread.sleep(100);
        ResponseEntity<String> response1 = exchange(
                getTargetPath() + GET_COMPOSITION_PATH, DELETE, null, String.class, null, ehrId, compositionUid2);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response1);

        ResponseEntity<JsonNode> response = getResponse(
                getTargetPath() + GET_VERSIONED_COMPOSITION_PATH + "?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                locatableUid.getUid(),
                DATE_TIME_FORMATTER.print(before));
        assertThat(response.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("data")).isNotNull();
        assertThat(body.get("lifecycle_state").get("value").asText()).isEqualTo("complete");

        DateTime after = DateTime.now();
        ResponseEntity<JsonNode> response2 = getResponse(
                getTargetPath() + GET_VERSIONED_COMPOSITION_PATH + "?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                locatableUid.getUid(),
                DATE_TIME_FORMATTER.print(after));
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response2);
        JsonNode body1 = response2.getBody();
        assertThat(body1).isNotNull();
        assertThat(body1.has("data")).isFalse();
        assertThat(body1.get("lifecycle_state").get("value").asText()).isEqualTo("deleted");

        compositionUid2 = postComposition(ehrId, compositionUpdated);
    }
}
