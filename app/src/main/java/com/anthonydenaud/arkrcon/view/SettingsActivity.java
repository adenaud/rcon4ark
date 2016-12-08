package com.anthonydenaud.arkrcon.view;

import android.os.Bundle;
import android.view.MenuItem;

import com.anthonydenaud.arkrcon.fragment.SettingsFragment;

import roboguice.activity.RoboActionBarActivity;


public class SettingsActivity extends RoboActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
