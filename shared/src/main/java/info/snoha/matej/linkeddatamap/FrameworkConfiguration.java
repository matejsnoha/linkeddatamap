package info.snoha.matej.linkeddatamap;

/**
 * This is the configuration file for the framework.<br>
 * To configure the Android application, see also:<br>
 *     build.gradle - package and version<br>
 *     strings.xml - app name and other texts<br>
 *     colors.xml - color theme
 */
public class FrameworkConfiguration {

	/**
	 * URI of Cloud API<br>
	 * used in: Setting -> Layers
	 */
	public static final String CLOUD_URI = "https://ldm.matej.snoha.info/api/1/";

	/**
	 * URI os SPARQL Query for Cloud API dataset<br>
	 * used in: cloud core
	 */
	public static final String CLOUD_STORE_QUERY = "https://ldm.matej.snoha.info/fuseki/cloud/query";

	/**
	 * Show add layers from cloud?<br>
	 * used in: Setting -> Layers
	 */
	public static final boolean ADD_LAYER_CLOUD_ENABLED = true;

	/**
	 * Show add layers from link?<br>
	 * used in: Setting -> Layers
	 */
	public static final boolean ADD_LAYER_LINK_ENABLED = true;

	/**
	 * Show add layers from text?<br>
	 * used in: Setting -> Layers
	 */
	public static final boolean ADD_LAYER_TEXT_ENABLED = true;

	/**
	 * Show feedback link?<br>
	 * used in: Setting -> Feedback
	 */
	public static final boolean FEEDBACK_ENABLED = true;

	/**
	 * URI to open in browser<br>
	 * used in: Setting -> Feedback
	 */
	public static final String FEEDBACK_URI = "https://web.usabilityscale.com/form" +
			"?ex=-LHinZ3B8WutHV_y1_Rj&pr=-LHinPl0Q_96J65U7oh4";

	/**
	 * Maximum number of SPARQL query results<br>
	 * used in: core
	 */
	public static final int SPARQL_MAX_RESULTS = 100_000;

}
