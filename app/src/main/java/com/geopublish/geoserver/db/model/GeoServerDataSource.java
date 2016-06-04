package com.geopublish.geoserver.db.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by edgar on 28/03/2015.
 */
public class GeoServerDataSource {
    //Metainformación de la base de datos
    public static final String BUS_ROUTES_TABLE_NAME = "BusRoutes";
    public static final String USER_STOPS_TABLE_NAME = "UserStops";
    public static final String STRING_TYPE = "nvarchar";
    public static final String INT_TYPE = "integer";
    public static final String DOUBLE_TYPE = "double";
    public static final String BOOL_TYPE = "bool";

    //Campos de la tabla BusRoutesColumns
    public static class BusRoutesColumns {
        public static final String ID = BaseColumns._ID;
        public static final String INDEX = "groupIndex";
        public static final String LAT = "vertexLatitude";
        public static final String LONG = "vertexLongitude";
        //Coordenadas UTM
        public static final String VERTEX_X = "vertexX";
        public static final String VERTEX_Y = "vertexY";
    }

    //Campos de la tabla Promos
    public static class StopsColumns {
        public static final String ID = BaseColumns._ID;
        public static final String DEVICE_ID = "idDevice";
        public static final String ASSIGNED_STOP_POINT_LAT = "assignedStopPointLat";
        public static final String ASSIGNED_STOP_POINT_LONG = "assignedStopPointLong";
        //Coordenadas UTM
        public static final String ASSIGNED_STOP_POINT_X = "assignedStopPointX";
        public static final String ASSIGNED_STOP_POINT_Y = "assignedStopPointY";
    }

    //Script de Creación de la tabla BusRoutes
    public static final String CREATE_BUS_ROUTE_SCRIPT =
            "create table " + BUS_ROUTES_TABLE_NAME + "(" +
                    BusRoutesColumns.ID + " " + INT_TYPE + " primary key autoincrement," +
                    BusRoutesColumns.INDEX + " " + INT_TYPE + " not null , " +
                    BusRoutesColumns.LAT + " " + DOUBLE_TYPE + " not null," +
                    BusRoutesColumns.LONG + " " + DOUBLE_TYPE + " not null," +
                    BusRoutesColumns.VERTEX_X + " " + DOUBLE_TYPE + " not null," +
                    BusRoutesColumns.VERTEX_Y + " " + DOUBLE_TYPE + " not null)";

    //Script de Creación de la tabla Promos
    public static final String CREATE_USER_STOPS_SCRIPT =
            "create table " + USER_STOPS_TABLE_NAME + "(" +
                    StopsColumns.ID + " " + INT_TYPE + " primary key autoincrement," +
                    StopsColumns.DEVICE_ID + " " + STRING_TYPE + " not null," +
                    StopsColumns.ASSIGNED_STOP_POINT_LAT + " " + DOUBLE_TYPE + " not null , " +
                    StopsColumns.ASSIGNED_STOP_POINT_LONG + " " + DOUBLE_TYPE + " NOT NULL," +
                    StopsColumns.ASSIGNED_STOP_POINT_X + " " + DOUBLE_TYPE + " NOT NULL," +
                    StopsColumns.ASSIGNED_STOP_POINT_Y + " " + DOUBLE_TYPE + " NOT NULL)";

    private GeoServerDBHelper openHelper;
    private SQLiteDatabase database;

    public GeoServerDataSource(Context context) {
        //Creando una instancia hacia la base de datos
        openHelper = new GeoServerDBHelper(context);
        database = openHelper.getWritableDatabase();
    }

    /**
     * Created by edgar
     * Guarda los puntos de la ruta
     * 18/01/2015
     */
    public void AddRoutePoint(int groupIndex, double latitude, double longitude, double utmX, double utmY) {
        //Al intentar hacer la adicion de coordenads con la coleccion ContentValues generaba error
        database.execSQL("INSERT INTO " + BUS_ROUTES_TABLE_NAME + "(" + BusRoutesColumns.INDEX + "," + BusRoutesColumns.LAT + "," + BusRoutesColumns.LONG + "," + BusRoutesColumns.VERTEX_X + "," + BusRoutesColumns.VERTEX_Y
                + ") VALUES(" + groupIndex + "," + String.valueOf(latitude) + "," + String.valueOf(longitude) + "," + String.valueOf(utmX) + "," + String.valueOf(utmY) + ")");
    }

    public void DeleteRoutePoints() {
        database.execSQL("DELETE FROM " + BUS_ROUTES_TABLE_NAME);
    }

}
