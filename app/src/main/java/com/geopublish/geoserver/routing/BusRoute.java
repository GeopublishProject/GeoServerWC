package com.geopublish.geoserver.routing;

import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;

/**
 * Created by edgar on 1/19/2016.
 * Guarda la ruta del bus y obtiene la posicion exacta del bus dentro de la ruta.
 */

/*
Esta clase contiene uno de los metodos mas importantes de toda la aplicacion, el cual ayuda a calcular la poscion del bus
dentro de la ruta. Hacer este calculo es de vital importancia porque dota de preciosn y velocidad  a varios metodos y hace
que el sistema no se recargue con demasiadas peticiones al servidor
El servicio principal de la aplicacion(servicio primario de la aplicacion y con el cual nacio la misma) es el de determinar
si el usuario se esta aproximando a su parada. Para hacer este calculo se puede usar las capacidad de android para que el
GPS envie alertas cuando un usuario se acerca a tantos metros de un punto de referencia, pero esto tiene el problema de que
estas alertas son dadas de manera radial y no lineal lo que supone un grave problema para el usuario si este se encuentra a
400 mts y el sistema envia un mensaje de parada sin saber si esta fue la parada asignada por el sistema o no. La unica manera
de solucionar esto es hacer el calculo de distancia lineal sobre la ruta, lo cual a su vez supone saber donde se encuentra el
bus exactamente dentro de la ruta.
En primera instancia se puede pensar que la posicion del bus dentro de la ruta viene dada por el segmento mas corto de la posicion GPS
al segmento de ruta, pero esto presenta el problema de que un bus puede estar en el otro tramo de la ruta como por ejemplo para rutas
compartidas de ida y vuelta por una misma calle. Para resolver este problema, lo que se necesita es tomar dos muestras consecutivas de
puntos y con base en cada punto del bus calcular los puntos dentro de los segmentos de ruta, es decir que para el punto PuntoBus1 se
obtienen los puntos PuntoRuta1 y PuntoRuta2 y para el PuntoBus2 se obtienen los puntos PuntoRyta3 y PuntoRuta4. Basado en estos puntos
se toman  las parejas PuntoRuta1, PuntoRuta3 y PuntoRuta2, PuntoRuta4 y para cada pareja se calcula si estos puntos son consecutivos
dentro de la ruta. Se presentan mas casos pero el caso descrito cubre en gran medida la gran mayoria de situaciones.
Falto indcar que el proceso se realiza continuamente cada vez que se detecta un cambio de poscion del bus(tablet) por lo que
el punto PuntoBus2 pasara a ser PuntoBus1 y el PuntoBus2 tendra la nueva posicion GPS detectada.
 */
public class BusRoute {
    BusRoutePart busRoutePart1;
    BusRoutePart busRoutePart2;
    GeometryFactory fact;
    ArrayList<Coordinate> coordinates;
    Coordinate point1;
    Coordinate point2;
    Coordinate point3;
    Coordinate point4;
    int pointsCount = 0;
    int searchIndex = 0;
    SearchPointResult searchPointResult;

    public BusRoute(ArrayList<Coordinate> coordinates, BusRoutePart busRoutePart1, BusRoutePart busRoutePart2) {
        this.busRoutePart1 = busRoutePart1;
        this.busRoutePart2 = busRoutePart2;
        this.coordinates = coordinates;

        fact = new GeometryFactory();
        searchPointResult = new SearchPointResult(0, false);
    }

