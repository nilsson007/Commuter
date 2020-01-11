package com.development.android.commuter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.app.Activity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    TramStopPagerAdapter tramStopPagerAdapter;

    static final byte MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 67;

    static final int ERROR_NO_ERROR = 0;

    static final int ERROR_NO_INTERNET = 1;

    static final int ERROR_NO_LOCATION = 2;

    static final int ERROR_LOCATION_OFF = 3;

    ViewPager mViewPager;

    AuthorizationToken authorizationToken;

    String baseUrl = "https://api.vasttrafik.se/bin/rest.exe/v2/location.nearbystops?originCoordLat=";

    Location location;

    FusedLocationProviderClient mFusedLocationClient;

    ArrayList<Map<String, String>> stopsList;

    ImageButton mUpdateButton;

    ImageView errorImage;

    private int error = ERROR_NO_ERROR;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.i("position", "location update");
            if (locationResult == null) {
                return;
            }
            for (Location _location : locationResult.getLocations()) {
                location = _location;
                updateView(true);
                mFusedLocationClient.removeLocationUpdates(locationCallback);
            }
        }
    };

    LocationRequest locationRequest = new LocationRequest();

    @SuppressLint("ClickableViewAccessibility")
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

        errorImage = findViewById(R.id.error_image);

        mViewPager = findViewById(R.id.pager);
        int oldOrientation;
        if (savedInstanceState != null) {
            authorizationToken = AuthorizationToken.initializeAuthToken(this, savedInstanceState.getString("token"));
            oldOrientation = savedInstanceState.getInt("orientation");
            error = savedInstanceState.getInt("error");
            stopsList = (ArrayList<Map<String, String>>)savedInstanceState.getSerializable("stopList");
        } else {
            authorizationToken = AuthorizationToken.initializeAuthToken(this, null);
            oldOrientation = -1;
        }

        // initialize location request
        locationRequest.setFastestInterval(100);
        locationRequest.setInterval(200);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (oldOrientation != this.getResources().getConfiguration().orientation && oldOrientation != -1) {
            if (error == ERROR_NO_INTERNET) {
                setError(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_signal_wifi_off_black_24dp),ERROR_NO_INTERNET);
                errorImage.setVisibility(View.VISIBLE);
            }
            else if (error == ERROR_NO_LOCATION) {
                setError(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_nobussstop),ERROR_NO_LOCATION);
                errorImage.setVisibility(View.VISIBLE);
            }
            else if (error == ERROR_LOCATION_OFF) {
                setError(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_location_off_black_24dp),ERROR_LOCATION_OFF);
                errorImage.setVisibility(View.VISIBLE);
            }
            updateView(false);
        }
        else {
            requestLocationUpdate();
            ((Animatable)mUpdateButton.getDrawable()).start();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        tramStopPagerAdapter = new TramStopPagerAdapter(getFragmentManager(), new ArrayList<Map<String, String>>());
        mViewPager.setAdapter(tramStopPagerAdapter);
        ((Animatable)mUpdateButton.getDrawable()).start();
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
        outState.putInt("error", error);
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
        }
        else {
            setError(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_location_off_black_24dp), ERROR_LOCATION_OFF);
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
                            tramStopPagerAdapter = new TramStopPagerAdapter(getFragmentManager(), stopsList);
                            mViewPager.setAdapter(tramStopPagerAdapter);
                            errorImage.setVisibility(View.INVISIBLE);
                        } catch (JSONException e) {

                            e.printStackTrace();
                            setError(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_nobussstop), ERROR_NO_LOCATION);
                            endUpdateAnimation();
                        }
                        endUpdateAnimation();
                        error = ERROR_NO_ERROR;
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
                    setError(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_signal_wifi_off_black_24dp), ERROR_NO_INTERNET);
                    endUpdateAnimation();
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
    void endUpdateAnimation(){
        mUpdateButton.setEnabled(true);
        ((Animatable2)mUpdateButton.getDrawable()).stop();
        mUpdateButton.setImageDrawable(getDrawable(R.drawable.update_location_stop_anim_vector));
        final Animatable2 animatable =(Animatable2)mUpdateButton.getDrawable();
        animatable.registerAnimationCallback(new Animatable2.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                super.onAnimationEnd(drawable);
                animatable.clearAnimationCallbacks();
                mUpdateButton.setImageDrawable(getDrawable(R.drawable.update_location_anim_vector));
            }
        });
        animatable.start();
    }
    void setError(Drawable drawable, int error_state)
    {
        errorImage.clearAnimation();
        errorImage.setImageDrawable(drawable);
        errorImage.setVisibility(View.VISIBLE);
        error = error_state;
    }
}
