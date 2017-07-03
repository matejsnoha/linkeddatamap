package info.snoha.matej.linkeddatamap.rdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class NTriples extends NBase {

	public NTriples() {
	}

	public NTriples(Writer writer) {
		super(writer);
	}

	public NTriples(OutputStream outputStream) {
		super(outputStream);
	}

	public NTriples t(String s, String p, String o) throws IOException {
		if (s != null && p != null && o != null) {
			writeln(s + " " + p + " " + o + " .");
		}
		return this;
	}
}
