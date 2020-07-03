package com.example.aidfirsttest1;

import java.util.Random;

public class Report {

    private String isAssigned;
    private String victimDetails;
    private String body;
    private String from;
    private boolean hasInternet;
    private String latitude;
    private String longitude;

    public Report(String isAss, String victimDetails, String body, String from, boolean hasInternet, String lat, String longi){
        this.isAssigned = isAss;
        this.victimDetails = victimDetails;
        this.body = body;
        this.from = from;
        this.hasInternet = hasInternet;
        this.latitude = lat;
        this.longitude = longi;
    }

    public String getIsAssigned() {
        return isAssigned;
    }

    public String getVictimDetails() {
        return victimDetails;
    }

    public String getBody() {
        return body;
    }

    public String getFrom() {
        return from;
    }

    public boolean isHasInternet() {
        return hasInternet;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
