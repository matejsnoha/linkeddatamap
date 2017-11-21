package info.snoha.matej.linkeddatamap.app.gui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.AbstractSettingsScreen;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.GeneralSettingsScreen;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.SettingsScreenRegistry;

public class SettingsFragment extends Fragment {

    private AbstractSettingsScreen screen;

    public static SettingsFragment getInstance(String screenName) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle arguments = new Bundle();
        arguments.putString("screen", screenName);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // root view
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        // settings layout
        ViewGroup settingsLayout = rootView.findViewById(R.id.settings_layout);

        // settings screen
        String screenName = getArguments().getString("screen", GeneralSettingsScreen.class.getSimpleName());
        screen = SettingsScreenRegistry.get(screenName);
        if (screen == null) {
            Log.warn("Invalid settings screen name " + screenName);
            screen = new GeneralSettingsScreen(getContext());
        }

        // parent activity title
        getActivity().setTitle(screen.getTitle());

        // content
        if (screen.getParent() != null) {
            ((ViewGroup) screen.getParent()).removeView(screen);
        }
        settingsLayout.addView(screen);

        return rootView;
    }

    public void refresh(){
        if (screen != null){
            screen.refreshSummary();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}