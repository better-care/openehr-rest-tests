package org.openehr.rest.conf;

import org.openehr.rest.auth.AuthUtils;
import org.openehr.rest.auth.NoopResponseErrorHandler;
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

/**
 * @author Dusan Markovic
 */
@Configuration
@ConditionalOnProperty(value = "auth.basic.username", matchIfMissing = true, havingValue = "wYu9jn8UuMih75Pndl7K")
@Import(MessageConvertersConfiguration.class)
public class NoAuthConfiguration {

    @Bean
    public RestTemplate restTemplateBasicAuth(
            @Value("${openehr.rest.uri}") URI uri,
            StringHttpMessageConverter stringHttpMessageConverter,
            Jaxb2RootElementHttpMessageConverter jaxb2MessageConverter,
            ByteArrayHttpMessageConverter byteArrayHttpMessageConverter,
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        return AuthUtils.createRestTemplate(
                uri, null, null, new NoopResponseErrorHandler(),
                stringHttpMessageConverter, jaxb2MessageConverter, byteArrayHttpMessageConverter, mappingJackson2HttpMessageConverter);
    }


}
