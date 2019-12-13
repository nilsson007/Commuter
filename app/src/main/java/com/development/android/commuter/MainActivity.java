package com.development.android.commuter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    TramStopPagerAdapter tramStopPagerAdapter;

    static final byte MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 67;

    //static final int MIN_ACCURACY = 150;

    static final int MIN_LOCATION_UPDATES = 1;

    static final int MAX_LOCATION_UPDATE_TIME = 5000;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    ViewPager mViewPager;

    AuthorizationToken authorizationToken;

    String baseUrl = "https://api.vasttrafik.se/bin/rest.exe/v2/location.nearbystops?originCoordLat=";

    Location location;

    FusedLocationProviderClient mFusedLocationClient;

    ArrayList<Map<String, String>> stopsList;

    ImageButton mUpdateButton;

    int locationUpdatesCountDown = MIN_LOCATION_UPDATES;

    CountDownTimer locationUpdateTimer = new CountDownTimer(MAX_LOCATION_UPDATE_TIME, MAX_LOCATION_UPDATE_TIME) {

        public void onTick(long millisUntilFinished) {

        }

        public void onFinish() {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
            updateView(true);
        }
    };

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.i("position", "location update");
            if (locationResult == null) {
                return;
            }
            for (Location _location : locationResult.getLocations()) {
                //if ((_location.getAccuracy() < MIN_ACCURACY) && (--locationUpdatesCountDown <= 0 )) {
                    location = _location;
                    locationUpdateTimer.cancel();
                    updateView(true);
                    mFusedLocationClient.removeLocationUpdates(locationCallback);
                    locationUpdatesCountDown = MIN_LOCATION_UPDATES;
                //}
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
        mUpdateButton = findViewById(R.id.update_button);
        mUpdateButton.setEnabled(false);
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocationUpdate();
                mUpdateButton.setEnabled(false);
                ((Animatable)mUpdateButton.getDrawable()).start();
            }
        });

        mViewPager = findViewById(R.id.pager);
        int oldOrientation;
        if (savedInstanceState != null) {
            authorizationToken = AuthorizationToken.initializeAuthToken(this, savedInstanceState.getString("token"));
            oldOrientation = savedInstanceState.getInt("orientation");
            stopsList = (ArrayList<Map<String, String>>)savedInstanceState.getSerializable("stopList");
        } else {
            authorizationToken = AuthorizationToken.initializeAuthToken(this, null);
            oldOrientation = -1;
        }

        // initialize location request
        locationRequest.setFastestInterval(100);
        locationRequest.setInterval(200);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (oldOrientation != this.getResources().getConfiguration().orientation && oldOrientation != -1)
            updateView(false);
        else requestLocationUpdate();
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
        tramStopPagerAdapter = new TramStopPagerAdapter(getFragmentManager(), new ArrayList<Map<String, String>>());
        mViewPager.setAdapter(tramStopPagerAdapter);
        requestLocationUpdate();
        Log.i("position","onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i("position","onSaveInstanceState");
        outState.putInt("orientation", this.getResources().getConfiguration().orientation);
        outState.putString("token", authorizationToken.getToken());
        outState.putSerializable("stopList", stopsList);
        super.onSaveInstanceState(outState);
    }

    private void updateView(boolean fetch) {
        Log.i("position","updateView");
        if (fetch) {
            if (location != null) {
                tramStopPagerAdapter = new TramStopPagerAdapter(getFragmentManager(), new ArrayList<Map<String, String>>());
                mViewPager.setAdapter(tramStopPagerAdapter);
                String url = baseUrl + location.getLatitude() + "&originCoordLong=" + location.getLongitude() + "&maxNo=200&format=json";
                sendRequest(url);
            }
        }else{
            tramStopPagerAdapter = new TramStopPagerAdapter(getFragmentManager(), stopsList);
            mViewPager.setAdapter(tramStopPagerAdapter);
            mUpdateButton.setEnabled(true);
            ((Animatable)mUpdateButton.getDrawable()).stop();
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
                }
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
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
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
                        stopsList = new ArrayList<>();
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
                        tramStopPagerAdapter = new TramStopPagerAdapter(getFragmentManager(), stopsList);
                        mViewPager.setAdapter(tramStopPagerAdapter);
                        mUpdateButton.setEnabled(true);
                        ((Animatable)mUpdateButton.getDrawable()).stop();
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
                    mViewPager.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_signal_wifi_off_black_24dp));
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(200, 200);
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    mViewPager.setLayoutParams(layoutParams);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                return authorizationToken.getTokenParams();
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(nextTramRequest);
    }
}
