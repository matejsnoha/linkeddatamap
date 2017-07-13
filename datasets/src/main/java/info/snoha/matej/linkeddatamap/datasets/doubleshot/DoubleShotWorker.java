package info.snoha.matej.linkeddatamap.datasets.doubleshot;

import com.google.devtools.common.options.OptionsParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import info.snoha.matej.linkeddatamap.Http;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.Utils;
import info.snoha.matej.linkeddatamap.rdf.NTriples;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Collections;

import static info.snoha.matej.linkeddatamap.rdf.NBase.lit;
import static info.snoha.matej.linkeddatamap.rdf.NBase.uri;
import static info.snoha.matej.linkeddatamap.rdf.Uris.newResource;
import static info.snoha.matej.linkeddatamap.rdf.Uris.rdf;
import static info.snoha.matej.linkeddatamap.rdf.Uris.schema;

public class DoubleShotWorker {

    private static final String RESOURCE_BASE = "http://matej.snoha.info/dp/resource/";

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

            OutputStream output = Utils.getOutputStream(outputFileName);

            // TODO do not allocate all in memory?
            DoubleShotModel model = new Gson().fromJson(input,
                    new TypeToken<DoubleShotModel>() {}.getType());

            try (NTriples tripleOutput = new NTriples(output)) {

                for (DoubleShotModel.DSVenue venue : model.getVenues()) {

                    String res = newResource(RESOURCE_BASE);

                    tripleOutput
                            .t(uri(res), rdf("type"), schema("CafeOrCoffeeShop"))
                            .t(uri(res), rdf("type"), schema("Place"))
                            .t(uri(res), schema("name"), lit(venue.name))
                            .t(uri(res), schema("aggregateRating"), lit(venue.rating))
                            .t(uri(res), schema("geo"), uri(res + "/geo"))
                            .t(uri(res + "/geo"), rdf("type"), schema("GeoCoordinates"))
                            .t(uri(res + "/geo"), schema("latitude"), lit(venue.location.lat))
                            .t(uri(res + "/geo"), schema("longitude"), lit(venue.location.lng))
                            .t(uri(res + "/geo"), schema("address"), uri(res + "/geo/addr"))
                            .t(uri(res + "/geo/addr"), rdf("type"), schema("PostalAddress"))
                            .t(uri(res + "/geo/addr"), schema("streetAddress"), lit(venue.location.address))
                            .t(uri(res + "/geo/addr"), schema("addressLocality"), lit(venue.location.city))
                            .t(uri(res + "/geo/addr"), schema("postalCode"), lit(venue.location.postalCode))
                            .t(uri(res + "/geo/addr"), schema("addressRegion"), lit(venue.location.state))
                            .t(uri(res + "/geo/addr"), schema("addressCountry"), lit(venue.location.cc))
                            .t(uri(res + "/geo/addr"), schema("description"), lit(venue.location.formattedAddress, ", "));

                }
            }

            Log.info("Processing finished, output written to " + Utils.getAbsolutePath(outputFileName));

        } catch (Exception e) {
            Log.error("Processing error", e);
        }
    }
}
