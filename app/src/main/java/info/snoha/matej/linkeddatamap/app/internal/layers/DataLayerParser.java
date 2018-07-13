package info.snoha.matej.linkeddatamap.app.internal.layers;

import com.hp.hpl.jena.rdf.model.Resource;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.rdf.Jena;
import info.snoha.matej.linkeddatamap.rdf.Prefixes;
import info.snoha.matej.linkeddatamap.rdf.Shacl;

public class DataLayerParser extends AbstractLayerParser<DataLayer> {

	@Override
	public DataLayer parse(String definition) {

		try {

			Jena jena = new Jena().withModel(definition);

			Resource r = jena.resourceWithType(Prefixes.LDM + "DataLayer");
			Resource service = jena.resourceWithType(Prefixes.SD + "Service");
			Resource graph = jena.resourceWithType(Prefixes.SD + "NamedGraph");
			Resource structure = jena.resourceWithType(Prefixes.LDM + "DataLayerStructure");

			return new DataLayer()
					.uri(jena.resourceUri(r))
					.title(jena.propertyValue(r, Prefixes.DCTERMS + "title"))
					.description(jena.propertyValue(r, Prefixes.DCTERMS + "description"))
					.sparqlEndpoint(jena.propertyValue(service, Prefixes.SD + "endpoint"))
					.sparqlNamedGraph(jena.propertyValue(graph, Prefixes.SD + "name"))
					.dataPointType(jena.propertyValue(structure, Prefixes.LDM + "dataPointType"))
					.dataPointName(jena.propertyValue(structure, Prefixes.LDM + "dataPointName"))
					.dataPointDescription(jena.propertyList(structure, Prefixes.LDM + "dataPointDescription"))
					.mapPointPath(Shacl.parseToString(jena, structure, Prefixes.LDM + "mapPointPath"))
					.mapLayer(jena.propertyValue(r, Prefixes.LDM + "mapLayer"))
					;

		} catch (Exception e) {
			Log.warn("Could not parse data layer", e);
			return null;
		}
	}
}
