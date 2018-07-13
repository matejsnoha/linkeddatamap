package info.snoha.matej.linkeddatamap.app.internal.layers;

import android.content.Context;
import info.snoha.matej.linkeddatamap.FrameworkConfiguration;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.model.BoundingBox;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;
import info.snoha.matej.linkeddatamap.rdf.Uris;

public class LayerQueryBuilder {

	public static String query(Context context, Layer layer, BoundingBox geoLimits) {
		try {

			DataLayer dataLayer = layer.getDataLayer();
			MapLayer mapLayer = layer.getMapLayer();

			String template = AndroidUtils.readRawResource(context,
					mapLayer.isSparqlJenaSpatial() ? R.raw.query_template_spatial : R.raw.query_template);

			// fill-in single items
			template = template.replace("{{from}}", dataLayer.getSparqlNamedGraph() != null ?
						"FROM <" + dataLayer.getSparqlNamedGraph() + ">" : "");
			template = template.replace("{{dataPointType}}", dataLayer.getDataPointType());
			template = template.replace("{{dataPointName}}", dataLayer.getdataPointName());
			template = template.replace("{{mapPointPath}}", dataLayer.getMapPointPath());
			template = template.replace("{{addressPath}}", mapLayer.getAddressPath());
			template = template.replace("{{latitudePath}}", mapLayer.getLatitudePath());
			template = template.replace("{{longitudePath}}", mapLayer.getLongitudePath());
			template = template.replace("{{spatialLimit}}", String.valueOf(FrameworkConfiguration.SPARQL_MAX_RESULTS));

			// fill-in description template
			if (dataLayer.getdataPointDescription() != null && !dataLayer.getdataPointDescription().isEmpty()) {
				int descriptionItemCount = 0;
				int descriptionUriItemCount = 0;
				String select = "?description";
				String where = "?dataPoint ";
				String bind = "BIND (CONCAT(";
				for (String descriptionItem : dataLayer.getdataPointDescription()) {
					descriptionItemCount++;
					if (Uris.isUri(descriptionItem)) {
						descriptionUriItemCount++;
						where += (descriptionUriItemCount > 1 ? " ; " : "")
								+ descriptionItem + " ?d" + descriptionUriItemCount;
						bind += (descriptionItemCount > 1 ? ", " : "")
								+ "STR(?d" + descriptionUriItemCount + ")";
					} else {
						bind += (descriptionItemCount > 1 ? ", " : "")
								+ "\"" + descriptionItem + "\"";
					}
				}
				where += " .";
				bind += ") AS ?description) .";
				String optional = "OPTIONAL {\n\t\t" + where + "\n\t\t" + bind + "\n\t}";

				template = template.replace("{{dataPointDescriptionSelect}}", select);
				template = template.replace("{{dataPointDescriptionOptional}}",
						descriptionUriItemCount != 0 ? optional : "");
			}

			// fill-in geo limits
			template = template.replace("{{minLat}}", String.valueOf(geoLimits.getMinLat()));
			template = template.replace("{{maxLat}}", String.valueOf(geoLimits.getMaxLat()));
			template = template.replace("{{minLong}}", String.valueOf(geoLimits.getMinLong()));
			template = template.replace("{{maxLong}}", String.valueOf(geoLimits.getMaxLong()));

			return template;

		} catch (Exception e) {
			Log.warn("Could not create query", e);
			return null;
		}
	}
}
