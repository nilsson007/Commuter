package com.development.android.commuter;

public class Tram {
    public String direction;
    public String number;
    public String waitTime1;
    public String waitTime2;
    public String signColor;
    public String textColor;
    public String journeyUrl;

    Tram(String shortName) {
        this.number = shortName;
    }
}
