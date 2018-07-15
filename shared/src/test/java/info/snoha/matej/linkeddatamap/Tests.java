package info.snoha.matej.linkeddatamap;

import com.hp.hpl.jena.rdf.model.Resource;
import info.snoha.matej.linkeddatamap.rdf.Jena;
import info.snoha.matej.linkeddatamap.rdf.NTriples;
import info.snoha.matej.linkeddatamap.rdf.Prefixes;
import info.snoha.matej.linkeddatamap.sparql.CsvSparqlClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static info.snoha.matej.linkeddatamap.rdf.NBase.lit;
import static info.snoha.matej.linkeddatamap.rdf.NBase.uri;
import static info.snoha.matej.linkeddatamap.rdf.Uris.newResource;
import static info.snoha.matej.linkeddatamap.rdf.Uris.rdf;
import static info.snoha.matej.linkeddatamap.rdf.Uris.schema;
import static org.junit.jupiter.api.Assertions.*;

public class Tests {

	/**
	 * Tests RDF writing, reading and lookup
	 */
	@Test
	public void rdfTest() throws Exception {

		// create serialized RDF
		String res = newResource(Prefixes.LDMRES);
		NTriples nt = new NTriples()
				.t(uri(res), rdf("type"), schema("CafeOrCoffeeShop"))
				.t(uri(res), rdf("type"), schema("Place"))
				.t(uri(res), schema("name"), lit("Test cafe"))
				.t(uri(res), schema("aggregateRating"), lit("9"))
				.t(uri(res), schema("geo"), uri(res + "/geo"))
				.t(uri(res + "/geo"), rdf("type"), schema("GeoCoordinates"))
				.t(uri(res + "/geo"), schema("latitude"), lit("50.1"))
				.t(uri(res + "/geo"), schema("longitude"), lit("14.2"));

		assertTrue(nt.toString().contains("Test cafe"));

		// load it into Jena
		Jena jena = new Jena().withModel(nt.toString());

		// find cafe
		Resource r = jena.resourceWithType(Prefixes.SCHEMA_ORG + "CafeOrCoffeeShop");
		assertNotNull(r);

		// find and check rating
		String rating = jena.propertyValue(r, Prefixes.SCHEMA_ORG + "aggregateRating");
		assertEquals(rating, "9");

	}

	/**
	 * Tests network and SPARQL queries
	 */
	@Test
	public void sparqlTest() throws Exception {

		String query =
				"PREFIX ldm:  <https://purl.org/ldm/>\n" +
				"PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
				"PREFIX s: <http://schema.org/>\n" +
				"SELECT ?layer ?name ?publisher\n" +
				"FROM <http://layers>\n" +
				"WHERE {\n" +
				"?layer a ldm:Layer ;\n" +
				"dcterms:title ?name ;\n" +
				"dcterms:publisher [ s:name ?publisher ] .\n" +
				"}";

		CsvSparqlClient.execute(FrameworkConfiguration.CLOUD_STORE_QUERY, query, new CsvSparqlClient.CSVCallback() {

			@Override
			public void onSuccess(List<String> columns, List<List<String>> results) {

				// check if there is at least one layer
				assertNotNull(results);
				assertTrue(results.size() >= 1);

				// check if all variables were returned
				assertEquals(3, results.get(0).size());
			}

			@Override
			public void onFailure(String reason) {
				fail(reason);
			}
		});
	}
}
