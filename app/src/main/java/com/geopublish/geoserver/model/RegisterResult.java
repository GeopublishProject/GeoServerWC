package com.geopublish.geoserver.model;

/**
 * Created by edgar on 9/20/2015.
 */
public class RegisterResult {
    public int resultCode = -1;
    public double latitude = 0;
    public double longitude = 0;

    public RegisterResult(int resultCode, double latitude, double longitude) {
        this.resultCode = resultCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
