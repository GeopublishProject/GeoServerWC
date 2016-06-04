package com.geopublish.geoserver.gps.emulator;

/**
 * Created by edgar on 11/10/2015.
 * Esta interefaz sencillamente es para avisar de una nueva posicion GPS
 */

public interface GPSEmulatorListener {
    void newPosition(double latitude, double longitude);
}