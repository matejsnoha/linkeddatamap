package info.snoha.matej.linkeddatamap.app.internal.layers;

import com.hp.hpl.jena.rdf.model.Resource;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.rdf.Jena;
import info.snoha.matej.linkeddatamap.rdf.Prefixes;

import java.io.InputStream;

public class LayerManager extends AbstractLayerManager<Layer> {

	@Override
	public Layer load(String definition) {

		try {

			Jena jena = new Jena().withModel(definition);

			Resource r = jena.resourceWithType(Prefixes.LDM + "Layer");

			return new Layer()
					.enabled(true)
					.uri(jena.resourceUri(r))
					.title(jena.propertyValue(r, Prefixes.DCTERMS + "title"))
					.description(jena.propertyValue(r, Prefixes.DCTERMS + "description"))
					.color(jena.propertyValue(r, Prefixes.SCHEMA_ORG + "color"))
					.mapLayer(new MapLayerManager().load(definition))
					.dataLayer(new DataLayerManager().load(definition))
					;

		} catch (Exception e) {
			Log.warn("Could not load map layer", e);
			return null;
		}
	}
}
