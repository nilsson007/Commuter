package com.development.android.commuter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
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

    static final byte MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 67;

    static final int MIN_ACCURACY = 100;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    ViewPager mViewPager;

    AuthorizationToken authorizationToken;

    String baseUrl = "https://api.vasttrafik.se/bin/rest.exe/v2/location.nearbystops?originCoordLat=";

    Location location;

    boolean orientationChange = false;

    FusedLocationProviderClient mFusedLocationClient;

    CountDownTimer locationUpdateTimer = new CountDownTimer(2000, 2000) {

        public void onTick(long millisUntilFinished) {

        }

        public void onFinish() {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
            updateView();
        }
    };

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location _location : locationResult.getLocations()) {
                if (_location.getAccuracy() < MIN_ACCURACY) {
                    location = _location;
                    locationUpdateTimer.cancel();
                    updateView();
                    mFusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        }
    };

    LocationRequest locationRequest = new LocationRequest();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("position","onCreate");
        // Load the UI from res/layout/activity_main.xml
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.pager);
        if (savedInstanceState != null) {
            authorizationToken = AuthorizationToken.initializeAuthToken(this, savedInstanceState.getString("token"));
        } else {
            authorizationToken = AuthorizationToken.initializeAuthToken(this, null);
        }

        // initialize location request
        locationRequest.setFastestInterval(100);
        locationRequest.setInterval(200);

        if (orientationChange) updateView();
        else requestLocationUpdate();

        int or = this.getResources().getConfiguration().orientation;
        // Init debug location
        /*
        location = new Location("PASSIVE_PROVIDER");
        location.setLatitude(57.702833);
        location.setLongitude(11.978622);
        */
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        tramStopPagerAdapter = new TramStopPagerAdapter(getSupportFragmentManager(), new ArrayList<Map<String, String>>());
        mViewPager.setAdapter(tramStopPagerAdapter);
        requestLocationUpdate();
        Log.i("position","onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i("position","onSaveInstanceState");
        outState.putString("token", authorizationToken.getToken());
        super.onSaveInstanceState(outState);
    }

    private void updateView() {
        Log.i("position","updateView");
        if (requestLocationPermission()) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location _location) {
                            // Got last known location. In some rare situations this can be null.
                            if (_location != null) {

                                location = _location;

                                String url = baseUrl + location.getLatitude() + "&originCoordLong=" + location.getLongitude() + "&maxNo=200&format=json";

                                sendRequest(url);
                            }
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    requestLocationUpdate();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
    public void requestLocationUpdate() {
        if (requestLocationPermission())  {
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */
            );
            locationUpdateTimer.start();
        }
    }

    private boolean requestLocationPermission() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
            return false;
        }
        else {
            return true;
        }
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
                                if (tmpJSONObject.getString("id").endsWith("00")) {
                                    Map<String, String> tmpMap = new HashMap<>();
                                    String tmpName = tmpJSONObject.getString("name");
                                    tmpName = tmpName.substring(0, tmpName.indexOf(','));
                                    tmpMap.put("name", tmpName);
                                    tmpMap.put("id", tmpJSONObject.getString("id"));

                                    double tmpLat = tmpJSONObject.getDouble("lat");
                                    double tmpLon = tmpJSONObject.getDouble("lon");

                                    float result[] = new float[1];
                                    Location.distanceBetween(location.getLatitude(),location.getLongitude(),tmpLat,tmpLon,result);
                                    tmpMap.put("dist", Integer.toString((int)result[0]));
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
                            authorizationToken.refreshToken(new UpdateChecker() {

                                @Override
                                public void onUpdate() {
                                    requestLocationUpdate();
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
                                requestLocationUpdate();
                            }
                        });
                    }
                    Log.i("VollyError", error.toString());
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authorizationToken.getTokenParams();
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(nextTramRequest);
    }
}
