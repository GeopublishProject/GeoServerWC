package com.geopublish.geoserver.db.model;

/**
 * Created by edgar on 1/21/2016.
 * Guarda los datos relativos a la posicion de la parada asignada por el sistema
 */
public class UserStopData {
    public String deviceId;
    public double assignedStopPointX;
    public double assignedStopPointY;

    public UserStopData(String deviceId, double assignedStopPointX, double assignedStopPointY) {
        this.deviceId = deviceId;
        this.assignedStopPointX = assignedStopPointX;
        this.assignedStopPointY = assignedStopPointY;
    }
}
