package com.development.android.commuter;

import org.json.JSONObject;

class TimeDiffString {

    private static final String REAL_TIME_TRAM_WAIT_NAME = "rtTime";

    private static final String TIME_TRAM_WAIT_NAME = "time";

    private static final String REAL_TIME_JOURNEY_WAIT_NAME = "rtArrTime";

    private static final String TIME_JOURNEY_WAIT_NAME = "arrTime";



    static String getTramWaitTime(JSONObject jsonTram, int nowTime) {
        return getWaitTime(jsonTram, nowTime, REAL_TIME_TRAM_WAIT_NAME, TIME_TRAM_WAIT_NAME);
    }
    private static String getWaitTime(JSONObject jsonTram, int nowTime, String realTime, String time ){
        String s_waitTime;
        String timeString;
        if (jsonTram.isNull(realTime)) {
            timeString = jsonTram.optString(time);
        } else {
            timeString = jsonTram.optString(realTime);
        }
        int departureTime = getTimeInt(timeString);
        int waitTime = departureTime - nowTime;

        if (waitTime <= -100) {
            s_waitTime = Integer.toString(60 * 24 - nowTime + departureTime);
        } else if (waitTime >= -1) {
            s_waitTime = Integer.toString(waitTime);
        } else {
            return null;
        }
        return s_waitTime;
    }
    static String getJourneyWaitTime(JSONObject jsonTram, int nowTime) {
        return getWaitTime(jsonTram, nowTime, REAL_TIME_JOURNEY_WAIT_NAME, TIME_JOURNEY_WAIT_NAME);
    }
    static int getTramNowTimeInt(JSONObject jsonTram){
        String timeString;
        if (jsonTram.isNull(REAL_TIME_TRAM_WAIT_NAME)) {
            timeString = jsonTram.optString(TIME_TRAM_WAIT_NAME);
        } else {
            timeString = jsonTram.optString(REAL_TIME_TRAM_WAIT_NAME);
        }
        return getTimeInt(timeString);
    }
    private static int getTimeInt(String timeString){
        return (Integer.parseInt(timeString.substring(3)) + Integer.parseInt(timeString.substring(0, 2)) * 60);
    }
}
