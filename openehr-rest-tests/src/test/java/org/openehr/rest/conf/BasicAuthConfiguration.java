package org.openehr.rest.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.openehr.rest.auth.AuthUtils.createRestTemplate;

/**
 * @author Dusan Markovic
 */
@Configuration
@ConditionalOnProperty(value = "auth.basic.username")
@Import(MessageConvertersConfiguration.class)
public class BasicAuthConfiguration {

    @Bean
    public RestTemplate restTemplate(
            @Value("${openehr.rest.uri}") URI uri,
            @Value("${auth.basic.username}") String username,
            @Value("${auth.basic.password}") String password,
            StringHttpMessageConverter stringHttpMessageConverter,
            Jaxb2RootElementHttpMessageConverter jaxb2MessageConverter,
            ByteArrayHttpMessageConverter byteArrayHttpMessageConverter,
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        return createRestTemplate(
                uri, username, password, null,
                stringHttpMessageConverter, jaxb2MessageConverter, byteArrayHttpMessageConverter, mappingJackson2HttpMessageConverter);
    }
}
