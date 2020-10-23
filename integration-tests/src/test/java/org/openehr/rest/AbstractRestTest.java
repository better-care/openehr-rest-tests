package org.openehr.rest;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.openehr.rest.conf.WebClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * @author Dusan Markovic
 */
@ContextConfiguration(classes = WebClientConfiguration.class)
public class AbstractRestTest {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime().withZoneUTC();

    @Autowired
    protected RestTemplate restTemplate;
    @Value("${openehr.rest.uri}")
    protected URI uri;

    protected String targetPath;

    @PostConstruct
    public void setUp() throws IOException {
        targetPath = uri.toURL().toString();
    }

    public String getTargetPath() {
        return targetPath;
    }

    protected <R> R get(String url, Class<R> responseType) {
        return getResponse(url, responseType).getBody();
    }

    protected <R> R get(String url, Class<R> responseType, MediaType acceptMediaTypes) {
        return getResponse(url, responseType, acceptMediaTypes).getBody();
    }

    protected <R> ResponseEntity<R> getResponse(String url, Class<R> responseType, Object... uriVariables) {
        return getResponse(restTemplate, url, responseType, null, uriVariables);
    }

    protected <R> ResponseEntity<R> getResponse(String url, Class<R> responseType, MediaType acceptMediaTypes, Object... uriVariables) {
        return getResponse(restTemplate, url, responseType, acceptMediaTypes, uriVariables);
    }

    protected <R> ResponseEntity<R> getResponse(
            RestTemplate restTemplate,
            String url,
            Class<R> responseType,
            MediaType acceptMediaTypes,
            Object... uriVariables) {
        HttpHeaders acceptHeaders = acceptMediaTypes == null ? new HttpHeaders() : createAcceptHeaders(acceptMediaTypes);
        return exchange(restTemplate, url, HttpMethod.GET, null, responseType, acceptHeaders, uriVariables);
    }

    protected <R> ResponseEntity<R> getResponse(RestTemplate restTemplate, String url, Class<R> responseType) {
        HttpHeaders acceptHeaders = new HttpHeaders();
        return exchange(restTemplate, url, HttpMethod.GET, null, responseType, acceptHeaders);
    }

    protected <R> ResponseEntity<R> deleteResponse(String url, Class<R> responseType, MediaType... acceptMediaTypes) {
        HttpHeaders acceptHeaders = acceptMediaTypes == null ? null : createAcceptHeaders(acceptMediaTypes);
        return exchange(url, HttpMethod.DELETE, null, responseType, acceptHeaders);
    }

    protected <R> ResponseEntity<R> deleteResponse(RestTemplate restTemplate, String url, Class<R> responseType, MediaType... acceptMediaTypes) {
        HttpHeaders acceptHeaders = acceptMediaTypes == null ? null : createAcceptHeaders(acceptMediaTypes);
        return exchange(restTemplate, url, HttpMethod.DELETE, null, responseType, acceptHeaders);
    }

    protected <R, B> ResponseEntity<R> exchange(
            String url,
            HttpMethod httpMethod,
            @Nullable B body,
            Class<R> responseType,
            MediaType acceptMediaTypes) {
        HttpHeaders acceptHeaders = acceptMediaTypes == null ? null : createAcceptHeaders(acceptMediaTypes);
        return exchange(url, httpMethod, body, responseType, acceptHeaders);
    }

    protected <R, B> ResponseEntity<R> exchange(
            String url,
            HttpMethod httpMethod,
            @Nullable B body,
            Class<R> responseType,
            @Nullable HttpHeaders headers,
            Object... uriVariables) {
        return exchange(restTemplate, url, httpMethod, body, responseType, headers, uriVariables);
    }

    protected <R, B> ResponseEntity<R> exchange(
            String url,
            HttpMethod httpMethod,
            @Nullable B body,
            @Nullable HttpHeaders headers,
            @Nullable MediaType defaultContentType,
            Class<R> responseType,
            Object... uriVariables) {
        return exchange(restTemplate, url, httpMethod, body, responseType, headers, defaultContentType, uriVariables);
    }

