package com.development.android.commuter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    TramStopPagerAdapter tramStopPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    ViewPager mViewPager;

    AuthorizationToken authorizationToken;

    UpdateChecker updateChecker;

    String baseUrl = "https://api.vasttrafik.se/bin/rest.exe/v2/location.nearbystops?originCoordLat=";

    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the UI from res/layout/activity_main.xml
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.pager);

        if (authorizationToken == null) {
            authorizationToken = AuthorizationToken.initializeAuthToken(this);
         } else {
            authorizationToken = AuthorizationToken.getAuthToken();
            Log.i("position","getAuthToken");
        }

        updateChecker = new UpdateChecker(this) {

            @Override
            public void onUpdate() {
                updateView();
            }
        };
        updateView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        tramStopPagerAdapter = new TramStopPagerAdapter(getSupportFragmentManager(), new ArrayList<Map<String, String>>());
        mViewPager.setAdapter(tramStopPagerAdapter);
        updateView();
        Log.i("position","onRestart");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void updateView() {
        Log.i("position","updateView");
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location _location) {
                        // Got last known location. In some rare situations this can be null.
                        if (_location != null) {

                            location = _location;

                            String url = baseUrl + location.getLatitude() + "&originCoordLong=" + location.getLongitude() + "&maxNo=20&format=json";

                            sendRequest(url);
                        }
                    }
                });
    }

    private void sendRequest(String url) {
        Log.i("position","sendRequest");
        StringRequest nextTramRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonResponse;
                        ArrayList<Map<String, String>> stopsList = new ArrayList<>();
                        try {
                            jsonResponse = new JSONObject(response);
                            jsonResponse = (JSONObject) jsonResponse.get("LocationList");
                            JSONArray jsonStopList = (JSONArray) jsonResponse.get("StopLocation");
                            for (int i = 0; i < jsonStopList.length(); i++) {
                                JSONObject tmpJSONObject = (JSONObject) jsonStopList.get(i);
                                if (tmpJSONObject.getString("id").endsWith("0")) {
                                    Map<String, String> tmpMap = new HashMap<>();
                                    tmpMap.put("name", tmpJSONObject.getString("name"));
                                    tmpMap.put("id", tmpJSONObject.getString("id"));

                                    double tmpLat = tmpJSONObject.getDouble("lat");
                                    double tmpLon = tmpJSONObject.getDouble("lon");

                                    tmpMap.put("dist", Integer.toString((int)getDistance(location.getLatitude(),location.getLongitude(),tmpLat,tmpLon)));

                                    stopsList.add(tmpMap);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        tramStopPagerAdapter = new TramStopPagerAdapter(getSupportFragmentManager(), stopsList);
                        mViewPager.setAdapter(tramStopPagerAdapter);
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
                } else {
                    if (authorizationToken.getToken() == null) {
                        authorizationToken.refreshToken(updateChecker);
                    }
                    Log.i("VollyError", error.toString());
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
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(nextTramRequest);
    }
    double getDistance(double lat1, double lon1, double lat2, double lon2){  // generally used geo measurement function
        double R = 6378.137; // Radius of earth in KM
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // meters
    }
}
