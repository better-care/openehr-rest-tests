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
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpHeaders.IF_MATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.*;

/**
 * @author Dusan Markovic
 */
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class})
@TestPropertySource(value = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {WebClientConfiguration.class})
public class OpenEhrEhrRestTest extends AbstractRestTest {

    private static final String PARTY_REF_UID = UUID.randomUUID().toString();

    @Test
    public void createEhrWithDefaultEhrStatus() throws IOException {
        ResponseEntity<JsonNode> response1 = exchange(getTargetPath() + "/ehr", POST, null, JsonNode.class);
        assertThat(response1.getStatusCode()).isEqualTo(CREATED);
        assertThat(response1.getBody()).isNull();
        validateLocationAndETag(response1);

        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<JsonNode> response2 = exchange(getTargetPath() + "/ehr", POST, null, JsonNode.class, headers);
        assertThat(response2.getStatusCode()).isEqualTo(CREATED);
        validateLocationAndETag(response2);
        JsonNode testEhr = response2.getBody();
        assertThat(testEhr).isNotNull();
        assertThat(getFieldValue(testEhr, "ehr_id")).isNotNull();
        JsonNode ehrStatus = testEhr.get("ehr_status");
        assertThat(ehrStatus).isNotNull();
        assertThat(testEhr.has("system_id")).isTrue();
        assertThat(testEhr.get("time_created")).isNotNull();
        assertThat(ehrStatus.has("is_queryable")).isTrue();
        assertThat(ehrStatus.has("is_modifiable")).isTrue();

        String ehrStatus1 = createEhrStatusString(true, false, "local", createRandomNumString());
        ResponseEntity<JsonNode> response3 = exchange(getTargetPath() + "/ehr", POST, ehrStatus1, JsonNode.class, headers);
        assertThat(response3.getStatusCode()).isEqualTo(CREATED);
        assertThat(response3.getBody()).isNotNull();
        JsonNode testEhr2 = response3.getBody();
        assertThat(testEhr2.get("ehr_status")).isNotNull();
        assertThat(testEhr2.get("ehr_status").has("is_queryable")).isTrue();
        assertThat(getUid(testEhr2.get("ehr_status"))).isNotNull();

        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr", POST, ehrStatus1, String.class, headers));
        assertThat(httpException.getStatusCode()).isEqualTo(CONFLICT);
    }

