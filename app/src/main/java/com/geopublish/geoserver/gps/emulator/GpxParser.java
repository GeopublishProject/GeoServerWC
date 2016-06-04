package com.geopublish.geoserver.gps.emulator;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GpxParser {
    public boolean executing;
    private InputStream mIs = null;
    private StringBuilder mStringBuilder = new StringBuilder();
    private List<GPSEmulatorListener> listeners = new ArrayList<GPSEmulatorListener>();
    private int currentPoint = 0;
    private int _startIndexPoint;

    /**
     * Instancia un nuevo lector de trackers
     *
     * @param is              Archivo GPX con la ruta
     * @param startIndexPoint Indice desde el cual se empieza a enviar señales de posicion GPS
     */
    public GpxParser(InputStream is, int startIndexPoint) {
        mIs = is;
        _startIndexPoint = startIndexPoint;
    }

    /**
     * Agrega un listener de cambio de posiscion
     *
     * @param toAdd
     */
    public void addListener(GPSEmulatorListener toAdd) {
        listeners.add(toAdd);
    }

    /**
     * Recorre el archivo tracker y obtiene la poscion del siguiente punto. Funciona como el metodo next en un resultset
     *
     * @return Objeto de tipo TrackPoint con la posciion del punto
     * @throws IOException
     */
    public TrackPoint nextTrkPt() throws IOException {
        mStringBuilder.delete(0, mStringBuilder.length());

        int c;
        while ((c = mIs.read()) != -1) {
            mStringBuilder.append((char) c);

            TrackPoint trkpt = new TrackPoint();
            if (trkpt.parse(mStringBuilder)) {
                return trkpt;
            }
        }
        return null;
    }

    /**
     * Comienza el proceso de lectura de archivo tracker y por supuesto de la emulacion de GPS
     */
    public void Start() {
        executing = true;
        LocationGenerator locationGenerator = new LocationGenerator(this);

        locationGenerator.start();
    }

    /**
     * Cierra el archivo GPX
     */
    public void release() {
        if (this.mIs != null) {
            try {
                this.mIs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Obtiene la fecha a partir de una fecha de texto especificamente para archivos GPX
     *
     * @param time Cadena que contiene la fecha del punto
     * @return
     */
    private Date getTime(String time) {
        Date tempDate = null;
        DateFormat formatter = new SimpleDateFormat("hh:mm:ss");

        try {
            tempDate = formatter.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return tempDate;
    }

    /**
     * Clase encargada de leer el archivo tracker GPX y disparar los eventos de cambio de posicion
     */
    private class LocationGenerator extends Thread {

        TrackPoint firstPoint = new TrackPoint();
        TrackPoint secondPoint = new TrackPoint();
        GpxParser gpxParser;

        public LocationGenerator(GpxParser gpxParser) {
            super();
            this.gpxParser = gpxParser;
        }

        public void run() {
            /* Logica general de este procedimiento
                leer punto 1
                leer punto siguiente

                diferencia entre punto2 y punto1
                establecer timer o establecer thread con los segundos de diferencia
                cuando ejecute el tick el timer ejecutar evento newPosition
                establecer punto 1 con el valor de punto 2
                leer nuevamente el punto siguiente y empieza cclo
             */

            int i = 1;
            int j = 1;

            try {
                while (true) {
                    if (currentPoint == 0) {
                        //Leemos todos los puntos del archivo hasta alcanzar la posicion del startIndex.
                        // A partir de aqui es que empezamos a mandar nuevas posiciones del GPS
                        while (i < _startIndexPoint) {
                            //Hacemos que se vaya leyendo el archivo
                            gpxParser.nextTrkPt();
                            i++;
                        }

                        firstPoint = gpxParser.nextTrkPt();
                        currentPoint = 1;
                    } else
                        //Si hemos leido el punto que queriamos asignamos al firstPoint el secondPoint
                        // que habiamos obtenido en la ocasion anterior
                        firstPoint = secondPoint;


                    //Para evitar el envio de peticiones demasiado sucesivas
                    //i++;
                    //if (i % 2==0)
                    //{
                    //Log.i("INFO", "Reading point  " + String.valueOf(j+1));
                    secondPoint = gpxParser.nextTrkPt();

                    if (secondPoint != null) //Enviat que al llegar al final del archivo nos genere error
                    {
                        if (firstPoint.getTime() != null) {   //Obtenenmos la diferencia de tiempos entre punto y punto
                            Date firstPointTime = getTime(firstPoint.getTime().substring(11, 19));
                            Date secondPointTime = getTime(secondPoint.getTime().substring(11, 19));

                            long diff = secondPointTime.getTime() - firstPointTime.getTime();

                            // Notify everybody that may be interested.
                            for (GPSEmulatorListener hl : listeners)
                                hl.newPosition(firstPoint.getLat(), firstPoint.getLon());

                            //Con este sleep le decimos al emulador que espere determinado tiempo para simular
                            //que se ha recorrido una distancia y que ha pasado cierto tiempo
                            this.sleep(diff);
                        }
                    }


                    //}
                    j++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
