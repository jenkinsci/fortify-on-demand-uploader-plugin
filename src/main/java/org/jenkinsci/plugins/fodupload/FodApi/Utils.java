package org.jenkinsci.plugins.fodupload.FodApi;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ProxyConfiguration;
import okhttp3.*;
import okio.Buffer;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.Json;
import org.jenkinsci.plugins.fodupload.models.response.Dast.Error;
import org.jenkinsci.plugins.fodupload.models.response.Dast.FodDastApiResponse;
import org.jenkinsci.plugins.fodupload.models.response.Dast.PostDastStartScanResponse;
import org.jenkinsci.plugins.fodupload.models.response.Dast.PutDastScanSetupResponse;
import org.jenkinsci.plugins.fodupload.models.response.PatchDastFileUploadResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Utils {

    static String getRawBody(InputStream stream) throws IOException {
        if (stream == null) return null;

        String content = null;

        content = IOUtils.toString(stream, "utf-8");

        return content;
    }

    static OkHttpClient createOkHttpClient(int connectionTimeout, int writeTimeout, int readTimeout, ProxyConfiguration proxy) {
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

    @SuppressFBWarnings("Nm - NM_METHOD_NAMING_CONVENTION")
    static ResponseContent ResponseContentFromOkHttp3(Response response) throws IOException {
        ResponseContent resp = new ResponseContent(response.body().byteStream(), response.isSuccessful(), response.code(), response.message());

        resp.parseBody();
        return resp;
    }

    @SuppressFBWarnings("Nm - NM_METHOD_NAMING_CONVENTION")
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

    @SuppressFBWarnings("Nm - NM_METHOD_NAMING_CONVENTION")
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



    public static  <T> T convertHttpResponseIntoDastApiResponse(ResponseContent response, T fodApiResponse) throws IOException {
        if (response.code() < 300) {
            System.out.println("response code: " + response.code());
            return parseHttpSuccessResponse(response, fodApiResponse);

        } else {

            return parseFailureResponse(response, fodApiResponse);

        }
    }

    public static <T> T parseHttpSuccessResponse(ResponseContent response, Object fodApiResponse) throws IOException {
        if (response.bodyContent()==null || response.bodyContent().isEmpty()) {
            ((FodDastApiResponse) fodApiResponse).httpCode = response.code();
            ((FodDastApiResponse) fodApiResponse).isSuccess = response.isSuccessful();
            ((FodDastApiResponse) fodApiResponse).reason = response.message();
            return (T) fodApiResponse;
        } else {
            return parseHttpBodyResponse(response, fodApiResponse);
        }
    }

    public static  <T> T parseFailureResponse(ResponseContent response, Object fodApiResponse) throws IOException {
        if (response.bodyContent() == null || response.bodyContent().isEmpty()) {
            ((FodDastApiResponse) fodApiResponse).isSuccess = false;
            ((FodDastApiResponse) fodApiResponse).httpCode = response.code();
            ((FodDastApiResponse) fodApiResponse).reason = response.message();
            Error err = new Error();
            err.errorCode = response.code();
            err.message = response.message();
            ((FodDastApiResponse) fodApiResponse).errors = new ArrayList<>();
            ((FodDastApiResponse) fodApiResponse).errors.add(err);
            return (T) fodApiResponse;

        } else {
            T parsedResponse = parseHttpBodyResponse(response, fodApiResponse);
            Error err = new Error();
            err.errorCode =  ((FodDastApiResponse) parsedResponse).httpCode;
            err.message = ((FodDastApiResponse) parsedResponse).reason;
            ((FodDastApiResponse) parsedResponse).errors = new ArrayList<>();
            ((FodDastApiResponse) parsedResponse).errors.add(err);
            return  parsedResponse;
        }
    }
    private  static  <T> T parseHttpBodyResponse(ResponseContent response, Object fodApiResponse) throws IOException {

        if (fodApiResponse instanceof PatchDastFileUploadResponse) {
            T parsedResponse = parseResponse(response, new TypeToken<PatchDastFileUploadResponse>() {
            }.getType());
            ((PatchDastFileUploadResponse) parsedResponse).isSuccess = response.isSuccessful();
            ((PatchDastFileUploadResponse) parsedResponse).httpCode = response.code();
            ((PatchDastFileUploadResponse) parsedResponse).reason = response.bodyContent();
            return parsedResponse;

        } else if (fodApiResponse instanceof PutDastScanSetupResponse) {
            T parsedResponse = parseResponse(response, new TypeToken<PutDastScanSetupResponse>() {
            }.getType());
            ((PutDastScanSetupResponse) parsedResponse).isSuccess = response.isSuccessful();
            ((PutDastScanSetupResponse) parsedResponse).httpCode = response.code();
            ((PutDastScanSetupResponse) parsedResponse).reason = response.bodyContent();
            return parsedResponse;

        } else if (fodApiResponse instanceof PostDastStartScanResponse) {
            T parsedResponse = parseResponse(response, new TypeToken<PostDastStartScanResponse>() {
            }.getType());

            ((PostDastStartScanResponse) parsedResponse).isSuccess = response.isSuccessful();
            ((PostDastStartScanResponse) parsedResponse).httpCode = response.code();
            ((PostDastStartScanResponse) parsedResponse).reason = response.bodyContent();
            return  parsedResponse;

        } else {
            ((FodDastApiResponse) fodApiResponse).httpCode = response.code();
            ((FodDastApiResponse) fodApiResponse).isSuccess = response.isSuccessful();
            ((FodDastApiResponse) fodApiResponse).reason = response.bodyContent();
            return (T) fodApiResponse;
        }
    }

    private static  <T> T parseResponse(ResponseContent response, Type t) throws IOException {
        String content = response.bodyContent();

        if (content == null)
            throw new IOException("Unexpected body to be null");
        else {
            try {
                return Json.getInstance().fromJson(content, t);
            } catch (JsonSyntaxException ex) {
                String bodyContent = "{\"content\":" + content + "}";
                return Json.getInstance().fromJson(bodyContent, t);
            }
        }

    }
}
