package com.anthonydenaud.rconark.view;

import android.os.Bundle;

import com.anthonydenaud.rconark.fargment.SettingsFragment;

import roboguice.activity.RoboActionBarActivity;


public class SettingsActivity extends RoboActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

}
