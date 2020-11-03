/*
 * Copyright (c) 2019 Vitasystems GmbH and Jake Smolka (Hannover Medical School).
 *
 * This file is part of project EHRbase
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openehr.rest;

import org.junit.jupiter.api.extension.ExtendWith;
import org.openehr.data.OpenEhrConformance;
import org.openehr.rest.conf.WebClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dusan Markovic
 */
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class})
@TestPropertySource(value = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {WebClientConfiguration.class})
public class OpenEhrQueryRestTest extends AbstractRestTest {

    private final List<String> allQueryNames = new ArrayList<>();
    protected String ehrId1;
    @Autowired
    private OpenEhrConformance conformance;
    private String query;
    private String namedParameterQuery;
    private String maxTempQuery;
    private String ehrQueryForSingleEhr;
    private String compositionQueryForSingleEhr;
    private String tempLargerThanName;
    private String maxTempName;
    private String groupOneName;
    private String groupTwoName;
    private String ehrQueryForSingleEhrName;
    private String compositionQueryForSingleEhrName;
    private String testQuery1, testQuery2, testQuery3, testQuery4, testQuery5;
//
//    @Override
//    @BeforeAll
//    public void setUp() throws IOException {
//        super.setUp();
//        EhrStatus ehrStatus = new EhrStatus();
//        ehrStatus.setUid(null);
//        ehrStatus.setQueryable(false);
//        PartySelf partySelf = new PartySelf();
//        PartyRef partyRef = new PartyRef();
//        GenericId genericId = new GenericId();
//        String randomNum = createRandomNumString();
//        genericId.setValue(randomNum);
//        partyRef.setId(genericId);
//        partyRef.setNamespace("ns1");
//        partyRef.setType("PERSON");
//        partySelf.setExternalRef(partyRef);
//        ehrStatus.setSubject(partySelf);
//
//        ResponseEntity<Ehr> ehrResponseEntity1 = exchange(getTargetPath() + "/ehr", POST, ehrStatus, Ehr.class, fullRepresentationHeaders());
//        ehrId1 = Objects.requireNonNull(ehrResponseEntity1.getBody()).getEhrId().getValue();
//
//        compositionUid2 = postComposition(ehrId, objectMapper.readValue(IOUtils.toString(
//                OpenEhrCompositionRestTest.class.getResourceAsStream("/rest/composition2.json"),
//                StandardCharsets.UTF_8), Composition.class));
//
//        query = "select o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/magnitude as temperature, " +
//                "o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/units as unit from EHR[" +
//                "ehr_id/value='" + ehrId +
//                "'] CONTAINS Observation o[openEHR-EHR-OBSERVATION.body_temperature-zn.v1] WHERE " +
//                "o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/magnitude > 37.1 ORDER BY temperature desc";
//        namedParameterQuery = "select o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/magnitude as temperature, " +
//                "o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/units as unit from EHR[" +
//                "ehr_id/value=:ehr_id] CONTAINS Observation o[openEHR-EHR-OBSERVATION.body_temperature-zn.v1] WHERE " +
//                "o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/magnitude > :temp ORDER BY temperature desc";
//        maxTempQuery = "select " +
//                "a_a/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value as temp, a_a/data[at0002]/events[at0003]/time as time " +
//                "from COMPOSITION a " +
//                "contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.body_temperature-zn.v1] " +
//                "ORDER BY temp/magnitude DESC " +
//                "FETCH 1";
//        ehrQueryForSingleEhr = "SELECT DISTINCT e/ehr_id/value FROM EHR e";
//        compositionQueryForSingleEhr = "SELECT DISTINCT e/ehr_id/value FROM EHR e CONTAINS COMPOSITION c";
//
//        tempLargerThanName = setQueryName("tempLargerThan");
//        maxTempName = setQueryName("maxTemp");
//        groupOneName = setQueryName("group::one");
//        groupTwoName = setQueryName("group::two");
//        ehrQueryForSingleEhrName = setQueryName("ehrQueryForSingleEhr");
//        compositionQueryForSingleEhrName = setQueryName("compositionQueryForSingleEhr");
//        testQuery1 = setQueryName("test1");
//        testQuery2 = setQueryName("test2");
//        testQuery3 = setQueryName("test3");
//        testQuery4 = setQueryName("test4");
//        testQuery5 = setQueryName("test5");
//        uploadNamedQuery("named parameter query", tempLargerThanName, namedParameterQuery);
//        uploadNamedQuery("max temp query", maxTempName, maxTempQuery);
//        uploadNamedQuery("named parameter query 1", groupOneName, namedParameterQuery);
//        uploadNamedQuery("named parameter query 2", groupTwoName, namedParameterQuery);
//        uploadNamedQuery("ehr query for single ehr", ehrQueryForSingleEhrName, this.ehrQueryForSingleEhr);
//        uploadNamedQuery("composition query for single ehr", compositionQueryForSingleEhrName, this.compositionQueryForSingleEhr);
//    }
//
//    @PreDestroy
//    public void destroy() {
//        for (String queryName : allQueryNames) {
//            deleteQuery(queryName);
//        }
//    }
//
//    @Test
//    public void queryGet() {
//
//        ResponseEntity<OpenEhrQueryResponse> response = getResponse(getTargetPath() + "/query/aql?q=" + query + "&offset=2", OpenEhrQueryResponse.class);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.getRows()).isNull();
//
//        ResponseEntity<OpenEhrQueryResponse> response2 = getResponse(getTargetPath() + "/query/aql?q=" + query, OpenEhrQueryResponse.class);
//        assertThat(response2.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body2 = response2.getBody();
//        assertThat(body2).isNotNull();
//        assertThat(body2.getName()).isNull();
//        assertThat(body2.getRows()).hasSize(2);
//        assertThat(body2.getColumns()).hasSize(2);
//        assertThat(body2.getColumns().get(0).getName()).isEqualTo("temperature");
//        assertThat(body2.getColumns().get(0).getPath()).isEqualTo("/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/magnitude");
//        assertThat(body2.getColumns().get(1).getName()).isEqualTo("unit");
//        assertThat(body2.getColumns().get(1).getPath()).isEqualTo("/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/units");
//        assertThat(body2.getRows().get(0).get(0)).isEqualTo(40.7d);
//        assertThat(body2.getRows().get(0).get(1)).isEqualTo("°C");
//        assertThat(body2.getRows().get(1).get(0)).isEqualTo(37.2d);
//        assertThat(body2.getRows().get(1).get(1)).isEqualTo("°C");
//
//        ResponseEntity<OpenEhrQueryResponse> response3 = getResponse(getTargetPath() + "/query/aql?q=" + query + "&offset=1", OpenEhrQueryResponse.class);
//        assertThat(response3.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body3 = response3.getBody();
//        assertThat(body3).isNotNull();
//        assertThat(body3.getName()).isNull();
//        assertThat(body3.getRows()).hasSize(1);
//        assertThat(body3.getColumns()).hasSize(2);
//        assertThat(body3.getRows().get(0).get(0)).isEqualTo(37.2d);
//        assertThat(body3.getRows().get(0).get(1)).isEqualTo("°C");
//
//        ResponseEntity<OpenEhrQueryResponse> response4 = getResponse(getTargetPath() + "/query/aql?q=" + query + "&fetch=1", OpenEhrQueryResponse.class);
//        assertThat(response4.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body4 = response4.getBody();
//        assertThat(body4).isNotNull();
//        assertThat(body4.getName()).isNull();
//        assertThat(body4.getRows()).hasSize(1);
//        assertThat(body4.getColumns()).hasSize(2);
//        assertThat(body4.getRows().get(0).get(0)).isEqualTo(40.7d);
//        assertThat(body4.getRows().get(0).get(1)).isEqualTo("°C");
//    }
//
//    @Test
//    public void queryGetByEhrIdRequestParam() {
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(getTargetPath() + "/query/aql?q={query}&ehr_id={ehr_id}&temp={temp}",
//                                                                 GET,
//                                                                 null,
//                                                                 OpenEhrQueryResponse.class,
//                                                                 null,
//                                                                 ehrQueryForSingleEhr,
//                                                                 ehrId,
//                                                                 37.1d);
//
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.getRows()).hasSize(1);
//    }
//
//    @Test
//    public void queryGetByEhrIdHeader() {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("openEHR-EHR-id", ehrId);
//
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(getTargetPath() + "/query/aql?q={query}&temp={temp}",
//                                                                 GET,
//                                                                 null,
//                                                                 OpenEhrQueryResponse.class,
//                                                                 httpHeaders,
//                                                                 compositionQueryForSingleEhr,
//                                                                 37.1d);
//
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.getRows()).hasSizeGreaterThan(0);
//    }
//
//    @Test
//    public void queryGetByEhrIdRequestParamAsQueryParam() {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("openEHR-EHR-id", ehrId);
//
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(getTargetPath() + "/query/aql?q={query}",
//                                                                 GET,
//                                                                 null,
//                                                                 OpenEhrQueryResponse.class,
//                                                                 httpHeaders,
//                                                                 "SELECT DISTINCT e/ehr_id/value FROM EHR e[ehr_id/value=:ehr_id]");
//
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.getRows()).hasSize(1);
//    }
//
//    @Test
//    public void queryGetEhrIdConflict() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("openEhr-ehr-id", ehrId);
//
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> exchange(getTargetPath() + "/query/aql?q={query}&ehr_id={ehr_id}&offset=2",
//                               GET,
//                               null,
//                               String.class,
//                               headers,
//                               query,
//                               "unknown-ehr-id"));
//        assertThat(httpException.getStatusCode()).isEqualTo(CONFLICT);
//    }
//
//    @Test
//    public void queryGet400() {
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(getTargetPath() + "/query/aql?q=balblablablablabla", String.class));
//        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
//    }
//
//    @Test
//    public void queryPost() {
//
//        OpenEhrQueryRequest request = new OpenEhrQueryRequest();
//        request.setQ(query);
//        request.setOffset(2);
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(getTargetPath() + "/query/aql", POST, request, OpenEhrQueryResponse.class);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.getName()).isNull();
//        assertThat(body.getRows()).isNull();
//
//        request.setOffset(0);
//        ResponseEntity<OpenEhrQueryResponse> response2 = exchange(getTargetPath() + "/query/aql", POST, request, OpenEhrQueryResponse.class);
//        assertThat(response2.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body2 = response2.getBody();
//        assertThat(body2).isNotNull();
//        assertThat(body2.getName()).isNull();
//        assertThat(body2.getRows()).hasSize(2);
//        assertThat(body2.getColumns()).hasSize(2);
//        assertThat(body2.getColumns().get(0).getName()).isEqualTo("temperature");
//        assertThat(body2.getColumns().get(0).getPath()).isEqualTo("/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/magnitude");
//        assertThat(body2.getColumns().get(1).getName()).isEqualTo("unit");
//        assertThat(body2.getColumns().get(1).getPath()).isEqualTo("/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/units");
//        assertThat(body2.getRows().get(0).get(0)).isEqualTo(40.7d);
//        assertThat(body2.getRows().get(0).get(1)).isEqualTo("°C");
//        assertThat(body2.getRows().get(1).get(0)).isEqualTo(37.2d);
//        assertThat(body2.getRows().get(1).get(1)).isEqualTo("°C");
//
//        request.setOffset(1);
//        ResponseEntity<OpenEhrQueryResponse> response3 = exchange(getTargetPath() + "/query/aql", POST, request, OpenEhrQueryResponse.class);
//        assertThat(response3.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body3 = response3.getBody();
//        assertThat(body3).isNotNull();
//        assertThat(body3.getName()).isNull();
//        assertThat(body3.getRows()).hasSize(1);
//        assertThat(body3.getColumns()).hasSize(2);
//        assertThat(body3.getRows().get(0).get(0)).isEqualTo(37.2d);
//        assertThat(body3.getRows().get(0).get(1)).isEqualTo("°C");
//
//        ResponseEntity<OpenEhrQueryResponse> response4 = getResponse(getTargetPath() + "/query/aql?q=" + query + "&fetch=1", OpenEhrQueryResponse.class);
//        assertThat(response4.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body4 = response4.getBody();
//        assertThat(body4).isNotNull();
//        assertThat(body4.getName()).isNull();
//        assertThat(body4.getRows()).hasSize(1);
//        assertThat(body4.getColumns()).hasSize(2);
//        assertThat(body4.getRows().get(0).get(0)).isEqualTo(40.7d);
//        assertThat(body4.getRows().get(0).get(1)).isEqualTo("°C");
//    }
//
//    @Test
//    public void queryPostByEhrIdQueryParam() {
//        OpenEhrQueryRequest request = new OpenEhrQueryRequest();
//        request.setQ(compositionQueryForSingleEhr);
//
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("ehr_id", ehrId);
//        queryParameters.put("temp", 37.1d);
//        request.setQueryParameters(queryParameters);
//
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(getTargetPath() + "/query/aql", POST, request, OpenEhrQueryResponse.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.getRows()).hasSizeGreaterThan(0);
//    }
//
//    @Test
//    public void queryPostByEhrIdHeader() {
//        OpenEhrQueryRequest request = new OpenEhrQueryRequest();
//        request.setQ(ehrQueryForSingleEhr);
//
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("temp", 37.1d);
//        request.setQueryParameters(queryParameters);
//
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("openEHR-EHR-id", ehrId);
//
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(getTargetPath() + "/query/aql", POST, request, OpenEhrQueryResponse.class, httpHeaders);
//
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.getRows()).hasSize(1);
//    }
//
//    @Test
//    public void queryPostEhrIdConflict() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("openEhr-ehr-id", ehrId);
//
//        OpenEhrQueryRequest request = new OpenEhrQueryRequest();
//        request.setQ(query);
//        request.setOffset(2);
//        request.setQueryParameters(Collections.singletonMap("ehr_id", "unknown-ehr-id"));
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> exchange(getTargetPath() + "/query/aql",
//                               POST,
//                               request,
//                               String.class,
//                               headers,
//                               query,
//                               "unknown-ehr-id"));
//        assertThat(httpException.getStatusCode()).isEqualTo(CONFLICT);
//    }
//
//    @Test
//    public void queryGetQualifiedQueryNamedParameterQuery() {
//
//        // 200
//        String qualifiedQueryName = tempLargerThanName;
//        ResponseEntity<OpenEhrQueryResponse> response = getResponse(
//                getTargetPath() + "/query/{qualified_query_name}?ehr_id={ehr_id}&temp={temp}",
//                OpenEhrQueryResponse.class,
//                qualifiedQueryName,
//                ehrId,
//                37.1d);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//
//        OpenEhrQueryResponse body = getBody(response);
//        assertThat(body).isNotNull();
//        assertThat(body.getName()).isEqualTo(qualifiedQueryName);
//        assertThat(body.getRows()).hasSize(2);
//
//        // 400 missing parameter
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/query/{qualified_query_name}?ehr_id={ehr_id}",
//                        String.class,
//                        qualifiedQueryName,
//                        ehrId,
//                        37.1d));
//        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
//
//        // 400 other
//        HttpStatusCodeException httpException1 = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(
//                        getTargetPath() + "/query/{qualified_query_name}?ehr_id={ehr_id}&temp={temp}",
//                        String.class,
//                        qualifiedQueryName,
//                        37.1d,
//                        ehrId));
//        assertThat(httpException1.getStatusCode()).isEqualTo(BAD_REQUEST);
//    }
//
//    @Test
//    public void queryGetQualifiedQueryNamedParameterQueryWithVersion() {
//        // 200
//        ResponseEntity<OpenEhrQueryResponse> response = getResponse(
//                getTargetPath() + "/query/{qualified_query_name}/0.0.0?ehr_id={ehr_id}&temp={temp}",
//                OpenEhrQueryResponse.class,
//                tempLargerThanName,
//                ehrId,
//                37.1d);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//
//        OpenEhrQueryResponse body = getBody(response);
//        assertThat(body).isNotNull();
//        assertThat(body.getName()).isEqualTo(tempLargerThanName);
//        assertThat(body.getRows()).hasSize(2);
//    }
//
//    @Test
//    public void queryGetQualifiedQueryNamedParameterQueryEhrIdRequestParam() {
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(
//                getTargetPath() + "/query/{qualified_query_name}?ehr_id={ehr_id}&temp={temp}",
//                GET,
//                null,
//                OpenEhrQueryResponse.class,
//                null,
//                ehrQueryForSingleEhrName,
//                ehrId,
//                37.1d);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//
//        OpenEhrQueryResponse body = getBody(response);
//        assertThat(body).isNotNull();
//        assertThat(body.getName()).isEqualTo(ehrQueryForSingleEhrName);
//        assertThat(body.getRows()).hasSize(1);
//    }
//
//    @Test
//    public void queryGetQualifiedQueryNamedParameterQueryEhrIdHeader() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("openEhr-ehr-id", ehrId);
//
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(
//                getTargetPath() + "/query/{qualified_query_name}?temp={temp}",
//                GET,
//                null,
//                OpenEhrQueryResponse.class,
//                headers,
//                ehrQueryForSingleEhrName,
//                37.1d);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//
//        OpenEhrQueryResponse body = getBody(response);
//        assertThat(body).isNotNull();
//        assertThat(body.getName()).isEqualTo(ehrQueryForSingleEhrName);
//        assertThat(body.getRows()).hasSize(1);
//    }
//
//    @Test
//    public void queryPostQualifiedQueryNamedParameterQuery() {
//
//        OpenEhrQueryRequest request = new OpenEhrQueryRequest();
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("ehr_id", ehrId);
//        queryParameters.put("temp", 37.1d);
//        request.setQueryParameters(queryParameters);
//        // 200
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(
//                getTargetPath() + "/query/{qualified_query_name}",
//                POST,
//                request,
//                OpenEhrQueryResponse.class,
//                null,
//                tempLargerThanName);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body = getBody(response);
//        assertThat(body).isNotNull();
//        assertThat(body.getRows()).hasSize(2);
//
//        // 400 missing parameter
//        queryParameters.remove("temp");
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> exchange(
//                        getTargetPath() + "/query/{qualified_query_name}",
//                        POST,
//                        request,
//                        String.class,
//                        null,
//                        tempLargerThanName));
//        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
//
//        // 400 other
//        queryParameters.put("ehr_id", 37.1d);
//        queryParameters.put("temp", ehrId);
//        HttpStatusCodeException httpException1 = assertThrows(
//                HttpStatusCodeException.class,
//                () -> exchange(
//                        getTargetPath() + "/query/{qualified_query_name}",
//                        POST,
//                        request,
//                        String.class,
//                        null,
//                        tempLargerThanName));
//        assertThat(httpException1.getStatusCode()).isEqualTo(BAD_REQUEST);
//    }
//
//    @Test
//    public void queryPostQualifiedQueryNamedParameterQueryWithVersion() {
//
//        OpenEhrQueryRequest request = new OpenEhrQueryRequest();
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("ehr_id", ehrId);
//        queryParameters.put("temp", 37.1d);
//        request.setQueryParameters(queryParameters);
//        // 200
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(
//                getTargetPath() + "/query/{qualified_query_name}/0.0.0",
//                POST,
//                request,
//                OpenEhrQueryResponse.class,
//                null,
//                tempLargerThanName);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body = getBody(response);
//        assertThat(body).isNotNull();
//        assertThat(body.getRows()).hasSize(2);
//    }
//
//    @Test
//    public void queryPostQualifiedQueryNamedParameterQueryRequestParamAndBodyEhrIdConflict() {
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("openEhr-ehr-id", ehrId);
//
//        OpenEhrQueryRequest request = new OpenEhrQueryRequest();
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("ehr_id", ehrId1);
//        queryParameters.put("temp", 37.1d);
//        request.setQueryParameters(queryParameters);
//
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> exchange(
//                        getTargetPath() + "/query/{qualified_query_name}",
//                        POST,
//                        request,
//                        String.class,
//                        headers,
//                        tempLargerThanName));
//        assertThat(httpException.getStatusCode()).isEqualTo(CONFLICT);
//    }
//
//    @Test
//    public void queryPostQualifiedQueryNamedParameterEhrIdHeader() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("openEhr-ehr-id", ehrId);
//
//        OpenEhrQueryRequest request = new OpenEhrQueryRequest();
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("temp", 37.1d);
//        request.setQueryParameters(queryParameters);
//
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(
//                getTargetPath() + "/query/{qualified_query_name}",
//                POST,
//                request,
//                OpenEhrQueryResponse.class,
//                headers,
//                ehrQueryForSingleEhrName);
//
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body = getBody(response);
//        assertThat(body).isNotNull();
//        assertThat(body.getRows()).hasSize(1);
//    }
//
//    @Test
//    public void queryPostQualifiedQueryNamedParameterEhrIdQueryParam() {
//        OpenEhrQueryRequest request = new OpenEhrQueryRequest();
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("ehr_id", ehrId);
//        queryParameters.put("temp", 37.1d);
//        request.setQueryParameters(queryParameters);
//
//        ResponseEntity<OpenEhrQueryResponse> response = exchange(
//                getTargetPath() + "/query/{qualified_query_name}",
//                POST,
//                request,
//                OpenEhrQueryResponse.class,
//                null,
//                compositionQueryForSingleEhrName);
//
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrQueryResponse body = getBody(response);
//        assertThat(body).isNotNull();
//        assertThat(body.getRows()).hasSizeGreaterThan(0);
//    }
//
//    @Test
//    public void queryPostQualifiedQueryNamedParameterQueryRequestParamAndHeaderEhrIdConflict() {
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("openEhr-ehr-id", ehrId);
//
//        OpenEhrQueryRequest request = new OpenEhrQueryRequest();
//        Map<String, Object> queryParameters = new HashMap<>();
//        queryParameters.put("ehr_id", "unknown-ehr-id");
//        queryParameters.put("temp", 37.1d);
//        request.setQueryParameters(queryParameters);
//
//        // 200
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> exchange(
//                        getTargetPath() + "/query/{qualified_query_name}",
//                        POST,
//                        request,
//                        String.class,
//                        headers,
//                        tempLargerThanName));
//        assertThat(httpException.getStatusCode()).isEqualTo(CONFLICT);
//    }
//
//    @Test
//    public void getQualifiedQueryDefinition() {
//
//        ResponseEntity<OpenEhrViewResponse[]> response = getResponse(
//                getTargetPath() + "/definition/query/{qualified_query_name}", OpenEhrViewResponse[].class, maxTempName);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrViewResponse[] body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.length).isEqualTo(1);
//
//        OpenEhrViewResponse viewResponse = body[0];
//        assertThat(viewResponse.getName()).isEqualTo(maxTempName);
//        assertThat(viewResponse.getType()).isEqualTo("aql");
//        assertThat(viewResponse.getQ()).isEqualTo(maxTempQuery);
//    }
//
//    @Test
//    public void searchQualifiedQueryDefinition() {
//
//        ResponseEntity<OpenEhrViewResponse[]> response = getResponse(
//                getTargetPath() + "/definition/query/{qualified_query_name}", OpenEhrViewResponse[].class, maxTempName);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrViewResponse[] body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.length).isEqualTo(1);
//
//        OpenEhrViewResponse viewResponse = body[0];
//        assertThat(viewResponse.getName()).isEqualTo(maxTempName);
//        assertThat(viewResponse.getType()).isEqualTo("aql");
//        assertThat(viewResponse.getQ()).isEqualTo(maxTempQuery);
//
//        ResponseEntity<OpenEhrViewResponse[]> response2 = getResponse(getTargetPath() + "/definition/query", OpenEhrViewResponse[].class);
//        assertThat(response2.getStatusCode()).isEqualTo(OK);
//        OpenEhrViewResponse[] body2 = response2.getBody();
//        assertThat(body2).isNotNull();
//        assertThat(body2).hasSizeGreaterThan(5);
//        assertThat(body2[0].getName()).isEqualTo(tempLargerThanName);
//        assertThat(body2[0].getType()).isEqualTo("aql");
//        assertThat(body2[0].getQ()).isEqualTo(namedParameterQuery);
//    }
//
//    @Test
//    public void getEmptyQualifiedQueryVersionedDefinitionByGroup() {
//        // precondition: any exists
//        ResponseEntity<OpenEhrViewResponse> response = getResponse(
//                getTargetPath() + "/definition/query/{qualified_query_name}/0.0.0", OpenEhrViewResponse.class, groupOneName);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrViewResponse body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.getName()).isEqualTo(groupOneName);
//        assertThat(body.getType()).isEqualTo("aql");
//        assertThat(body.getQ()).isEqualTo(namedParameterQuery);
//
//        // check that filter only by group is not working
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(getTargetPath() + "/definition/query/{qualified_query_name}/0.0.0", String.class, "group"));
//        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
//
//        HttpStatusCodeException httpException1 = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(getTargetPath() + "/definition/query/{qualified_query_name}/0.0.0", String.class, "group::on"));
//        assertThat(httpException1.getStatusCode()).isEqualTo(NOT_FOUND);
//    }
//
//    @Test
//    public void searchQualifiedQueryVersionedDefinitionByGroup() {
//
//        ResponseEntity<OpenEhrViewResponse[]> response = getResponse(
//                getTargetPath() + "/definition/query/{qualified_query_name}", OpenEhrViewResponse[].class, "group");
//
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrViewResponse[] body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.length).isEqualTo(2);
//    }
//
//    @Test
//    public void setQualifiedQueryDefinition() {
//
//        OpenEhrViewRequest view = new OpenEhrViewRequest();
//        view.setDescription(testQuery1);
//        view.setQ(query);
//        ResponseEntity<OpenEhrViewResponse> response = exchange(getTargetPath() + "/definition/query/{qualified_query_name}",
//                                                                PUT,
//                                                                view,
//                                                                OpenEhrViewResponse.class,
//                                                                null,
//                                                                testQuery1);
//        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
//        validateLocationAndETag(response, true, true);
//        assertThat(response.getBody()).isNull();
//
//        ResponseEntity<OpenEhrViewResponse[]> response1 = getResponse(
//                getTargetPath() + "/definition/query/{qualified_query_name}", OpenEhrViewResponse[].class, testQuery1);
//        assertThat(response1.getStatusCode()).isEqualTo(OK);
//
//        OpenEhrViewResponse[] body1 = response1.getBody();
//        assertThat(body1).isNotNull();
//        assertThat(body1.length).isEqualTo(1);
//
//        OpenEhrViewResponse viewResponse1 = body1[0];
//        assertThat(viewResponse1).isNotNull();
//        assertThat(viewResponse1.getName()).isEqualTo(testQuery1);
//        assertThat(viewResponse1.getType()).isEqualTo("aql");
//        assertThat(viewResponse1.getQ()).isEqualTo(query);
//    }
//
//    @Test
//    public void setQualifiedQueryDefinitionPreferRepresentation() {
//
//        OpenEhrViewRequest view = new OpenEhrViewRequest();
//        view.setDescription(testQuery2);
//        view.setQ(query);
//
//        ResponseEntity<OpenEhrViewResponse> response = exchange(getTargetPath() + "/definition/query/{qualified_query_name}",
//                                                                PUT,
//                                                                view,
//                                                                OpenEhrViewResponse.class,
//                                                                fullRepresentationHeaders(),
//                                                                testQuery2);
//
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        validateLocationAndETag(response, true, true);
//        OpenEhrViewResponse viewResponse = response.getBody();
//
//        assertThat(viewResponse).isNotNull();
//        assertThat(viewResponse.getName()).isEqualTo(testQuery2);
//        assertThat(viewResponse.getType()).isEqualTo("aql");
//        assertThat(viewResponse.getQ()).isEqualTo(query);
//    }
//
//    @Test
//    public void setQualifiedQueryDefinitionWithAqlType() {
//
//        OpenEhrViewRequest view = new OpenEhrViewRequest();
//        view.setDescription(testQuery3);
//        view.setQ(query);
//        ResponseEntity<String> response = exchange(
//                getTargetPath() + "/definition/query/{qualified_query_name}?type={type}",
//                PUT,
//                view,
//                String.class,
//                null,
//                testQuery3,
//                "AQL");
//        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
//    }
//
//    @Test
//    public void setQualifiedQueryDefinitionWithUnsupportedType() {
//
//        OpenEhrViewRequest view = new OpenEhrViewRequest();
//        view.setDescription(testQuery4);
//        view.setQ(query);
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> exchange(getTargetPath() + "/definition/query/{qualified_query_name}?type={type}",
//                               PUT,
//                               view,
//                               String.class,
//                               null,
//                               testQuery4,
//                               "UnsupportedType"));
//        assertThat(httpException.getStatusCode()).isEqualTo(BAD_REQUEST);
//    }
//
//    @Test
//    public void setQualifiedQueryVersionedDefinition() {
//
//        OpenEhrViewRequest view = new OpenEhrViewRequest();
//        view.setDescription(testQuery5);
//        view.setQ(query);
//        ResponseEntity<OpenEhrViewResponse> response = exchange(getTargetPath() + "/definition/query/{qualified_query_name}/2.0.0",
//                                                                PUT,
//                                                                view,
//                                                                OpenEhrViewResponse.class,
//                                                                null,
//                                                                testQuery5);
//        assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
//        validateLocationAndETag(response, true, true);
//        assertThat(response.getBody()).isNull();
//
//        // versioning is unsupported
//        HttpStatusCodeException httpException = assertThrows(
//                HttpStatusCodeException.class,
//                () -> getResponse(getTargetPath() + "/definition/query/{qualified_query_name}/2.0.0", String.class, testQuery5));
//        assertThat(httpException.getStatusCode()).isEqualTo(NOT_FOUND);
//    }
//
//    @Test
//    public void getConformance() {
//        ResponseEntity<OpenEhrConformance> response = exchange(getTargetPath(), OPTIONS, null, OpenEhrConformance.class);
//        assertThat(response.getStatusCode()).isEqualTo(OK);
//        OpenEhrConformance body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(body.getConformanceProfile()).isEqualTo(conformance.getConformanceProfile());
//        assertThat(body.getRestapiSpecsVersion()).isEqualTo(conformance.getRestapiSpecsVersion());
//        assertThat(body.getSolution()).isEqualTo(conformance.getSolution());
//        assertThat(body.getSolutionVersion()).isEqualTo(conformance.getSolutionVersion());
//        assertThat(body.getVendor()).isEqualTo(conformance.getVendor());
//        assertThat(body.getEndpoints()).isEqualTo(conformance.getEndpoints());
//    }
//
//    private void deleteQuery(String queryName) {
//        //todo missing in spec https://specifications.openehr.org/releases/ITS-REST/latest/definitions.html
//    }
//
//    private String setQueryName(String queryName) {
//        allQueryNames.add(queryName);
//        return queryName;
//    }

}
