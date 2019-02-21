package com.anthonydenaud.arkrcon.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.anthonydenaud.arkrcon.R;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }
}
