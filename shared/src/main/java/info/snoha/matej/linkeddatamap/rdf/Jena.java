package info.snoha.matej.linkeddatamap.rdf;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import info.snoha.matej.linkeddatamap.Log;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Jena {

	public static final boolean DEBUG = false;

	private Model model;

	public Jena withModel(String definitionOrUri) {
		model = ModelFactory.createDefaultModel();
		try {
			if (UrlValidator.getInstance().isValid((definitionOrUri))) {
				Log.debug("Jena loading using HTTP content negotiation: " + definitionOrUri);
				new RDFReaderFImpl().getReader("Turtle").read(model, definitionOrUri);
			} else {
				model.read(new ByteArrayInputStream(definitionOrUri.getBytes("UTF-8")), null, "TURTLE");
			}
		} catch (Exception e) {
			Log.warn("Jena could not load model", e);
		}
		return this;
	}

	public Jena withModel(InputStream modelStream) {
		model = ModelFactory.createDefaultModel();
		model.read(modelStream, null, "TURTLE");
		return this;
	}

	public Jena withModel(Model model) {
		this.model = model;
		return this;
	}

	public Model getModel() {
		return model;
	}

	public Resource resourceWithType(String type) {
		try {
			return model.listResourcesWithProperty(
					RDF.type, model.createResource(type)).nextResource();
		} catch (Exception e) {
			if (DEBUG) {
				Log.debug("Could not find resource of type " + type, e);
			}
			return null;
		}
	}

	public String resourceUri(Resource resource) {
		try {
			return "<" + resource.getURI() + ">";
		} catch (Exception e) {
			if (DEBUG) {
				Log.debug("Could not read resource uri for " + resource, e);
			}
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
			if (DEBUG) {
				Log.debug("Could not read property " + property, e);
			}
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
			for (RDFNode node : toList(resource.getRequiredProperty(
					model.createProperty(property)).getObject().asResource())) {

				if (node.isLiteral()) {
					list.add((T) node.asLiteral().getValue());
				} else if (node.isURIResource()) {
					list.add((T) ("<" + node.asResource().getURI() + ">"));
				} else {
					Log.debug("Unsupported RDF node type for " + node.toString());
				}
			}
		} catch (Exception e) {
			if (DEBUG) {
				Log.debug("Could not read property list of " + property, e);
			}
		}
		return list;
	}

	public static List<RDFNode> toList(Resource resource) {
		try {
			List<RDFNode> list = new ArrayList<>();
			ExtendedIterator<RDFNode> it = resource.as(RDFList.class).iterator();
			while (it.hasNext()) {
				list.add(it.next());
			}
			return list;
		} catch (Exception e) {
			if (DEBUG) {
				Log.debug("Could not convert to list " + resource, e);
			}
			return null;
		}
	}

	public static boolean isList(Resource resource) {
		return toList(resource) != null; // TODO ?
	}
}
