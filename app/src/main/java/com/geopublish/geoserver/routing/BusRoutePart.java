package com.geopublish.geoserver.routing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import java.util.ArrayList;

/**
 * Created by edgar on 1/19/2016.
 * Almacena las distintos rutas de bus. Cuando se habla de rutas de bus en realidad hace relacion a una sola ruta pero que se dividio
 * en segmentos para representar lo que sucede realmente y hacer los calculos mas eficientes
 */
public class BusRoutePart {

    public int part;
    public ArrayList<Coordinate> coordinates;
    private Geometry tempRoute;
    private LineString lineString;

    public BusRoutePart(int part, ArrayList<Coordinate> coordinates) {
        this.part = part;
        this.coordinates = coordinates;
    }

    public Geometry getRoute() {
        if (tempRoute == null) {
            //TODO: Ojo no se que paso aqui pero este metodo se esta usando y siempre deulve null. Tal vez no ha dado error porque el caso que usa este metodo no se ha presentado
            //tempRoute = new GeometryFactory().createLineString(coordinates);
        }

        return tempRoute;
    }

    public LineString getStringRoute() {
        if (lineString == null) {
            Coordinate[] tempCoords = coordinates.toArray(new Coordinate[coordinates.size()]);
            lineString = new GeometryFactory().createLineString(tempCoords);
        }

        return lineString;
    }


}
