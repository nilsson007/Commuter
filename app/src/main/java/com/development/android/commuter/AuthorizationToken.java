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

public class AuthorizationToken extends Object {

    private String key;
    private String secret;
    private Random ran = new Random();

    private void setToken(String token) {
        this.token = token;
    }
    public enum TokenState {
        OK,
        UPDATING,
        NOT_AVAILABLE
    }

    public TokenState getState() {
        return state;
    }

    private void setState(TokenState state) {
        this.state = state;
    }

    private TokenState state;

    private String token = null;

    private Context context;

    private ArrayList<UpdateChecker> updateCheckers;

    private RequestQueue queue;

    static private AuthorizationToken authToken;

    private AuthorizationToken(String in_key, String in_secret, Context in_context){
        key = in_key;
        secret = in_secret;
        context = in_context;
        queue = Volley.newRequestQueue(context);
        updateCheckers = new ArrayList<>();
        setState(TokenState.NOT_AVAILABLE);
        newToken();
    }

    public String getToken() {
        return token;
    }

    static public AuthorizationToken initializeAuthToken(Context context) {

        authToken = new AuthorizationToken("NApoXpLxa7OjYnsbyD9DmT_PFp0a", "ucbvykQAMhiglpmu4dCvU3DnOfQa", context);

        return authToken;
    }

    static public AuthorizationToken getAuthToken() {

        return authToken;
    }

    public void newToken() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.vasttrafik.se/token",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonResponce = null;
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
                Map<String,String> params = new HashMap<String, String>();
                params.put("grant_type","client_credentials");
                params.put("scope", Secure.ANDROID_ID);
                int num = ran.nextInt(100);
                //params.put("scope","device_" + num);
                Log.d("id",Secure.ANDROID_ID);

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<String, String>();
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

    public void refreshToken(UpdateChecker updateChecker) {

        updateCheckers.add(updateChecker);

        if (getState() == TokenState.UPDATING) {

        }
        else {
            newToken();
        }
        setState(TokenState.UPDATING);
    }
}
