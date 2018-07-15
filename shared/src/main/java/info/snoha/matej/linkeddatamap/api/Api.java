package info.snoha.matej.linkeddatamap.api;

import com.google.gson.Gson;
import info.snoha.matej.linkeddatamap.FrameworkConfiguration;
import info.snoha.matej.linkeddatamap.Http;
import info.snoha.matej.linkeddatamap.Log;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Locale;
import java.util.Map;

public class Api {

    protected static OkHttpClient httpClient = Http.getClient();
    protected static Gson gson = new Gson();

    protected static final String CLOUD_URI = FrameworkConfiguration.CLOUD_URI;

    public static Response httpRequest(String requestType, String url, Map<String, String> parameters,
                                       Map<String, String> headers) {

        Response result = null;

        // request
        Request request = null;
        Request.Builder requestBuilder = new Request.Builder()
                .url(url);

        // parameters
        FormBody.Builder requestBodyBuilder = new FormBody.Builder();
        if (parameters != null) {
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                requestBodyBuilder.add(parameter.getKey(), parameter.getValue());
            }
        }

        // headers
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }
        }

        // type-specific things
        switch (requestType.toUpperCase(Locale.US)) {

            case "HEAD":
                requestBuilder.head();
                break;

            case "GET":
                requestBuilder.get();
                break;

            case "POST":
                requestBuilder.post(requestBodyBuilder.build());
                break;

            case "PUT":
                requestBuilder.put(requestBodyBuilder.build());
                break;

            case "PATCH":
                requestBuilder.patch(requestBodyBuilder.build());
                break;

            case "DELETE":
                if (parameters != null) {
                    requestBuilder.delete(requestBodyBuilder.build());
                } else {
                    requestBuilder.delete();
                }
                break;

            default:
                Log.warn("Unsupported request type " + requestType);
                return result;
        }

        // build request
        request = requestBuilder.build();
        // Log.debug(request);

        try {

            // execute
            Call call = httpClient.newCall(request);
            Response response = call.execute();

            // response
            if (!response.isSuccessful()) {
                Log.warn(response);
            }
            result = response;

        } catch (Exception ex) {

            Log.warn("HTTP request (" + requestType + "/" + url
                    + ") failure: " + ex);
        }

        return result;
    }

    public static String getResponseString(Response response) {
        try {
            return response != null
                    && response.isSuccessful()
                    && response.body() != null
                    ? response.body().string() : null;
        } catch (Exception e) {
            Log.warn("Cannot get response string", e);
            return null;
        }
    }

    public String httpGet(String url,
                                 Map<String, String> parameters, Map<String, String> headers) {

        Response response = httpRequest("GET", url, parameters, headers);
        return getResponseString(response);
    }

    public static <T> T httpGet(String url, Map<String, String> parameters, Map<String, String> headers,
                         Class<T> responseClass) {

        Response response = httpRequest("GET", url, parameters, headers);
        return gson.fromJson(getResponseString(response), responseClass);
    }

    public static String httpPut(String url,
                                 Map<String, String> parameters, Map<String, String> headers) {

        Response response = httpRequest("PUT", url, parameters, headers);
        return getResponseString(response);
    }

    public static String httpPost(String url,
                                  Map<String, String> parameters, Map<String, String> headers) {

        Response response = httpRequest("POST", url, parameters, headers);
        return getResponseString(response);
    }

}
