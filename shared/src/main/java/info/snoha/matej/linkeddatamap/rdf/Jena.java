package info.snoha.matej.linkeddatamap.rdf;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Jena {

	private Model model;

	public Jena withModel(InputStream modelStream) {
		model = ModelFactory.createDefaultModel();
		model.read(modelStream, null, "TURTLE");
		return this;
	}

	public Jena withModel(Model model) {
		this.model = model;
		return this;
	}

	public Resource resourceWithType(String type) {
		return model.listResourcesWithProperty(
				RDF.type, model.createResource(type)).nextResource();
	}

	public <T> T propertyValue(Resource resource, String property) {
		return (T) resource.getRequiredProperty(model.createProperty(property)).getObject().asLiteral().getValue();
	}

	public <T> List<T> propertyList(Resource resource, String property) {
		List<T> list = new ArrayList<>();

		// if anyone wonders why am I writing these helper functions, this is a 1-liner to read an RDF List using JENA:
		// for (ExtendedIterator<RDFNode> it = resource.getRequiredProperty(model.createProperty(property))
		// 		.getObject().asResource().as(RDFList.class).iterator();
		// 		it.hasNext(); list.add((T) it.next().asLiteral().getValue())) ;

		Resource listResource = resource.getRequiredProperty(model.createProperty(property)).getObject().asResource();
		ExtendedIterator<RDFNode> it = listResource.as(RDFList.class).iterator();
		while (it.hasNext()) {
			list.add((T) it.next().asLiteral().getValue());
		}
		return list;
	}
}
