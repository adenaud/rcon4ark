package com.anthonydenaud.arkrcon.view;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.anthonydenaud.arkrcon.fargment.CustomCommandsFragment;
import com.anthonydenaud.arkrcon.service.LogService;
import com.google.inject.Inject;

import com.anthonydenaud.arkrcon.Codes;
import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.adapter.RconFragmentPagerAdapter;
import com.anthonydenaud.arkrcon.event.ConnectionListener;
import com.anthonydenaud.arkrcon.fargment.ChatLogFragment;
import com.anthonydenaud.arkrcon.fargment.CommandsFragment;
import com.anthonydenaud.arkrcon.fargment.PlayersFragment;
import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.service.ArkService;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class RconActivity extends RoboActionBarActivity implements ConnectionListener {


    private RconFragmentPagerAdapter rconFragmentPagerAdapter;

    @Inject
    private PlayersFragment playersFragment;
    @Inject
    private CommandsFragment commandsFragment;
    @Inject
    private ChatLogFragment chatLogFragment;


    @Inject
    private ArkService arkService;

    @Inject
    private LogService logService;


    @InjectView(R.id.container)
    private ViewPager mViewPager;
    private SharedPreferences preferences;
    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rcon);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (preferences.getBoolean("keep_screen_on", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        }


        server = getIntent().getParcelableExtra("server");
        setTitle(server.getName());


        arkService.addConnectionListener(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rconFragmentPagerAdapter = new RconFragmentPagerAdapter(this, getSupportFragmentManager());
        rconFragmentPagerAdapter.addFragment(playersFragment);
        rconFragmentPagerAdapter.addFragment(commandsFragment);
        rconFragmentPagerAdapter.addFragment(chatLogFragment);

        mViewPager.setOffscreenPageLimit(rconFragmentPagerAdapter.getCount());
        mViewPager.setAdapter(rconFragmentPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rcon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            setResult(RESULT_OK, getIntent());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnect(boolean reconnecting) {

    }

    @Override
    public void onDisconnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setResult(Codes.RESULT_CONNECTION_DROP, getIntent());
                finish();
            }
        });
    }

    @Override
    public void onConnectionFail(String message) {

    }

    @Override
    public void finish() {
        if(preferences.getBoolean("save_log",false)){
            if(!logService.write(this, server, chatLogFragment.getLog())){
                Snackbar.make(findViewById(android.R.id.content),R.string.error_log_write,Snackbar.LENGTH_SHORT).show();
            }
        }
        super.finish();
    }

}
