package com.development.android.commuter;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

class Tram {
    String direction;
    public String number;
    String waitTime1;
    String waitTime2;
    String signColor;
    String textColor;
    String journeyUrl;
    int height;
    int nowTime;
    ArrayList<JourneyStop> journeyStops;
    boolean open = false;

    Tram(String shortName) {
        this.number = shortName;
        journeyStops = new ArrayList<>();
    }
    void makeJourneyStopList(JSONArray jsonArray, String tramStopName){
        journeyStops.clear();
        boolean startSaving = false;
        while (jsonArray.length() > 0) {
            JourneyStop stop = new JourneyStop();
            stop.name = jsonArray.optJSONObject(0).optString("name");
            stop.name = stop.name.substring(0, stop.name.indexOf(','));
            if (startSaving) {
                JSONObject tmpJson = jsonArray.optJSONObject(0);
                stop.time = TimeDiffString.getJourneyWaitTime(tmpJson,nowTime);
                journeyStops.add(stop);
            }
            if (stop.name.equals(tramStopName)) {
                startSaving = true;
            }
            jsonArray.remove(0);
        }
    }
}
