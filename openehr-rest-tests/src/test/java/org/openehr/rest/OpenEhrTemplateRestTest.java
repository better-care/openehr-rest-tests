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
import org.apache.commons.io.IOUtils;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openehr.data.OpenEhrTemplateDefinition;
import org.openehr.rest.conf.WebClientConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.transform.Source;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.test.util.AssertionErrors.assertFalse;

/**
 * @author Dusan Markovic
 */
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class})
@TestPropertySource(value = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {WebClientConfiguration.class})
public class OpenEhrTemplateRestTest extends AbstractRestTest {

    @Test
    public void upload() throws IOException {
        String templateString = IOUtils.toString(OpenEhrTemplateRestTest.class.getResourceAsStream("/rest/Demo Vitals.opt"), StandardCharsets.UTF_8)
                .trim().replaceFirst("^([\\W]+)<","<");

        // full representation
        HttpHeaders headers = fullRepresentationHeaders();
        headers.setContentType(APPLICATION_XML);
        headers.setAccept(Collections.singletonList(APPLICATION_XML));
        ResponseEntity<String> response = exchange(getTargetPath() + "/definition/template/adl1.4", POST, templateString, String.class, headers);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);

        String responseTemplate = response.getBody();
        assertThat(responseTemplate).isNotNull();
        compareTemplates(templateString, responseTemplate);

        validateLocationAndETag(response, false, true);

        // minimal representation
        ResponseEntity<JsonNode> response2 = uploadMinimal(templateString);
        assertThat(response2.getStatusCode()).isEqualTo(CREATED);
        assertThat(response2.getBody()).isNull();
        validateLocationAndETag(response2, false, true);
    }

    @Test
    public void getTemplates() throws IOException {
        ResponseEntity<OpenEhrTemplateDefinition[]> response = getResponse(getTargetPath() + "/definition/template/adl1.4", OpenEhrTemplateDefinition[].class);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        OpenEhrTemplateDefinition[] body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSizeGreaterThanOrEqualTo(1)
                .filteredOn(node -> node.getTemplateId().equals("Demo Vitals"))
                .extracting(
                        OpenEhrTemplateDefinition::getTemplateId,
                        OpenEhrTemplateDefinition::getConcept,
                        OpenEhrTemplateDefinition::getArchetypeId
                ).contains(Tuple.tuple("Demo Vitals", "Vitals", "openEHR-EHR-COMPOSITION.encounter.v1"));

        String xmlTemplate = IOUtils.toString(
                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/test_singletemplate.xml"),
                StandardCharsets.UTF_8);
        ResponseEntity<String> response1 = exchange(
                getTargetPath() + "/definition/template/adl1.4",
                POST,
                xmlTemplate,
                String.class,
                createContentTypeHeaders(APPLICATION_XML, APPLICATION_XML));
        assertThat(response1.getStatusCode()).isEqualTo(CREATED);

        response = getResponse(getTargetPath() + "/definition/template/adl1.4", OpenEhrTemplateDefinition[].class);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSizeGreaterThanOrEqualTo(1)
                .filteredOn(node -> node.getTemplateId().equals("Diagnosis"))
                .extracting(
                        OpenEhrTemplateDefinition::getTemplateId,
                        OpenEhrTemplateDefinition::getConcept,
                        OpenEhrTemplateDefinition::getArchetypeId
                ).contains(Tuple.tuple("Diagnosis", "Diagnosis", "openEHR-EHR-COMPOSITION.report.v1"));
    }

    @Test
    public void uploadAndGetTemplate() throws IOException {
        String templateControl = IOUtils.toString(
                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/test_singletemplate.xml"),
                StandardCharsets.UTF_8);
        HttpHeaders headers = createContentTypeHeaders(APPLICATION_XML, APPLICATION_XML);
        ResponseEntity<String> response = exchange(
                getTargetPath() + "/definition/template/adl1.4",
                POST,
                templateControl,
                String.class,
                headers);
        assertThat(response.getStatusCode()).isEqualTo(CREATED);

        String locationUrl = response.getHeaders().getLocation().toString();
        ResponseEntity<String> templateResponse = getResponse(locationUrl, String.class, APPLICATION_XML);
        assertThat(templateResponse.getStatusCode()).isEqualTo(OK);
        String templateTest = templateResponse.getBody();
        assertThat(templateTest).isNotNull();

        compareTemplates(templateControl, templateTest);
    }

    @Test
    public void list() throws IOException {
        uploadMinimal(IOUtils.toString(OpenEhrTemplateRestTest.class.getResourceAsStream("/rest/Demo Vitals.opt"), StandardCharsets.UTF_8));

        ResponseEntity<JsonNode> response = getResponse(getTargetPath() + "/definition/template/adl1.4", JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(OK);

        assertThat(response.getBody())
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(1)
                .filteredOn(node -> node.path("template_id").textValue().equals("Demo Vitals"))
                .extracting(
                        node -> node.path("template_id").textValue(),
                        node -> node.path("concept").textValue(),
                        node -> node.path("archetype_id").textValue()
                ).contains(Tuple.tuple("Demo Vitals", "Vitals", "openEHR-EHR-COMPOSITION.encounter.v1"));
    }

    protected ResponseEntity<JsonNode> uploadMinimal(String optString) {
        HttpHeaders minimalHeaders = new HttpHeaders();
        minimalHeaders.setContentType(APPLICATION_XML);
        minimalHeaders.setAccept(Collections.singletonList(APPLICATION_XML));
        return exchange(getTargetPath() + "/definition/template/adl1.4", POST, optString, JsonNode.class, minimalHeaders);
    }

    private void compareTemplates(String templateControl, String templateTest) {
        Source control = Input.fromString(templateControl).build();
        Source test = Input.fromString(templateTest).build();
        Diff build = DiffBuilder.compare(control)
                .withTest(test)
                .ignoreComments()
                .ignoreElementContentWhitespace()
                .ignoreWhitespace()
                .build();
        assertFalse(build.toString(), build.hasDifferences());
    }
}
