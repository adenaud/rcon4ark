package com.anthonydenaud.arkrcon.view;

import android.os.Bundle;

import com.anthonydenaud.arkrcon.fargment.SettingsFragment;

import roboguice.activity.RoboActionBarActivity;


public class SettingsActivity extends RoboActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

}
