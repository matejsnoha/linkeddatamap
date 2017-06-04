package info.snoha.matej.linkeddatamap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Log {

	private static Logger logger;

	static {

		// configure logging to console
		ConsoleAppender consoleAppender = new ConsoleAppender(
				new EnhancedPatternLayout("%d %-5p [%t in %C{-4}.%M():%L] %m%n"));
		BasicConfigurator.configure(consoleAppender);

		logger = Logger.getLogger("info.snoha.matej.linkeddatamap");

		// only log once
		logger.setAdditivity(false);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);

		// set log level
		logger.setLevel(Level.ALL);

		// log
		logger.info("Logging started");
	}

	private static void log(Level level, Object message) {
		log(level, message, null);
	};

	private static void log(Level level, Object message, Throwable throwable) {
		logger.log(Log.class.getName(), level, message, throwable);
	};

	public static void debug(Object message) {
		log(Level.DEBUG, message);
	}

	public static void debug(Object message, Throwable throwable) {
		log(Level.DEBUG, message, throwable);
	}

	public static void info(Object message) {
		log(Level.INFO, message);
	}

	public static void info(Object message, Throwable throwable) {
		log(Level.INFO, message, throwable);
	}

	public static void warn(Object message) {
		log(Level.WARN, message);
	}

	public static void warn(Object message, Throwable throwable) {
		log(Level.WARN, message, throwable);
	}

	public static void error(Object message) {
		log(Level.ERROR, message);
	}

	public static void error(Object message, Throwable throwable) {
		log(Level.ERROR, message, throwable);
	}
}
