package org.openehr.rest;

import care.better.platform.locatable.LocatableUid;
import care.better.platform.util.ConversionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehr.data.OpenEhrErrorResponse;
import org.openehr.jaxb.rm.Folder;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    public void createFolder() {
        // full representation
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<Folder> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, createFolder("folder1"), Folder.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        Folder folder = response.getBody();
        assertThat(folder).isNotNull();
        assertThat(folder.getUid()).isNotNull();
        validateLocationAndETag(response);

        // check location
        validateLocationHeader(Folder.class, response.getHeaders().getLocation(), folder.getUid().getValue(), (Folder f) -> f.getUid().getValue());
    }

    @Test
    public void createFolder400() {
        ResponseEntity<OpenEhrErrorResponse> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, null, OpenEhrErrorResponse.class, null, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void createFolder404() {
        ResponseEntity<OpenEhrErrorResponse> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory",
                                                                 POST,
                                                                 createFolder("folder1"),
                                                                 OpenEhrErrorResponse.class,
                                                                 null,
                                                                 "blablablabla");
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void createFolder409() {
        // given
        HttpHeaders headers = new HttpHeaders();
        Folder folder = createFolder("folder1");

        // when
        ResponseEntity<Folder> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, folder, Folder.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);

        ResponseEntity<JsonNode> secondResponse = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, folder, JsonNode.class, headers, ehrId);

        // then
        assertThat(secondResponse.getStatusCode()).isEqualTo(CONFLICT);
    }

    @Test
    public void updateFolder() {
        HttpHeaders headers = fullRepresentationHeaders();
        ResponseEntity<Folder> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, createFolder("folder1"), Folder.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        Folder requestFolder = response.getBody();

        // 200 full representation
        headers.set(IF_MATCH, requestFolder.getUid().getValue());
        requestFolder.setUid(null);
        requestFolder.getName().setValue("NewName");
        response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", PUT, requestFolder, Folder.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        Folder folder = response.getBody();
        assertThat(folder).isNotNull();
        assertThat(folder.getUid()).isNotNull();
        assertThat(folder.getName().getValue()).isEqualTo("NewName");
        validateLocationAndETag(response);

        // 412 conflict of IF_MATCH provided uid
        headers.set(IF_MATCH, UUID.randomUUID().toString());
        response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", PUT, requestFolder, Folder.class, headers, ehrId);
        assertThat(response.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(response);

        // check location
        validateLocationHeader(Folder.class, response.getHeaders().getLocation(), folder.getUid().getValue(), (Folder f) -> f.getUid().getValue());
    }

    @Test
    public void deleteFolder() {

        HttpHeaders headers = fullRepresentationHeaders();
        // 404 directory not found
        headers.setContentType(null);
        ResponseEntity<OpenEhrErrorResponse> response1 = exchange(getTargetPath() + "/ehr/{ehr_id}/directory",
                                                                  DELETE,
                                                                  null,
                                                                  headers,
                                                                  null,
                                                                  OpenEhrErrorResponse.class,
                                                                  ehrId);
        assertThat(response1.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(response1, false, false);

        // post directory
        ResponseEntity<Folder> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, createFolder("folder1"), Folder.class, headers, ehrId);
        Folder requestFolder = response.getBody();
        String uid = requestFolder.getUid().getValue();

        // 404 ehr_id not found
        headers.setContentType(null);
        headers.set(IF_MATCH, uid);
        response1 = exchange(getTargetPath() + "/ehr/{ehr_id}/directory",
                             DELETE,
                             null,
                             headers,
                             null,
                             OpenEhrErrorResponse.class,
                             "blablabla");
        assertThat(response1.getStatusCode()).isEqualTo(NOT_FOUND);
        validateLocationAndETag(response1, false, false);

        // 412 conflict of IF_MATCH provided uid
        headers.setContentType(null);
        headers.set(IF_MATCH, UUID.randomUUID().toString());
        response1 = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", DELETE, null, headers, null, OpenEhrErrorResponse.class, ehrId);
        assertThat(response1.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
        validateLocationAndETag(response1);
        assertThat(response1.getBody()).isNull();

        // check location
        validateLocationHeader(Folder.class, response.getHeaders().getLocation(), uid, (Folder f) -> f.getUid().getValue());

        // 204
        headers.setContentType(null);
        headers.set(IF_MATCH, uid);
        response1 = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", DELETE, null, headers, null, OpenEhrErrorResponse.class, ehrId);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response1, false, false);
    }

    @Test
    public void retrieveFolder() {

        ResponseEntity<Folder> response = exchange(
                getTargetPath() + "/ehr/{ehr_id}/directory",
                POST,
                createFolder("folder1"),
                Folder.class,
                fullRepresentationHeaders(),
                ehrId);
        String uid = response.getBody().getUid().getValue();

        // 200
        response = getResponse(getTargetPath() + "/ehr/{ehr_id}/directory/{version_uid}", Folder.class, ehrId, uid);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response, false, false);
        Folder folder = response.getBody();
        assertThat(folder).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set(IF_MATCH, uid);
        ResponseEntity<String> response1 = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", DELETE, null, String.class, headers, ehrId);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);

        // 404 folder_uid not found
        ResponseEntity<JsonNode> notFoundResponse = getResponse(getTargetPath() + "/ehr/{ehr_id}/directory/{version_uid}", JsonNode.class, ehrId, "blablabla");
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(NOT_FOUND);

        // 404 ehr_id not found
        notFoundResponse = getResponse(getTargetPath() + "/ehr/{ehr_id}/directory/{version_uid}", JsonNode.class, "blablabla", uid);
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void retrieveFolderWithPath() {

        String subFolderName = "subFolder";
        String mainFolderName = "MainFolder";
        Folder requestFolder = createFolderWithSubfolder(subFolderName, mainFolderName);
        HttpHeaders fullRepresentationHeaders = fullRepresentationHeaders();
        ResponseEntity<Folder> response = exchange(
                getTargetPath() + "/ehr/{ehr_id}/directory",
                POST,
                requestFolder,
                Folder.class,
                fullRepresentationHeaders,
                ehrId);
        String uid = response.getBody().getUid().getValue();

        // 200
        response = getResponse(getTargetPath() + "/ehr/{ehr_id}/directory/{version_uid}?path={path}", Folder.class, ehrId, uid, subFolderName);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response, false, false);
        Folder folder = response.getBody();
        assertThat(folder).isNotNull();
        assertThat(folder.getName().getValue()).isEqualTo(subFolderName);

        // 204 for nonexistant path
        response = getResponse(getTargetPath() + "/ehr/{ehr_id}/directory/{version_uid}?path={path}", Folder.class, ehrId, uid, "someNonexistantFolderName");
        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        folder = response.getBody();
        assertThat(folder).isNull();
    }

    @Test
    public void retrieveFolderByTimestamp() throws InterruptedException {
        DateTime atTheBeginning = DateTime.now();

        HttpHeaders headers = fullRepresentationHeaders();
        String subFolderName = "subFolder";
        String mainFolderName = "MainFolder";
        Folder inputFolder = createFolderWithSubfolder(subFolderName, mainFolderName);
        ResponseEntity<Folder> response = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", POST, inputFolder, Folder.class, headers, ehrId);
        DateTime before = DateTime.now();
        Thread.sleep(100);

        Folder requestFolder = response.getBody();
        String uid = requestFolder.getUid().getValue();

        String oldName = requestFolder.getName().getValue();
        String newName = "NewName";
        requestFolder.setUid(null);
        requestFolder.getName().setValue(newName);

        HttpHeaders headers1 = new HttpHeaders();
        headers1.set(IF_MATCH, uid);
        ResponseEntity<Folder> response1 = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", PUT, requestFolder, Folder.class, headers1, ehrId);
        assertThat(response1.getStatusCode()).isEqualTo(NO_CONTENT);
        validateLocationAndETag(response1);

        // check location
        LocatableUid precedingUid = new LocatableUid(uid);
        validateLocationHeader(Folder.class, response1.getHeaders().getLocation(), precedingUid.next().toString(), (Folder f) -> f.getUid().getValue());
        uid = getHeaderETag(response1);

        // retrieve before change
        ResponseEntity<Folder> response2 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}",
                Folder.class,
                ehrId,
                DATE_TIME_FORMATTER.print(before));
        assertThat(response2.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response2);
        Folder folder = response2.getBody();
        assertThat(folder).isNotNull();
        assertThat(folder.getName().getValue()).isEqualTo(oldName);

        // check location
        validateLocationHeader(Folder.class, response2.getHeaders().getLocation(), oldName, (Folder f) -> f.getName().getValue());

        // retrieve after change
        DateTime after = DateTime.now();
        ResponseEntity<Folder> response3 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}",
                Folder.class,
                ehrId,
                DATE_TIME_FORMATTER.print(after));
        assertThat(response3.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response3);
        folder = response3.getBody();
        assertThat(folder).isNotNull();
        assertThat(folder.getName().getValue()).isEqualTo(newName);

        // check location
        validateLocationHeader(Folder.class, response3.getHeaders().getLocation(), newName, (Folder f) -> f.getName().getValue());

        // 404 ehr_id not found
        ResponseEntity<JsonNode> notFoundResponse = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}",
                JsonNode.class,
                "blablabla",
                DATE_TIME_FORMATTER.print(before));
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(NOT_FOUND);

        ResponseEntity<Folder> response4 = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}&path={path}",
                Folder.class,
                ehrId,
                DATE_TIME_FORMATTER.print(before),
                subFolderName);
        assertThat(response4.getStatusCode()).isEqualTo(OK);
        validateLocationAndETag(response4);

        // check location
        validateLocationHeader(Folder.class, response4.getHeaders().getLocation(), mainFolderName, (Folder f) -> f.getName().getValue());

        notFoundResponse = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}&path={path}",
                JsonNode.class,
                ehrId,
                DATE_TIME_FORMATTER.print(before),
                "someNonexistantFolderName");
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(NOT_FOUND);

        notFoundResponse = getResponse(
                getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}&path={path}",
                JsonNode.class,
                ehrId,
                DATE_TIME_FORMATTER.print(before.minusDays(1)),
                subFolderName);
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(NOT_FOUND);

        // delete directory
        HttpHeaders deleteDirectoryHeaders = new HttpHeaders();
        deleteDirectoryHeaders.set(IF_MATCH, uid);
        ResponseEntity<String> deletedResponse = exchange(getTargetPath() + "/ehr/{ehr_id}/directory", DELETE, null, String.class, deleteDirectoryHeaders, ehrId);
        assertThat(deletedResponse.getStatusCode()).isEqualTo(NO_CONTENT);

        // retrieve deleted directory
        ResponseEntity<Folder> response5 = getResponse(getTargetPath() + "/ehr/{ehr_id}/directory", Folder.class, ehrId);
        assertThat(response5.getStatusCode()).isEqualTo(NO_CONTENT);

        // retrieve non-existing directory in the past while the last one has been deleted
        ResponseEntity<JsonNode> notFoundResponse1 = getResponse(getTargetPath() + "/ehr/{ehr_id}/directory?version_at_time={version_at_time}",
                                       JsonNode.class,
                                       ehrId,
                                       DATE_TIME_FORMATTER.print(atTheBeginning));
        assertThat(notFoundResponse1.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    private Folder createFolder(String name) {
        Folder folder = new Folder();
        folder.setName(ConversionUtils.getText(name));

        Folder folder1 = new Folder();
        folder1.setName(ConversionUtils.getText(name + "/F1"));

        folder.getFolders().add(folder1);
        return folder;
    }
}
