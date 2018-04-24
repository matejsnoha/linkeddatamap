package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.view.View;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.activities.MapsActivity;

public class LocationPermissionSettingsItem extends AbstractSettingsItem {

    public LocationPermissionSettingsItem(Context context) {
        super(context);
    }

    @Override
    public int getIcon() {
        return R.raw.gps;
    }

    @Override
    public int getTitleResource() {
        return R.string.location_permission;
    }

    @Override
    public String getSummary() {
        return MapsActivity.getInstance().hasLocationPermission() ? "OK" : "Click to grant permission";
    }

    @Override
    public void onClick(View view) {
        MapsActivity.getInstance().requestLocationPermission();
    }
}
