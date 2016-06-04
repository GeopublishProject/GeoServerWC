package com.geopublish.geoserver.model;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.layer.overlay.Marker;

/**
 * Created by edgar on 9/20/2015.
 * Clase encargada de guadar la asociacion entre el usuario y su icono de parada. 
 */
public class UserMarker extends Marker {

    public String deviceId;

    public UserMarker(LatLong localLatLong, Bitmap bitmap, int horizontallOffSet, int verticalOffSet, String deviceId) {
        super(localLatLong, bitmap, horizontallOffSet, verticalOffSet);
        this.deviceId = deviceId;
    }
}
