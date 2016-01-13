package com.anthonydenaud.rconark.fargment;

import android.os.Bundle;

import com.anthonydenaud.rconark.R;

import roboguice.fragment.provided.RoboPreferenceFragment;

public class SettingsFragment extends RoboPreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }
}
