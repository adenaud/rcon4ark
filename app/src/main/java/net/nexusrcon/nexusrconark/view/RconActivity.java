package net.nexusrcon.nexusrconark.view;

import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.inject.Inject;

import net.nexusrcon.nexusrconark.R;
import net.nexusrcon.nexusrconark.adapter.RconFragmentPagerAdapter;
import net.nexusrcon.nexusrconark.fargment.ChatFragment;
import net.nexusrcon.nexusrconark.fargment.CommandsFragment;
import net.nexusrcon.nexusrconark.fargment.PlayersFragment;
import net.nexusrcon.nexusrconark.model.Server;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class RconActivity extends RoboActionBarActivity {


    private RconFragmentPagerAdapter rconFragmentPagerAdapter;

    @Inject
    private PlayersFragment playersFragment;
    @Inject
    private CommandsFragment commandsFragment;
    @Inject
    private ChatFragment chatFragment;


    @InjectView(R.id.container)
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rcon);

        Server server = getIntent().getParcelableExtra("server");
        setTitle(server.getName());


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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
}
