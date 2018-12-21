package com.development.android.commuter;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
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

    String id;

    static String baseUrl = "https://api.vasttrafik.se/bin/rest.exe/v2/departureBoard?id=";

    String url;

    public TramStopFragment() {

        authorizationToken = AuthorizationToken.getAuthToken();

    }

    private void updateView() {
        StringRequest nextTramRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<Tram> tramList = new ArrayList<>();
                        JSONObject jsonResponse = new JSONObject();
                        try {
                            jsonResponse = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i("Response", response);
                        }
                        JSONArray jsonArray  = jsonResponse.optJSONObject("DepartureBoard").optJSONArray("Departure");
                        if (jsonArray != null) {
                            tramList = getTramArray(jsonArray);
                        }
                        else {
                            jsonResponse = jsonResponse.optJSONObject("DepartureBoard").optJSONObject("Departure");
                            if (jsonResponse != null) {
                                jsonArray = new JSONArray();
                                jsonArray.put(jsonResponse.toString());
                                tramList = getTramArray(jsonArray);
                            }
                        }
                        TramStopListAdapter nextTramAdapter = new TramStopListAdapter(nextTramList.getContext(), tramList);
                        nextTramList.setAdapter(nextTramAdapter);
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
                                    updateView();
                                }
                            });
                            break;
                        default:
                            Log.i("NetworkResponse", Integer.toString(error.networkResponse.statusCode));
                            break;
                    }
                }
                else {
                    if (authorizationToken.getToken() == null) {
                        authorizationToken.refreshToken(new UpdateChecker() {

                            @Override
                            public void onUpdate() {
                                updateView();
                            }
                        });
                    }
                    Log.i("NetworkResponse", error.toString());
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + authorizationToken.getToken());
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(nextTramRequest);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tram_stop_list, container, false);
        nextTramList = rootView.findViewById(R.id.tram_stop_list);
        TextView stopName = rootView.findViewById(R.id.tram_stop_name);
        TextView stopDist = rootView.findViewById(R.id.tram_stop_distance);

        String name = getArguments().getString("name");
        String dist = getArguments().getString("dist");
        id = getArguments().getString("id");

        stopName.setText(name);
        stopDist.setText(dist);

        Calendar time = Calendar.getInstance();

        String year = Integer.toString(time.get(Calendar.YEAR));
        String month = getZeroAddNowString(time.get(Calendar.MONTH) + 1 );
        String day = getZeroAddNowString(time.get(Calendar.DAY_OF_MONTH));
        String hour = getZeroAddNowString(time.get(Calendar.HOUR_OF_DAY));
        String min = getZeroAddNowString(time.get(Calendar.MINUTE));

        url = baseUrl + id + "&date=" + year + "-" + month + "-" + day + "&time=" + hour + ":" + min + "&timeSpan=120&format=json&needJourneyDetail=0&maxDeparturesPerLine=3";

        updateView();

        return rootView;
    }
    String getZeroAddNowString(int nowInt) {
        String now;
        if (nowInt < 10) {
            now = "0" + nowInt;
        } else {
            now = Integer.toString(nowInt);
        }
        return now;
    }
    ArrayList<Tram> getTramArray(JSONArray jsonTramList) {

        ArrayList<Tram> tramList = new ArrayList<>();
        ArrayList<String> checkList = new ArrayList<>();
        Calendar time = Calendar.getInstance();
        int nowTime = time.get(Calendar.MINUTE) + time.get(Calendar.HOUR_OF_DAY) * 60;

        for (int i = 0; i < jsonTramList.length(); i++)
        {
            try {
                JSONObject jsonTram = jsonTramList.getJSONObject(i);
                if (checkList.indexOf(jsonTram.get("sname") + "" + jsonTram.get("direction")) == -1) {
                    Tram tramData = new Tram(jsonTram.getString("sname"));

                    int departureTime = (Integer.parseInt(jsonTram.getString("time").substring(3)) + Integer.parseInt(jsonTram.getString("time").substring(0,2)) * 60);
                    int waitTime = departureTime - nowTime -1;

                    if (waitTime <= -100) {tramData.waitTime1 = Integer.toString(60 * 24  - nowTime + departureTime);}
                    else if (waitTime >= -1) {tramData.waitTime1 = Integer.toString(waitTime);}
                    else { continue;}
                    if ((waitTime) == 0) {tramData.waitTime1 = "Nu";}
                    tramData.direction = jsonTram.getString("direction");
                    tramData.textColor = jsonTram.getString("bgColor");
                    tramData.signColor = jsonTram.getString("fgColor");
                    tramData.waitTime2 = "-";
                    tramList.add(tramData);
                    checkList.add(jsonTram.get("sname") + "" + jsonTram.get("direction"));
                } else {
                    Tram tramData = tramList.get(checkList.indexOf(jsonTram.get("sname") + "" + jsonTram.get("direction")));

                    if (tramData.waitTime2.equals("-")) {
                        int departureTime = (Integer.parseInt(jsonTram.getString("time").substring(3)) + Integer.parseInt(jsonTram.getString("time").substring(0, 2)) * 60);
                        int waitTime = departureTime - nowTime - 1;

                        if (waitTime <= -100) {
                            tramData.waitTime2 = Integer.toString(60 * 24 - nowTime + departureTime);
                        } else if (waitTime >= -1) {
                            tramData.waitTime2 = Integer.toString(waitTime);
                        }
                        if ((waitTime) == 0) {
                            tramData.waitTime2 = "Nu";
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(tramList,  new Comparator<Tram>() {
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
        return tramList;
    }
}
