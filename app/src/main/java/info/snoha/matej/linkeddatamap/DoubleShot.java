package info.snoha.matej.linkeddatamap;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DoubleShot {

    public static class Shop {
        Venue venue;
    }

    public static class Venue {
        String name;
        Location location;
    }

    public static class Location {
        Double lat;
        Double lng;
        String[] formattedAddress;
    }

    public static class SimpleShop {
        String name;
        String address;
        Double latitude;
        Double longitude;

        public SimpleShop(String name, String address, Double latitude, Double longitude) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static List<SimpleShop> getShops(Context context) {

        try {

            String shopsString = IOUtils.toString(context.getResources().openRawResource(R.raw.shops));
            List<Shop> shops = new Gson().fromJson(shopsString, new TypeToken<List<Shop>>() {
                    }.getType());

            List<SimpleShop> simpleShops = new ArrayList<>();
            for (Shop shop : shops) {

                simpleShops.add(new SimpleShop(
                        shop.venue.name,
                        StringUtils.join(shop.venue.location.formattedAddress, ", "),
                        shop.venue.location.lat,
                        shop.venue.location.lng
                        ));
            }
            return simpleShops;

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
