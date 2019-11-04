package com.development.android.commuter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Log;

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
            stop.id = jsonArray.optJSONObject(0).optString("id");
            if (startSaving) {
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
                    Log.i("ParseException","date parse");
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
