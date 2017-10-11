package info.snoha.matej.linkeddatamap.app.internal.layers;

import com.hp.hpl.jena.rdf.model.Resource;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.rdf.Jena;
import info.snoha.matej.linkeddatamap.rdf.Prefixes;
import info.snoha.matej.linkeddatamap.rdf.Shacl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MapLayerManager {

	public static MapLayer load(String definition) {
		try {
			return load(new ByteArrayInputStream(definition.getBytes("UTF-8")));
		} catch (Exception e) {
			Log.warn("Could not read definition string");
			return null;
		}
	}

	public static MapLayer load(InputStream definition) {

		try {

			Jena jena = new Jena().withModel(definition);

			Resource r = jena.resourceWithType(Prefixes.MS + "MapLayer");
			Resource service = jena.resourceWithType(Prefixes.SD + "Service");
			Resource graph = jena.resourceWithType(Prefixes.SD + "NamedGraph");
			Resource structure = jena.resourceWithType(Prefixes.MS + "MapLayerStructure");

			MapLayer layer = new MapLayer()
					.uri(jena.resourceUri(r))
					.title(jena.propertyValue(r, Prefixes.DCTERMS + "title"))
					.description(jena.propertyValue(r, Prefixes.DCTERMS + "description"))
					.sparqlEndpoint(jena.propertyValue(service, Prefixes.SD + "endpoint"))
					.sparqlNamedGraph(jena.propertyValue(graph, Prefixes.SD + "name"))
					.sparqlJenaSpatial(jena.propertyValue(service, Prefixes.MS + "jenaSpatial"))
					.addressPointType(jena.propertyValue(structure, Prefixes.MS + "addressPointType"))
					.addressPath(Shacl.parseToString(jena, structure, Prefixes.MS + "addressPath"))
					.latitudePath(Shacl.parseToString(jena, structure, Prefixes.MS + "latitudePath"))
					.longitudePath(Shacl.parseToString(jena, structure, Prefixes.MS + "longitudePath"))
					;

			return layer;

		} catch (Exception e) {
			Log.warn("Could not load map layer", e);
			return null;
		}
	}
}
