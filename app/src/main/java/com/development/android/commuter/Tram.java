package com.development.android.commuter;

import java.util.ArrayList;
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
    ArrayList<JourneyStop> journeyStops;
    boolean open = false;

    Tram(String shortName) {
        this.number = shortName;
        journeyStops = new ArrayList<>();
    }
    void makeJourneyStopList(JSONArray jsonArray, String tramStopName){
        boolean startSaving = false;
        while (jsonArray.length() > 0) {
            JourneyStop stop = new JourneyStop();
            stop.name = jsonArray.optJSONObject(0).optString("name");
            stop.name = stop.name.substring(0, stop.name.indexOf(','));
            if (startSaving) {
                JSONObject tmpJson = jsonArray.optJSONObject(0);
                if (tmpJson.optString("rtArrTime").equals("")) {
                    stop.time = jsonArray.optJSONObject(0).optString("arrTime");
                }else{
                    stop.time = jsonArray.optJSONObject(0).optString("rtArrTime");
                }
                journeyStops.add(stop);
            }
            if (stop.name.equals(tramStopName)) {
                startSaving = true;
            }
            jsonArray.remove(0);
        }
    }
}
