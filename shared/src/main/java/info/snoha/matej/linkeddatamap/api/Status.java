package info.snoha.matej.linkeddatamap.api;

import java.io.IOException;

public class Status extends Api {

    public static class StatusResponse extends ApiResponse {

        public String time;

        public StatusResponse(String message, String time) {
            super(message);
            this.time = time;
        }
    }

    public static StatusResponse getStatus() throws IOException {
        return httpGet(CLOUD_URI + "status", null, null, StatusResponse.class);
    }
}
