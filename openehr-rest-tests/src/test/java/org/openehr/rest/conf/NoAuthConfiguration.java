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
    public RestTemplate restTemplate(
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
