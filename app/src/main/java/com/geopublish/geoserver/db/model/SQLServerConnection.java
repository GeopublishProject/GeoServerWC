package com.geopublish.geoserver.db.model;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import com.geopublish.geoserver.model.UpdatePositionResult;
import com.geopublish.geoserver.model.UserMessage;
import com.geopublish.geoserver.model.RegisterResult;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import serialization.UserPromotionMessage;


/**
 * Created by edgar on 29/08/2015.
 * Clase que maneja la conexion con la base de datos SQL Server y tiene
 * todos los metodos de base de datos. La conexion se mantiene siepre abierta
 */
public class SQLServerConnection {

    private Connection _conn;

    @SuppressLint("NewApi")
    public SQLServerConnection() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

            ConnectionURL = "jdbc:jtds:sqlserver://" + "SQL5020.Smarterasp.net:1433/DB_9EBA44_geopublish;";
            connection = DriverManager.getConnection(ConnectionURL, "DB_9EBA44_geopublish_admin", "geopublish");

            /*
            ConnectionURL = "jdbc:jtds:sqlserver://" + "DESKTOP-T95V4U4:1433\\SQLEXPRESS;";
            connection = DriverManager.getConnection(ConnectionURL, "sa", "system");
            */
        } catch (SQLException se) {
            Log.e("ERROR", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERROR", e.getMessage());
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }

        _conn = connection;
    }

    public Connection getConnection() {
        return _conn;
    }

    /**
     * Created by edgar Molibna on 29/08/2015.
     * Registra los datos del usuario en la base de datos
     *
     * @param deviceId   Identificador del dispositivo. No es el mismo id que aparece al conectar el dispositivo al pc
     * @param userName   Nombre de usuario
     * @param bornDate   Fecha de nacimiento
     * @param occupation Ocupacion
     * @param sex        Sexo del usuario
     */
    public int RegisterUser(String deviceId, String userName, String bornDate, String occupation, String sex) {
        //TODO: Quitar las siguientes lineas. Revisar en todos los metodos
        Connection con = _conn;
        CallableStatement cs = null;
        int result = -1;

        try {

            cs = con.prepareCall("{call p_RegisterUser(?,?,?,?,?,?)}");

            cs.setString("deviceID", deviceId);
            cs.setString("userName", userName);
            cs.setString("bornDate", bornDate);
            cs.setString("occupation", occupation);
            cs.setString("sex", sex);
            cs.registerOutParameter("result", Types.INTEGER);

            cs.execute();
            result = cs.getInt("result");

        } catch (SQLException e) {

            System.err.println("SQLException: " + e.getMessage());
        } finally {
            if (cs != null) {
                /*
                try {
                    cs.close();
                } catch (SQLException e) {
                    System.err.println("SQLException: " + e.getMessage());
                }
                */
            }

        }

        return result;
    }

    /**
     * Created by edgar Molibna on 29/08/2015.
     * Registra los datos de la parada para un usuario. Retorna un codigo de error indicando cual
     * fue el resultado del intento de registro de direccion de parada
     *
     * @param tabletLat       Latitud de la tablet(servidor, bus)
     * @param tabletLong      Longitud de la tablet(servidor, bus)
     * @param deviceId        Identificador del dispositivo de usuario
     * @param deviceToken     Token del dispositivo de usuario entregado por el servicio GCM
     * @param userName        Nombre de usuario
     * @param FirstDirection  Primera parte de la direccion y su número(Calle,Carrera, Diagonal, Transversal)
     * @param SecondDirection Segunda parte de la direccion y su número(Calle,Carrera, Diagonal, Transversal)
     * @param Distance        Distancia de la interseccion entre FirstDirection y Seconddirection al punto de destino. Ej: Calle 45 Carrera 70 - 32 (32 es la distancia)
     * @param stopMarkerId    Identificador del marcador usuado por el usuario para reconocer su parada
     */
    public RegisterResult RegisterStop(double tabletLat, double tabletLong, String deviceId, String deviceToken, String userName, String FirstDirection, String SecondDirection, Integer Distance, String stopMarkerId) {
        Connection con = _conn;
        CallableStatement cs = null;
        RegisterResult registerResult = null;

        try {

            cs = con.prepareCall("{call p_RegisterUserStop(?,?,?,?,?,?,?,?,?,?,?,?)}");

            cs.setDouble("busLat", tabletLat);
            cs.setDouble("busLong", tabletLong);

            cs.setString("deviceID", deviceId);
            cs.setString("deviceToken", deviceToken);
            cs.setString("userName", userName);
            cs.setString("FirstDirection", FirstDirection);
            cs.setString("SecondDirection", SecondDirection);
            cs.setInt("Distance", Distance);
            cs.setString("stopMarkerId", stopMarkerId);
            cs.registerOutParameter("result", Types.INTEGER);
            cs.registerOutParameter("lat", Types.DOUBLE);
            cs.registerOutParameter("lon", Types.DOUBLE);

            cs.execute();

            registerResult = new RegisterResult(cs.getInt("result"), cs.getDouble("lat"), cs.getDouble("lon"));
        } catch (SQLException e) {

            System.err.println("SQLException: " + e.getMessage());
        } finally {
            if (cs != null) {
                /*
                try {
                    cs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                */
            }
            //Esta porcion de codigo puede ser replicada a todos los demas metodos para cerrar la conexion
            //
            /*
                 if (con != null) {

                try {
                    con.close();
                    } catch (SQLException e) {
                    System.err.println("SQLException: " + e.getMessage());
                    }

                }
            */
        }

        return registerResult;
    }

    /**
     * Created by edgar Molina on 29/08/2015.
     * Registra la posicion gps del ubs y obtiene la lista de  los usuarios cuya parada es cercano a este
     *
     * @param latitude     Latitud donde se encuentra la tablet(bus,server)
     * @param longitude    Longitud donde se encuentra la tablet(bus,server)
     * @param disableQuery Si es "verdadero" deshabilita el proceso que obtiene los dispostivos de usuario que estan cerca a su parada
     */
    public UpdatePositionResult GetDevicesInRange(double latitude, double longitude, boolean disableQuery) {
        Connection con = _conn;
        CallableStatement cs = null;
        ResultSet rs = null;
        UpdatePositionResult updatePositionResult = new UpdatePositionResult();

        try {

            cs = con.prepareCall("call p_GetDevicesInRange(?, ?, ?,?)");

            cs.setDouble("latitude", latitude);
            cs.setDouble("longitude", longitude);
            cs.setInt("enableUpdates", 1);
            cs.setBoolean("disableQuery", disableQuery);

            boolean results = cs.execute();
            int rowsAffected = 0;

            // Protects against lack of SET NOCOUNT in stored prodedure
            while (results || rowsAffected != -1) {
                if (results) {
                    rs = cs.getResultSet();
                    break;
                } else {
                    rowsAffected = cs.getUpdateCount();
                }
                results = cs.getMoreResults();
            }

            ArrayList<UserMessage> userMessages = new ArrayList<>();

            if (rs != null) {
                while (rs.next()) {
                    UserMessage userMessage = new UserMessage(rs.getString("id"), rs.getString("status"), rs.getString("userName"), rs.getDouble("distance"));

                    userMessages.add(userMessage);
                }
            }
            updatePositionResult.setUserMessage(userMessages);

            return updatePositionResult;
        } catch (SQLException e) {
            updatePositionResult.setResult(e.getMessage());
            System.err.println("SQLException: " + e.getMessage());
        } finally {
            if (cs != null) {
                /*
                try {
                   cs.close();
                } catch (SQLException e) {
                    System.err.println("SQLException: " + e.getMessage());
                }
                */
            }
        }

        return updatePositionResult;
    }

    /**
     * Created by edgar Molina on 29/08/2015.
     * Registra los datos del usuario que desea suscribirse a una promoción.
     * Retorna un objeto que indica cual es el resultado del proceso de suscripción
     *
     * @param userDeviceId  Identificador del dispositivo del usuario
     * @param promotionCode Codigo de promocion al cual se quiere suscribir el usuario
     */
    public UserPromotionMessage AddDevicePromo(String userDeviceId, String promotionCode) {
        Connection con = _conn;
        CallableStatement cs = null;
        ResultSet rs = null;
        UserPromotionMessage userPromotionMessage = new UserPromotionMessage();

        try {

            cs = con.prepareCall("call p_AddDevicePromo(?, ?, ?)");

            cs.setString("userDeviceId", userDeviceId);
            cs.setString("promotionCode", promotionCode);
            cs.registerOutParameter("result", Types.INTEGER);

            boolean results = cs.execute();
            int rowsAffected = 0;

            // Protects against lack of SET NOCOUNT in stored prodedure
            while (results || rowsAffected != -1) {
                if (results) {
                    rs = cs.getResultSet();
                    break;
                } else {
                    rowsAffected = cs.getUpdateCount();
                }
                results = cs.getMoreResults();
            }

            if (rs != null) {
                while (rs.next()) {
                    userPromotionMessage.clientName = rs.getString("name");
                    userPromotionMessage.clientLogo = rs.getBytes("logo");
                    userPromotionMessage.iconFileName = rs.getString("iconFilename");
                    ;
                    userPromotionMessage.promoDescription = rs.getString("description");
                    userPromotionMessage.promoExpirationDate = rs.getDate("expirationDate");
                    userPromotionMessage.promotionalPicture = rs.getBytes("promotionalPicture");
                    userPromotionMessage.promotionalPictureName = rs.getString("promotionalPictureName");
                }
            }

            if (!cs.getMoreResults())
                userPromotionMessage.result = cs.getInt("result");


            return userPromotionMessage;
        } catch (SQLException e) {

            System.err.println("SQLException: " + e.getMessage());
        } finally {
            if (cs != null) {
                /*
                try {
                    cs.close();
                } catch (SQLException e) {
                    System.err.println("SQLException: " + e.getMessage());
                }
                */
            }


        }
        return userPromotionMessage;
    }

    /**
     * Created by edgar Molibna on 29/08/2015.
     * Obtiene la ruta de un bus dependiendo del código del bus
     *
     * @param routeName Nombre de la ruta
     */
    public ResultSet GetRouteDetails(String routeName) {
        Connection con = _conn;
        CallableStatement cs = null;
        ResultSet rs = null;

        try {
            if (_conn == null) return null;

            cs = con.prepareCall("{call p_GetRouteDetails(?)}");
            cs.setString("busRouteName", routeName);
            cs.execute();

            return cs.getResultSet();

        } catch (SQLException e) {

            System.err.println("SQLException: " + e.getMessage());
        } finally {
            if (cs != null) {

            }
        }

        return rs;
    }

    /**
     * Created by edgar Molibna on 29/08/2015.
     * Obtiene una lista con los puntos de interes
     */
    public ArrayList<InterestingSite> GetInterestingSites() {
        Connection con = _conn;
        CallableStatement cs = null;
        ResultSet rs = null;
        ArrayList<InterestingSite> interestingSites = new ArrayList<>();

        try {
            if (con == null) return null;

            cs = con.prepareCall("call p_GetInterestingSites()");

            boolean results = cs.execute();
            int rowsAffected = 0;

            // Protects against lack of SET NOCOUNT in stored prodedure
            while (results || rowsAffected != -1) {
                if (results) {
                    rs = cs.getResultSet();
                    break;
                } else {
                    rowsAffected = cs.getUpdateCount();
                }
                results = cs.getMoreResults();
            }

            while (rs.next()) {
                InterestingSite interestingSite = new InterestingSite(rs.getString("siteName"), rs.getDouble("lat"), rs.getDouble("lon"));

                interestingSites.add(interestingSite);
            }

        } catch (SQLException e) {

            System.err.println("SQLException: " + e.getMessage());
        } finally {
            if (cs != null) {
                /*
                try {
                   cs.close();
                } catch (SQLException e) {
                    System.err.println("SQLException: " + e.getMessage());
                }
                */
            }
        }

        return interestingSites;
    }

    public ArrayList<MediaFile> GetPlayList() {
        Connection con = _conn;
        CallableStatement cs = null;
        ResultSet rs = null;
        ArrayList<MediaFile> mediaFiles = new ArrayList<>();

        try {
            if (con == null) return null;

            cs = con.prepareCall("call GetMediaFiles()");

            boolean results = cs.execute();
            int rowsAffected = 0;

            // Protects against lack of SET NOCOUNT in stored prodedure
            while (results || rowsAffected != -1) {
                if (results) {
                    rs = cs.getResultSet();
                    break;
                } else {
                    rowsAffected = cs.getUpdateCount();
                }
                results = cs.getMoreResults();
            }

            while (rs.next()) {
                MediaFile mediaFile = new MediaFile(rs.getInt("id"), rs.getString("name"));

                mediaFiles.add(mediaFile);
            }

        } catch (SQLException e) {

            System.err.println("SQLException: " + e.getMessage());
        } finally {
            if (cs != null) {
                /*
                try {
                   cs.close();
                } catch (SQLException e) {
                    System.err.println("SQLException: " + e.getMessage());
                }
                */
            }
        }

        return mediaFiles;
    }

    public void AddMediaPlaying(int idMediaFile, String codeTablet, java.sql.Timestamp playingDate) {
        Connection con = _conn;
        CallableStatement cs = null;

        try {

            cs = con.prepareCall("call AddMediaPlaying(?, ?, ?)");

            cs.setInt("idMediaFile", idMediaFile);
            cs.setString("codeTablet", codeTablet);
            cs.setTimestamp("recordDate",playingDate);

            cs.execute();

        } catch (SQLException e) {

            System.err.println("SQLException: " + e.getMessage());
        } finally {
            if (cs != null) {
                /*
                try {
                    cs.close();
                } catch (SQLException e) {
                    System.err.println("SQLException: " + e.getMessage());
                }
                */
            }
        }

    }

}
