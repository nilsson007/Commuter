package com.development.android.commuter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class JourneyStopListAdapter extends ArrayAdapter<JourneyStop> {

    JourneyStopListAdapter(Context context, ArrayList<JourneyStop> stops) {
        super(context, 0, stops);
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
        // Return the completed view to render on screen
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new TramStopFragment();
                Bundle args = new Bundle();
                args.putString("name", stop.name);
                args.putString("dist", "");
                args.putString("id", stop.id);
                args.putSerializable("time", stop.calTime);
                args.putBoolean("poopUp",true);
                fragment.setArguments(args);
                FrameLayout frame = new FrameLayout(getContext());
                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layout.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
                layout.setMargins(dpToPx(10),dpToPx(10),dpToPx(10),dpToPx(10));
                int id = View.generateViewId();
                frame.setId(id);
                frame.setLayoutParams(layout);
                frame.setBackgroundColor(getContext().getColor(R.color.colorPrimaryDark));
                frame.setElevation(20);
                Activity context = (Activity)getContext();
                RelativeLayout fragmentView = (RelativeLayout) parent.getParent().getParent().getParent().getParent().getParent();
                fragmentView.addView(frame);
                FragmentTransaction transaction = context.getFragmentManager().beginTransaction();
                transaction.add(id, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        return convertView;
    }
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
