package com.chatapp.ramji.buddyplans;

import java.io.Serializable;

/**
 * Created by user on 23-07-2017.
 */

public class Location implements Serializable {

    private double latitude;
    private double longitude;

    public Location(double mlat, double mlong) {
        latitude = mlat;
        longitude = mlong;

    }

    public Location() {

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
