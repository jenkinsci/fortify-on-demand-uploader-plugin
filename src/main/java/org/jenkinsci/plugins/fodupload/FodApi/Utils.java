package org.jenkinsci.plugins.fodupload.FodApi;

import hudson.ProxyConfiguration;
import okhttp3.*;
import okio.Buffer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class Utils {

    static String getRawBody(InputStream stream) throws IOException {
        if (stream == null) return null;

        String content = null;

        content = IOUtils.toString(stream, "utf-8");

        return content;
    }

    static OkHttpClient CreateOkHttpClient(int connectionTimeout, int writeTimeout, int readTimeout, ProxyConfiguration proxy) {
        OkHttpClient.Builder baseClient = new OkHttpClient().newBuilder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS);

        // If there's no proxy just create a normal client
        if (proxy == null)
            return baseClient.build();

        OkHttpClient.Builder proxyClient = baseClient
                .proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.name, proxy.port)));
        // Otherwise set up proxy
        Authenticator proxyAuthenticator;
        final String credentials = Credentials.basic(proxy.getUserName(), proxy.getPassword());

        proxyAuthenticator = (route, response) -> response.request().newBuilder()
                .header("Proxy-Authorization", credentials)
                .build();

        proxyClient.proxyAuthenticator(proxyAuthenticator);
        return proxyClient.build();
    }

    static ResponseContent ResponseContentFromOkHttp3(Response response) throws IOException {
        ResponseContent resp = new ResponseContent(response.body().byteStream(), response.isSuccessful(), response.code(), response.message());

        resp.parseBody();
        return resp;
    }

    static HttpRequest OkHttpRequestToHttpRequest(Request request) throws IOException {
        HttpRequest.Verb verb;

        try {
            verb = HttpRequest.parseVerb(request.method());
        } catch (IllegalArgumentException e) {
            throw new IOException("Unsupported http verb");
        }

        if (request.body() == null) {
            return new StringBodyRequest(request.url().toString(), verb, null, null);
        }

        if (request.body().contentType() == null) throw new IOException("Content-Type not provided");

        String contentType = request.body().contentType().type();
        String subType = request.body().contentType().subtype();
        Charset charset = request.body().contentType().charset(StandardCharsets.UTF_8);

        if (charset != StandardCharsets.UTF_8) throw new IOException("Unsupported charset: " + charset.name());

        if ((contentType != null && contentType.equals("text")) || (subType != null && subType.equals("json"))) {
            Buffer buffer = new Buffer();

            request.body().writeTo(buffer);
            String body = buffer.readUtf8();

            return new StringBodyRequest(request.url().toString(), verb, request.body().contentType().toString(), body);
        }

        throw new IOException("Unsupported Content-Type: " + request.body().contentType().toString());
    }

    static <T extends HttpRequest> Request HttpRequestToOkHttpRequest(T request) {
        Request.Builder r = new Request.Builder()
                .url(request.url());

        for (Map.Entry<String, String> h : request.headers()) {
            r = r.addHeader(h.getKey(), h.getValue());
        }

        switch (request.verb()) {
            case Get:
                r = r.get();
                break;
            case Post:
                r = r.post(getOkHttpRequestBody(request));
                break;
            case Put:
                r = r.put(getOkHttpRequestBody(request));
                break;
            case Patch:
                r = r.patch(getOkHttpRequestBody(request));
                break;
            case Delete:
                r = r.delete();
                break;
        }

        return r.build();
    }

    private static <T extends HttpRequest> RequestBody getOkHttpRequestBody(T request) {
        if (request instanceof FormBodyRequest) {
            FormBodyRequest r = (FormBodyRequest) request;
            FormBody.Builder b = new FormBody.Builder();

            for (Map.Entry<String, String> e : r.body()) {
                b.add(e.getKey(), e.getValue());
            }

            return b.build();
        } else if (request instanceof StringBodyRequest) {
            StringBodyRequest r = (StringBodyRequest) request;

            return RequestBody.create(MediaType.parse(r.contentType()), r.body());
        }

        return null;
    }
}
