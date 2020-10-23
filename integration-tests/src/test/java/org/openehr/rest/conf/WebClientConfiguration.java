package org.openehr.rest.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openehr.rest.serialize.BetterObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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

}
