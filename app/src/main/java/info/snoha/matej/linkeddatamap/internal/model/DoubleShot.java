package info.snoha.matej.linkeddatamap.internal.model;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.snoha.matej.linkeddatamap.R;

public class DoubleShot {

    public static class Shop {
        public Venue venue;
    }

    public static class Venue {
        public String name;
        public Location location;
    }

    public static class Location {
        public Double lat;
        public Double lng;
        public String[] formattedAddress;
    }

    public static class SimplePlace {
        public String name;
        public String address;
        public Double latitude;
        public Double longitude;

        public SimplePlace(String name, String address, Double latitude, Double longitude) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static List<SimplePlace> getPlaces(Context context) {

        try {

            String shopsString = IOUtils.toString(context.getResources().openRawResource(R.raw.shops));
            List<Shop> shops = new Gson().fromJson(shopsString,
                    new TypeToken<List<Shop>>() {}.getType());

            List<SimplePlace> simplePlaces = new ArrayList<>();
            for (Shop shop : shops) {

                simplePlaces.add(new SimplePlace(
                        shop.venue.name,
                        StringUtils.join(shop.venue.location.formattedAddress, ", "),
                        shop.venue.location.lat,
                        shop.venue.location.lng
                        ));
            }
            return simplePlaces;

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
