package com.development.android.commuter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import java.util.Locale;
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

    public String name;

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
                        ArrayList<Tram> tramList = getTramArray(response);
                        final TramStopListAdapter nextTramAdapter = new TramStopListAdapter(nextTramList.getContext(), tramList);
                        nextTramList.setAdapter(nextTramAdapter);
                        nextTramList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ListView lv = view.findViewById(R.id.nextStopList);

                                final Tram tram = ((Tram)parent.getItemAtPosition(position));
                                String journeyUrl = tram.journeyUrl;
                                StringRequest journeyRequest = new StringRequest(Request.Method.GET, journeyUrl,
                                        getJourneyRequestResponse(lv), new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.i("Poop", error.toString());

                                    }}) {
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        return authorizationToken.getTokenParams();
                                    }
                                };
                                RequestQueue queue = Volley.newRequestQueue(getContext());
                                queue.add(journeyRequest);
                                View listItem = (View)lv.getChildAt(0);

                                lv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1000)); //listItem.getMeasuredHeight()*3));
                                lv.setNestedScrollingEnabled(true);

                                ((SwipeRefreshLayout)nextTramList.getParent()).setEnabled(false);
                                Log.i("position","onItemCick");
                            }
                        });
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
                return authorizationToken.getTokenParams();
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

        // Initialize update by swipe.
        SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        final MainActivity parent = (MainActivity) this.getContext();
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        //Log.i("position", "onRefresh called from SwipeRefreshLayout");
                        parent.requestLocationUpdate();
                    }
                }
        );

        name = getArguments().getString("name");
        String dist = getArguments().getString("dist");
        id = getArguments().getString("id");

        stopName.setText(name);
        stopDist.setText(dist);

        Calendar time = Calendar.getInstance();

        String year  = String.format(Locale.US,"%04d", time.get(Calendar.YEAR));
        String month = String.format(Locale.US,"%02d", time.get(Calendar.MONTH) + 1 );
        String day   = String.format(Locale.US,"%02d", time.get(Calendar.DAY_OF_MONTH));
        String hour  = String.format(Locale.US,"%02d", time.get(Calendar.HOUR_OF_DAY));
        String min   = String.format(Locale.US,"%02d", time.get(Calendar.MINUTE));

        url = baseUrl + id + "&date=" + year + "-" + month + "-" + day + "&time=" + hour + ":" + min + "&timeSpan=120&format=json&needJourneyDetail=1&maxDeparturesPerLine=3";

        updateView();

        return rootView;
    }

    private Response.Listener<String> getJourneyRequestResponse(ListView _listView){
        final ListView lv = _listView;
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonResponse = new JSONObject();
                try {
                    jsonResponse = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("Json Departure Response", response);
                }
                JSONArray jsonArray = jsonResponse.optJSONObject("JourneyDetail").optJSONArray("Stop");
                ArrayList<String> values = new ArrayList<>();
                boolean startSaving = false;
                while (jsonArray.length() > 0) {
                    String tmpName = jsonArray.optJSONObject(0).optString("name");
                    tmpName = tmpName.substring(0, tmpName.indexOf(','));
                    if (startSaving) values.add(tmpName);
                    if (tmpName.equals(name)){
                        startSaving = true;
                    }
                    jsonArray.remove(0);
                }
                ArrayAdapter<String> la = new ArrayAdapter<>(lv.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, values);
                lv.setAdapter(la);
            }
        };
    }

    ArrayList<Tram> getTramArray(String response) {
        ArrayList<Tram> tramList = new ArrayList<>();
        JSONObject jsonResponse = new JSONObject();
        try {
            jsonResponse = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("Json Departure Response", response);
        }
        JSONArray jsonArray = jsonResponse.optJSONObject("DepartureBoard").optJSONArray("Departure");
        if (jsonArray != null) {
            tramList = makeTramArray(jsonArray);
        } else {
            jsonResponse = jsonResponse.optJSONObject("DepartureBoard").optJSONObject("Departure");
            if (jsonResponse != null) {
                jsonArray = new JSONArray();
                jsonArray.put(jsonResponse.toString());
                tramList = makeTramArray(jsonArray);
            }
        }
        return tramList;
    }

    ArrayList<Tram> makeTramArray(JSONArray jsonTramList) {

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

                    int departureTime;
                    if (jsonTram.isNull("rtTime")){
                        departureTime = (Integer.parseInt(jsonTram.getString("time").substring(3)) + Integer.parseInt(jsonTram.getString("time").substring(0,2)) * 60);
                    } else {
                        departureTime = (Integer.parseInt(jsonTram.getString("rtTime").substring(3)) + Integer.parseInt(jsonTram.getString("rtTime").substring(0, 2)) * 60);
                    }
                    int waitTime = departureTime - nowTime;

                    if (waitTime <= -100) {tramData.waitTime1 = Integer.toString(60 * 24  - nowTime + departureTime);}
                    else if (waitTime >= -1) {tramData.waitTime1 = Integer.toString(waitTime);}
                    else { continue;}
                    if ((waitTime) == 0) {tramData.waitTime1 = "Nu";}
                    tramData.direction = jsonTram.getString("direction");
                    tramData.textColor = jsonTram.getString("bgColor");
                    tramData.signColor = jsonTram.getString("fgColor");
                    tramData.waitTime2 = "-";
                    tramData.journeyUrl = jsonTram.getJSONObject("JourneyDetailRef").getString("ref");
                    tramList.add(tramData);
                    checkList.add(jsonTram.get("sname") + "" + jsonTram.get("direction"));
                } else {
                    Tram tramData = tramList.get(checkList.indexOf(jsonTram.get("sname") + "" + jsonTram.get("direction")));

                    if (tramData.waitTime2.equals("-")) {
                        int departureTime;
                        if (jsonTram.isNull("rtTime")){
                            departureTime = (Integer.parseInt(jsonTram.getString("time").substring(3)) + Integer.parseInt(jsonTram.getString("time").substring(0,2)) * 60);
                        } else {
                            departureTime = (Integer.parseInt(jsonTram.getString("rtTime").substring(3)) + Integer.parseInt(jsonTram.getString("rtTime").substring(0, 2)) * 60);
                        }
                        int waitTime = departureTime - nowTime;

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
