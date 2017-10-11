package info.snoha.matej.linkeddatamap.rdf;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import info.snoha.matej.linkeddatamap.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * https://www.w3.org/TR/shacl/#property-paths
 */
public class Shacl {

	public static abstract class PropertyPath implements Iterable<PropertyPath> {

		@Override
		public Iterator<PropertyPath> iterator() {
			return IteratorUtils.emptyIterator();
		}
	}

	public static class PredicatePath extends PropertyPath {

		private final String predicate;

		public PredicatePath(String predicate) {
			this.predicate = predicate;
		}

		@Override
		public String toString() {
			return predicate;
		}
	}

	public static class SequencePath extends PropertyPath  {

		private final Collection<PropertyPath> children;

		public SequencePath(Collection<PropertyPath> children) {
			this.children = children;
		}

		@Override
		public Iterator<PropertyPath> iterator() {
			return children.iterator();
		}

		@Override
		public String toString() {
			return StringUtils.join(children, "/");
		}
	}

	public static class AlternativePath extends PropertyPath {

		private final Collection<PropertyPath> children;

		public AlternativePath(Collection<PropertyPath> children) {
			this.children = children;
		}

		@Override
		public Iterator<PropertyPath> iterator() {
			return children.iterator();
		}

		@Override
		public String toString() {
			return StringUtils.join(children, "|");
		}
	}

	public static class InversePath extends PropertyPath {

		private final PropertyPath invertedPath;

		public InversePath(PropertyPath invertedPath) {
			this.invertedPath = invertedPath;
		}

		@Override
		public Iterator<PropertyPath> iterator() {
			return IteratorUtils.singletonIterator(invertedPath);
		}

		@Override
		public String toString() {
			return "^" + invertedPath;
		}
	}

	public static class ZeroOrMorePath extends PropertyPath {
	}

	public static class OneOrMorePath extends PropertyPath {
	}

	public static class ZeroOrOnePath extends PropertyPath {
	}

	public static class ShaclParserVisitor implements RDFVisitor {

		private final Jena jena;

		public ShaclParserVisitor(Jena jena) {
			this.jena = jena;
		}

		public Object visitBlank(Resource r, AnonId id) {
			Log.debug("blank: " + id);

			if (Jena.isList(r)) {

				// list
				List<RDFNode> list = Jena.toList(r);
				return new SequencePath(CollectionUtils.collect(list, input ->
						(PropertyPath) input.visitWith(this)));

			} else {

				// inverse
				Resource invertedPath = r.getRequiredProperty(jena.getModel().createProperty(
						Prefixes.SH + "inversePath")).getObject().asResource();
				return new InversePath((PropertyPath) invertedPath.visitWith(this));
			}
		}

		public Object visitURI(Resource r, String uri) {
			Log.debug("uri: " + uri);
			return new PredicatePath(("<" + r.getURI() + ">"));
		}

		public Object visitLiteral(Literal l) {
			Log.debug("lit: " + l);
			return null; // not allowed?
		}
	}

	public static String parseToString(Jena jena, Resource resource, String property) {
		try {
			RDFNode node = resource.getRequiredProperty(jena.getModel().createProperty(property)).getObject();
			PropertyPath path = (PropertyPath) node.visitWith(new ShaclParserVisitor(jena));
			return path != null ? path.toString() : null;
		} catch (Exception e) {
			Log.debug("Could not parse SHACL path to string", e);
			return null;
		}
	}
}
