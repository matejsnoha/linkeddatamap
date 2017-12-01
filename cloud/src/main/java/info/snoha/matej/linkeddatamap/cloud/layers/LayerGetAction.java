package info.snoha.matej.linkeddatamap.cloud.layers;

import com.danisola.restify.url.RestParser;
import com.danisola.restify.url.RestParserFactory;
import com.danisola.restify.url.RestUrl;
import com.danisola.restify.url.types.StrVar;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.api.ApiResponse;
import info.snoha.matej.linkeddatamap.api.Layers;
import info.snoha.matej.linkeddatamap.cloud.Action;
import info.snoha.matej.linkeddatamap.cloud.CloudUtils;
import info.snoha.matej.linkeddatamap.cloud.Context;

public class LayerGetAction implements Action {

    @Override
    public String execute(Context context) throws Exception {

        RestParser parser = RestParserFactory.parser("/api/1/layers/{}", StrVar.strVar("layer"));
        RestUrl url = parser.parse(context.getURI());
        String layer = url.isValid() ? url.variable("layer") : null;

        if (layer == null || layer.isEmpty()) {
            context.setContentType(Context.ContentType.JSON);
            context.writeJsonData(new ApiResponse(false, "No layer specified"));
            return null;
        }

        layer = CloudUtils.urlDecode(layer);

        Log.info("Sending layer " + layer);

        String layerData = CloudLayerManager.getLayer(context.getServletContext(), layer);

        ApiResponse response = new Layers.LayerResponse("OK", layerData);

        context.setContentType(Context.ContentType.JSON);
        context.writeJsonData(response);
        return null;
    }
}
