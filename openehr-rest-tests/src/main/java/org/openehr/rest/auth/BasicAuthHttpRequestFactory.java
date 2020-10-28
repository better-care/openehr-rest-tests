package org.openehr.rest.auth;

import org.apache.http.HttpHost;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.URI;

/**
 * @author Dusan Markovic
 */
public class BasicAuthHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

    protected final HttpHost host;
    protected CredentialsProvider credentialsProvider;

    public BasicAuthHttpRequestFactory(HttpClient client, HttpHost host) {
        super(client);
        this.host = host;
    }

    public BasicAuthHttpRequestFactory(HttpClient client, HttpHost host, CredentialsProvider credentialsProvider) {
        this(client, host);
        this.credentialsProvider = credentialsProvider;
    }

    @Override
    protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
        return createHttpContext();
    }

    protected HttpContext createHttpContext() {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicScheme = new BasicScheme();
        authCache.put(host, basicScheme);

        HttpClientContext context = HttpClientContext.create();
        if (credentialsProvider != null) {
            context.setCredentialsProvider(credentialsProvider);
        }
        context.setAuthCache(authCache);

        return context;
    }
}
