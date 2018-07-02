package info.snoha.matej.linkeddatamap.cloud.layers;

import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.api.ApiResponse;
import info.snoha.matej.linkeddatamap.api.Layers;
import info.snoha.matej.linkeddatamap.cloud.Action;
import info.snoha.matej.linkeddatamap.cloud.Context;

import java.util.List;

public class LayerListAction implements Action {

    @Override
    public String execute(Context context) throws Exception {

        Log.info("Sending layer list");

        List<Layers.LayerMetadata> layers = CloudLayerManager.getLayerUris(context.getServletContext(),
                CloudLayerManager.LayerType.LAYER);

        ApiResponse response = new Layers.LayerListResponse("OK", layers);

        context.setContentType(Context.ContentType.JSON);
        context.writeJsonData(response);
        return null;
    }
}
