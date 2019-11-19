package com.development.android.commuter;

import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

/**
 * A {@link PagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages. This provides the data for the {@link ViewPager}.
 */
public class TramStopPagerAdapter extends PagerAdapter {
    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;
    private ArrayList<Fragment.SavedState> mSavedState = new ArrayList();
    private ArrayList<Fragment> mFragments = new ArrayList();
    private Fragment mCurrentPrimaryItem = null;
    private ArrayList<Map<String, String>> tramStops;
    private int stopCount;

    TramStopPagerAdapter(FragmentManager fm, ArrayList<Map<String, String>> _tramStops) {
        this.mFragmentManager = fm;
        tramStops = _tramStops;
        stopCount = tramStops.size();
    }

    /**
     * Get fragment corresponding to a specific position. This will be used to populate the
     * contents of the {@link ViewPager}.
     *
     * @param position Position to fetch fragment for.
     * @return Fragment for specified position.
     */
    Fragment getItem(int position) {

        Fragment fragment = new TramStopFragment();
        Bundle args = new Bundle();
        args.putString("name", tramStops.get(position).get("name"));
        args.putString("dist", tramStops.get(position).get("dist") + "m");
        args.putString("id", tramStops.get(position).get("id"));
        args.putInt("position", position);
        args.putSerializable("time", Calendar.getInstance());
        args.putBoolean("poopUp",false);
        fragment.setArguments(args);
        return fragment;
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * Get number of pages the {@link ViewPager} should render.
     *
     * @return Number of fragments to be rendered as pages.
     */
    public int getCount() {
        return stopCount;
    }

    /**
     *  The following code is copied from FragmentStatePagerAdapter with the Fragment ref replaced
     */

    public void startUpdate(@NonNull ViewGroup container) {
        if (container.getId() == -1) {
            throw new IllegalStateException("ViewPager with adapter " + this + " requires a view id");
        }
    }

    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment fragment;
        if (this.mFragments.size() > position) {
            fragment = this.mFragments.get(position);
            if (fragment != null) {
                return fragment;
            }
        }

        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction();
        }

        fragment = this.getItem(position);
        if (this.mSavedState.size() > position) {
            Fragment.SavedState fss = this.mSavedState.get(position);
            if (fss != null) {
                fragment.setInitialSavedState(fss);
            }
        }

        while(this.mFragments.size() <= position) {
            this.mFragments.add(null);
        }

        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);
        this.mFragments.set(position, fragment);
        this.mCurTransaction.add(container.getId(), fragment);
        return fragment;
    }

    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Fragment fragment = (Fragment)object;
        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction();
        }

        while(this.mSavedState.size() <= position) {
            this.mSavedState.add(null);
        }

        this.mSavedState.set(position, fragment.isAdded() ? this.mFragmentManager.saveFragmentInstanceState(fragment) : null);
        this.mFragments.set(position, null);
        this.mCurTransaction.remove(fragment);
    }

    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Fragment fragment = (Fragment)object;
        if (fragment != this.mCurrentPrimaryItem) {
            if (this.mCurrentPrimaryItem != null) {
                this.mCurrentPrimaryItem.setMenuVisibility(false);
                this.mCurrentPrimaryItem.setUserVisibleHint(false);
            }

            fragment.setMenuVisibility(true);
            fragment.setUserVisibleHint(true);
            this.mCurrentPrimaryItem = fragment;
        }

    }

    public void finishUpdate(@NonNull ViewGroup container) {
        if (this.mCurTransaction != null) {
            this.mCurTransaction.commitNowAllowingStateLoss();
            this.mCurTransaction = null;
        }

    }

    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return ((Fragment)object).getView() == view;
    }

    public Parcelable saveState() {
        Bundle state = null;
        if (this.mSavedState.size() > 0) {
            state = new Bundle();
            Fragment.SavedState[] fss = new Fragment.SavedState[this.mSavedState.size()];
            this.mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }

        for(int i = 0; i < this.mFragments.size(); ++i) {
            Fragment f = this.mFragments.get(i);
            if (f != null && f.isAdded()) {
                if (state == null) {
                    state = new Bundle();
                }

                String key = "f" + i;
                this.mFragmentManager.putFragment(state, key, f);
            }
        }

        return state;
    }

    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle)state;
            bundle.setClassLoader(loader);
            Parcelable[] fss = bundle.getParcelableArray("states");
            this.mSavedState.clear();
            this.mFragments.clear();
            if (fss != null) {
                for(int i = 0; i < fss.length; ++i) {
                    this.mSavedState.add((Fragment.SavedState)fss[i]);
                }
            }

            Iterable<String> keys = bundle.keySet();
            Iterator var6 = keys.iterator();

            while(true) {
                while(true) {
                    String key;
                    do {
                        if (!var6.hasNext()) {
                            return;
                        }

                        key = (String)var6.next();
                    } while(!key.startsWith("f"));

                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = this.mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        while(this.mFragments.size() <= index) {
                            this.mFragments.add(null);
                        }

                        f.setMenuVisibility(false);
                        this.mFragments.set(index, f);
                    } else {
                        Log.w("TramStopPagerAdapt", "Bad fragment at key " + key);
                    }
                }
            }
        }
    }
}
