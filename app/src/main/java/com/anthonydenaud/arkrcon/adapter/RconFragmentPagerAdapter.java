package com.anthonydenaud.arkrcon.adapter;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.fragment.RconFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anthony on 13/10/2015.
 */
public class RconFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;
    private Context context;


    public RconFragmentPagerAdapter(Context context, FragmentManager fm) {
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

    public int indexOf(Fragment fragment){
        return fragments.indexOf(fragment);
    }

    @Override
    public CharSequence getPageTitle(int position) {

        String title = "";

        if (position == 0) {
            title = context.getString(R.string.fragment_players);
        }
        if (position == 1) {
            title = context.getString(R.string.fragment_commands);
        }
        if (position == 2) {
            title = context.getString(R.string.fragment_chat_log);
        }
        return title;
    }

    public void addFragment(RconFragment fragment) {
        this.fragments.add(fragment);
    }
}