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

import org.junit.jupiter.api.extension.ExtendWith;
import org.openehr.rest.conf.WebClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Dusan Markovic
 */
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class})
@TestPropertySource(value = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {WebClientConfiguration.class})
public class OpenEhrTemplateRestTest extends AbstractRestTest {

    @Autowired
    private Jaxb2RootElementHttpMessageConverter jaxb2MessageConverter;

//    @Test
//    public void upload() throws IOException {
//        String optString = IOUtils.toString(OpenEhrTemplateRestTest.class.getResourceAsStream("/rest/Demo Vitals.opt"), StandardCharsets.UTF_8);
//
//        // full representation
//        HttpHeaders headers = fullRepresentationHeaders();
//        headers.setContentType(APPLICATION_XML);
//        headers.setAccept(Collections.singletonList(APPLICATION_XML));
//        ResponseEntity<Template> response = exchange(getTargetPath() + "/definition/template/adl1.4", POST, optString, Template.class, headers);
//        assertThat(response.getStatusCode()).isEqualTo(CREATED);
//
//        Template responseTemplate = response.getBody();
//        assertThat(responseTemplate).isNotNull();
//        assertThat(responseTemplate.getTemplateId().getValue()).isEqualTo("Demo Vitals");
//        validateLocationAndETag(response, false, true);
//
//        // minimal representation
//        ResponseEntity<Template> response2 = uploadMinimal(optString);
//        assertThat(response2.getStatusCode()).isEqualTo(CREATED);
//        assertThat(response2.getBody()).isNull();
//        validateLocationAndETag(response2, false, true);
//    }
//
//    @Test
//    public void getTemplates() throws IOException {
//
//        ResponseEntity<OpenEhrTemplateDefinition[]> response = getResponse(getTargetPath() + "/definition/template/adl1.4", OpenEhrTemplateDefinition[].class);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrTemplateDefinition[] body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body).hasSizeGreaterThanOrEqualTo(1)
//                .filteredOn(node -> node.getTemplateId().equals("Demo Vitals"))
//                .extracting(
//                        OpenEhrTemplateDefinition::getTemplateId,
//                        OpenEhrTemplateDefinition::getConcept,
//                        OpenEhrTemplateDefinition::getArchetypeId
//                ).contains(Tuple.tuple("Demo Vitals", "Vitals", "openEHR-EHR-COMPOSITION.encounter.v1"));
//
//        String xmlTemplate = IOUtils.toString(
//                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/test_singletemplate.xml"),
//                StandardCharsets.UTF_8);
//        ResponseEntity<String> response1 = exchange(
//                getTargetPath() + "/definition/template/adl1.4",
//                POST,
//                xmlTemplate,
//                String.class,
//                createContentTypeHeaders(APPLICATION_XML, APPLICATION_XML));
//        assertThat(response1.getStatusCode()).isEqualTo(CREATED);
//
//        response = getResponse(getTargetPath() + "/definition/template/adl1.4", OpenEhrTemplateDefinition[].class);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body).hasSizeGreaterThanOrEqualTo(1)
//                .filteredOn(node -> node.getTemplateId().equals("Diagnosis"))
//                .extracting(
//                        OpenEhrTemplateDefinition::getTemplateId,
//                        OpenEhrTemplateDefinition::getConcept,
//                        OpenEhrTemplateDefinition::getArchetypeId
//                ).contains(Tuple.tuple("Diagnosis", "Diagnosis", "openEHR-EHR-COMPOSITION.report.v1"));
//    }
//
//    @Test
//    public void uploadAndGetTemplate() throws IOException {
//
//        String xmlTemplate = IOUtils.toString(
//                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/test_singletemplate.xml"),
//                StandardCharsets.UTF_8);
//        HttpHeaders headers = createContentTypeHeaders(APPLICATION_XML, APPLICATION_XML);
//        ResponseEntity<String> response = exchange(
//                getTargetPath() + "/definition/template/adl1.4",
//                POST,
//                xmlTemplate,
//                String.class,
//                headers);
//        assertThat(response.getStatusCode()).isEqualTo(CREATED);
//
//        String locationUrl = response.getHeaders().getLocation().toString();
//        ResponseEntity<Template> getTemplateResponse = getResponse(locationUrl, Template.class, APPLICATION_XML);
//        assertThat(getTemplateResponse.getStatusCode()).isEqualTo(OK);
//
//        Template templateFromString = (Template)jaxb2MessageConverter.read(Template.class, new HttpInputMessage() {
//            @Override
//            public InputStream getBody() {
//                return OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/test_singletemplate.xml");
//            }
//
//            @Override
//            public HttpHeaders getHeaders() {
//                return headers;
//            }
//        });
//        Template body = getTemplateResponse.getBody();
//        assertThat(templateFromString.getUid().getValue()).isEqualTo(body.getUid().getValue());
//    }
//
//    protected ResponseEntity<Template> uploadMinimal(String optString) {
//        HttpHeaders minimalHeaders = new HttpHeaders();
//        minimalHeaders.setContentType(APPLICATION_XML);
//        minimalHeaders.setAccept(Collections.singletonList(APPLICATION_XML));
//        return exchange(getTargetPath() + "/definition/template/adl1.4", POST, optString, Template.class, minimalHeaders);
//    }
//
//    @Test
//    public void list() throws IOException {
//        uploadMinimal(IOUtils.toString(OpenEhrTemplateRestTest.class.getResourceAsStream("/rest/Demo Vitals.opt"), StandardCharsets.UTF_8));
//
//        ResponseEntity<JsonNode> response = exchange(getTargetPath() + "/definition/template/adl1.4", HttpMethod.GET, null, JsonNode.class);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//
//        assertThat(response.getBody())
//                .isNotNull()
//                .hasSizeGreaterThanOrEqualTo(1)
//                .filteredOn(node -> node.path("template_id").textValue().equals("Demo Vitals"))
//                .extracting(
//                        node -> node.path("template_id").textValue(),
//                        node -> node.path("concept").textValue(),
//                        node -> node.path("archetype_id").textValue()
//                ).contains(Tuple.tuple("Demo Vitals", "Vitals", "openEHR-EHR-COMPOSITION.encounter.v1"));
//    }
}
