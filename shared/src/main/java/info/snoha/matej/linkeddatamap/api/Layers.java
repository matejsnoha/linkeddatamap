package info.snoha.matej.linkeddatamap.api;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class Layers extends Api {

    public static class LayerListResponse extends ApiResponse {

        public List<String> layers;

        public LayerListResponse(String message, List<String> layers) {
            super(message);
            this.layers = layers;
        }
    }

    public static class LayerResponse extends ApiResponse {

        public String layer;

        public LayerResponse(String message, String layer) {
            super(message);
            this.layer = layer;
        }
    }

    public static LayerListResponse getMapLayers() throws IOException {
        return httpGet(CLOUD_URL + "layers/?type=map",
                null, null, LayerListResponse.class);
    }

    public static LayerListResponse getDataLayers() throws IOException {
        return httpGet(CLOUD_URL + "layers/?type=data",
                null, null, LayerListResponse.class);
    }

    public static LayerResponse getLayer(String url) throws IOException {
        return httpGet(CLOUD_URL + "layers/" + URLEncoder.encode(url, "UTF-8"),
                null, null, LayerResponse.class);
    }
}
