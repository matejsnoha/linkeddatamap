package info.snoha.matej.linkeddatamap.app.internal.layers;

import com.hp.hpl.jena.rdf.model.Resource;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.rdf.Jena;
import info.snoha.matej.linkeddatamap.rdf.Prefixes;

public class LayerParser extends AbstractLayerParser<Layer> {

	@Override
	public Layer parse(String definition) {

		try {

			Jena jena = new Jena().withModel(definition);

			Resource r = jena.resourceWithType(Prefixes.LDM + "Layer");

			return new Layer()
					.enabled(true)
					.uri(jena.resourceUri(r))
					.title(jena.propertyValue(r, Prefixes.DCTERMS + "title"))
					.description(jena.propertyValue(r, Prefixes.DCTERMS + "description"))
					.color(jena.propertyValue(r, Prefixes.SCHEMA_ORG + "color"))
					.mapLayer(new MapLayerParser().parse(definition))
					.dataLayer(new DataLayerParser().parse(definition))
					;

		} catch (Exception e) {
			Log.warn("Could not parse map layer", e);
			return null;
		}
	}
}
