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

package org.openehr.rest.auth;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dusan Markovic
 */
public class AuthUtils {
    public static RestTemplate createRestTemplate(
            URI uri, String username, String password, ResponseErrorHandler errorHandler,
            StringHttpMessageConverter stringHttpMessageConverter,
            Jaxb2RootElementHttpMessageConverter jaxb2MessageConverter,
            ByteArrayHttpMessageConverter byteArrayHttpMessageConverter,
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        HttpClient client = createDefaultHttpClient(username, password);

        RestTemplate templateWithAuth = username == null ?
                new RestTemplate() :
                new RestTemplate(new BasicAuthHttpRequestFactory(client, host, getCredentialsProvider(username, password)));
        List<HttpMessageConverter<?>> newMessageConverters = templateWithAuth.getMessageConverters().stream()
                .filter(mc -> !(mc instanceof MappingJackson2HttpMessageConverter))
                .filter(mc -> !(mc instanceof Jaxb2RootElementHttpMessageConverter))
                .filter(mc -> !(mc instanceof StringHttpMessageConverter))
                .filter(mc -> !(mc instanceof ByteArrayHttpMessageConverter))
                .collect(Collectors.toList());
        newMessageConverters.add(0, stringHttpMessageConverter);   // important that this one is first
        newMessageConverters.add(1, jaxb2MessageConverter);
        newMessageConverters.add(2, byteArrayHttpMessageConverter);
        newMessageConverters.add(3, mappingJackson2HttpMessageConverter);
        templateWithAuth.setMessageConverters(newMessageConverters);
        if (errorHandler != null) {
            templateWithAuth.setErrorHandler(errorHandler);
        }
        return templateWithAuth;
    }

    protected static CloseableHttpClient createDefaultHttpClient(String username, String password) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (username != null && password != null) {
            httpClientBuilder.setDefaultCredentialsProvider(getCredentialsProvider(username, password));
        }
        return httpClientBuilder.build();
    }

    protected static CredentialsProvider getCredentialsProvider(String username, String password) {
        BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
        basicCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        return basicCredentialsProvider;
    }
}
