package com.development.android.commuter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private View lastClickedView;
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
        final ListView nextStopListView = convertView.findViewById(R.id.nextStopList);
        if (lastClicked != position){
            nextStopListView.setAdapter(null);
            setViewHeight(nextStopListView, 0);
        }else{
            setViewHeight(nextStopListView, tram.height);
            nextStopListView.setAdapter( new JourneyStopListAdapter(getContext(), tram.journeyStops));
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
        //sendJourneyListUpdate(nextStopListView, tram);
        // Set on click listener
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tram.open == false) {
                    if (lastClicked != -1){
                        setViewHeight(lastClickedView, 0);
                        getItem(lastClicked).open = false;
                    }
                    lastClickedView = nextStopListView;
                    lastClicked = position;
                    tram.open = true;
                    JourneyStopListAdapter nextStopListViewAdapter = (JourneyStopListAdapter)nextStopListView.getAdapter();
                    if (nextStopListViewAdapter == null) {
                        sendJourneyListUpdate(nextStopListView, tram);
                    }else{
                        setViewHeight(nextStopListView, tram.height);
                    }
                }else{
                    setViewHeight(nextStopListView,0);
                    lastClicked = -1;
                    tram.open = false;
                }
            }});
        // Return the completed view to render on screen
        return convertView;
    }
    
    private void sendJourneyListUpdate(ListView lv, Tram tram) {
        JourneyStopListAdapter la = new JourneyStopListAdapter(lv.getContext(), tram.journeyStops);
        lv.setAdapter(la);
        String journeyUrl = tram.journeyUrl;
        StringRequest journeyRequest = new StringRequest(Request.Method.GET, journeyUrl,
                getJourneyRequestResponseListener(lv,tram), new Response.ErrorListener() {
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
    }

    private Response.Listener<String> getJourneyRequestResponseListener(final ListView lv, final Tram tram) {
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
                tram.makeJourneyStopList(jsonArray, name);
                JourneyStopListAdapter la = (JourneyStopListAdapter)lv.getAdapter();
                View listItem = la.getView(0, null, lv);
                listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                int totalHeight = listItem.getMeasuredHeight() * tram.journeyStops.size();
                tram.height = totalHeight + (lv.getDividerHeight() * (la.getCount() - 1));
                if(tram.open == true) {
                    setViewHeight(lv, tram.height);
                    la.notifyDataSetChanged();
                }
            }
        };
    }
    void setViewHeight(View view, int height){
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }
}
