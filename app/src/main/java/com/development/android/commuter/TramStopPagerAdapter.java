package com.development.android.commuter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.Map;

/**
 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages. This provides the data for the {@link ViewPager}.
 */
public class TramStopPagerAdapter extends FragmentStatePagerAdapter {
    // END_INCLUDE (fragment_pager_adapter)

    private ArrayList<Map<String, String>> tramStops;

    private int stopCount;

    //private Fragment[] fragmentArray;

    TramStopPagerAdapter(FragmentManager fm, ArrayList<Map<String, String>> _tramStops) {
        super(fm);
        tramStops = _tramStops;
        stopCount = tramStops.size();

        //fragmentArray = new Fragment[stopCount];
    }

    // BEGIN_INCLUDE (fragment_pager_adapter_getitem)

    /**
     * Get fragment corresponding to a specific position. This will be used to populate the
     * contents of the {@link ViewPager}.
     *
     * @param position Position to fetch fragment for.
     * @return Fragment for specified position.
     */
    @Override
    public Fragment getItem(int position) {

        Fragment fragment = new TramStopFragment();
        Bundle args = new Bundle();
        args.putString("name", tramStops.get(position).get("name"));
        args.putString("dist", tramStops.get(position).get("dist"));
        args.putString("id", tramStops.get(position).get("id"));
        fragment.setArguments(args);
        return fragment;


        /*if (fragmentArray[position] == null) {
            Fragment fragment = new TramStopFragment();
            Bundle args = new Bundle();
            args.putString("name", tramStops.get(position).get("name"));
            args.putString("dist", tramStops.get(position).get("dist"));
            args.putString("id", tramStops.get(position).get("id"));
            fragment.setArguments(args);
            fragmentArray[position] = fragment;
        }
        return fragmentArray[position];*/
    }

    /**
     * Get number of pages the {@link ViewPager} should render.
     *
     * @return Number of fragments to be rendered as pages.
     */
    @Override
    public int getCount() {
        return stopCount;
    }
}
