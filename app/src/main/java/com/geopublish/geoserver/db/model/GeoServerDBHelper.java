package com.geopublish.geoserver.db.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by edgar on 28/03/2015.
 * Clase encargada de la creaacion y modificacion de la base de datos local
 */
public class GeoServerDBHelper extends SQLiteOpenHelper {
    public GeoServerDBHelper(Context context) {
        super(context,
                "GeoServerDB",//String name
                null,//factory
                1//int version
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Crear la base de datos
        db.execSQL(GeoServerDataSource.CREATE_USER_STOPS_SCRIPT);
        db.execSQL(GeoServerDataSource.CREATE_BUS_ROUTE_SCRIPT);
    }

    /**
     * Crea la base de datos si esta no existe y si exsiste entonces se actualiza la estructura de la base de datos.
     * ATENCION: Este metodo recrea la base de datos, es decir que elimina las tablas existentes y las vuelve a crear
     * pero no actualiza las tablas ni mantiene los datos
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Actualizar la estructura de la base de datos

        if (newVersion > oldVersion) {
            //Eliminamos las tablas existentes y las creamos nuevamente
            db.execSQL("DROP TABLE IF EXISTS " + GeoServerDataSource.USER_STOPS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + GeoServerDataSource.BUS_ROUTES_TABLE_NAME);

            //Creamos las tablas nuevamente
            db.execSQL(GeoServerDataSource.CREATE_USER_STOPS_SCRIPT);
            db.execSQL(GeoServerDataSource.CREATE_BUS_ROUTE_SCRIPT);
        }
    }
}
