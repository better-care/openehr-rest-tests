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
import com.fasterxml.jackson.databind.node.TextNode;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;

/**
 * @author Dusan Markovic
 */
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class})
@TestPropertySource(value = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {WebClientConfiguration.class})
public class OpenEhrFolderRestTest extends AbstractRestTest {

    @BeforeEach
    @Override
    public void setUp() throws IOException {
        super.setUp();
    }

    @Test
    public void createFolder() throws JsonProcessingException {
        // full representation
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<JsonNode> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, createFolder("folder1"), JsonNode.class, headers,
                                                     ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        JsonNode folder = response.getBody();
        assertThat(folder).isNotNull();
        assertThat(getUid(folder)).isNotNull();
        validateLocationAndETag(response);

        // check location
        validateLocationHeader("FOLDER", response.getHeaders().getLocation(), getUid(folder), this::getUid);
    }

    @Test
    public void createFolder400() {
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, null, OpenEhrErrorResponse.class, null, ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(httpException.getResponseBodyAsString()).isNotEmpty();
    }

    @Test
    public void createFolder404() {
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/directory",
                               POST,
                               createFolder("folder1"),
                               OpenEhrErrorResponse.class,
                               null,
                               "blablablabla"));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(httpException.getResponseBodyAsString()).isNotEmpty();
    }

    @Test
    public void createFolder409() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        JsonNode folder = createFolder("folder1");

        ResponseEntity<JsonNode> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, folder, JsonNode.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);

        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, folder, JsonNode.class, headers, ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(CONFLICT);
    }

    @Test
    public void updateFolder() throws JsonProcessingException {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<JsonNode> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, createFolder("folder1"), JsonNode.class, headers,
                                                     ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        JsonNode requestFolder = response.getBody();

        // 200 full representation
        headers.set(IF_MATCH, getUid(requestFolder));
        ((ObjectNode)requestFolder).set("uid", null);
        ((ObjectNode)requestFolder.get("name")).set("value", new TextNode("NewName"));
        response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", PUT, requestFolder, JsonNode.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        JsonNode folder = response.getBody();
        assertThat(folder).isNotNull();
        assertThat(folder.get("uid")).isNotNull();
        assertThat(folder.get("name").get("value").asText()).isEqualTo("NewName");
        validateLocationAndETag(response);

        // 412 conflict of IF_MATCH provided uid
        headers.set(IF_MATCH, UUID.randomUUID().toString());
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/directory", PUT, requestFolder, JsonNode.class, headers, ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(httpException);

        // check location
        validateLocationHeader("FOLDER", response.getHeaders().getLocation(), getUid(folder), this::getUid);
    }

    @Test
    public void deleteFolder() throws JsonProcessingException {

        HttpHeaders headers = fullRepresentationHeaders();
        // 404 directory not found
        headers.setContentType(null);
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/directory",
                               DELETE,
                               null,
                               headers,
                               null,
                               OpenEhrErrorResponse.class,
                               ehrId));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException, false, false);

        // post directory
        ResponseEntity<JsonNode> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, createFolder("folder1"), JsonNode.class, headers,
                                                     ehrId);
        JsonNode requestFolder = response.getBody();
        String uid = getUid(requestFolder);

        // 404 ehr_id not found
        headers.setContentType(null);
        headers.set(IF_MATCH, uid);
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/directory",
                               DELETE,
                               null,
                               headers,
                               null,
                               OpenEhrErrorResponse.class,
                               "blablabla"));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(httpException1, false, false);

        // 412 conflict of IF_MATCH provided uid
        headers.setContentType(null);
        headers.set(IF_MATCH, UUID.randomUUID().toString());
        HttpStatusCodeException httpException2 = assertThrows(
                HttpStatusCodeException.class,
                () -> exchange(getTargetPath() + "/ehr/{ehr_id}/directory", DELETE, null, headers, null, OpenEhrErrorResponse.class, ehrId));
        assertThat(httpException2.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(httpException2);
        assertThat(httpException2.getResponseBodyAsString()).isEmpty();

        // check location
        validateLocationHeader("FOLDER", response.getHeaders().getLocation(), uid, this::getUid);

        // 204
        headers.setContentType(null);
        headers.set(IF_MATCH, uid);
        ResponseEntity<OpenEhrErrorResponse> response1 = exchange(
                getTargetPath() + "/ehr/{ehr_id}/directory", DELETE, null, headers, null, OpenEhrErrorResponse.class, ehrId);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response1, false, false);
    }

    @Test
    public void retrieveFolder() throws JsonProcessingException {

        ResponseEntity<JsonNode> response = exchange(
                getTargetPath() + "/ehr/{ehr_id}/directory",
                POST,
                createFolder("folder1"),
                JsonNode.class,
                fullRepresentationHeaders(),
                ehrId);
        String uid = getUid(response.getBody());

        // 200
        response = getResponse(getTargetPath() + "/ehr/{ehr_id}/directory/{version_uid}", JsonNode.class, ehrId, uid);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response, false, false);
        JsonNode folder = response.getBody();
        assertThat(folder).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set(IF_MATCH, uid);
        ResponseEntity<String> response1 = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", DELETE, null, String.class, headers, ehrId);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);

        // 404 folder_uid not found
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(getTargetPath() + "/ehr/{ehr_id}/directory/{version_uid}", String.class, ehrId, "blablabla"));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 ehr_id not found
        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(getTargetPath() + "/ehr/{ehr_id}/directory/{version_uid}", String.class, "blablabla", uid));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void retrieveFolderWithPath() throws JsonProcessingException {

        String subFolderName = "subFolder";
        String mainFolderName = "MainFolder";
        JsonNode requestFolder = createFolderWithSubfolder(subFolderName, mainFolderName);
        HttpHeaders fullRepresentationHeaders = fullRepresentationHeaders();
        ResponseEntity<JsonNode> response = exchange(
                getTargetPath() + "/ehr/{ehr_id}/directory",
                POST,
                requestFolder,
                JsonNode.class,
                fullRepresentationHeaders,
                ehrId);
        String uid = getUid(response.getBody());

        // 200
        response = getResponse(getTargetPath() + "/ehr/{ehr_id}/directory/{version_uid}?path={path}", JsonNode.class, ehrId, uid, subFolderName);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response, false, false);
        JsonNode folder = response.getBody();
        assertThat(folder).isNotNull();
        assertThat(folder.get("name").get("value").asText()).isEqualTo(subFolderName);

        // 204 for nonexistant path
        response = getResponse(getTargetPath() + "/ehr/{ehr_id}/directory/{version_uid}?path={path}", JsonNode.class, ehrId, uid, "someNonexistantFolderName");
        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        folder = response.getBody();
        assertThat(folder).isNull();
    }

    @Test
    public void retrieveFolderByTimestamp() throws InterruptedException, JsonProcessingException {
        DateTime atTheBeginning = DateTime.now();

        HttpHeaders headers = fullRepresentationHeaders();
        String subFolderName = "subFolder";
        String mainFolderName = "MainFolder";
        JsonNode inputFolder = createFolderWithSubfolder(subFolderName, mainFolderName);
        ResponseEntity<JsonNode> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, inputFolder, JsonNode.class, headers, ehrId);
        JsonNode requestFolder = response.getBody();
        assertThat(requestFolder).isNotNull();
        DateTime before = DateTime.now();
        Thread.sleep(100);

        String uid = getUid(requestFolder);

        String oldName = getFieldValue(requestFolder, "name");
        String newName = "NewName";
        ((ObjectNode)requestFolder).set("uid", null);
        ((ObjectNode)requestFolder.get("name")).set("value", new TextNode(newName));

        HttpHeaders headers1 = new HttpHeaders();
        headers1.set(IF_MATCH, uid);
        ResponseEntity<JsonNode> response1 = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", PUT, requestFolder, JsonNode.class, headers1, ehrId);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response1);

        // check location
        LocatableUid precedingUid = new LocatableUid(uid);
        validateLocationHeader("FOLDER", response1.getHeaders().getLocation(), precedingUid.next().toString(), this::getUid);
        uid = getHeaderETag(response1);

        // retrieve before change
        ResponseEntity<JsonNode> response2 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                DATE_TIME_FORMATTER.print(before));
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response2);
        JsonNode folder = response2.getBody();
        assertThat(folder).isNotNull();
        assertThat(folder.get("name").get("value").asText()).isEqualTo(oldName);

        // check location
        validateLocationHeader("FOLDER", response2.getHeaders().getLocation(), oldName, jsonNode -> getFieldValue(jsonNode, "name"));

        // retrieve after change
        DateTime after = DateTime.now();
        ResponseEntity<JsonNode> response3 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}",
                JsonNode.class,
                ehrId,
                DATE_TIME_FORMATTER.print(after));
        assertThat(response3.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response3);
        folder = response3.getBody();
        assertThat(folder).isNotNull();
        assertThat(folder.get("name").get("value").asText()).isEqualTo(newName);

        // check location
        validateLocationHeader("FOLDER", response3.getHeaders().getLocation(), newName, jsonNode -> getFieldValue(jsonNode, "name"));

        // 404 ehr_id not found
        HttpStatusCodeException httpException = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}",
                        JsonNode.class,
                        "blablabla",
                        DATE_TIME_FORMATTER.print(before)));
        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);

        ResponseEntity<JsonNode> response4 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}&path={path}",
                JsonNode.class,
                ehrId,
                DATE_TIME_FORMATTER.print(before),
                subFolderName);
        assertThat(response4.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response4);

        // check location
        validateLocationHeader("FOLDER", response4.getHeaders().getLocation(), mainFolderName, jsonNode -> getFieldValue(jsonNode, "name"));

        HttpStatusCodeException httpException1 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}&path={path}",
                        String.class,
                        ehrId,
                        DATE_TIME_FORMATTER.print(before),
                        "someNonexistantFolderName"));
        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);

        HttpStatusCodeException httpException2 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(
                        getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}&path={path}",
                        String.class,
                        ehrId,
                        DATE_TIME_FORMATTER.print(before.minusDays(1)),
                        subFolderName));
        assertThat(httpException2.getStatusCode()).isEqualTo(NOT_FOUND);

        // delete directory
        HttpHeaders deleteDirectoryHeaders = new HttpHeaders();
        deleteDirectoryHeaders.set(IF_MATCH, uid);
        ResponseEntity<String> deletedResponse = exchange(getTargetPath() + "/ehr/{ehr_id}/directory",
                                                          DELETE,
                                                          null,
                                                          String.class,
                                                          deleteDirectoryHeaders,
                                                          ehrId);
        assertThat(deletedResponse.getStatusCode()).isEqualTo(NO_CONTENT);

        // retrieve deleted directory
        ResponseEntity<String> response5 = getResponse(getTargetPath() + "/ehr/{ehr_id}/directory", String.class, ehrId);
        assertThat(response5.getStatusCode()).isEqualTo(NO_CONTENT);

        // retrieve non-existing directory in the past while the last one has been deleted
        HttpStatusCodeException httpException3 = assertThrows(
                HttpStatusCodeException.class,
                () -> getResponse(getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}",
                                  String.class,
                                  ehrId,
                                  DATE_TIME_FORMATTER.print(atTheBeginning)));
        assertThat(httpException3.getStatusCode()).isEqualTo(NOT_FOUND);
    }
}
