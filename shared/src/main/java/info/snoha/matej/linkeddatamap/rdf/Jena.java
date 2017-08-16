package info.snoha.matej.linkeddatamap.rdf;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import info.snoha.matej.linkeddatamap.Log;

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
		try {
			return model.listResourcesWithProperty(
					RDF.type, model.createResource(type)).nextResource();
		} catch (Exception e) {
			Log.debug("Could not find resource of type " + type, e);
			return null;
		}
	}

	public String resourceUri(Resource resource) {
		try {
			return "<" + resource.getURI() + ">";
		} catch (Exception e) {
			Log.debug("Could not read resource uri for " + resource, e);
			return null;
		}
	}

	public <T> T propertyValue(Resource resource, String property) {
		try {
			RDFNode node = resource.getRequiredProperty(model.createProperty(property)).getObject();
			if (node.isLiteral()) {
				return (T) node.asLiteral().getValue();
			} else if (node.isURIResource()) {
				return (T) ("<" + node.asResource().getURI() + ">");
			} else {
				throw new IllegalArgumentException("Unsupported RDF node type");
			}
		} catch (Exception e) {
			Log.debug("Could not read property " + property, e);
			return null;
		}
	}

	public <T> List<T> propertyList(Resource resource, String property) {
		List<T> list = new ArrayList<>();

		// if anyone wonders why am I writing these helper functions, this is a 1-liner to read an RDF List using JENA:
		// for (ExtendedIterator<RDFNode> it = resource.getRequiredProperty(model.createProperty(property))
		// 		.getObject().asResource().as(RDFList.class).iterator();
		// 		it.hasNext(); list.add((T) it.next().asLiteral().getValue())) ;

		try {
			Resource listResource = resource.getRequiredProperty(model.createProperty(property)).getObject().asResource();
			ExtendedIterator<RDFNode> it = listResource.as(RDFList.class).iterator();
			while (it.hasNext()) {
				RDFNode node = it.next();
				if (node.isLiteral()) {
					list.add((T) node.asLiteral().getValue());
				} else if (node.isURIResource()) {
					list.add((T) ("<" + node.asResource().getURI() + ">"));
				} else {
					Log.debug("Unsupported RDF node type for " + node.toString());
				}
			}
		} catch (Exception e) {
			Log.debug("Could not read property list of " + property, e);
		}
		return list;
	}
}
