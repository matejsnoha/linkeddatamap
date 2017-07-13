package info.snoha.matej.linkeddatamap;

import com.google.gson.Gson;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Log {

	private static Logger localLogger;

	private static final int HISTORY_SIZE = 1000; // lines

	private static final String PATTERN = "%d %-5p [%t in %C{1}.%M():%L] %m%n";
	private static final String PATTERN_LOGCAT = "[%t in %C{1}.%M():%L] %m%n"; // no date and level

	private static final Gson gson = new Gson();

	public interface PluggableLogger {

		void log(Level level, String message, Throwable e);
	}

	private static Set<PluggableLogger> pluggableLoggers = new LinkedHashSet<>();

	static {

		if (OS.isAndroid()) {

			// Android
			LogConfigurator logConfigurator = new LogConfigurator();
			logConfigurator.setRootLevel(Level.ALL);
			logConfigurator.setUseLogCatAppender(true);
			logConfigurator.setUseFileAppender(false);
			logConfigurator.setLogCatPattern(PATTERN_LOGCAT);
			logConfigurator.configure();

		} else {

			// pure Java
			ConsoleAppender consoleAppender = new ConsoleAppender(
					new EnhancedPatternLayout(PATTERN));
			BasicConfigurator.configure(consoleAppender);

		}

		localLogger = Logger.getLogger("info.snoha.matej.linkeddatamap");

		// keep a rolling buffer of messages with all priorities
		localLogger.addAppender(new RecordingAppender(
				new EnhancedPatternLayout(PATTERN), HISTORY_SIZE));

		// set log level
		localLogger.setLevel(Level.ALL);

		// log
		info("Logging started");

//		String version = "v" + GlobalContext.version;
//		info("Version: " + version);

	}

	public static void addPluggableLogger(PluggableLogger logger) {
		pluggableLoggers.add(logger);
	}

	public static void removePluggableLogger(PluggableLogger logger) {
		pluggableLoggers.remove(logger);
	}

	private static void log(Level level, String message) {
		log(level, message, null);
	}

	private static void log(Level level, Object object) {
		if (object instanceof Throwable) {
			log(level, "Exception", (Throwable) object);
		} else {
			log(level, gson.toJson(object), null);
		}
	}

	private static void log(Level level, String message, Throwable throwable) {

		if (localLogger != null) {
			localLogger.log(Log.class.getName(), level, message, throwable);
		} else {
			System.out.println(level + " " + message);
			if (throwable != null) {
				System.out.print(exceptionToString(throwable));
			}
		}

		if (pluggableLoggers != null) {
			for (PluggableLogger logger : pluggableLoggers) {
				logger.log(level, message, throwable);
			}
		}
	}

	public static void debug(String message) {
		log(Level.DEBUG, message);
	}

	public static void debug(Object object) {
		log(Level.DEBUG, object);
	}

	public static void debug(String message, Throwable throwable) {
		log(Level.DEBUG, message, throwable);
	}

	public static void info(String message) {
		log(Level.INFO, message);
	}

	public static void info(Object object) {
		log(Level.INFO, object);
	}

	public static void info(String message, Throwable throwable) {
		log(Level.INFO, message, throwable);
	}

	public static void warn(String message) {
		log(Level.WARN, message);
	}

	public static void warn(Object object) {
		log(Level.WARN, object);
	}

	public static void warn(String message, Throwable throwable) {
		log(Level.WARN, message, throwable);
	}

	public static void error(String message) {
		log(Level.ERROR, message);
	}

	public static void error(Object object) {
		log(Level.ERROR, object);
	}

	public static void error(String message, Throwable throwable) {
		log(Level.ERROR, message, throwable);
	}

	public static void wtf(String message) { // What a Terrible Failure
		log(Level.FATAL, message);
	}

	public static void wtf(Object object) {
		log(Level.FATAL, object);
	}

	public static void wtf(String message, Throwable throwable) {
		log(Level.FATAL, message, throwable);
	}

	public static String exceptionToString(Throwable ex) {
		if (ex == null) {
			return null;
		}
		StringBuilder res = new StringBuilder();
		res.append(ex.getClass().getSimpleName());
		if (ex.getMessage() != null) {
			res.append(": " + ex.getMessage());
		}
		if (ex.getStackTrace() != null) {
			for (StackTraceElement st : ex.getStackTrace()) {
				res.append("\nin " + st.getMethodName() + "()");
				res.append(" (" + st.getFileName() + ":" + st.getLineNumber() + ")");
			}
		}
		return res.toString();
	}

	private static class RecordingAppender extends AppenderSkeleton {

		private CircularFifoQueue<String> history;

		public RecordingAppender(Layout layout, int historySize) {
			this.layout = layout;
			this.history = new CircularFifoQueue<>(historySize);
		}

		@Override
		protected void append(LoggingEvent event) {
			// called for filtered events according to level
		}

		@Override
		public void close() {
		}

		@Override
		public synchronized void doAppend(LoggingEvent event) {
			super.doAppend(event);
			history.add(layout.format(event)); // called for all events
		}

		@Override
		public boolean requiresLayout() {
			return true;
		}

		public synchronized List<String> getHistory() {
			return new ArrayList<>(history);
		}
	}
}