    @Test
    public void createEhrWithProvidedEhrId() {
        HttpHeaders headers = fullRepresentationHeaders();
        String ehrUid = UUID.randomUUID().toString();
        ResponseEntity<JsonNode> response1 = createEhrWithProvidedUidAndStatus(null, headers, ehrUid);
        assertThat(response1.getStatusCode()).isEqualTo(CREATED);
        validateLocationAndETag(response1);
        JsonNode testEhr = response1.getBody();
        assertThat(testEhr).isNotNull();
        assertThat(getFieldValue(testEhr, "ehr_id")).isEqualTo(ehrUid);
        JsonNode ehrStatus = testEhr.get("ehr_status");
        assertThat(ehrStatus).isNotNull();
        assertThat(testEhr.has("system_id")).isTrue();
        assertThat(testEhr.has("time_created")).isTrue();
        assertThat(testEhr.get("time_created")).isNotNull();

        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> createEhrWithProvidedUidAndStatus(null, headers, ehrUid));
        assertThat(httpException.getStatusCode()).isEqualTo(CONFLICT);
    }

    @Test
    public void tryCreateEhrWithInvalidProvidedEhrId() {
        HttpHeaders headers = fullRepresentationHeaders();
        String ehrUid = "invalid";

        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}", PUT, null, Object.class, headers, ehrUid));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);

        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotNull();
        assertThat(body).contains("UUID");
    }

    @Test
    public void createEhrWithProvidedStatus() {
        HttpHeaders headers = fullRepresentationHeaders();
        String ehrUid = UUID.randomUUID().toString();
        String genericId = createRandomNumString();
        ResponseEntity<JsonNode> response1 = createEhrWithProvidedUidAndStatus(createEhrStatusString(true, true, "local", genericId), headers, ehrUid);
        assertThat(response1.getStatusCode()).isEqualTo(CREATED);
        JsonNode testEhr = response1.getBody();
        assertThat(testEhr).isNotNull();
        assertThat(getFieldValue(testEhr, "ehr_id")).isEqualTo(ehrUid);
        JsonNode ehrStatus = testEhr.get("ehr_status");
        assertThat(ehrStatus).isNotNull();
        assertThat(ehrStatus.has("subject")).isTrue();
        assertThat(ehrStatus.get("subject").get("external_ref")).isNotNull();
        assertThat(ehrStatus.get("subject").get("external_ref").get("id").get("value").asText()).isEqualTo(genericId);
        assertThat(testEhr.has("system_id")).isTrue();
        assertThat(testEhr.has("time_created")).isTrue();
        validateLocationAndETag(response1);

        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> createEhrWithProvidedUidAndStatus(null, headers, ehrUid));
        assertThat(httpException.getStatusCode()).isEqualTo(CONFLICT);
    }

    @Test
    public void tryCreateEhrWithProvidedStatusWithMissingSubject() {
        HttpHeaders headers = fullRepresentationHeaders();
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> createEhrWithProvidedStatusExpectError(
                        "{\n" +
                                "  \"_type\": \"EHR_STATUS\",\n" +
                                "  \"archetype_node_id\": \"archetype_node_id\",\n" +
                                "  \"name\": {\n" +
                                "    \"_type\": \"DV_TEXT\",\n" +
                                "    \"value\": \"status name\"\n" +
                                "  },\n" +
                                "  \"subject\": null,\n" +
                                "  \"is_modifiable\": \"true\",\n" +
                                "  \"is_queryable\": \"true\"\n" +
                                "}", headers, String.class));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotNull();
        assertThat(body.toLowerCase()).contains("subject");
    }

    @Test
    public void tryCreateEhrWithProvidedStatusWithMissingSubjectNamespace() {
        HttpHeaders headers = fullRepresentationHeaders();
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> createEhrWithProvidedStatusExpectError(createEhrStatusString(true, true, "", createRandomNumString()), headers, String.class));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotNull();
        assertThat(body).isNotNull();
        assertThat(body.toLowerCase()).contains("subject", "namespace");
    }

    @Test
    public void tryCreateEhrWithProvidedStatusWithMissingSubjectType() {
        HttpHeaders headers = fullRepresentationHeaders();
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> createEhrWithProvidedStatusExpectError(
                        "{\n" +
                                "  \"_type\": \"EHR_STATUS\",\n" +
                                "  \"archetype_node_id\": \"archetype_node_id\",\n" +
                                "  \"name\": {\n" +
                                "    \"_type\": \"DV_TEXT\",\n" +
                                "    \"value\": \"status name\"\n" +
                                "  },\n" +
                                "  \"subject\": {\n" +
                                "    \"_type\": \"PARTY_SELF\"," +
                                "    \"external_ref\": {\n" +
                                "      \"_type\": \"PARTY_REF\"," +
                                "      \"id\": {\n" +
                                "        \"_type\": \"GENERIC_ID\",\n" +
                                "        \"value\": \"" + createRandomNumString() + "\",\n" +
                                "        \"scheme\": \"id_scheme\"\n" +
                                "      },\n" +
                                "      \"namespace\": \"local\",\n" +
                                "      \"type\": \"\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"is_modifiable\": \"true\",\n" +
                                "  \"is_queryable\": \"true\"\n" +
                                "}", headers, String.class));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotNull();
        assertThat(body).isNotNull();
        assertThat(body.toLowerCase()).contains("subject", "type");
    }

    @Test
    public void tryCreateEhrWithProvidedStatusWithMissingSubjectId() {
        HttpHeaders headers = fullRepresentationHeaders();
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> createEhrWithProvidedStatusExpectError(
                        "{\n" +
                                "  \"_type\": \"EHR_STATUS\",\n" +
                                "  \"archetype_node_id\": \"archetype_node_id\",\n" +
                                "  \"name\": {\n" +
                                "    \"_type\": \"DV_TEXT\",\n" +
                                "    \"value\": \"status name\"\n" +
                                "  },\n" +
                                "  \"subject\": {\n" +
                                "    \"_type\": \"PARTY_SELF\"," +
                                "    \"external_ref\": {\n" +
                                "      \"_type\": \"PARTY_REF\"," +
                                "      \"namespace\": \"local\",\n" +
                                "      \"type\": \"PERSON\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"is_modifiable\": \"true\",\n" +
                                "  \"is_queryable\": \"true\"\n" +
                                "}", headers, String.class));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotNull();
        assertThat(body.toLowerCase()).contains("subject");
    }

    @Test
    public void tryCreateEhrWithProvidedStatusWithBlankSubjectIdValue() {
        HttpHeaders headers = fullRepresentationHeaders();
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> createEhrWithProvidedStatusExpectError(
                        "{\n" +
                                "  \"_type\": \"EHR_STATUS\",\n" +
                                "  \"archetype_node_id\": \"archetype_node_id\",\n" +
                                "  \"name\": {\n" +
                                "    \"_type\": \"DV_TEXT\",\n" +
                                "    \"value\": \"status name\"\n" +
                                "  },\n" +
                                "  \"subject\": {\n" +
                                "    \"_type\": \"PARTY_SELF\"," +
                                "    \"external_ref\": {\n" +
                                "      \"_type\": \"PARTY_REF\"," +
                                "      \"id\": {\n" +
                                "        \"_type\": \"GENERIC_ID\",\n" +
                                "        \"value\": \"" + createRandomNumString() + "\",\n" +
                                "        \"scheme\": \"id_scheme\"\n" +
                                "      },\n" +
                                "      \"namespace\": \"local\",\n" +
                                "      \"type\": \"\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"is_modifiable\": \"true\",\n" +
                                "  \"is_queryable\": \"true\"\n" +
                                "}", headers, String.class));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        String body = httpException.getResponseBodyAsString();
        assertThat(body).isNotNull();
        assertThat(body.toLowerCase()).contains("subject");
    }

    @Test
    public void retrieveEhr() {
        ResponseEntity<JsonNode> response = getResponse(getTargetPath() + "/ehr/{ehr_id}", JsonNode.class, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        JsonNode testEhr = response.getBody();
        assertThat(testEhr).isNotNull();
        assertThat(getFieldValue(testEhr, "ehr_id")).isEqualTo(ehrId);
        validateLocationAndETag(response, false, false);
    }

    @Test
    public void retrieveEhr400() {
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(getTargetPath() + "/ehr/{ehr_id}", OpenEhrErrorResponse.class, ""));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException.getResponseBodyAsString()).isNotNull();
        validateLocationAndETag(httpException, false, false);
    }

    @Test
    public void retrieveEhr404() {
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(getTargetPath() + "/ehr/{ehr_id}", OpenEhrErrorResponse.class, ehrId + "404"));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(httpException.getResponseBodyAsString()).isNotNull();
        validateLocationAndETag(httpException, false, false);
    }


    @Test
    public void retrieveEhrBySubjectId() {
        String customNamespace = "notDefault";
        String status = createEhrStatusString(true, true, customNamespace, PARTY_REF_UID);
        String ehrUid = UUID.randomUUID().toString();
        ResponseEntity<JsonNode> response1 = createEhrWithProvidedUidAndStatus(status, null, ehrUid);
        assertThat(response1.getStatusCode()).isEqualTo(CREATED);

        ResponseEntity<JsonNode> response2 = getResponse(
                getTargetPath() + "/ehr?subject_id={subject_id}&subject_namespace={subject_namespace}", JsonNode.class, PARTY_REF_UID, customNamespace);
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response2, false, false);
        JsonNode testEhr = response2.getBody();
        assertThat(testEhr).isNotNull();
        assertThat(getFieldValue(testEhr, "ehr_id")).isEqualTo(ehrUid);
        JsonNode ehrStatus = testEhr.get("ehr_status");
        assertThat(ehrStatus).isNotNull();
        assertThat(ehrStatus.get("subject").get("external_ref").get("id").get("value").asText()).isEqualTo(PARTY_REF_UID);
        assertThat(ehrStatus.get("subject").get("external_ref").get("namespace").asText()).isEqualTo(customNamespace);
    }

    @Test
    public void retrieveEhrBySubjectId404() {
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr?subject_id={subject_id}&subject_namespace={subject_namespace}",
                        OpenEhrErrorResponse.class,
                        "baltazar",
                        "baltazar"));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException, false, false);
    }

    @Test
    public void retrieveEhrStatusByTimestamp() {
        HttpHeaders headers = fullRepresentationHeaders();
        String ehrUid = UUID.randomUUID().toString();
        ResponseEntity<JsonNode> ehrResponse = createEhrWithProvidedUidAndStatus(null, headers, ehrUid);
        assertThat(ehrResponse.getStatusCode()).isEqualTo(CREATED);

        DateTime before = DateTime.now();
        JsonNode testEhr = ehrResponse.getBody();
        JsonNode status = testEhr.get("ehr_status");
        assertThat(status).isNotNull();
        String versionUid = getUid(status);
        ((ObjectNode)status).set("is_queryable", BooleanNode.FALSE);
        ((ObjectNode)status).set("uid", null);

        HttpHeaders headers1 = new HttpHeaders();
        headers1.set(IF_MATCH, versionUid);
        ResponseEntity<JsonNode> response1 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, status, JsonNode.class, headers1, ehrUid);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response1);

        ResponseEntity<JsonNode> response2 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                JsonNode.class,
                ehrUid,
                DATE_TIME_FORMATTER.print(before));
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response2, false, false);
        JsonNode ehrStatus1 = response2.getBody();
        assertThat(ehrStatus1).isNotNull();
        assertThat(ehrStatus1.get("is_queryable").asBoolean()).isTrue();

        DateTime after = DateTime.now();
        ResponseEntity<JsonNode> response3 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                JsonNode.class,
                ehrUid,
                DATE_TIME_FORMATTER.print(after));
        assertThat(response3.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response3, false, false);
        JsonNode ehrStatus2 = response3.getBody();
        assertThat(ehrStatus2).isNotNull();
        assertThat(ehrStatus2.get("is_queryable").asBoolean()).isFalse();

        String invalidDateTimeString = "2018-13-29T12:40:57.995+02:00";
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                        String.class,
                        ehrUid,
                        invalidDateTimeString));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        validateLocationAndETag(httpException, false, false);
        assertThat(httpException.getResponseBodyAsString()).isEmpty();

        String future = DATE_TIME_FORMATTER.print(before.minusYears(1));
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                        OpenEhrErrorResponse.class,
                        ehrUid,
                        future));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException1, false, false);
        assertThat(httpException1.getResponseBodyAsString()).isNotNull();

        HttpStatusCodeException httpException2 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                        String.class,
                        ehrUid + "404",
                        DATE_TIME_FORMATTER.print(after)));
        assertThat(httpException2.getStatusCode()).isEqualTo(NOT_FOUND);

        validateLocationAndETag(httpException2, false, false);
        assertThat(httpException2.getResponseBodyAsString()).isNotNull();
    }

    @Test
    public void retrieveEhrStatusByVersionUid() {
        ResponseEntity<JsonNode> ehrResponse = getResponse(getTargetPath() + "/ehr/{ehr_id}", JsonNode.class, ehrId);
        JsonNode testEhr = ehrResponse.getBody();
        assertThat(testEhr).isNotNull();
        String ehrUid = getFieldValue(testEhr, "ehr_id");
        String versionUid = getUid(testEhr.get("ehr_status"));

        ResponseEntity<JsonNode> response1 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status/{version_uid}",
                JsonNode.class,
                ehrUid,
                versionUid);
        assertThat(response1.getStatusCode()).isEqualTo(OK);

        validateLocationAndETag(response1, false, false);
        JsonNode ehrStatus = response1.getBody();
        assertThat(ehrStatus).isNotNull();

        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/ehr_status/{version_uid}",
                        String.class,
                        ehrUid,
                        "blablablabla"));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);

        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/ehr_status/{version_uid}",
                        String.class,
                        "blablablabla",
                        versionUid));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void updateEhrStatus() throws JsonProcessingException {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<JsonNode> response1 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                "");
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        JsonNode requestStatus = response1.getBody();
        assertThat(requestStatus).isNotNull();
        assertThat(requestStatus.get("is_queryable").asBoolean()).isTrue();
        String uid1 = getUid(requestStatus);
        ((ObjectNode)requestStatus).set("is_queryable", BooleanNode.FALSE);
        ((ObjectNode)requestStatus).set("uid", null);

        headers.set(IF_MATCH, uid1);
        ResponseEntity<JsonNode> response2 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, requestStatus, JsonNode.class, headers, ehrId);
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response2);
        JsonNode ehrStatus = response2.getBody();
        assertThat(ehrStatus).isNotNull();
        assertThat(ehrStatus.get("is_queryable").asBoolean()).isFalse();
        String uid2 = getUid(ehrStatus);

        headers.set(IF_MATCH, nonExistingUid);
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, requestStatus, String.class, headers, ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(httpException);

        headers.remove(IF_MATCH);
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, requestStatus, String.class, headers, ehrId));
        assertThat(httpException1.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(httpException1);

        headers.set(IF_MATCH, uid2);
        String malformedJsonString = objectMapper.writeValueAsString(requestStatus).replaceFirst("\\{", "\\}");
        HttpStatusCodeException httpException2 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, malformedJsonString, String.class, headers, ehrId));
        assertThat(httpException2.getStatusCode()).isEqualTo(BAD_REQUEST);
        validateLocationAndETag(httpException2, false, false);
    }

    @Test
    public void retrieveVersionedEhrStatus() {
        ResponseEntity<JsonNode> response1 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                "");
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        LocatableUid locatableUid = new LocatableUid(getUid(response1.getBody()));

        ResponseEntity<JsonNode> response = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status",
                JsonNode.class,
                ehrId);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response, false, false);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(getUid(body)).isEqualTo(locatableUid.getUid());
    }

    @Test
    public void retrieveVersionedEhrStatus404() {
        ResponseEntity<JsonNode> response1 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                "");
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        String uid = getUid(response1.getBody());
        // 404 nonexistent ehr
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
                        String.class,
                        "blablablabla",
                        uid));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 nonexistent version_uid
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
                        String.class,
                        ehrId,
                        UUID.randomUUID().toString()));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void retrieveEhrStatusVersion() throws IOException {
        super.setUp();

        ResponseEntity<JsonNode> response1 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                "");
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        String uid = getUid(response1.getBody());

        // 200
        ResponseEntity<JsonNode> response2 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
                JsonNode.class,
                ehrId,
                uid);
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        JsonNode body = response2.getBody();
        assertThat(body).isNotNull();
        JsonNode ehrStatus = body.get("preceding_version_uid");
        assertThat(body.has("preceding_version_uid")).isFalse();
        assertThat(body.get("data")).isNotNull();
