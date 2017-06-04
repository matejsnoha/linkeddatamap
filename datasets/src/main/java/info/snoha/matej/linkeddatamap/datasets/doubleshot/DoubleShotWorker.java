package info.snoha.matej.linkeddatamap.datasets.doubleshot;

import com.google.devtools.common.options.OptionsParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import info.snoha.matej.linkeddatamap.Http;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collections;
import java.util.UUID;

public class DoubleShotWorker {

    private static final String PROCESSED_RESOURCE_BASE = "http://matej.snoha.info/dp/resource/";

    public static void main(String[] args) {

        OptionsParser parser = OptionsParser.newOptionsParser(DoubleShotOptions.class);
        parser.parseAndExitUponError(args);

        DoubleShotOptions options = parser.getOptions(DoubleShotOptions.class);
        if (options.input.isEmpty() || options.output.isEmpty()) {
            printUsage(parser);
            return;
        }

        process(options.input, options.output);
    }

    private static void printUsage(OptionsParser parser) {
        System.out.println("Usage: java -jar <jarname>.jar OPTIONS");
        System.out.println(parser.describeOptions(Collections.emptyMap(),
                OptionsParser.HelpVerbosity.LONG));
    }

    public static void process(String inputFileName, String outputFileName) {

        try {

            Log.info("Input: " + inputFileName);
            Log.info("Output: " + outputFileName);

            Reader input = Http.isUrl(inputFileName)
                    ? Http.httpGetReader(inputFileName)
                    : new InputStreamReader(Utils.getInputStream(inputFileName));

            PrintWriter output = new PrintWriter(Utils.getOutputStream(outputFileName));

            DoubleShotModel model = new Gson().fromJson(input,
                    new TypeToken<DoubleShotModel>() {}.getType());

            for (DoubleShotModel.DSVenue venue : model.getVenues()) {

                String base = PROCESSED_RESOURCE_BASE + UUID.randomUUID().toString();

                output.println(
                        "<" + base + "> " +
                        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
                        "<http://schema.org/CafeOrCoffeeShop> .");

                output.println(
                        "<" + base + "> " +
                        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
                        "<http://schema.org/Place> .");

                output.println(
                        "<" + base + "> " +
                        "<http://schema.org/name> " +
                        "\"" + venue.name + "\" .");

                if (venue.rating != null)
                output.println(
                        "<" + base + "> " +
                        "<http://schema.org/aggregateRating> " +
                        "\"" + venue.rating + "\" .");

                output.println(
                        "<" + base + "> " +
                        "<http://schema.org/geo> " +
                        "<" + base + "/geo" + "> .");

                output.println(
                        "<" + base + "/geo" + "> " +
                        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
                        "<http://schema.org/GeoCoordinates> .");

                output.println(
                        "<" + base + "/geo" + "> " +
                        "<http://schema.org/address> " +
                        "\"" + StringUtils.join(venue.location.formattedAddress, ", ") + "\" .");

                output.println(
                        "<" + base + "/geo" + "> " +
                        "<http://schema.org/latitude> " +
                        "\"" + venue.location.lat + "\" .");

                output.println(
                        "<" + base + "/geo" + "> " +
                        "<http://schema.org/longitude> " +
                        "\"" + venue.location.lng + "\" .");

            }
            output.close();

            Log.info("Processing finished, output written to " + Utils.getAbsolutePath(outputFileName));

        } catch (Exception e) {
            Log.error("Processing error", e);
        }
    }
}
