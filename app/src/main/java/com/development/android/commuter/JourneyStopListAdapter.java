package com.development.android.commuter;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class JourneyStopListAdapter extends ArrayAdapter<JourneyStop> {

    private TramStopFragment parentFragment;

    private double lastTouchDownX;
    private double lastTouchDownY;

    JourneyStopListAdapter(Context context, ArrayList<JourneyStop> stops, TramStopFragment pf) {
        super(context, 0, stops);
        parentFragment = pf;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        // Get the data item for this position
        final JourneyStop stop = getItem(position);
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


        convertView.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // save the X,Y coordinates
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    lastTouchDownX = event.getX()/Resources.getSystem().getDisplayMetrics().widthPixels;
                    lastTouchDownY = event.getRawY()-getStatusBarHeight();
                }

                // let the touch event pass on to whoever needs it
                return false;
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TramStopFragment fragment = new TramStopFragment();
                Bundle args = new Bundle();
                args.putString("name", stop.name);
                args.putString("dist", "");
                args.putString("id", stop.id);
                args.putSerializable("time", stop.calTime);
                args.putBoolean("popUp",true);
                fragment.setArguments(args);
                parentFragment.setChildFragment(fragment, lastTouchDownX, lastTouchDownY);
            }
        });
        return convertView;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = Resources.getSystem().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
