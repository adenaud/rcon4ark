package net.nexusrcon.nexusrconark.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import com.google.inject.Inject;

import net.nexusrcon.nexusrconark.R;
import net.nexusrcon.nexusrconark.fargment.RconFragment;

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
            title = context.getString(R.string.fragment_chat);
        }
        return title;
    }

    public void addFragment(RconFragment fragment) {
        this.fragments.add(fragment);

    }
}