//        assertThat(body.getData()).isOfAnyClassIn(EhrStatus.class);
        assertThat(getUid(body.get("data"))).isEqualTo(uid);
        validateLocationAndETag(response2, false, false);

        // 404 nonexistent ehr
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
                        String.class,
                        "blablablabla",
                        uid));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 nonexistent version
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
                        String.class,
                        ehrId,
                        UUID.randomUUID().toString()));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void retrieveEhrStatusVersionAtTime() throws InterruptedException {
        DateTime before = DateTime.now();
        Thread.sleep(100);

        ResponseEntity<JsonNode> response1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                                                         JsonNode.class,
                                                         ehrId,
                                                         "");
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        JsonNode status = response1.getBody();
        assertThat(status).isNotNull();
        String versionUid = getUid(status);

        LocatableUid oldLocatableUid = new LocatableUid(versionUid);
        ((ObjectNode)status).set("uid", null);

        HttpHeaders headers1 = new HttpHeaders();
        headers1.set(IF_MATCH, versionUid);
        ResponseEntity<JsonNode> response2 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, status, JsonNode.class, headers1, ehrId);
        assertThat(response2.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response2);

        ResponseEntity<JsonNode> response3 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                DATE_TIME_FORMATTER.print(before));
        assertThat(response3.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response3);
        JsonNode body1 = response3.getBody();
        assertThat(body1).isNotNull();
        assertThat(body1.get("data")).isNotNull();
        assertThat(getUid(body1)).isEqualTo(oldLocatableUid.toString());

        ResponseEntity<JsonNode> response4 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                DATE_TIME_FORMATTER.print(DateTime.now()));
        assertThat(response4.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response4);
        JsonNode body2 = response4.getBody();
        assertThat(body2).isNotNull();
        assertThat(body2.get("data")).isNotNull();
        assertThat(getUid(body2)).isEqualTo(oldLocatableUid.next().toString());

        String invalidDateTimeString = "2018-13-29T12:40:57.995+02:00";
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version?version_at_time={version_at_time}",
                        String.class,
                        ehrId,
                        invalidDateTimeString));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException.getResponseBodyAsString()).isEmpty();
        validateLocationAndETag(httpException, false, false);
    }

    private ResponseEntity<JsonNode> createEhrWithProvidedUidAndStatus(Object ehrStatus, HttpHeaders headers, String ehrUid) {
        return exchange(getTargetPath() + "/ehr/{ehr_id}", PUT, ehrStatus, JsonNode.class, headers, ehrUid);
    }

    private <T> ResponseEntity<T> createEhrWithProvidedStatusExpectError(Object ehrStatus, HttpHeaders headers, Class<T> responseType) {
        return exchange(getTargetPath() + "/ehr/{ehr_id}", PUT, ehrStatus, responseType, headers, UUID.randomUUID().toString());
    }

    private String createEhrStatusString(boolean isModifiable, boolean isQueryable, String namespace, String partyId) {
        return "{\n" +
                "  \"_type\": \"EHR_STATUS\",\n" +
                "  \"archetype_node_id\": \"archetype_node_id\",\n" +
                "  \"name\": {\n" +
                "    \"_type\": \"DV_TEXT\",\n" +
                "    \"value\": \"status name\"\n" +
                "  },\n" +
                "  \"subject\": {\n" +
                "    \"_type\": \"PARTY_SELF\"," +
                "    \"external_ref\": {\n" +
                "      \"_type\": \"PARTY_REF\"," +
                "      \"id\": {\n" +
                "        \"_type\": \"GENERIC_ID\",\n" +
                "        \"value\": \"" + partyId + "\",\n" +
                "        \"scheme\": \"id_scheme\"\n" +
                "      },\n" +
                "      \"namespace\": \"" + namespace + "\",\n" +
                "      \"type\": \"PERSON\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"is_modifiable\": \"" + isModifiable + "\",\n" +
                "  \"is_queryable\": \"" + isQueryable + "\"\n" +
                "}";
    }
}
