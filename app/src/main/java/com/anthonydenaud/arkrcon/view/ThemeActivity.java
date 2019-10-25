package com.anthonydenaud.arkrcon.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.anthonydenaud.arkrcon.R;


/**
 * Created by Anthony on 15/07/2017.
 */

public abstract class ThemeActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    protected static final String THEME_LIGHT = "light";
    protected static final String THEME_DARK = "dark";
    protected static final String THEME_ARK = "ark";

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        //setTheme(false);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        //setToolBarTheme();
        super.onPostCreate(savedInstanceState);
    }

    private void setTheme(boolean recreate){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = preferences.getString("theme",null);
        applyTheme(theme);
        if(recreate){
            recreate();
        }
    }

    protected void setToolBarTheme(){
        if(toolbar != null){
            toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Dark_ActionBar);
        }
    }

    protected void applyTheme(String theme){

        if(THEME_DARK.equals(theme)){
            getApplication().setTheme(R.style.AppDarkTheme);
            setTheme(R.style.AppDarkTheme_NoActionBar);
        }
        if(THEME_ARK.equals(theme)){
            getApplication().setTheme(R.style.AppArkTheme);
            setTheme(R.style.AppArkTheme_NoActionBar);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if("theme".equals(s)){
            setTheme(true);
        }
    }
}
