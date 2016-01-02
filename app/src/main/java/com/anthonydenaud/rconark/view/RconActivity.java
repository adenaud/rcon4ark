package com.anthonydenaud.rconark.view;

import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.inject.Inject;

import com.anthonydenaud.rconark.Codes;
import com.anthonydenaud.rconark.R;
import com.anthonydenaud.rconark.adapter.RconFragmentPagerAdapter;
import com.anthonydenaud.rconark.event.ConnectionListener;
import com.anthonydenaud.rconark.fargment.ChatFragment;
import com.anthonydenaud.rconark.fargment.CommandsFragment;
import com.anthonydenaud.rconark.fargment.PlayersFragment;
import com.anthonydenaud.rconark.model.Server;
import com.anthonydenaud.rconark.service.ArkService;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class RconActivity extends RoboActionBarActivity implements ConnectionListener {


    private RconFragmentPagerAdapter rconFragmentPagerAdapter;

    @Inject
    private PlayersFragment playersFragment;
    @Inject
    private CommandsFragment commandsFragment;
    @Inject
    private ChatFragment chatFragment;

    @Inject
    private ArkService arkService;


    @InjectView(R.id.container)
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rcon);

        Server server = getIntent().getParcelableExtra("server");
        setTitle(server.getName());


        arkService.addConnectionListener(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //chatFragment.setRetainInstance(true);

        rconFragmentPagerAdapter = new RconFragmentPagerAdapter(this,getSupportFragmentManager());
        rconFragmentPagerAdapter.addFragment(playersFragment);
        rconFragmentPagerAdapter.addFragment(commandsFragment);
        rconFragmentPagerAdapter.addFragment(chatFragment);


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

        if(id == android.R.id.home){
            setResult(RESULT_OK,getIntent());
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
                setResult(Codes.RESULT_CONNECTION_DROP,getIntent());
                finish();
            }
        });
    }

    @Override
    public void onConnectionFail(String message) {

    }
}
