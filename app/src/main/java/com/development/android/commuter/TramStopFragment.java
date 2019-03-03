package com.development.android.commuter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

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
    AuthorizationToken authorizationToken;

    ListView nextTramList;

    TramStopListAdapter nextTramAdapter;

    String id;

    public String name;

    static String baseUrl = "https://api.vasttrafik.se/bin/rest.exe/v2/departureBoard?id=";

    String url;

    SwipeRefreshLayout swipeRefreshLayout;

    ArrayList<Tram> tramList;

    public TramStopFragment() {

        authorizationToken = AuthorizationToken.getAuthToken();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("orientation", this.getResources().getConfiguration().orientation);
        outState.putSerializable("tram list", tramList);
        super.onSaveInstanceState(outState);
    }

    private void updateView(boolean fetch) {
        if (fetch) {
            tramList = new ArrayList<>();
            nextTramAdapter = new TramStopListAdapter(nextTramList.getContext(), tramList, name);
            nextTramList.setAdapter(nextTramAdapter);
            StringRequest nextTramRequest = new StringRequest(Request.Method.GET, url,
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
                                Log.i("NetworkResponse", Integer.toString(error.networkResponse.statusCode));
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
                        Log.i("NetworkResponse", error.toString());
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
            Log.i("position", "send request");
        }else{
            nextTramAdapter = new TramStopListAdapter(nextTramList.getContext(), tramList, name);
            nextTramList.setAdapter(nextTramAdapter);
            nextTramAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tram_stop_list, container, false);
        nextTramList = rootView.findViewById(R.id.tram_stop_list);
        TextView stopName = rootView.findViewById(R.id.tram_stop_name);
        TextView stopDist = rootView.findViewById(R.id.tram_stop_distance);

        // Initialize update by swipe.
        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        //Log.i("position", "onRefresh called from SwipeRefreshLayout");
                        //parent.requestLocationUpdate();
                        updateView(true);
                    }
                }
        );

        name = getArguments().getString("name");
        String dist = getArguments().getString("dist");
        id = getArguments().getString("id");

        stopName.setText(name);
        stopDist.setText(dist);

        Calendar time = Calendar.getInstance();

        String year = String.format(Locale.US, "%04d", time.get(Calendar.YEAR));
        String month = String.format(Locale.US, "%02d", time.get(Calendar.MONTH) + 1);
        String day = String.format(Locale.US, "%02d", time.get(Calendar.DAY_OF_MONTH));
        String hour = String.format(Locale.US, "%02d", time.get(Calendar.HOUR_OF_DAY));
        String min = String.format(Locale.US, "%02d", time.get(Calendar.MINUTE));

        // Checks if screen was just rotated
        int oldOrientation;
        if (savedInstanceState != null) {
            oldOrientation = savedInstanceState.getInt("orientation");
            tramList = (ArrayList<Tram>) savedInstanceState.getSerializable("tram list");
        } else {
            oldOrientation = -1;
        }

        if (oldOrientation != this.getResources().getConfiguration().orientation && oldOrientation != -1) {
            updateView(false);
        }
        else {
            url = baseUrl + id + "&date=" + year + "-" + month + "-" + day + "&time=" + hour + ":" + min + "&timeSpan=90&format=json&needJourneyDetail=1&maxDeparturesPerLine=3";
            updateView(true);
        }

        return rootView;
    }

    void fillTramArray(String response) {
        JSONObject jsonResponse = new JSONObject();
        try {
            jsonResponse = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("Json Departure Response", response);
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
        Calendar time = Calendar.getInstance();
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
    }
}
