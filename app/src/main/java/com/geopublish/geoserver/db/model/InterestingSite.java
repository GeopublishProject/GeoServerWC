package com.geopublish.geoserver.db.model;

/**
 * Created by edgar on 10/27/2015.
 * Almacena la informacion de los puntos de interes
 */
public class InterestingSite {
    public String name;
    public double lat;
    public double lon;

    public InterestingSite(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }
}
