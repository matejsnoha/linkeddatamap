package info.snoha.matej.linkeddatamap.rdf;

import info.snoha.matej.linkeddatamap.Log;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

public abstract class NBase implements AutoCloseable {

	protected StringBuilder builder;

	protected Writer writer;

	public NBase() {
		this.builder = new StringBuilder();
	}

	public NBase(Writer writer) {
		this.writer = writer;
	}

	public NBase(OutputStream outputStream) {
		this.writer = new PrintWriter(outputStream);
	}

	protected void write(String str) throws IOException {
		if (builder != null) {
			builder.append(str);
		} else {
			writer.write(str);
		}
	}

	protected void writeln(String str) throws IOException {
		write(str + "\n");
	}

	public void close() {
		if (writer != null) {
			try {
				writer.flush();
				writer.close();
			} catch (Exception e) {
				Log.warn("Could not close writer", e);
			}
		}
	}

	@Override
	public String toString() {
		return builder != null ? builder.toString() : writer.toString();
	}

	public static String lit(String what) {
		return what != null ? '"' + what + '"' : null;
	}

	public static String lit(String[] what, String sep) {
		return what != null ? lit(StringUtils.join(what, sep)) : null;
	}

	public static String lit(Number what) {
		return what != null ? lit(String.valueOf(what)) : null;
	}

	public static String uri(String what) {
		return what != null ? '<' + what + '>' : null;
	}
}
