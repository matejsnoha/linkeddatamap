package info.snoha.matej.linkeddatamap.rdf;

import com.google.gson.reflect.TypeToken;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import info.snoha.matej.linkeddatamap.Log;

import java.io.InputStream;

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
		Literal l = resource.getRequiredProperty(model.createProperty(property)).getObject().asLiteral();
		return (T) l.getValue();

//		// Java generics hell
//		Class<?> classOfT = new TypeToken<T>(){}.getRawType();
//		if (String.class.isAssignableFrom(classOfT)) {
//			return (T) l.getString();
//		} else {
//			Log.warn("Unknown return type " + classOfT);
//			return (T) l.getValue();
//		}
	}
}
