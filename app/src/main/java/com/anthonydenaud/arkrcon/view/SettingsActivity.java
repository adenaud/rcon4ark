package com.anthonydenaud.arkrcon.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.fragment.SettingsFragment;

import roboguice.activity.RoboActionBarActivity;


public class SettingsActivity extends ThemeActivity{

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

    @Override
    protected void applyTheme(String theme) {
        if(THEME_DARK.equals(theme)){
            setTheme(R.style.AppDarkTheme);
            getApplication().setTheme(R.style.AppDarkTheme);
        }
        if(THEME_ARK.equals(theme)){
            setTheme(R.style.AppArkTheme);
            getApplication().setTheme(R.style.AppArkTheme);
        }
    }
}
