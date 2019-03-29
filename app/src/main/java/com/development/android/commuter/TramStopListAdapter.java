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
    private int lastClicked = -1;

    TramStopListAdapter(Context context, ArrayList<Tram> trams, String stopName) {
        super(context, 0, trams);

        authorizationToken = AuthorizationToken.getAuthToken();

        for (Tram tram : trams){
            if (tram.open) {
                lastClicked = trams.indexOf(tram);
                break;
            }
        }

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
        if (!tram.open){
            setViewHeight(nextStopListView, 0);
        }else{
            nextStopListView.setAdapter(new JourneyStopListAdapter(getContext(),tram.journeyStops));
            setViewHeight(nextStopListView, tram.height);
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
        if (tram.journeyStops.isEmpty()) {
            sendJourneyListUpdate(nextStopListView, tram);
        }
        else {
            JourneyStopListAdapter journeyStopListAdapter = new JourneyStopListAdapter(getContext(), tram.journeyStops);
            nextStopListView.setAdapter(journeyStopListAdapter);
        }
        // Set on click listener
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tram.open) {
                    if (lastClicked != -1){
                        View lastClickedView = getViewByPosition(lastClicked, (ListView)parent);
                        if (lastClickedView != null) {
                            ListView lastClickedListView = lastClickedView.findViewById(R.id.nextStopList);
                            ResizeAnimation resizeAnimation = new ResizeAnimation(
                                    lastClickedListView,
                                    0,
                                    getItem(lastClicked).height
                            );
                            lastClickedListView.startAnimation(resizeAnimation);
                        }
                        getItem(lastClicked).open = false;
                    }
                    lastClicked = position;
                    tram.open = true;
                    JourneyStopListAdapter nextStopListViewAdapter = new JourneyStopListAdapter(getContext(),tram.journeyStops);
                    nextStopListView.setAdapter(nextStopListViewAdapter);
                    ResizeAnimation resizeAnimation = new ResizeAnimation(
                            nextStopListView,
                            tram.height,
                            0
                    );
                    nextStopListView.startAnimation(resizeAnimation);
                }else{
                    ResizeAnimation resizeAnimation = new ResizeAnimation(
                            nextStopListView,
                            0,
                            tram.height
                    );
                    nextStopListView.startAnimation(resizeAnimation);
                    lastClicked = -1;
                    tram.open = false;
                }
            }});
        // Return the completed view to render on screen
        return convertView;
    }
    
    private void sendJourneyListUpdate(ListView lv, Tram tram) {
        //JourneyStopListAdapter la = new JourneyStopListAdapter(lv.getContext(), tram.journeyStops);
        //lv.setAdapter(la);
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
                JSONArray jsonArray = new JSONArray();
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    jsonArray = jsonResponse.optJSONObject("JourneyDetail").optJSONArray("Stop");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("Json Departure Response", response);
                }
                if (jsonArray != null) {
                    tram.makeJourneyStopList(jsonArray, name);
                    View listItem = LayoutInflater.from(getContext()).inflate(R.layout.journey_stop_list_item, lv, false);
                    listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    tram.height = (listItem.getMeasuredHeight() + lv.getDividerHeight()) * tram.journeyStops.size();
                    if (tram.open) {
                        setViewHeight(lv, tram.height);
                    }
                }else{
                    sendJourneyListUpdate(lv, tram);
                }
            }
        };
    }
    private void setViewHeight(ListView view, int height){
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
        ArrayAdapter aa = ((ArrayAdapter)view.getAdapter());
        if (aa != null) {
            aa.notifyDataSetChanged();
        }
    }

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return null;
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
