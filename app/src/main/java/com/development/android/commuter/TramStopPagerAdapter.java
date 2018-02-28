package com.development.android.commuter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages. This provides the data for the {@link ViewPager}.
 */
public class TramStopPagerAdapter extends FragmentPagerAdapter {
    // END_INCLUDE (fragment_pager_adapter)

    ArrayList<Map<String, String>> tramStops;

    Fragment[] fragmentArray;

    int stopCount;

    TramStopPagerAdapter(FragmentManager fm, ArrayList<Map<String, String>> _tramStops) {
        super(fm);
        tramStops = _tramStops;
        stopCount = tramStops.size();
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
