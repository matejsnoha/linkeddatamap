package info.snoha.matej.linkeddatamap.rdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class NQuads extends NBase {

	public NQuads() {
	}

	public NQuads(Writer writer) {
		super(writer);
	}

	public NQuads(OutputStream outputStream) {
		super(outputStream);
	}

	public NQuads q(String g, String s, String p, String o) throws IOException {
		if (g != null && s != null && p != null && o != null) {
			writeln(g + " " + s + " " + p + " " + o + " .");
		}
		return this;
	}
}
