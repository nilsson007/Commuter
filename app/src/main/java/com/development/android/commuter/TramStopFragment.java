package com.development.android.commuter;

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

    UpdateChecker updateChecker;

    ListView nextTramList;

    String id;

    static String baseUrl = "https://api.vasttrafik.se/bin/rest.exe/v2/departureBoard?id=";

    String url;

    public TramStopFragment() {

        authorizationToken = AuthorizationToken.getAuthToken();

        updateChecker = new UpdateChecker(this) {

            @Override
            public void onUpdate() {
                TramStopFragment tramStopParent = (TramStopFragment) parent;
                tramStopParent.updateView();
            }
        };
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
                        //}) != null) {
                            jsonArray.put(jsonResponse.toString());
                            tramList = getTramArray(jsonArray);
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
                            authorizationToken.refreshToken(updateChecker);
                            break;
                        default:
                            Log.i("NetworkResponse", Integer.toString(error.networkResponse.statusCode));
                            break;
                    }
                }
                else {
                    if (authorizationToken.getToken() == null) {
                        authorizationToken.refreshToken(updateChecker);
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

        url = baseUrl + id + "&date=" + year + "-" + month + "-" + day + "&time=" + hour + ":" + min + "&timeSpan=59&format=json&needJourneyDetail=0&maxDeparturesPerLine=2";

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
                    tramData.direction = jsonTram.getString("direction");

                    int departureTime = (Integer.parseInt(jsonTram.getString("time").substring(3)) + Integer.parseInt(jsonTram.getString("time").substring(0,2)) * 60);
                    int waitTime = departureTime - nowTime;

                    tramData.waitTime1 = Integer.toString(waitTime >= - 100 ? waitTime - 1 : 60 * 24  - nowTime + departureTime - 1);
                    if ((waitTime -1) == 0) {tramData.waitTime1 = "Nu";}
                    tramData.signColor = jsonTram.getString("fgColor");
                    tramData.textColor = jsonTram.getString("bgColor");
                    tramData.waitTime2 = "-";
                    tramList.add(tramData);
                    checkList.add(jsonTram.get("sname") + "" + jsonTram.get("direction"));
                } else {
                    Tram tramData = tramList.get(checkList.indexOf(jsonTram.get("sname") + "" + jsonTram.get("direction")));

                    int departureTime = (Integer.parseInt(jsonTram.getString("time").substring(3)) + Integer.parseInt(jsonTram.getString("time").substring(0,2)) * 60);
                    int waitTime = departureTime - nowTime;

                    tramData.waitTime2 = Integer.toString(waitTime >= - 100 ? waitTime - 1 : 60 * 24  - nowTime + departureTime - 1);
                    if ((waitTime -1) == 0) {tramData.waitTime2 = "Nu";}
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tramList;
    }
}
