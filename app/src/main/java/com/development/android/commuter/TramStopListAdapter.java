package com.development.android.commuter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class TramStopListAdapter extends ArrayAdapter<Tram> {
    TramStopListAdapter(Context context, ArrayList<Tram> trams) {
        super(context, 0, trams);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Tram tram = getItem(position);
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
        tramSign.setText(tram.number);
        tramSign.setTextColor(Color.parseColor(tram.textColor));
        tramSign.setBackgroundColor(Color.parseColor(tram.signColor));
        waitText1.setText(tram.waitTime1);
        waitText2.setText(tram.waitTime2);
        // Return the completed view to render on screen
        return convertView;
    }
}