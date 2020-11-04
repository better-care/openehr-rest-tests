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

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehr.data.OpenEhrErrorResponse;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        String ehrStatus1 = "{\n" +
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
                "      \"type\": \"PERSON\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"is_modifiable\": \"true\",\n" +
                "  \"is_queryable\": \"false\"\n" +
                "}";
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
        ResponseEntity<JsonNode> response1 = createEhrWithProvidedUidAndStatus(
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
                        "        \"value\": \"" + genericId + "\",\n" +
                        "        \"scheme\": \"id_scheme\"\n" +
                        "      },\n" +
                        "      \"namespace\": \"local\",\n" +
                        "      \"type\": \"PERSON\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"is_modifiable\": \"true\",\n" +
                        "  \"is_queryable\": \"true\"\n" +
                        "}", headers, ehrUid);
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
                                "      \"namespace\": \"\",\n" +
                                "      \"type\": \"PERSON\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"is_modifiable\": \"true\",\n" +
                                "  \"is_queryable\": \"true\"\n" +
                                "}", headers, String.class));
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

    //
//    @Test
//    public void retrieveEhrBySubjectId() {
//        String customNamespace = "notDefault";
//        EhrStatus status = composeEhrStatus(customNamespace);
//        String ehrUid = UUID.randomUUID().toString();
//        ResponseEntity<Ehr> response1 = createEhrWithProvidedUidAndStatus(status, null, ehrUid);
//        assertThat(response1.getStatusCode()).isEqualTo(CREATED);
//
//        ResponseEntity<Ehr> response2 = getResponse(getTargetPath() + "/ehr?subject_id={subject_id}&subject_namespace={subject_namespace}", Ehr.class,
//                                                    PARTY_REF_UID, customNamespace);
//        assertThat(response2.getStatusCode()).isEqualTo(OK);
//        Ehr testEhr = response2.getBody();
//        assertThat(testEhr).isNotNull();
//        assertThat(testEhr.getEhrId().getValue()).isEqualTo(ehrUid);
//        assertThat(testEhr.getEhrStatus().getSubject().getExternalRef().getId().getValue()).isEqualTo(PARTY_REF_UID);
//        assertThat(testEhr.getEhrStatus().getSubject().getExternalRef().getNamespace()).isEqualTo(customNamespace);
//        validateLocationAndETag(response2, false, false);
//    }
//
//    @Test
//    public void retrieveEhrBySubjectId404() {
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/ehr?subject_id={subject_id}&subject_namespace={subject_namespace}",
//                        OpenEhrErrorResponse.class,
//                        "baltazar",
//                        "baltazar"));
//        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
//        validateLocationAndETag(httpException, false, false);
//    }
//
//    @Test
//    public void retrieveEhrStatusByTimestamp() {
//        HttpHeaders headers = fullRepresentationHeaders();
//        String ehrUid = UUID.randomUUID().toString();
//        ResponseEntity<Ehr> ehrResponse = createEhrWithProvidedUidAndStatus(null, headers, ehrUid);
//        assertThat(ehrResponse.getStatusCode()).isEqualTo(CREATED);
//
//        DateTime before = DateTime.now();
//        Ehr testEhr = ehrResponse.getBody();
//        EhrStatus status = testEhr.getEhrStatus();
//        status.setQueryable(false);
//        String versionUid = status.getUid().getValue();
//        status.setUid(null);
//
//        HttpHeaders headers1 = new HttpHeaders();
//        headers1.set(IF_MATCH, versionUid);
//        ResponseEntity<EhrStatus> response1 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, status, EhrStatus.class, headers1, ehrUid);
//        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
//        validateLocationAndETag(response1);
//
//        ResponseEntity<EhrStatus> response2 = getResponse(
//                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
//                EhrStatus.class,
//                ehrUid,
//                DATE_TIME_FORMATTER.print(before));
//        assertThat(response2.getStatusCode()).isEqualTo(OK);
//        validateLocationAndETag(response2, false, false);
//        EhrStatus ehrStatus1 = response2.getBody();
//        assertThat(ehrStatus1).isNotNull();
//        assertThat(ehrStatus1.isQueryable()).isTrue();
//
//        DateTime after = DateTime.now();
//        ResponseEntity<EhrStatus> response3 = getResponse(
//                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
//                EhrStatus.class,
//                ehrUid,
//                DATE_TIME_FORMATTER.print(after));
//        assertThat(response3.getStatusCode()).isEqualTo(OK);
//        validateLocationAndETag(response3, false, false);
//        EhrStatus ehrStatus2 = response3.getBody();
//        assertThat(ehrStatus2).isNotNull();
//        assertThat(ehrStatus2.isQueryable()).isFalse();
//
//        String invalidDateTimeString = "2018-13-29T12:40:57.995+02:00";
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
//                        EhrStatus.class,
//                        ehrUid,
//                        invalidDateTimeString));
//        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
//        validateLocationAndETag(httpException, false, false);
//        assertThat(httpException.getResponseBodyAsString()).isEmpty();
//
//        String future = DATE_TIME_FORMATTER.print(before.minusYears(1));
//        HttpStatusCodeException httpException1 = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
//                        OpenEhrErrorResponse.class,
//                        ehrUid,
//                        future));
//        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
//        validateLocationAndETag(httpException1, false, false);
//        assertThat(httpException1.getResponseBodyAsString()).isNotNull();
//
//        HttpStatusCodeException httpException2 = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
//                        String.class,
//                        ehrUid + "404",
//                        DATE_TIME_FORMATTER.print(after)));
//        assertThat(httpException2.getStatusCode()).isEqualTo(NOT_FOUND);
//
//        validateLocationAndETag(httpException2, false, false);
//        assertThat(httpException2.getResponseBodyAsString()).isNotNull();
//    }
//
//    @Test
//    public void retrieveEhrStatusByVersionUid() {
//        ResponseEntity<Ehr> ehrResponse = getResponse(getTargetPath() + "/ehr/{ehr_id}", Ehr.class, ehrId);
//        Ehr testEhr = ehrResponse.getBody();
//        String ehrUid = testEhr.getEhrId().getValue();
//        String versionUid = testEhr.getEhrStatus().getUid().getValue();
//
//        ResponseEntity<EhrStatus> response1 = getResponse(
//                getTargetPath() + "/ehr/{ehr_id}/ehr_status/{version_uid}",
//                EhrStatus.class,
//                ehrUid,
//                versionUid);
//        assertThat(response1.getStatusCode()).isEqualTo(OK);
//
//        validateLocationAndETag(response1, false, false);
//        EhrStatus ehrStatus = response1.getBody();
//        assertThat(ehrStatus).isNotNull();
//
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/ehr/{ehr_id}/ehr_status/{version_uid}",
//                        String.class,
//                        ehrUid,
//                        "blablablabla"));
//        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
//
//        HttpStatusCodeException httpException1 = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/ehr/{ehr_id}/ehr_status/{version_uid}",
//                        String.class,
//                        "blablablabla",
//                        versionUid));
//        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
//    }
//
//    @Test
//    public void updateEhrStatus() throws JsonProcessingException {
//        HttpHeaders headers = fullRepresentationHeaders();
//        ResponseEntity<EhrStatus> response1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
//                                                          EhrStatus.class,
//                                                          ehrId,
//                                                          "");
//        assertThat(response1.getStatusCode()).isEqualTo(OK);
//        EhrStatus requestStatus = response1.getBody();
//        assertThat(requestStatus.isQueryable()).isTrue();
//        String uid1 = requestStatus.getUid().getValue();
//        requestStatus.setQueryable(false);
//        requestStatus.setUid(null);
//
//        headers.set(IF_MATCH, uid1);
//        ResponseEntity<EhrStatus> response2 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, requestStatus, EhrStatus.class, headers, ehrId);
//        assertThat(response2.getStatusCode()).isEqualTo(OK);
//        validateLocationAndETag(response2);
//        EhrStatus ehrStatus = response2.getBody();
//        assertThat(ehrStatus).isNotNull();
//        assertThat(ehrStatus.isQueryable()).isFalse();
//        String uid2 = ehrStatus.getUid().getValue();
//
//        headers.set(IF_MATCH, nonExistingUid);
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, requestStatus, EhrStatus.class, headers, ehrId));
//        assertThat(httpException.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
//        validateLocationAndETag(httpException);
//
//        headers.remove(IF_MATCH);
//        HttpStatusCodeException httpException1 = assertThrows(
//                HttpStatusCodeException.class,
//                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, requestStatus, EhrStatus.class, headers, ehrId));
//        assertThat(httpException1.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
//        validateLocationAndETag(httpException1);
//
//        headers.set(IF_MATCH, uid2);
//        String malformedJsonString = objectMapper.writeValueAsString(requestStatus).replaceFirst("\\{", "\\}");
//        HttpStatusCodeException httpException2 = assertThrows(
//                HttpStatusCodeException.class,
//                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, malformedJsonString, String.class, headers, ehrId));
//        assertThat(httpException2.getStatusCode()).isEqualTo(BAD_REQUEST);
//        validateLocationAndETag(httpException2, false, false);
//    }
//
//    @Test
//    public void retrieveVersionedEhrStatus() {
//        ResponseEntity<EhrStatus> response1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
//                                                          EhrStatus.class,
//                                                          ehrId,
//                                                          "");
//        assertThat(response1.getStatusCode()).isEqualTo(OK);
//        LocatableUid locatableUid = new LocatableUid(response1.getBody().getUid().getValue());
//
//        ResponseEntity<VersionedObjectDto> response = getResponse(
//                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status",
//                VersionedObjectDto.class,
//                ehrId);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        VersionedObjectDto body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.getUid().getValue()).isEqualTo(locatableUid.getUid());
//        validateLocationAndETag(response, false, false);
//    }
//
//    @Test
//    public void retrieveVersionedEhrStatus404() {
//        ResponseEntity<EhrStatus> response1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
//                                                          EhrStatus.class,
//                                                          ehrId,
//                                                          "");
//        assertThat(response1.getStatusCode()).isEqualTo(OK);
//        String uid = response1.getBody().getUid().getValue();
//        // 404 nonexistent ehr
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
//                        JsonNode.class,
//                        "blablablabla",
//                        uid));
//        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
//
//        // 404 nonexistent version_uid
//        HttpStatusCodeException httpException1 = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
//                        JsonNode.class,
//                        ehrId,
//                        UUID.randomUUID().toString()));
//        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
//    }
//
//    @Test
//    public void retrieveEhrStatusVersion() throws IOException {
//        super.setUp();
//
//        ResponseEntity<EhrStatus> response1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
//                                                          EhrStatus.class,
//                                                          ehrId,
//                                                          "");
//        assertThat(response1.getStatusCode()).isEqualTo(OK);
//        String uid = response1.getBody().getUid().getValue();
//
//        // 200
//        ResponseEntity<OriginalVersion> response2 = getResponse(
//                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
//                OriginalVersion.class,
//                ehrId,
//                uid);
//        assertThat(response2.getStatusCode()).isEqualTo(OK);
//        OriginalVersion body = response2.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.getPrecedingVersionUid()).isNull();
//        assertThat(body.getData()).isNotNull();
//        assertThat(body.getData()).isOfAnyClassIn(EhrStatus.class);
//        assertThat(((Locatable)body.getData()).getUid().getValue()).isEqualTo(uid);
//        validateLocationAndETag(response2, false, false);
//
//        // 404 nonexistent ehr
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
//                        String.class,
//                        "blablablabla",
//                        uid));
//        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
//
//        // 404 nonexistent version
//        HttpStatusCodeException httpException1 = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
//                        String.class,
//                        ehrId,
//                        UUID.randomUUID().toString()));
//        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
//    }
//
//    @Test
//    public void retrieveEhrStatusVersionAtTime() throws InterruptedException {
//        DateTime before = DateTime.now();
//        Thread.sleep(100);
//
//        ResponseEntity<EhrStatus> response1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
//                                                          EhrStatus.class,
//                                                          ehrId,
//                                                          "");
//        assertThat(response1.getStatusCode()).isEqualTo(OK);
//        EhrStatus status = response1.getBody();
//        String versionUid = status.getUid().getValue();
//
//        LocatableUid oldLocatableUid = new LocatableUid(status.getUid().getValue());
//        status.setUid(null);
//
//        HttpHeaders headers1 = new HttpHeaders();
//        headers1.set(IF_MATCH, versionUid);
//        ResponseEntity<EhrStatus> response2 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, status, EhrStatus.class, headers1, ehrId);
//        assertThat(response2.getStatusCode()).isEqualTo(NO_CONTENT);
//        validateLocationAndETag(response2);
//
//        ResponseEntity<OriginalVersion> response3 = getResponse(
//                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version?version_at_time={version_at_time}",
//                OriginalVersion.class,
//                ehrId,
//                DATE_TIME_FORMATTER.print(before));
//        assertThat(response3.getStatusCode()).isEqualTo(OK);
//        validateLocationAndETag(response3);
//        OriginalVersion body1 = response3.getBody();
//        assertThat(body1).isNotNull();
//        assertThat(body1.getData()).isNotNull();
//        assertThat(body1.getUid().getValue()).isEqualTo(oldLocatableUid.toString());
//
//        ResponseEntity<OriginalVersion> response4 = getResponse(
//                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version?version_at_time={version_at_time}",
//                OriginalVersion.class,
//                ehrId,
//                DATE_TIME_FORMATTER.print(DateTime.now()));
//        assertThat(response4.getStatusCode()).isEqualTo(OK);
//        validateLocationAndETag(response4);
//        OriginalVersion body2 = response4.getBody();
//        assertThat(body2).isNotNull();
//        assertThat(body2.getData()).isNotNull();
//        assertThat(body2.getUid().getValue()).isEqualTo(oldLocatableUid.next().toString());
//
//        String invalidDateTimeString = "2018-13-29T12:40:57.995+02:00";
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version?version_at_time={version_at_time}",
//                        OriginalVersion.class,
//                        ehrId,
//                        invalidDateTimeString));
//        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
//        assertThat(httpException.getResponseBodyAsString()).isEmpty();
//        validateLocationAndETag(httpException, false, false);
//    }
//
    private ResponseEntity<JsonNode> createEhrWithProvidedUidAndStatus(Object ehrStatus, HttpHeaders headers, String ehrUid) {
        return exchange(getTargetPath() + "/ehr/{ehr_id}", PUT, ehrStatus, JsonNode.class, headers, ehrUid);
    }

    private <T> ResponseEntity<T> createEhrWithProvidedStatusExpectError(Object ehrStatus, HttpHeaders headers, Class<T> responseType) {
        return exchange(getTargetPath() + "/ehr/{ehr_id}", PUT, ehrStatus, responseType, headers, UUID.randomUUID().toString());
    }
//
//    private EhrStatus composeEhrStatus(String customNamespace) {
//        EhrStatus status = new EhrStatus();
//        PartySelf subject = new PartySelf();
//        status.setName(ConversionUtils.getText("status"));
//        status.setArchetypeNodeId("status.archetype.node");
//        PartyRef partyRef = new PartyRef();
//        partyRef.setId(ConversionUtils.getHierObjectId(PARTY_REF_UID));
//        partyRef.setNamespace(customNamespace);
//        partyRef.setType("some_type");
//
//        subject.setExternalRef(partyRef);
//        status.setSubject(subject);
//        return status;
//    }
}
