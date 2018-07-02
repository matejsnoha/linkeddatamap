package info.snoha.matej.linkeddatamap.api;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class Layers extends Api {

    public static class LayerMetadata {

        public final String uri;
        public final String name;
        public final String publisher;

        public LayerMetadata(String uri, String name, String publisher) {
            this.uri = uri;
            this.name = name;
            this.publisher = publisher;
        }

        @Override
        public String toString() {
            return name + "\n" + publisher + "\n" + uri;
        }
    }

    public static class LayerListResponse extends ApiResponse {

        public List<LayerMetadata> layers;

        public LayerListResponse(String message, List<LayerMetadata> layers) {
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

    public static LayerListResponse getLayers() throws IOException {
        return httpGet(CLOUD_URL + "layers",
                null, null, LayerListResponse.class);
    }

    public static LayerResponse getLayer(String url) throws IOException {
        return httpGet(CLOUD_URL + "layers/" + URLEncoder.encode(url, "UTF-8"),
                null, null, LayerResponse.class);
    }
}
