package info.snoha.matej.linkeddatamap.app.internal.layers;

import com.hp.hpl.jena.rdf.model.Resource;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.rdf.Jena;
import info.snoha.matej.linkeddatamap.rdf.Prefixes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DataLayerManager {

	public static DataLayer load(String definition) {
		try {
			return load(new ByteArrayInputStream(definition.getBytes("UTF-8")));
		} catch (Exception e) {
			Log.warn("Could not read definition string");
			return null;
		}
	}

	public static DataLayer load(InputStream definition) {

		try {

			Jena jena = new Jena().withModel(definition);

			Resource r = jena.resourceWithType(Prefixes.MS + "DataLayer");
			Resource service = jena.resourceWithType(Prefixes.SD + "Service");
			Resource graph = jena.resourceWithType(Prefixes.SD + "NamedGraph");
			Resource structure = jena.resourceWithType(Prefixes.MS + "DataLayerStructure");

			DataLayer layer = new DataLayer()
					.uri(jena.resourceUri(r))
					.title(jena.propertyValue(r, Prefixes.DCTERMS + "title"))
					.description(jena.propertyValue(r, Prefixes.DCTERMS + "description"))
					.sparqlEndpoint(jena.propertyValue(service, Prefixes.SD + "endpoint"))
					.sparqlNamedGraph(jena.propertyValue(graph, Prefixes.SD + "name"))
					.dataPointType(jena.propertyValue(structure, Prefixes.MS + "dataPointType"))
					.dataName(jena.propertyValue(structure, Prefixes.MS + "dataName"))
					.dataDescription(jena.propertyList(structure, Prefixes.MS + "dataDescription"))
					.mapPointPath(jena.propertyValue(structure, Prefixes.MS + "mapPointPath"))
					.mapLayer(jena.propertyValue(r, Prefixes.MS + "mapLayer"))
					;

			return layer;

		} catch (Exception e) {
			Log.warn("Could not load data layer", e);
			return null;
		}
	}
}
