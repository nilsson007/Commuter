package com.development.android.commuter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class TramStopListAdapter extends ArrayAdapter<Tram> {

    private AuthorizationToken authorizationToken;
    private String name;
    private int lastClicked = -1;

    TramStopListAdapter(Context context, ArrayList<Tram> trams, String stopName) {
        super(context, 0, trams);

        authorizationToken = AuthorizationToken.getAuthToken();

        name = stopName;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        // Get the data item for this position
        final Tram tram = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tram_stop_list_item, parent, false);
        }
        // Lookup view for data population
        TextView tramName = convertView.findViewById(R.id.tram_stop_list_item_name);
        TextView tramSign = convertView.findViewById(R.id.tram_symbol);
        TextView waitText1 = convertView.findViewById(R.id.tram_wait1);
        TextView waitText2 = convertView.findViewById(R.id.tram_wait2);
        // Populate the data into the template view using the data object
        tramName.setText(tram.direction);
        Drawable bg = getContext().getDrawable(R.drawable.tram_number_bg);
        bg.setColorFilter(Color.parseColor(tram.signColor), PorterDuff.Mode.SRC_ATOP);
        tramSign.setBackground(bg);
        tramSign.setText(tram.number);
        tramSign.setTextColor(Color.parseColor(tram.textColor));
        waitText1.setText(tram.waitTime1);
        waitText2.setText(tram.waitTime2);
        // Set on click listener
        LinearLayout ll = convertView.findViewById(R.id.tram_stop_list_item_group);
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListView lv = view.findViewById(R.id.nextStopList);
                if (lv.getMeasuredHeight() == 0) {
                    if (lastClicked != -1){
                        ListView lvClose = parent.getChildAt(lastClicked).findViewById(R.id.nextStopList);
                        ViewGroup.LayoutParams params = lvClose.getLayoutParams();
                        params.height = 0;
                        lvClose.setLayoutParams(params);
                    }
                    lastClicked = position;
                    ListAdapter la = lv.getAdapter();
                    if (la == null) {
                        String journeyUrl = tram.journeyUrl;
                        StringRequest journeyRequest = new StringRequest(Request.Method.GET, journeyUrl,
                                getJourneyRequestResponse(lv,tram), new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("Poop", error.toString());

                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                return authorizationToken.getTokenParams();
                            }
                        };
                        RequestQueue queue = Volley.newRequestQueue(getContext());
                        queue.add(journeyRequest);
                    }else{
                        ViewGroup.LayoutParams params = lv.getLayoutParams();
                        params.height = tram.height;
                        lv.setLayoutParams(params);
                    }
                }else{
                    //lv.setAdapter(null);
                    ViewGroup.LayoutParams params = lv.getLayoutParams();
                    params.height = 0;
                    lv.setLayoutParams(params);
                }
            }});
        // Return the completed view to render on screen
        return convertView;
    }

    private Response.Listener<String> getJourneyRequestResponse(ListView _listView, final Tram tram) {
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
            ArrayList<JourneyStop> stops = new ArrayList<>();
            boolean startSaving = false;
            while (jsonArray.length() > 0) {
                JourneyStop stop = new JourneyStop();
                stop.name = jsonArray.optJSONObject(0).optString("name");
                stop.name = stop.name.substring(0, stop.name.indexOf(','));
                if (startSaving) {
                    stop.time = jsonArray.optJSONObject(0).optString("rtArrTime");
                    stops.add(stop);
                }
                if (stop.name.equals(name)) {
                    startSaving = true;
                }
                jsonArray.remove(0);
            }
            JourneyStopListAdapter la = new JourneyStopListAdapter(lv.getContext(), stops);
            lv.setAdapter(la);
            View listItem = la.getView(0, null, lv);
            listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

            int totalHeight = listItem.getMeasuredHeight() * stops.size();
            ViewGroup.LayoutParams params = lv.getLayoutParams();
            tram.height = totalHeight + (lv.getDividerHeight() * (la.getCount() - 1));
            params.height = tram.height;
            lv.setLayoutParams(params);
            }
        };
    }
}
