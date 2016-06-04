package com.geopublish.geoserver.model;

import org.mapsforge.core.model.LatLong;

/**
 * Created by edgar on 1/18/2016.
 */
public class RoutePoint {

    byte index;
    LatLong point;

    public RoutePoint(byte index, LatLong point) {
        this.index = index;
        this.point = point;
    }

}
