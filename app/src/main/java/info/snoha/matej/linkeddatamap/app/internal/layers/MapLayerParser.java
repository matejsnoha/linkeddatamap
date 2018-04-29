package info.snoha.matej.linkeddatamap.app.internal.layers;

import com.hp.hpl.jena.rdf.model.Resource;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.rdf.Jena;
import info.snoha.matej.linkeddatamap.rdf.Prefixes;
import info.snoha.matej.linkeddatamap.rdf.Shacl;

public class MapLayerParser extends AbstractLayerParser<MapLayer> {

	@Override
	public MapLayer parse(String definition) {

		try {

			Jena jena = new Jena().withModel(definition);

			Resource r = jena.resourceWithType(Prefixes.LDM + "MapLayer");
			Resource service = jena.resourceWithType(Prefixes.SD + "Service");
			Resource graph = jena.resourceWithType(Prefixes.SD + "NamedGraph");
			Resource structure = jena.resourceWithType(Prefixes.LDM + "MapLayerStructure");

			return new MapLayer()
					.uri(jena.resourceUri(r))
					.title(jena.propertyValue(r, Prefixes.DCTERMS + "title"))
					.description(jena.propertyValue(r, Prefixes.DCTERMS + "description"))
					.sparqlEndpoint(jena.propertyValue(service, Prefixes.SD + "endpoint"))
					.sparqlNamedGraph(jena.propertyValue(graph, Prefixes.SD + "name"))
					.sparqlJenaSpatial(jena.propertyValue(service, Prefixes.LDM + "jenaSpatial"))
					.addressPointType(jena.propertyValue(structure, Prefixes.LDM + "addressPointType"))
					.addressPath(Shacl.parseToString(jena, structure, Prefixes.LDM + "addressPath"))
					.latitudePath(Shacl.parseToString(jena, structure, Prefixes.LDM + "latitudePath"))
					.longitudePath(Shacl.parseToString(jena, structure, Prefixes.LDM + "longitudePath"))
					;

		} catch (Exception e) {
			Log.warn("Could not parse map layer", e);
			return null;
		}
	}
}
