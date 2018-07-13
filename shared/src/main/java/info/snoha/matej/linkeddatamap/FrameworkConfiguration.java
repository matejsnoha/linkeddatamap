package info.snoha.matej.linkeddatamap;

public class FrameworkConfiguration {

	/**
	 * Show feedback link?<br>
	 * used in: Setting -> Feedback
	 */
	public static boolean FEEDBACK_ENABLED = true;

	/**
	 * URI to open in browser<br>
	 * used in: Setting -> Feedback
	 */
	public static final String FEEDBACK_URI =
			"https://web.usabilityscale.com/form?ex=-LHinZ3B8WutHV_y1_Rj&pr=-LHinPl0Q_96J65U7oh4";

	/**
	 * Maximum number of SPARQL query results
	 */
	public static int SPARQL_MAX_RESULTS = 100_000;

}
