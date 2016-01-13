package com.anthonydenaud.rconark.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.anthonydenaud.rconark.R;
import com.anthonydenaud.rconark.fargment.RconFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anthony on 13/10/2015.
 */
public class RconFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;
    private Context context;


    public RconFragmentPagerAdapter(Context context,FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<>();
        this.context = context;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {

        String title = "";

        if(position == 0){
            title = context.getString(R.string.fragment_players);
        }
        if(position == 1){
            title = context.getString(R.string.fragment_commands);
        }
        if(position == 2){
            title = context.getString(R.string.fragment_custom_commands);
        }
        if(position == 3){
            title = context.getString(R.string.fragment_chat);
        }
        if(position == 4){
            title = context.getString(R.string.fragment_log);
        }
        return title;
    }

    public void addFragment(RconFragment fragment) {
        this.fragments.add(fragment);
    }


}
