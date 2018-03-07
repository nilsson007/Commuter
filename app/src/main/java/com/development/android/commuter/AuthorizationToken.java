package com.development.android.commuter;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by niqe on 2018-01-10.
 * Holds authorization token for vasttrariks API, and updates it when it expiers
 */

class AuthorizationToken {

    private String key;
    private String secret;

    private void setToken(String token) {
        this.token = token;
    }
    public enum TokenState {
        OK,
        UPDATING,
        NOT_AVAILABLE
    }

    private TokenState getState() {
        return state;
    }

    private void setState(TokenState state) {
        this.state = state;
    }

    private TokenState state;

    private String token = "";

    private ArrayList<UpdateChecker> updateCheckers;

    private RequestQueue queue;

    static private AuthorizationToken authToken;

    private AuthorizationToken(String in_key, String in_secret, Context context){
        key = in_key;
        secret = in_secret;
        queue = Volley.newRequestQueue(context);
        updateCheckers = new ArrayList<>();
        setState(TokenState.NOT_AVAILABLE);
        newToken();
    }

    String getToken() {
        return token;
    }

    static AuthorizationToken initializeAuthToken(Context context, String saved_token) {

        authToken = new AuthorizationToken(context.getString(R.string.key), context.getString(R.string.secret), context);

        if (saved_token != null) {
            authToken.setToken(saved_token);
        }

        return authToken;
    }

    static AuthorizationToken getAuthToken() {

        return authToken;
    }

    private void newToken() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.vasttrafik.se/token",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonResponce;
                        try {
                            jsonResponce = new JSONObject(response);
                            setToken(jsonResponce.getString("access_token"));
                            while (updateCheckers.size() > 0){
                                updateCheckers.get(0).onUpdate();
                                updateCheckers.remove(0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        setState(TokenState.OK);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.printf(error.toString());
            }
        }) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("grant_type","client_credentials");
                params.put("scope", "hej");

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                String credentials = key +":"+ secret;
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                header.put("Authorization", auth);
                header.put("Content-Type", "application/x-www-form-urlencoded");
                return header;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void refreshToken(UpdateChecker updateChecker) {

        updateCheckers.add(updateChecker);

        if (getState() != TokenState.UPDATING) {
            newToken();
        }
        setState(TokenState.UPDATING);
    }
}
