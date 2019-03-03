package com.development.android.commuter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class JourneyStopListAdapter extends ArrayAdapter<JourneyStop> {

    JourneyStopListAdapter(Context context, ArrayList<JourneyStop> stops) {
        super(context, 0, stops);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        JourneyStop stop = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.journey_stop_list_item, parent, false);
        }
        if (position == (getCount() - 1)){
            convertView.findViewById(R.id.journey_stop_dot).setBackgroundResource(R.drawable.journey_stop_last_dot);
        }
        // Lookup view for data population
        TextView stopName = convertView.findViewById(R.id.journey_stop_name);
        TextView stopTime = convertView.findViewById(R.id.journey_stop_time);
        stopName.setText(stop.name);
        stopTime.setText(stop.time);
        // Return the completed view to render on screen
        return convertView;
    }
}
