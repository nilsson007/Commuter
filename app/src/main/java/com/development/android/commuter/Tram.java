package com.development.android.commuter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

class Tram {
    String direction;
    public String number;
    String waitTime1 = "-";
    String waitTime2 = "-";
    String signColor = "#555555";
    String textColor = "#333333";
    String journeyUrl;
    int height;
    int nowTime;
    ArrayList<JourneyStop> journeyStops;
    private ListView journeyStopsView;
    boolean open = false;
    private boolean listReady = false;
    private boolean expanded = false;

    Tram(String shortName) {
        this.number = shortName;
        journeyStops = new ArrayList<>();
    }

    void setListView(ListView _journeyStopsView){
        journeyStopsView = _journeyStopsView;
    }

    void expandList() {
        if (listReady && journeyStopsView != null) {
            ResizeAnimation resizeAnimation = new ResizeAnimation(
                    journeyStopsView,
                    height,
                    0
            );
            journeyStopsView.startAnimation(resizeAnimation);
            expanded = true;
        }
    }
    
    void collapseList(){
        if (journeyStopsView != null) {
            ResizeAnimation resizeAnimation = new ResizeAnimation(
                    journeyStopsView,
                    0,
                    height
            );
            journeyStopsView.startAnimation(resizeAnimation);
            expanded = false;
        }
    }

    void makeJourneyStopList(JSONArray jsonArray, String tramStopName){
        journeyStops.clear();
        boolean startSaving = false;
        while (jsonArray.length() > 0) {
            JourneyStop stop = new JourneyStop();
            stop.name = jsonArray.optJSONObject(0).optString("name");
            stop.name = stop.name.substring(0, stop.name.indexOf(','));
            if (startSaving) {
                stop.id = jsonArray.optJSONObject(0).optString("id");
                JSONObject tmpJson = jsonArray.optJSONObject(0);
                stop.time = TimeDiffString.getJourneyWaitTime(tmpJson,nowTime);
                String timeStr;
                if (tmpJson.has("rtArrTime")){
                    timeStr = tmpJson.optString("rtArrDate") + " " + tmpJson.optString("rtArrTime");
                } else {
                    timeStr = tmpJson.optString("arrDate") + " " + tmpJson.optString("arrTime");
                }
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
                try {
                    cal.setTime(sdf.parse(timeStr));// all done
                    stop.calTime = cal;
                }catch (ParseException e){

                }
                journeyStops.add(stop);
            }
            if (stop.name.equals(tramStopName)) {
                startSaving = true;
            }
            jsonArray.remove(0);
        }

        View listItem = LayoutInflater.from(journeyStopsView.getContext()).inflate(R.layout.journey_stop_list_item, journeyStopsView, false);
        listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        height = (listItem.getMeasuredHeight() + journeyStopsView.getDividerHeight()) * journeyStops.size();

        listReady = true;
        if (open && !expanded) {
            expandList();
        }else if (open) // after rotation
        {
            TramStopListAdapter.setViewHeight(journeyStopsView, height);
            expanded = true;
        }
    }
}
