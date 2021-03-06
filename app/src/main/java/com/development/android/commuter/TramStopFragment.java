package com.development.android.commuter;


import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Comparator;

/**
 * A dummy fragment representing a section of the app, but that simply displays dummy text.
 * This would be replaced with your application's content.
 */
public class TramStopFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private AuthorizationToken authorizationToken;

    private AbsListView nextTramList;

    private TramStopListAdapter nextTramAdapter;

    private String id;

    public String name;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<Tram> tramList;

    private View rootView;

    private Calendar time;

    private TramStopFragment childFragment;

    private int position;

    private double popUpClickX;

    private double popUpClickY;

    final private Fragment thisFragment;

    private boolean inScrolling = false;

    public TramStopFragment() {

        authorizationToken = AuthorizationToken.getAuthToken();
        thisFragment = this;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("orientation", this.getResources().getConfiguration().orientation);
        outState.putSerializable("tram list", tramList);
        outState.putDouble("popUpClickX", popUpClickX);
        outState.putDouble("popUpClickY", popUpClickY);
        if(childFragment != null){
            getChildFragmentManager().putFragment(outState, "child" + position, childFragment);
        }
        super.onSaveInstanceState(outState);
    }

    private void updateView(boolean fetch) {
        if (fetch) {
            tramList = new ArrayList<>();
            nextTramAdapter = new TramStopListAdapter(nextTramList.getContext(), tramList, name, this);
            nextTramList.setAdapter(nextTramAdapter);
            StringRequest nextTramRequest = new StringRequest(Request.Method.GET, makeUrl(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            fillTramArray(response);
                            nextTramAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                authorizationToken.refreshToken(new UpdateChecker() {

                                    @Override
                                    public void onUpdate() {
                                        updateView(true);
                                    }
                                });
                                break;
                            default:
                                break;
                        }
                    } else {
                        if (authorizationToken.getToken() == null) {
                            authorizationToken.refreshToken(new UpdateChecker() {

                                @Override
                                public void onUpdate() {
                                    updateView(true);
                                }
                            });
                        }
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    return authorizationToken.getTokenParams();
                }
            };
            RequestQueue queue = Volley.newRequestQueue(getContext());
            queue.add(nextTramRequest);
        }else{
            nextTramAdapter = new TramStopListAdapter(nextTramList.getContext(), tramList, name, this);
            nextTramList.setAdapter(nextTramAdapter);
            nextTramAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tram_stop_list, container, false);
        nextTramList = rootView.findViewById(R.id.tram_stop_list);
        TextView stopName = rootView.findViewById(R.id.tram_stop_name);
        TextView stopDist = rootView.findViewById(R.id.tram_stop_distance);
        TextView stopTime = rootView.findViewById(R.id.tram_stop_time);

        nextTramList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View updateButton = ((MainActivity)getContext()).mUpdateButton;
                int motion = event.getActionMasked();
                if (motion == MotionEvent.ACTION_MOVE && !inScrolling) {
                    inScrolling = true;
                    AlphaAnimation anim = new AlphaAnimation(1.0f,0.3f);
                    anim.setDuration(200);
                    anim.setFillAfter(true);
                    anim.setFillEnabled(true);
                    updateButton.startAnimation(anim);
                }
                if (motion == MotionEvent.ACTION_UP || motion == MotionEvent.ACTION_CANCEL) {
                    inScrolling = false;
                    AlphaAnimation anim = new AlphaAnimation(0.3f,1f);
                    anim.setDuration(200);
                    anim.setFillAfter(true);
                    anim.setFillEnabled(true);
                    updateButton.startAnimation(anim);
                }
                return false;
            }
        });

        name = getArguments().getString("name");
        String dist = getArguments().getString("dist");
        id = getArguments().getString("id");
        position = getArguments().getInt("position");

        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);

        time = (Calendar) getArguments().getSerializable("time");

        if(!getArguments().getBoolean("popUp")) {
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            time = Calendar.getInstance();
                            updateView(true);
                        }
                    }
            );
        }
        else
        {
            swipeRefreshLayout.setEnabled(false);
            String hour = String.format(Locale.US, "%02d", time.get(Calendar.HOUR_OF_DAY));
            String min = String.format(Locale.US, "%02d", time.get(Calendar.MINUTE));
            stopTime.setText(hour + ":" + min);

            final ImageButton close = rootView.findViewById(R.id.tram_stop_close);
            close.setVisibility(View.VISIBLE);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((TramStopFragment)thisFragment.getParentFragment()).closePopUpFragment();
                    close.setEnabled(false);
                }
            });
        }

        stopName.setText(name);
        stopDist.setText(dist);


        // Checks if screen was just rotated
        int oldOrientation;
        Fragment fragment = null;
        if (savedInstanceState != null) {
            oldOrientation = savedInstanceState.getInt("orientation");
            tramList = (ArrayList<Tram>) savedInstanceState.getSerializable("tram list");
            fragment = getChildFragmentManager().getFragment(savedInstanceState, "child" + position);
        } else {
            oldOrientation = -1;
        }

        if (oldOrientation != this.getResources().getConfiguration().orientation && oldOrientation != -1) {
            updateView(false);
            popUpClickX = savedInstanceState.getDouble("popUpClickX");
            popUpClickY = savedInstanceState.getDouble("popUpClickY");
            if (fragment != null){
                setChildFragment((TramStopFragment) fragment,popUpClickX,popUpClickY);
            }
        }
        else {
            updateView(true);
        }

        return rootView;
    }

    void fillTramArray(String response) {
        JSONObject jsonResponse = new JSONObject();
        try {
            jsonResponse = new JSONObject(response);
        } catch (JSONException e) {
            Tram tram = new Tram("----");
            tramList.add(tram);
            e.printStackTrace();
        }
        JSONArray jsonArray = jsonResponse.optJSONObject("DepartureBoard").optJSONArray("Departure");
        if (jsonArray != null) {
            makeTramArray(jsonArray);
        } else {
            jsonResponse = jsonResponse.optJSONObject("DepartureBoard").optJSONObject("Departure");
            if (jsonResponse != null) {
                jsonArray = new JSONArray();
                jsonArray.put(jsonResponse.toString());
                makeTramArray(jsonArray);
            }
        }
    }

    void makeTramArray(JSONArray jsonTramList) {
        ArrayList<String> checkList = new ArrayList<>();
        int nowTime = time.get(Calendar.MINUTE) + time.get(Calendar.HOUR_OF_DAY) * 60 + time.get(Calendar.SECOND) / 30;

        for (int i = 0; i < jsonTramList.length(); i++) {
            try {
                JSONObject jsonTram = jsonTramList.getJSONObject(i);
                int index = checkList.indexOf(jsonTram.get("sname") + "" + jsonTram.get("direction"));
                if (index == -1) {
                    Tram tramData = new Tram(jsonTram.getString("sname"));
                    tramData.waitTime1 = TimeDiffString.getTramWaitTime(jsonTram,nowTime);
                    if (tramData.waitTime1 != null) {
                        tramData.nowTime = TimeDiffString.getTramNowTimeInt(jsonTram);
                        tramData.waitTime1 = tramData.waitTime1.equals("0") ? "Nu" : tramData.waitTime1;
                        tramData.direction = jsonTram.getString("direction");
                        tramData.textColor = jsonTram.getString("bgColor");
                        tramData.signColor = jsonTram.getString("fgColor");
                        tramData.waitTime2 = "-";
                        tramData.journeyUrl = jsonTram.getJSONObject("JourneyDetailRef").getString("ref");
                        tramList.add(tramData);
                        checkList.add(jsonTram.get("sname") + "" + jsonTram.get("direction"));
                    }
                } else {
                    Tram tramData = tramList.get(index);

                    if (tramData.waitTime2.equals("-")) {
                        tramData.waitTime2 = TimeDiffString.getTramWaitTime(jsonTram,nowTime);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        tramList.sort(new Comparator<Tram>() {
            @Override
            public int compare(Tram item, Tram t1) {
                String s1 = item.number;
                String s2 = t1.number;
                try {
                    return Integer.parseInt(s1) - Integer.parseInt(s2);
                } catch (NumberFormatException e) {
                    return s1.compareToIgnoreCase(s2);
                }
            }

        });
        if (tramList.size() == 0){
            Tram tram = new Tram("?");
            tramList.add(tram);
        }
    }
    String makeUrl(){
        String year = String.format(Locale.US, "%04d", time.get(Calendar.YEAR));
        String month = String.format(Locale.US, "%02d", time.get(Calendar.MONTH) + 1);
        String day = String.format(Locale.US, "%02d", time.get(Calendar.DAY_OF_MONTH));
        String hour = String.format(Locale.US, "%02d", time.get(Calendar.HOUR_OF_DAY));
        String min = String.format(Locale.US, "%02d", time.get(Calendar.MINUTE));

        String baseUrl = "https://api.vasttrafik.se/bin/rest.exe/v2/departureBoard?id=";
        return baseUrl + id + "&date=" + year + "-" + month + "-" + day + "&time=" + hour + ":" + min + "&timeSpan=90&format=json&needJourneyDetail=1&maxDeparturesPerLine=3";

    }
    void setChildFragment(TramStopFragment fragment, double x, double y){
        childFragment = fragment;
        popUpClickX = x;
        popUpClickY = y;
        MyFrameLayout frame = new MyFrameLayout(getContext(),x,y){
            @Override
            public void onAnimationEnd(){
                super.onAnimationEnd();
                if (this.closing) {
                    FragmentManager fm = thisFragment.getChildFragmentManager();
                    fm.beginTransaction().remove(childFragment).commit();
                    childFragment = null;
                    this.closing = false;
                }
            }
        };
        RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layout.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        layout.setMargins(dpToPx(10),dpToPx(10),dpToPx(10),dpToPx(10));
        frame.setLayoutParams(layout);
        frame.setBackgroundColor(getContext().getColor(R.color.colorPrimaryDark));
        frame.setElevation(dpToPx(10));
        ((RelativeLayout)rootView).addView(frame);
        FragmentTransaction transaction = thisFragment.getChildFragmentManager().beginTransaction();
        if (fragment.getId() == 0) {
            frame.setId(View.generateViewId());
            transaction.add(frame.getId(), fragment);
        }else {
            frame.setId(fragment.getId());
            transaction.replace(frame.getId(), fragment);
        }
        transaction.commit();
    }
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    void closePopUpFragment(){
        try {
            ((MyFrameLayout) childFragment.getView().getParent()).closeViewAnimation();
        } catch(Exception e) {}
    }
}
