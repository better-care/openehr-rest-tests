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

package org.openehr.rest.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openehr.data.OpenEhrConformance;
import org.openehr.rest.json.OpenEhrObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Dusan Markovic
 */
@Configuration
@Import(value = {BasicAuthConfiguration.class})
public class WebClientConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new OpenEhrObjectMapper();
    }

    @Bean
    public OpenEhrConformance openEhrConformance(
            @Value("${openehr.conformance.solution}") String conformanceSolution,
            @Value("${openehr.conformance.vendor}") String conformanceVendor,
            @Value("${openehr.conformance.restapi-specs-version}") String conformanceRestapiSpecsVersion,
            @Value("${openehr.conformance.profile}") String conformanceProfile,
            @Value("${openehr.conformance.endpoints}") String[] conformanceEndpoints) {
        OpenEhrConformance openEhrConformance = new OpenEhrConformance();
        openEhrConformance.setSolution(conformanceSolution);
        openEhrConformance.setVendor(conformanceVendor);
        openEhrConformance.setRestapiSpecsVersion(conformanceRestapiSpecsVersion);
        openEhrConformance.setConformanceProfile(conformanceProfile);
        openEhrConformance.setEndpoints(Arrays.stream(conformanceEndpoints).map(e -> "/" + e).collect(Collectors.toList()));
        return openEhrConformance;
    }


}