    protected <R, B> ResponseEntity<R> exchange(
            String url,
            HttpMethod httpMethod,
            @Nullable B body,
            Class<R> responseType) {
        return exchange(restTemplate, url, httpMethod, body, responseType, null);
    }

    protected <R> ResponseEntity<R> exchange(
            RestTemplate restTemplate,
            String url,
            HttpMethod httpMethod,
            Class<R> responseType) {
        return exchange(restTemplate, url, httpMethod, null, responseType, null);
    }

    protected <R, B> ResponseEntity<R> exchange(
            RestTemplate restTemplate,
            String url,
            HttpMethod httpMethod,
            @Nullable B body,
            Class<R> responseType,
            @Nullable HttpHeaders headers,
            Object... uriVariables) {
        return exchange(restTemplate, url, httpMethod, body, responseType, headers, APPLICATION_JSON, uriVariables);
    }

    protected <R, B> ResponseEntity<R> exchange(
            RestTemplate restTemplate,
            String url,
            HttpMethod httpMethod,
            @Nullable B body,
            Class<R> responseType,
            @Nullable HttpHeaders headers,
            @Nullable MediaType defaultContentType,
            Object... uriVariables) {
        if (headers == null) {
            headers = createAcceptHeaders(APPLICATION_JSON);
        } else if (!headers.containsKey(ACCEPT)) {
            headers.setAccept(Collections.singletonList(APPLICATION_JSON));
        }
        if (headers.getContentType() == null) {
            headers.setContentType(defaultContentType);
        }
        HttpEntity<B> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, httpMethod, requestEntity, responseType, uriVariables);
    }

    protected HttpHeaders createAcceptHeaders(MediaType... headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Arrays.asList(headers));
        return httpHeaders;
    }

    protected HttpHeaders createContentTypeHeaders(MediaType contentType) {
        return createContentTypeHeaders(contentType, APPLICATION_JSON);
    }

    protected HttpHeaders createContentTypeHeaders(MediaType contentType, MediaType... accept) {
        HttpHeaders httpHeaders = createAcceptHeaders(accept);
        httpHeaders.setContentType(contentType);
        return httpHeaders;
    }

    protected void validateLocationAndETag(ResponseEntity<?> response) {
        validateLocationAndETag(response, true, true);
    }

    protected void validateLocationAndETag(ResponseEntity<?> response, boolean etagAndLastModified, boolean location) {
        URI locationUri = response.getHeaders().getLocation();
        if (location) {
            assertThat(locationUri).isNotNull();
            assertThat(locationUri.toString()).startsWith(getTargetPath());
            String decodedUrl;
            try {
                decodedUrl = URLDecoder.decode(locationUri.toString(), StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Unable to decode url: " + locationUri);
            }
            ResponseEntity<String> r = getResponse(decodedUrl, String.class, MediaType.ALL);
            assertThat(r).isNotNull();
            if (r.getStatusCode() == OK) {
                assertThat(r.getBody()).isNotNull();
            } else if (r.getStatusCode() == NO_CONTENT) {
                assertThat(r.getBody()).isNull();
            } else {
                throw new RuntimeException("Unable to validate location header: " + locationUri);
            }
        } else {
            assertThat(locationUri).isNull();
        }
        String eTagString = response.getHeaders().getETag();
        if (etagAndLastModified) {
            assertThat(eTagString).isNotNull();
            assertThat(eTagString).startsWith("\"");
            assertThat(eTagString).endsWith("\"");
            long lastModified = response.getHeaders().getLastModified();
            if (response.getStatusCode().is2xxSuccessful()) {
                assertThat(lastModified).isGreaterThan(0L);
            }
            if (location) {
                assertThat(locationUri.toString()).endsWith(eTagString.substring(1, eTagString.length() - 1));
            }
        } else {
            assertThat(eTagString).isNull();
        }
    }

    protected HttpHeaders fullRepresentationHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Prefer", "return=representation");
        return headers;
    }
}
