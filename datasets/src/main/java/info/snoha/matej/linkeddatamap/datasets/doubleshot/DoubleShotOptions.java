package info.snoha.matej.linkeddatamap.datasets.doubleshot;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;

public class DoubleShotOptions extends OptionsBase {

	private static final String DATA_SOURCE = "https://api.foursquare.com/v2/lists/4e9af4da8b81c0254e8a9322?" +
			"&client_id=MNBWH1VDKPIBVQ4QE2QXPPPUKIM0H3Y0JBV0S0ELBCMJR3WD" +
			"&client_secret=A5MAS0YK3BDNIGUN2JFNUW3QOFK1U11WQX1O0DMJOGKVSSGD" +
			"&v=20161214";

	@Option(
			name = "input",
			abbrev = 'i',
			help = "Input file (JSON) or URL",
			category = "data",
			defaultValue = DATA_SOURCE
	)
	public String input;

	@Option(
			name = "output",
			abbrev = 'o',
			help = "Output file (n-triples)",
			category = "data",
			defaultValue = ""
	)
	public String output;
}
