package info.snoha.matej.linkeddatamap.cloud.layers;

import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.sparql.CsvSparqlClient;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CloudLayerManager {

    private static final String SPARQL_QUERY_URL = "https://ldm.matej.snoha.info/fuseki/cloud/query";
    private static final String SPARQL_GRAPH = "http://layers";

    private static final String SARQL_QUERY_LIST_LAYERS = "/WEB-INF/query_list_layers.sparql";
    private static final String SARQL_QUERY_GET_LAYER = "/WEB-INF/query_get_layer.sparql";

    public enum LayerType {
        LAYER("Layer"),
        MAP_LAYER("MapLayer"),
        DATA_LAYER("DataLayer");

        private String typeName;

        LayerType(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }
    }

    public static List<String> getLayerUris(ServletContext context, LayerType type) throws IOException {

        String query = IOUtils.toString(context.getResourceAsStream(SARQL_QUERY_LIST_LAYERS), StandardCharsets.UTF_8);
        query = query.replace("{{graph}}", SPARQL_GRAPH);
        query = query.replace("{{layerType}}", type.getTypeName());

        List<String> layers = new ArrayList<>();

        CsvSparqlClient.execute(SPARQL_QUERY_URL, query, new CsvSparqlClient.CSVCallback() {

            @Override
            public void onSuccess(List<String> columns, List<List<String>> results) {
                for (List<String> row : results) {
                    layers.add(row.get(0));
                }
            }

            @Override
            public void onFailure(String reason) {
                Log.warn(reason);
            }
        });
        return layers;
    }

    public static String getLayer(ServletContext context, String layerUri) throws IOException {

        String query = IOUtils.toString(context.getResourceAsStream(SARQL_QUERY_GET_LAYER), StandardCharsets.UTF_8);
        query = query.replace("{{graph}}", SPARQL_GRAPH);
        query = query.replace("{{layer}}", layerUri);

        List<String> layers = new ArrayList<>();

        CsvSparqlClient.execute(SPARQL_QUERY_URL, query, new CsvSparqlClient.RawCallback() {

            @Override
            public void onSuccess(String result) {
                layers.add(result);
            }

            @Override
            public void onFailure(String reason) {
                Log.warn(reason);
            }
        });
        return layers.get(0);
    }
}
