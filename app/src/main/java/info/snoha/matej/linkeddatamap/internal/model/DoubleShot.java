package info.snoha.matej.linkeddatamap.internal.model;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.internal.utils.Utils;

public class DoubleShot {

    public static class Shop {
        public Venue venue;
    }

    public static class Venue {
        public String name;
        public Location location;
        public Float rating;
    }

    public static class Location {
        public Double lat;
        public Double lng;
        public String[] formattedAddress;
    }

    public static void dump(Context context, String filename) {

        try {

            File file = Utils.getFile(filename);
            PrintWriter writer = new PrintWriter(file);

            String shopsString = IOUtils.toString(context.getResources().openRawResource(R.raw.shops));
            List<Shop> shops = new Gson().fromJson(shopsString,
                    new TypeToken<List<Shop>>() {}.getType());

            for (Shop shop : shops) {

                String base = "http://matej.snoha.info/dp/resource/" + UUID.randomUUID().toString();

                writer.println(
                        "<" + base + "> " +
                        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
                        "<http://schema.org/CafeOrCoffeeShop> .");

                writer.println(
                        "<" + base + "> " +
                        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
                        "<http://schema.org/Place> .");

                writer.println(
                        "<" + base + "> " +
                        "<http://schema.org/name> " +
                        "\"" + shop.venue.name + "\" .");

                if (shop.venue.rating != null)
                writer.println(
                        "<" + base + "> " +
                        "<http://schema.org/aggregateRating> " +
                        "\"" + shop.venue.rating + "\" .");

                writer.println(
                        "<" + base + "> " +
                        "<http://schema.org/geo> " +
                        "<" + base + "/geo" + "> .");

                writer.println(
                        "<" + base + "/geo" + "> " +
                        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
                        "<http://schema.org/GeoCoordinates> .");

                writer.println(
                        "<" + base + "/geo" + "> " +
                        "<http://schema.org/address> " +
                        "\"" + StringUtils.join(shop.venue.location.formattedAddress, ", ") + "\" .");

                writer.println(
                        "<" + base + "/geo" + "> " +
                        "<http://schema.org/latitude> " +
                        "\"" + shop.venue.location.lat + "\" .");

                writer.println(
                        "<" + base + "/geo" + "> " +
                        "<http://schema.org/longitude> " +
                        "\"" + shop.venue.location.lng + "\" .");

            }
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
