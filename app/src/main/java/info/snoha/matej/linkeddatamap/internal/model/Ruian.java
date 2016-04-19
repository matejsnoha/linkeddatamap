package info.snoha.matej.linkeddatamap.internal.model;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.snoha.matej.linkeddatamap.R;

public class Ruian {

    public static class SimplePlace {
        public String name;
        public String address;
        public Double latitude;
        public Double longitude;
        public String url;

        public SimplePlace(String url, String name, String address, Double latitude, Double longitude) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.url = url;
        }
    }

    public static List<SimplePlace> getPlaces(Context context) {
        return getPlaces(context, null);
    }

    public static List<SimplePlace> getPlaces(Context context, Integer limit) {

        try {

            List<SimplePlace> places = new ArrayList<>();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            context.getResources().openRawResource(R.raw.ruian_prague)));
            String line;
            while ((line = reader.readLine()) != null && (limit == null || places.size() < limit)) {
                String[] lineData = line.replace("\"", "").split(",");

                places.add(new SimplePlace(
                        lineData[0],
                        lineData[1] + " " + lineData[3] + "/" + lineData[2],
                        "Flats: " + lineData[4] + ", Floors: " + lineData[5]
                                + "\n\nAddress: <" + lineData[0] + ">",
                        Double.valueOf(lineData[6]),
                        Double.valueOf(lineData[7]))
                );
            }

            return places;

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static Map<String, String> getPlaceToObjectMapping(Context context) {
        return getPlaceToObjectMapping(context, null);
    }

    public static Map<String, String> getPlaceToObjectMapping(Context context, Integer limit) {

        try {

            Map<String, String> map = new HashMap<>();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            context.getResources().openRawResource(R.raw.ruian_mapping)));
            String line;
            while ((line = reader.readLine()) != null && (limit == null || map.size() < limit)) {
                String[] lineData = line.replace("\"", "").split(",");

                map.put(lineData[0], lineData[1]);
            }

            return map;

        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
