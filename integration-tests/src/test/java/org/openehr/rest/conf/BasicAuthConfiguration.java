package org.openehr.rest.conf;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openehr.rest.BasicAuthHttpRequestFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dusan Markovic
 */
@Configuration
@ConditionalOnProperty(value = "auth.basic.username")
@Import(MessageConvertersConfiguration.class)
public class BasicAuthConfiguration {

    @Bean
    public RestTemplate restTemplateBasicAuth(
            @Value("${openehr.rest.uri}") URI uri,
            @Value("${auth.basic.username}") String username,
            @Value("${auth.basic.password}") String password,
            StringHttpMessageConverter stringHttpMessageConverter,
            Jaxb2RootElementHttpMessageConverter jaxb2MessageConverter,
            ByteArrayHttpMessageConverter byteArrayHttpMessageConverter,
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        return createRestTemplateWithBasicAuth(
                uri, username, password, new NoopResponseErrorHandler(),
                stringHttpMessageConverter, jaxb2MessageConverter, byteArrayHttpMessageConverter, mappingJackson2HttpMessageConverter);
    }

    public RestTemplate createRestTemplateWithBasicAuth(
            URI uri, String username, String password, ResponseErrorHandler errorHandler,
            StringHttpMessageConverter stringHttpMessageConverter,
            Jaxb2RootElementHttpMessageConverter jaxb2MessageConverter,
            ByteArrayHttpMessageConverter byteArrayHttpMessageConverter,
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        HttpClient client = createDefaultHttpClient(username, password);
        BasicAuthHttpRequestFactory requestFactory = new BasicAuthHttpRequestFactory(client, host, getCredentialsProvider(username, password));

        RestTemplate templateWithAuth = new RestTemplate(requestFactory);
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

    protected CloseableHttpClient createDefaultHttpClient(String username, String password) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (username != null && password != null) {
            httpClientBuilder.setDefaultCredentialsProvider(getCredentialsProvider(username, password));
        }
        return httpClientBuilder.build();
    }

    protected CredentialsProvider getCredentialsProvider(String username, String password) {
        BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
        basicCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        return basicCredentialsProvider;
    }

    private static final class NoopResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) {
        }
    }

}
