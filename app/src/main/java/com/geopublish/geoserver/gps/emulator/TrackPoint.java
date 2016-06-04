package com.geopublish.geoserver.gps.emulator;

/*
Clase encargada de leer un archivo de tracker GPX
 */
public class TrackPoint {
    private final static String START_TAG = "<trkpt ";
    private final static String START_ANY_TAG = "<";
    private final static String END_TAG_EMPTY = "/>";
    private final static String END_TAG_FULL = "</trkpt>";

    private final static String ATTR_LAT = "lat=\"";
    private final static String ATTR_LON = "lon=\"";
    private final static String ELEM_ELE = "<ele>";
    private final static String ELEM_TIME = "<time>";

    private double mLat = 0.0;
    private double mLon = 0.0;
    private double mEle = Double.MIN_VALUE;
    private String mTime;

    public TrackPoint() {
    }

    /**
     * Extrae de una linea procesada del archivo los datos de posicion, elevacion y tiempo del punto
     *
     * @param s Linea del archivo
     * @return Retorna siempre true indicando que el proceso fue correcto
     */
    private boolean parse(String s) {
        int lat = s.indexOf(ATTR_LAT);
        int lon = s.indexOf(ATTR_LON);
        int ele = s.indexOf(ELEM_ELE);
        int time = s.indexOf(ELEM_TIME);

        if (lat < 0 || lon < 0) {
            //throw new InvalidParameterException("trkpt without lat or lon attribute");
            return false;
        }

        int endLat = s.indexOf("\"", lat + ATTR_LAT.length());
        int endLon = s.indexOf("\"", lon + ATTR_LON.length());

        mLat = Double.parseDouble(s.substring(lat + ATTR_LAT.length(), endLat));
        mLon = Double.parseDouble(s.substring(lon + ATTR_LON.length(), endLon));

        if (ele > 0) {
            mEle = Double.parseDouble(s.substring(ele + ELEM_ELE.length(), s.indexOf("<", ele + ELEM_ELE.length())));
        }

        if (time > 0) {
            mTime = s.substring(time + ELEM_TIME.length(), s.indexOf("<", time + ELEM_TIME.length()));
        }

        //System.out.println("lat " + (lat+ATTR_LAT.length()) + " endLat " + endLat);

        return true;
    }

    /**
     * @param s
     * @return
     */
    public boolean parse(StringBuilder s) {
        int startTag = s.indexOf(START_TAG);
        if (startTag < 0) return false;

        int nextTag = s.indexOf(START_ANY_TAG, startTag);
        int endTagEmpty = s.indexOf(END_TAG_EMPTY, startTag);
        int endTagFull = s.indexOf(END_TAG_FULL, startTag);

        if (endTagEmpty + END_TAG_EMPTY.length() == s.length()) {
            // It's like <trkpt .../>EOF
            //System.out.println("matches 0 " + s.substring(startTag, endTagEmpty + END_TAG_EMPTY.length()));
            return parse(s.substring(startTag, endTagEmpty + END_TAG_EMPTY.length()));
        } else if (nextTag > 0 && endTagEmpty > 0 && nextTag < endTagEmpty) {
            // It's like <trkpt ...><...
            if (nextTag == endTagFull) {
                // It's like <trkpt ...></trkpt>
                return parse(s.substring(startTag, endTagFull + END_TAG_FULL.length()));
            } else if (endTagFull > 0) {
                // It's like <trkpt ...>...</trkpt>
                //System.out.println("matches 2 " + s.substring(startTag, endTagFull + END_TAG_FULL.length()));
                return parse(s.substring(startTag, endTagFull + END_TAG_FULL.length()));
            } else {
                return false;
            }
        } else if (nextTag > 0 && endTagFull > 0) {
            //System.out.println("matches 3 " + s.substring(startTag, endTagFull + END_TAG_FULL.length()));
            return parse(s.substring(startTag, endTagFull + END_TAG_FULL.length()));
        } else {
            //System.out.println("no match " + s.toString());
            return false;
        }
    }

    public double getLat() {
        return mLat;
    }

    public double getLon() {
        return mLon;
    }

    public double getEle() {
        return mEle;
    }

    public String getTime() {
        return mTime;
    }
}