    public Coordinate getBusPositionInRoute(double x, double y) {
        long startTime = System.currentTimeMillis();

        boolean pointFound = false;
        Coordinate tempCoordinate1;
        Coordinate tempCoordinate2;
        Coordinate calculatedPointInRoute = null;

        Point point = fact.createPoint(new Coordinate(x, y));
        Geometry isodecagon = point.buffer(20, 20);

        Log.i("INFO", "Bus point: " + String.valueOf(point.getCoordinate().x) + " | Y: " + String.valueOf(point.getCoordinate().y));

        Geometry busRouteSegments1 = busRoutePart1.getStringRoute().intersection(isodecagon);
        Geometry busRouteSegments2 = busRoutePart2.getStringRoute().intersection(isodecagon);

        tempCoordinate1 = getClosestPointInSegment(point.getCoordinate(), busRouteSegments1);
        tempCoordinate2 = getClosestPointInSegment(point.getCoordinate(), busRouteSegments2);

        switch (pointsCount) {
            case 0:
                point1 = tempCoordinate1;
                point2 = tempCoordinate2;

                pointsCount = 1;

                if ((point1 == null) && (point2 == null))
                    return null;

                //Buscamos y devolvemos dentro del segmento de referencia
                if ((point1 != null) && (point2 != null)) {
                    if (point.getCoordinate().distance(point1) < point.getCoordinate().distance(point2))
                        return point1;
                    else
                        return point2;
                }

                if (point1 != null) return point1;
                if (point2 != null) return point2;

                //if (point1!=null  )Log.i("INFO", "point1: " + String.valueOf(point1.x) + " | Y: " + String.valueOf(point1.y) ); else Log.i("INFO", "point1: null"  );
                //if (point2!=null  )Log.i("INFO", "point2: " + String.valueOf(point2.x) + " | Y: " + String.valueOf(point2.y) ); else Log.i("INFO", "point2: null"  );
                //if (point3!=null  )Log.i("INFO", "point3: " + String.valueOf(point3.x) + " | Y: " + String.valueOf(point3.y) ); else Log.i("INFO", "point3: null"  );
                //if (point4!=null  )Log.i("INFO", "point4: " + String.valueOf(point4.x) + " | Y: " + String.valueOf(point4.y) ); else Log.i("INFO", "point4: null"  );

                break;

            case 1:
                point3 = tempCoordinate1;
                point4 = tempCoordinate2;
                pointsCount = 2;

                //if (point1!=null  )Log.i("INFO", "point1: " + String.valueOf(point1.x) + " | Y: " + String.valueOf(point1.y) ); else Log.i("INFO", "point1: null"  );
                //if (point2!=null  )Log.i("INFO", "point2: " + String.valueOf(point2.x) + " | Y: " + String.valueOf(point2.y) ); else Log.i("INFO", "point2: null"  );
                //if (point3!=null  )Log.i("INFO", "point3: " + String.valueOf(point3.x) + " | Y: " + String.valueOf(point3.y) ); else Log.i("INFO", "point3: null"  );
                //if (point4!=null  )Log.i("INFO", "point4: " + String.valueOf(point4.x) + " | Y: " + String.valueOf(point4.y) ); else Log.i("INFO", "point4: null"  );

                break;
            case 2:
                point1 = point3;
                point2 = point4;
                point3 = tempCoordinate1;
                point4 = tempCoordinate2;

                //if (point1!=null  )Log.i("INFO", "point1: " + String.valueOf(point1.x) + " | Y: " + String.valueOf(point1.y) ); else Log.i("INFO", "point1: null"  );
                //if (point2!=null  )Log.i("INFO", "point2: " + String.valueOf(point2.x) + " | Y: " + String.valueOf(point2.y) ); else Log.i("INFO", "point2: null"  );
                //if (point3!=null  )Log.i("INFO", "point3: " + String.valueOf(point3.x) + " | Y: " + String.valueOf(point3.y) ); else Log.i("INFO", "point3: null"  );
                //if (point4!=null  )Log.i("INFO", "point4: " + String.valueOf(point4.x) + " | Y: " + String.valueOf(point4.y) ); else Log.i("INFO", "point4: null"  );
                break;
        }

        if ((point1 != null) && (point3 != null)) {
            //Tomamos el primer segmento de la ruta. no importa que los puntos no esten aqui porque se asumiria que al no encontralos entonces
            //el bus se encuentra en el otro segmento

            searchPointResult = arePointsConsecutiveInLines(busRoutePart1.getStringRoute(), searchIndex, point1, point3);
            searchIndex = searchPointResult.index;
            //LX Log.i("INFO", "startSearchIndex OUT: " + String.valueOf(searchIndex));
            if (searchPointResult.arePointsConsecutives) {
                calculatedPointInRoute = point3;
                pointFound = true;
            } else {
                //ATENCION: Este bloque de codigo trata el caso donde las posiciones del bus dan solamente del otro lado de la ruta
                //En una ruta que comparten la misma via de ida y regreso puede darse el caso de que el bus VIENE de "VUELTA" pero sus puntos se registran en el segmento de IDA
                //por lo tanto al hacer los calculos para ver si los puntos se encuentran en la misma orientacion de la ruta de IDA no va a dar un resultado positivo debido a que
                //la orientacion de los puntos es contraria al sentido de los puntos del segmento de IDA. Pero tenemos una opcion para estimar donde se encuentra el bus dentro de la
                //ruta de VUELTA y es que tomamos el punto del bus y buscamos su punto mas cercano en el segmento contrario de VUELTA y listo. ESTA LOGICA APLICA IGUAL PARA
                //CUANDO EL BUS SE ENCUENTRA REALMENTE SOBRE EL SEGMENTO DE IDA PERO LAS POSICIONES INDICAN QUE SE ENCUENTRA MAS CERCANO AL SEGMENTO DE VUELTA

                if (point4 == null)
                    calculatedPointInRoute = getClosestPointInSegment(point.getCoordinate(), busRoutePart2.getRoute());
                else
                    calculatedPointInRoute = point4;

                pointFound = true;
            }
        }

        if (!pointFound) {
            if ((point2 != null) && (point4 != null)) {
                //Tomamos el primer segmento de la ruta. no importa que los puntos no esten aqui porque se asumiria que al no encontralos entonces
                //el bus se encuentra en el otro segmento

                searchPointResult = arePointsConsecutiveInLines(busRoutePart2.getStringRoute(), searchIndex, point2, point4);
                //searchPointResult=arePointsConsecutiveInLines(busRoutePart2.getStringRoute(),0,point2, point4 );
                searchIndex = searchPointResult.index;
                //LX Log.i("INFO", "startSearchIndex OUT: " + String.valueOf(searchIndex));
                if (searchPointResult.arePointsConsecutives) {
                    calculatedPointInRoute = point4;
                } else {
                    //APLICAR la misma logica de arriba pero a la inversa
                    if (point3 == null)
                        calculatedPointInRoute = getClosestPointInSegment(point.getCoordinate(), busRoutePart1.getRoute());
                    else
                        calculatedPointInRoute = point3;
                }
            }
        }

        if (calculatedPointInRoute != null) {
            Log.i("INFO", "X: " + String.valueOf(calculatedPointInRoute.x) + " | Y: " + String.valueOf(calculatedPointInRoute.y));
            //if (point1!=null  )Log.i("INFO", "point1: " + String.valueOf(point1.x) + " | Y: " + String.valueOf(point1.y) ); else Log.i("INFO", "point1: null"  );
            //if (point2!=null  )Log.i("INFO", "point2: " + String.valueOf(point2.x) + " | Y: " + String.valueOf(point2.y) ); else Log.i("INFO", "point2: null"  );
            //if (point3!=null  )Log.i("INFO", "point3: " + String.valueOf(point3.x) + " | Y: " + String.valueOf(point3.y) ); else Log.i("INFO", "point3: null"  );
            //if (point4!=null  )Log.i("INFO", "point4: " + String.valueOf(point4.x) + " | Y: " + String.valueOf(point4.y) ); else Log.i("INFO", "point4: null"  );
        } else
            searchIndex = 0;


        long stopTime = System.currentTimeMillis();
        Log.i("INFO", String.valueOf(stopTime - startTime));

        point = null;
        isodecagon = null;
        busRouteSegments1 = null;
        busRouteSegments2 = null;

        return calculatedPointInRoute;
    }

