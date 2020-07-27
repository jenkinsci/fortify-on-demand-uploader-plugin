package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
// import org.jenkinsci.plugins.fodupload.models.response.GenericListResponse;
import org.jenkinsci.plugins.fodupload.models.response.ScanSummaryDTO;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;

public class StaticScanSummaryController extends ControllerBase {
    private PrintStream logger;


    public StaticScanSummaryController(FodApiConnection apiConnection, PrintStream logger) {
        super(apiConnection);
        this.logger = logger;
    }

    /**
     * @param releaseId releaseId is used in url query string
     * @param scanId    scanId is used in url query string
     * @return ScanSummaryDTO
     * @throws java.io.IOException in some circumstances
     */
    public ScanSummaryDTO getReleaseScanSummary(final int releaseId, final int scanId) throws IOException {

        if (apiConnection.getToken() == null)
            apiConnection.authenticate();

        HttpUrl.Builder builder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments(String.format("/api/v3/releases/%d/scans/%d", releaseId, scanId));
        logger.println("--------------------------");
        logger.println("Retrieving scan summary data");
        logger.println(String.format("ReleaseID: %s; ScanID: %s", releaseId, scanId));
        logger.println("--------------------------");

        String url = builder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiConnection.getToken())
                .addHeader("Accept", "application/json")
                .get()
                .build();
        Response response = apiConnection.getClient().newCall(request).execute();

        if (response.code() == HttpStatus.SC_FORBIDDEN) {
            // Re-authenticate
            apiConnection.authenticate();
            response = apiConnection.getClient().newCall(request).execute();
        }

        // Read the results and close the response
        String content = IOUtils.toString(response.body().byteStream(), "utf-8");
        response.body().close();

        Gson gson = new Gson();
        // Create a type of ScanSummaryDTO to play nice with gson.
        Type t = new TypeToken<ScanSummaryDTO>() {
        }.getType();

        ScanSummaryDTO results = gson.fromJson(content, t);

        if (results != null) {
            return results;
        } else {
            logger.println("Error retrieving scan summary data from API. Please log into online website to view summary information.");
            logger.println(String.format("API response code: %s", response.code()));
            logger.println(String.format("API response message: %s", response.message()));
            return null;
        }
    }

}
