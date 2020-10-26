package org.openehr.rest;

import care.better.platform.locatable.LocatableUid;
import care.better.platform.model.Ehr;
import care.better.platform.model.EhrStatus;
import care.better.platform.model.VersionedObjectDto;
import care.better.platform.util.ConversionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehr.data.OpenEhrErrorResponse;
import org.openehr.jaxb.rm.GenericId;
import org.openehr.jaxb.rm.Locatable;
import org.openehr.jaxb.rm.OriginalVersion;
import org.openehr.jaxb.rm.PartyRef;
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
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
        ResponseEntity<Ehr> response1 = exchange(getTargetPath() + "/ehr", POST, null, Ehr.class);
        assertThat(response1.getStatusCode()).isEqualTo(CREATED);
        assertThat(response1.getBody()).isNull();
        validateLocationAndETag(response1);

        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<Ehr> response2 = exchange(getTargetPath() + "/ehr", POST, null, Ehr.class, fullRepresentationHeaders());
        assertThat(response2.getStatusCode()).isEqualTo(CREATED);
        Ehr testEhr = response2.getBody();
        assertThat(testEhr).isNotNull();
        assertThat(testEhr.getEhrId()).isNotNull();
        EhrStatus ehrStatus = testEhr.getEhrStatus();
        assertThat(ehrStatus).isNotNull();
        assertThat(testEhr.getSystemId()).isNotNull();
        assertThat(testEhr.getTimeCreated()).isNotNull();
        validateLocationAndETag(response2);

        String ehrStatusJson = objectMapper.writeValueAsString(ehrStatus);
        assertThat(ehrStatusJson).contains("\"is_queryable\"");
        assertThat(ehrStatusJson).contains("\"is_modifiable\"");

        ehrStatus.setUid(null);
        ehrStatus.setQueryable(false);

        PartySelf partySelf = new PartySelf();
        PartyRef partyRef = new PartyRef();
        GenericId genericId = new GenericId();
        genericId.setValue(createRandomNumString());
        partyRef.setId(genericId);
        partyRef.setNamespace("local");
        partyRef.setType("PERSON");
        partySelf.setExternalRef(partyRef);
        ehrStatus.setSubject(partySelf);

        ResponseEntity<Ehr> response3 = exchange(getTargetPath() + "/ehr", POST, ehrStatus, Ehr.class, headers);
        assertThat(response3.getStatusCode()).isEqualTo(CREATED);
        assertThat(response3.getBody()).isNotNull();
        Ehr testEhr2 = response3.getBody();
        EhrStatus ehrStatus2 = testEhr2.getEhrStatus();
        assertThat(ehrStatus2).isNotNull();
        assertThat(ehrStatus2.isQueryable()).isFalse();
        assertThat(ehrStatus2.getUid()).isNotNull();

        ResponseEntity<Ehr> response4 = exchange(getTargetPath() + "/ehr", POST, ehrStatus, Ehr.class, headers);
        assertThat(response4.getStatusCode()).isEqualTo(CONFLICT);
    }

    @Test
    public void createEhrWithProvidedEhrId() {
        HttpHeaders headers = fullRepresentationHeaders();
        String ehrUid = UUID.randomUUID().toString();
        ResponseEntity<Ehr> response1 = createEhrWithProvidedUidAndStatus(null, headers, ehrUid);
        assertThat(response1.getStatusCode()).isEqualTo(CREATED);
        Ehr testEhr = response1.getBody();
        assertThat(testEhr).isNotNull();
        assertThat(testEhr.getEhrId().getValue()).isEqualTo(ehrUid);
        EhrStatus ehrStatus = testEhr.getEhrStatus();
        assertThat(ehrStatus).isNotNull();
        assertThat(testEhr.getSystemId()).isNotNull();
        assertThat(testEhr.getTimeCreated()).isNotNull();
        validateLocationAndETag(response1);

        ResponseEntity<Ehr> response2 = createEhrWithProvidedUidAndStatus(null, headers, ehrUid);
        assertThat(response2.getStatusCode()).isEqualTo(CONFLICT);
    }

    @Test
    public void tryCreateEhrWithInvalidProvidedEhrId() {
        // given
        HttpHeaders headers = fullRepresentationHeaders();
        String ehrUid = "invalid";

        // when
        ResponseEntity<Object> response = exchange(getTargetPath() + "/ehr/{ehr_id}", PUT, null, Object.class, headers, ehrUid);

        // then
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);

        Object body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).isInstanceOf(Map.class);
        assertThat((Map)body).containsEntry("message", "The value of the ehr_id unique identifier must be valid UUID value.");
    }

    @Test
    public void createEhrWithProvidedStatus() {
        HttpHeaders headers = fullRepresentationHeaders();
        String ehrUid = UUID.randomUUID().toString();
        String genericId = createRandomNumString();
        ResponseEntity<Ehr> response1 = createEhrWithProvidedUidAndStatus(
                "{\n" +
                        "  \"_type\": \"EHR_STATUS\",\n" +
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
        Ehr testEhr = response1.getBody();
        assertThat(testEhr).isNotNull();
        assertThat(testEhr.getEhrId().getValue()).isEqualTo(ehrUid);
        EhrStatus ehrStatus = testEhr.getEhrStatus();
        assertThat(ehrStatus).isNotNull();
        assertThat(ehrStatus.getSubject()).isNotNull();
        assertThat(ehrStatus.getSubject().getExternalRef()).isNotNull();
        assertThat(ehrStatus.getSubject().getExternalRef().getId().getValue()).isEqualTo(genericId);
        assertThat(testEhr.getSystemId()).isNotNull();
        assertThat(testEhr.getTimeCreated()).isNotNull();
        validateLocationAndETag(response1);

        ResponseEntity<Ehr> response2 = createEhrWithProvidedUidAndStatus(null, headers, ehrUid);
        assertThat(response2.getStatusCode()).isEqualTo(CONFLICT);
    }

    @Test
    public void tryCreateEhrWithProvidedStatusWithMissingSubject() {
        HttpHeaders headers = fullRepresentationHeaders();

        ResponseEntity<JsonNode> response = createEhrWithProvidedStatus(
                "{\n" +
                        "  \"_type\": \"EHR_STATUS\",\n" +
                        "  \"subject\": null,\n" +
                        "  \"is_modifiable\": \"true\",\n" +
                        "  \"is_queryable\": \"true\"\n" +
                        "}", headers, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message").asText()).isEqualTo("No subject has been specified to associate with EHR.");
    }

    @Test
    public void tryCreateEhrWithProvidedStatusWithMissingSubjectNamespace() {
        HttpHeaders headers = fullRepresentationHeaders();

        ResponseEntity<JsonNode> response = createEhrWithProvidedStatus(
                "{\n" +
                        "  \"_type\": \"EHR_STATUS\",\n" +
                        "  \"subject\": {\n" +
                        "    \"_type\": \"PARTY_SELF\"," +
                        "    \"external_ref\": {\n" +
                        "      \"_type\": \"PARTY_REF\"," +
                        "      \"id\": {\n" +
                        "        \"_type\": \"GENERIC_ID\",\n" +
                        "        \"value\": \"117\",\n" +
                        "        \"scheme\": \"id_scheme\"\n" +
                        "      },\n" +
                        "      \"namespace\": \"\",\n" +
                        "      \"type\": \"PERSON\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"is_modifiable\": \"true\",\n" +
                        "  \"is_queryable\": \"true\"\n" +
                        "}", headers, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message").asText()).isEqualTo("No or empty subject namespace has been specified to associate with EHR.");
    }

    @Test
    public void tryCreateEhrWithProvidedStatusWithMissingSubjectType() {
        HttpHeaders headers = fullRepresentationHeaders();

        ResponseEntity<JsonNode> response = createEhrWithProvidedStatus(
                "{\n" +
                        "  \"_type\": \"EHR_STATUS\",\n" +
                        "  \"subject\": {\n" +
                        "    \"_type\": \"PARTY_SELF\"," +
                        "    \"external_ref\": {\n" +
                        "      \"_type\": \"PARTY_REF\"," +
                        "      \"id\": {\n" +
                        "        \"_type\": \"GENERIC_ID\",\n" +
                        "        \"value\": \"117\",\n" +
                        "        \"scheme\": \"id_scheme\"\n" +
                        "      },\n" +
                        "      \"namespace\": \"local\",\n" +
                        "      \"type\": \"\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"is_modifiable\": \"true\",\n" +
                        "  \"is_queryable\": \"true\"\n" +
                        "}", headers, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message").asText()).isEqualTo("No or empty subject type has been specified to associate with EHR.");
    }

    @Test
    public void tryCreateEhrWithProvidedStatusWithMissingSubjectId() {
        HttpHeaders headers = fullRepresentationHeaders();

        ResponseEntity<JsonNode> response = createEhrWithProvidedStatus(
                "{\n" +
                        "  \"_type\": \"EHR_STATUS\",\n" +
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
                        "}", headers, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message").asText()).isEqualTo("No or empty subject ID has been specified to associate with EHR.");
    }

    @Test
    public void tryCreateEhrWithProvidedStatusWithBlankSubjectIdValue() {
        HttpHeaders headers = fullRepresentationHeaders();

        ResponseEntity<JsonNode> response = createEhrWithProvidedStatus(
                "{\n" +
                        "  \"_type\": \"EHR_STATUS\",\n" +
                        "  \"subject\": {\n" +
                        "    \"_type\": \"PARTY_SELF\"," +
                        "    \"external_ref\": {\n" +
                        "      \"_type\": \"PARTY_REF\"," +
                        "      \"id\": {\n" +
                        "        \"_type\": \"GENERIC_ID\",\n" +
                        "        \"value\": \"   \",\n" +
                        "        \"scheme\": \"id_scheme\"\n" +
                        "      },\n" +
                        "      \"namespace\": \"local\",\n" +
                        "      \"type\": \"PERSON\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"is_modifiable\": \"true\",\n" +
                        "  \"is_queryable\": \"true\"\n" +
                        "}", headers, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message").asText()).isEqualTo("No or empty subject ID has been specified to associate with EHR.");
    }

    @Test
    public void retrieveEhr() {

        ResponseEntity<Ehr> response = getResponse(getTargetPath() + "/ehr/{ehr_id}", Ehr.class, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        Ehr testEhr = response.getBody();
        assertThat(testEhr).isNotNull();
        assertThat(testEhr.getEhrId().getValue()).isEqualTo(ehrId);
        validateLocationAndETag(response, false, false);
    }

    @Test
    public void retrieveEhr400() {

        ResponseEntity<OpenEhrErrorResponse> response = getResponse(getTargetPath() + "/ehr/{ehr_id}", OpenEhrErrorResponse.class, "");
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        validateLocationAndETag(response, false, false);
    }

    @Test
    public void retrieveEhr404() {

        ResponseEntity<OpenEhrErrorResponse> response = getResponse(getTargetPath() + "/ehr/{ehr_id}", OpenEhrErrorResponse.class, ehrId + "404");
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        validateLocationAndETag(response, false, false);
    }

    @Test
    public void retrieveEhrBySubjectId() {

        String customNamespace = "notDefault";

        EhrStatus status = composeEhrStatus(customNamespace);
        String ehrUid = UUID.randomUUID().toString();
        ResponseEntity<Ehr> response1 = createEhrWithProvidedUidAndStatus(status, null, ehrUid);
        assertThat(response1.getStatusCode()).isEqualTo(CREATED);

        ResponseEntity<Ehr> response2 = getResponse(getTargetPath() + "/ehr?subject_id={subject_id}&subject_namespace={subject_namespace}", Ehr.class,
                                                    PARTY_REF_UID, customNamespace);
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        Ehr testEhr = response2.getBody();
        assertThat(testEhr).isNotNull();
        assertThat(testEhr.getEhrId().getValue()).isEqualTo(ehrUid);
        assertThat(testEhr.getEhrStatus().getSubject().getExternalRef().getId().getValue()).isEqualTo(PARTY_REF_UID);
        assertThat(testEhr.getEhrStatus().getSubject().getExternalRef().getNamespace()).isEqualTo(customNamespace);
        validateLocationAndETag(response2, false, false);
    }

    @Test
    public void retrieveEhrBySubjectId404() {

        ResponseEntity<OpenEhrErrorResponse> response = getResponse(
                getTargetPath() + "/ehr?subject_id={subject_id}&subject_namespace={subject_namespace}",
                OpenEhrErrorResponse.class,
                "baltazar",
                "baltazar");
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        validateLocationAndETag(response, false, false);
    }

    @Test
    public void retrieveEhrStatusByTimestamp() {

        HttpHeaders headers = fullRepresentationHeaders();
        String ehrUid = UUID.randomUUID().toString();
        ResponseEntity<Ehr> ehrResponse = createEhrWithProvidedUidAndStatus(null, headers, ehrUid);
        assertThat(ehrResponse.getStatusCode()).isEqualTo(CREATED);

        DateTime before = DateTime.now();
        Ehr testEhr = ehrResponse.getBody();
        EhrStatus status = testEhr.getEhrStatus();
        status.setQueryable(false);
        String versionUid = status.getUid().getValue();
        status.setUid(null);

        HttpHeaders headers1 = new HttpHeaders();
        headers1.set(IF_MATCH, versionUid);
        ResponseEntity<EhrStatus> response1 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, status, EhrStatus.class, headers1, ehrUid);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response1);

        ResponseEntity<EhrStatus> response2 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                EhrStatus.class,
                ehrUid,
                DATE_TIME_FORMATTER.print(before));
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response2, false, false);
        EhrStatus ehrStatus1 = response2.getBody();
        assertThat(ehrStatus1).isNotNull();
        assertThat(ehrStatus1.isQueryable()).isTrue();

        DateTime after = DateTime.now();
        ResponseEntity<EhrStatus> response3 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                EhrStatus.class,
                ehrUid,
                DATE_TIME_FORMATTER.print(after));
        assertThat(response3.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response3, false, false);
        EhrStatus ehrStatus2 = response3.getBody();
        assertThat(ehrStatus2).isNotNull();
        assertThat(ehrStatus2.isQueryable()).isFalse();

        String invalidDateTimeString = "2018-13-29T12:40:57.995+02:00";
        ResponseEntity<EhrStatus> response4 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                EhrStatus.class,
                ehrUid,
                invalidDateTimeString);
        assertThat(response4.getStatusCode()).isEqualTo(BAD_REQUEST);
        validateLocationAndETag(response4, false, false);
        assertThat(response4.getBody()).isNull();

        String future = DATE_TIME_FORMATTER.print(before.minusYears(1));
        ResponseEntity<OpenEhrErrorResponse> response5 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                OpenEhrErrorResponse.class,
                ehrUid,
                future);
        assertThat(response5.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(response5, false, false);
        assertThat(response5.getBody()).isNotNull();

        ResponseEntity<JsonNode> response6 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                JsonNode.class,
                ehrUid + "404",
                DATE_TIME_FORMATTER.print(after));
        assertThat(response6.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(response6, false, false);
        assertThat(response6.getBody()).isNotNull();
    }

    @Test
    public void retrieveEhrStatusByVersionUid() {

        ResponseEntity<Ehr> ehrResponse = getResponse(getTargetPath() + "/ehr/{ehr_id}", Ehr.class, ehrId);
        Ehr testEhr = ehrResponse.getBody();
        String ehrUid = testEhr.getEhrId().getValue();
        String versionUid = testEhr.getEhrStatus().getUid().getValue();

        ResponseEntity<EhrStatus> response1 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status/{version_uid}",
                EhrStatus.class,
                ehrUid,
                versionUid);
        assertThat(response1.getStatusCode()).isEqualTo(OK);

        validateLocationAndETag(response1, false, false);
        EhrStatus ehrStatus = response1.getBody();
        assertThat(ehrStatus).isNotNull();

        ResponseEntity<JsonNode> response2 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status/{version_uid}",
                JsonNode.class,
                ehrUid,
                "blablablabla");
        assertThat(response2.getStatusCode()).isEqualTo(NOT_FOUND);

        ResponseEntity<JsonNode> response3 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/ehr_status/{version_uid}",
                JsonNode.class,
                "blablablabla",
                versionUid);
        assertThat(response3.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void updateEhrStatus() throws JsonProcessingException {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<EhrStatus> response1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                                                          EhrStatus.class,
                                                          ehrId,
                                                          "");
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        EhrStatus requestStatus = response1.getBody();
        assertThat(requestStatus.isQueryable()).isTrue();
        String uid1 = requestStatus.getUid().getValue();
        requestStatus.setQueryable(false);
        requestStatus.setUid(null);

        headers.set(IF_MATCH, uid1);
        ResponseEntity<EhrStatus> response2 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, requestStatus, EhrStatus.class, headers, ehrId);
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response2);
        EhrStatus ehrStatus = response2.getBody();
        assertThat(ehrStatus).isNotNull();
        assertThat(ehrStatus.isQueryable()).isFalse();
        String uid2 = ehrStatus.getUid().getValue();

        headers.set(IF_MATCH, nonExistingUid);
        ResponseEntity<EhrStatus> response3 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, requestStatus, EhrStatus.class, headers, ehrId);
        assertThat(response3.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(response3);

        headers.remove(IF_MATCH);
        ResponseEntity<EhrStatus> response4 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, requestStatus, EhrStatus.class, headers, ehrId);
        assertThat(response4.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(response4);

        headers.set(IF_MATCH, uid2);
        String malformedJsonString = objectMapper.writeValueAsString(requestStatus);
        malformedJsonString = malformedJsonString.replaceFirst("\\{", "\\}");
        ResponseEntity<String> response5 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, malformedJsonString, String.class, headers, ehrId);
        assertThat(response5.getStatusCode()).isEqualTo(BAD_REQUEST);
        validateLocationAndETag(response5, false, false);
    }

    @Test
    public void retrieveVersionedEhrStatus() {

        ResponseEntity<EhrStatus> response1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                                                          EhrStatus.class,
                                                          ehrId,
                                                          "");
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        LocatableUid locatableUid = new LocatableUid(response1.getBody().getUid().getValue());

        ResponseEntity<VersionedObjectDto> response = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status",
                VersionedObjectDto.class,
                ehrId);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        VersionedObjectDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getUid().getValue()).isEqualTo(locatableUid.getUid());
        validateLocationAndETag(response, false, false);
    }

    @Test
    public void retrieveVersionedEhrStatus404() {

        ResponseEntity<EhrStatus> response1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                                                          EhrStatus.class,
                                                          ehrId,
                                                          "");
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        String uid = response1.getBody().getUid().getValue();
        // 404 nonexistent ehr
        ResponseEntity<JsonNode> response2 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
                JsonNode.class,
                "blablablabla",
                uid);
        assertThat(response2.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 nonexistent version_uid
        ResponseEntity<JsonNode> response3 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
                JsonNode.class,
                ehrId,
                UUID.randomUUID().toString());
        assertThat(response3.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void retrieveEhrStatusVersion() {

        ResponseEntity<EhrStatus> response1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                                                          EhrStatus.class,
                                                          ehrId,
                                                          "");
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        String uid = response1.getBody().getUid().getValue();

        // 200
        ResponseEntity<OriginalVersion> response2 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
                OriginalVersion.class,
                ehrId,
                uid);
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        OriginalVersion body = response2.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getPrecedingVersionUid()).isNull();
        assertThat(body.getData()).isNotNull();
        assertThat(body.getData()).isOfAnyClassIn(EhrStatus.class);
        assertThat(((Locatable)body.getData()).getUid().getValue()).isEqualTo(uid);
        validateLocationAndETag(response2, false, false);

        // 404 nonexistent ehr
        ResponseEntity<JsonNode> response3 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
                JsonNode.class,
                "blablablabla",
                uid);
        assertThat(response3.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 nonexistent version
        ResponseEntity<JsonNode> response4 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}",
                JsonNode.class,
                ehrId,
                UUID.randomUUID().toString());
        assertThat(response4.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void retrieveEhrStatusVersionAtTime() throws InterruptedException {
        DateTime before = DateTime.now();
        Thread.sleep(100);

        ResponseEntity<EhrStatus> response1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/ehr_status?version_at_time={version_at_time}",
                                                          EhrStatus.class,
                                                          ehrId,
                                                          "");
        assertThat(response1.getStatusCode()).isEqualTo(OK);
        EhrStatus status = response1.getBody();
        String versionUid = status.getUid().getValue();

        LocatableUid oldLocatableUid = new LocatableUid(status.getUid().getValue());
        status.setUid(null);

        HttpHeaders headers1 = new HttpHeaders();
        headers1.set(IF_MATCH, versionUid);
        ResponseEntity<EhrStatus> response2 = exchange(getTargetPath() + "/ehr/{ehr_id}/ehr_status", PUT, status, EhrStatus.class, headers1, ehrId);
        assertThat(response2.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response2);

        ResponseEntity<OriginalVersion> response3 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version?version_at_time={version_at_time}",
                OriginalVersion.class,
                ehrId,
                DATE_TIME_FORMATTER.print(before));
        assertThat(response3.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response3);
        OriginalVersion body1 = response3.getBody();
        assertThat(body1).isNotNull();
        assertThat(body1.getData()).isNotNull();
        assertThat(body1.getUid().getValue()).isEqualTo(oldLocatableUid.toString());

        ResponseEntity<OriginalVersion> response4 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version?version_at_time={version_at_time}",
                OriginalVersion.class,
                ehrId,
                DATE_TIME_FORMATTER.print(DateTime.now()));
        assertThat(response4.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response4);
        OriginalVersion body2 = response4.getBody();
        assertThat(body2).isNotNull();
        assertThat(body2.getData()).isNotNull();
        assertThat(body2.getUid().getValue()).isEqualTo(oldLocatableUid.next().toString());

        String invalidDateTimeString = "2018-13-29T12:40:57.995+02:00";
        ResponseEntity<OriginalVersion> response5 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/versioned_ehr_status/version?version_at_time={version_at_time}",
                OriginalVersion.class,
                ehrId,
                invalidDateTimeString);
        assertThat(response5.getStatusCode()).isEqualTo(BAD_REQUEST);
        validateLocationAndETag(response5, false, false);
        assertThat(response5.getBody()).isNull();
    }

    private ResponseEntity<Ehr> createEhrWithProvidedUidAndStatus(Object ehrStatus, HttpHeaders headers, String ehrUid) {
        return exchange(getTargetPath() + "/ehr/{ehr_id}", PUT, ehrStatus, Ehr.class, headers, ehrUid);
    }

    private <T> ResponseEntity<T> createEhrWithProvidedStatus(Object ehrStatus, HttpHeaders headers, Class<T> responseType) {
        return exchange(getTargetPath() + "/ehr/{ehr_id}", PUT, ehrStatus, responseType, headers, UUID.randomUUID().toString());
    }

    private EhrStatus composeEhrStatus(String customNamespace) {
        EhrStatus status = new EhrStatus();
        PartySelf subject = new PartySelf();

        PartyRef partyRef = new PartyRef();
        partyRef.setId(ConversionUtils.getHierObjectId(PARTY_REF_UID));
        partyRef.setNamespace(customNamespace);
        partyRef.setType("some_type");

        subject.setExternalRef(partyRef);
        status.setSubject(subject);
        return status;
    }
}
