package org.openehr.rest.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openehr.data.OpenEhrConformance;
import org.openehr.rest.serialize.BetterObjectMapper;
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
        return new BetterObjectMapper();
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