    /**
     * En un conjunto de segmentos de linea, obtiene el punto de segmento mas cercano a un punto de referencia
     *
     * @param referencePoint Punto de referencia( puede ser la posicion de un carro o bus)
     * @param segments       Segmentos de ruta(Puntos consecutivos)
     * @return Coordenada con el punto dentro del segmento
     */
    private Coordinate getClosestPointInSegment(Coordinate referencePoint, Geometry segments) {
        if (referencePoint == null) return null;
        if (segments == null) return null;

        Double minDistance = 20.0;
        Double tempDistance;
        LineSegment lineSegment;
        Coordinate tempPoint = null;

        for (int i = 0; i < segments.getNumGeometries(); i++) {
            Coordinate[] coordinates = segments.getGeometryN(i).getCoordinates();

            for (int j = 0; j < coordinates.length - 1; j++) {
                lineSegment = new LineSegment(coordinates[j], coordinates[j + 1]);

                Coordinate closestPt = lineSegment.closestPoint(referencePoint);
                tempDistance = referencePoint.distance(closestPt);

                if (tempDistance < minDistance) {
                    minDistance = tempDistance;
                    tempPoint = closestPt;
                }
            }
        }

        return tempPoint;
    }

    /**
     * Determina si dos puntos son consecutivos o no en la ruta
     *
     * @param lineString       Ruta
     * @param startSearchIndex Indice  del vertice de ruta a partir del cual se empieza a buscar
     * @param point1           Punto1
     * @param point2           Punto2
     * @return Objeto de tipo SearchPointResult que contiene un valor booleano indicando si los puntos se encuentran en la ruta o no
     */
    private SearchPointResult arePointsConsecutiveInLines(LineString lineString, int startSearchIndex, Coordinate point1, Coordinate point2) {
        // if (1==1) return true;

        SearchPointResult tempSearchPointResult = new SearchPointResult(startSearchIndex, false);

        if ((point1 == null) || (point2 == null)) return tempSearchPointResult;
        //return false;

        //Puede darse el caso de que dos puntos de posicion genere los mismos puntos dentro de la ruta por lo
        // que asumimos que si se esta en la ruta
        if (point1.equals(point2)) {
            //return true;
            tempSearchPointResult.arePointsConsecutives = true;
        }

        //int index = 0;
        boolean originFound = false;
        boolean arePointsConsecutives = false;
        boolean p1Found = false;
        double distance = 0.0;
        int startIndex = startSearchIndex;
        LineSegment segment;

        //Log.i("INFO-PROCALC", "X: " + String.valueOf(point1.x) + " | Y: " + String.valueOf(point1.y) );
        //Log.i("INFO-PROCALC", "X: " + String.valueOf(point2.x) + " | Y: " + String.valueOf(point2.y) );

        //Si en 500 metros no se ha determinado que los puntos son consecutivos entionces no busca mas. El conteo de la distancia
        //comienza a funcionar desde que encuentra el primer punto y es 500 porque en la vida real se ha detectado que a veces el GPS
        //tiene separacion de puntos de 200 mts
        while ((startIndex < lineString.getNumPoints() - 1) && distance < 500 && (tempSearchPointResult.arePointsConsecutives == false)) {
            //Log.i("INFO-POINT1", "X: " + String.valueOf(point1.x) + " | Y: " + String.valueOf(point1.y) );

            //tempSearchPointResult.index=startIndex;
            tempSearchPointResult.index = startIndex;
            segment = new LineSegment(lineString.getCoordinateN(startIndex), lineString.getCoordinateN(startIndex + 1));
            //LX  Log.i("INFO", "search index: " +  String.valueOf( startIndex) + " | Distance: " + String.valueOf(segment.distance(point1)));
            //Log.i("Distance calc ", String.valueOf(segment.distance(point1)) );

            //LX if (originFound) distance= distance + lineString.getCoordinateN(startIndex).distance(lineString.getCoordinateN(startIndex+1));

            if (segment.distance(point1) < 0.0000001) {
                //Empezamos a contar los mil metros desde el momento en que encuentra el primer punto
                //distance= distance + point1.distance(point2);
                //no esta entrano aqui  y aqui ppuede estar la demora
                originFound = true;
                //LX Log.i("INFO-POINT1", "X: " + String.valueOf(point1.x) + " | Y: " + String.valueOf(point1.y) );

                //Esta linea es imnportantisima para determinar si efectivamente dos puntos son consecutivos.
                //Por ejemplo p1 y p2 estan dentro del mismo segmento de linea pero p2 esta primero que p1, entonces
                //al encontrar p1 luego se busca de p1 al punto final del segmento si p2 se encuentra alli y como no
                //lo va a encontrar recorriendo toda el lineString entonces genera un resultado indicando que los
                //puntos no son consecutivos
                segment = new LineSegment(point1, lineString.getCoordinateN(startIndex + 1));

                // distance = distance  - p1 hasta final segmento ()
                //LX  distance= distance + point1.distance(lineString.getCoordinateN(startIndex+1));

                if (segment.distance(point2) < 0.0000001) {
                    tempSearchPointResult.arePointsConsecutives = true;
                    //arePointsConsecutives= true;
                    //LX Log.i("INFO-POINT2", "X: " + String.valueOf(point2.x) + " | Y: " + String.valueOf(point2.y) );
                }
            } else {

                if (segment.distance(point2) < 0.00000001) {
                    //LX Log.i("INFO-POINT2-DIST. SEGM", "X: " + String.valueOf(point2.x) + " | Y: " + String.valueOf(point2.y));
                    if (originFound == true) {
                        tempSearchPointResult.arePointsConsecutives = true;
                        arePointsConsecutives = true;
                    }
                }
            }

            startIndex++;
        }

        segment = null;
        //Log.i("INFO", "are points consec: " + String.valueOf(tempSearchPointResult.arePointsConsecutives));
        //Log.i("INFO", "startSearchIndex: " + String.valueOf(startSearchIndex));

        //Como no se encontro que los puntos son consecutivos quiere decir que a pesar de que los puntos estuvieron
        //estos no se encontraron en la orientacion correcta y como el indice base cambio y se encuentra al final de la
        //ruta es necesario dejarlo en base 1 para que la busqueda pueda comenzar desde el principio y corregir el problema
        if (tempSearchPointResult.arePointsConsecutives == false)
            tempSearchPointResult.index = 0;

        //if (searchPointResult.index-1>-1)
        //    searchPointResult.index=searchPointResult.index-1;

        return tempSearchPointResult;
        // return arePointsConsecutives;
    }

    public class SearchPointResult {
        public int index;
        public boolean arePointsConsecutives;

        public SearchPointResult(int index, boolean arePointsConsecutives) {
            this.index = index;
            this.arePointsConsecutives = arePointsConsecutives;
        }


    }


}
