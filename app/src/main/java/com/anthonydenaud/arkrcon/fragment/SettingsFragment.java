package com.anthonydenaud.arkrcon.fragment;

import android.os.Bundle;

import com.anthonydenaud.arkrcon.R;

import roboguice.fragment.provided.RoboPreferenceFragment;

public class SettingsFragment extends RoboPreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }
}
