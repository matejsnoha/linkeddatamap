package info.snoha.matej.linkeddatamap.app.internal.layers;

import android.content.Context;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;

public class LayerQueryBuilder {

	public static String query(Context context, DataLayer dataLayer, MapLayer mapLayer) {
		try {

			String template = AndroidUtils.readRawResource(context, R.raw.query_template);
			template = template.replace("{{graph}}", dataLayer.getSparqlNamedGraph());
			template = template.replace("{{dataPointType}}", dataLayer.getDataPointType());
			template = template.replace("{{dataName}}", dataLayer.getDataName());
			template = template.replace("{{mapPointPath}}", dataLayer.getMapPointPath());
			template = template.replace("{{addressPath}}", mapLayer.getAddressPath());
			template = template.replace("{{latitudePath}}", mapLayer.getLatitudePath());
			template = template.replace("{{longitudePath}}", mapLayer.getLongitudePath());
			return template;

		} catch (Exception e) {
			Log.warn("Could not create query", e);
			return null;
		}
	}
}
