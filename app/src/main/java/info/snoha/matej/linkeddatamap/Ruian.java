package info.snoha.matej.linkeddatamap;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ruian {

    public static class SimplePlace {
        String name;
        String address;
        Double latitude;
        Double longitude;

        public SimplePlace(String name, String address, Double latitude, Double longitude) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
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
                        lineData[1] + " " + lineData[3] + "/" + lineData[2],
                        "Flats: " + lineData[4] + ", Floors: " + lineData[5]
                                + "\n\n<" + lineData[0] + ">",
                        Double.valueOf(lineData[6]),
                        Double.valueOf(lineData[7]))
                );
            }

            return places;

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
