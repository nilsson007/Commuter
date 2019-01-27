package com.development.android.commuter;

class Tram {
    String direction;
    public String number;
    String waitTime1;
    String waitTime2;
    String signColor;
    String textColor;
    String journeyUrl;
    int height;

    Tram(String shortName) {
        this.number = shortName;
    }
}
