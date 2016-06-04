package com.geopublish.geoserver.model;

/**
 * Created by edgar on 02/09/2015.
 */
public class UserMessage {
    private String _id;
    private String _type;
    private String _userName;
    private double _distance ;

    public UserMessage(String id,String type, String userName, double distance)
    {
        this._id=id;
        this._type=    type;
        this._userName=userName;
        this._distance= distance;
    }

    public String getId()
    {
        return this._id;
    }

    public String getStatus()
    {
        return this._type;
    }

    /**
     * Obtiene el texto de anuncio de parada
     * @return Cadena de texto con el anuncio de parada
     */
    public String getTextMessage()
    {
        String message="";
        String tempUserName=_userName;
        int blankPosition= tempUserName.indexOf(" ");

        if (blankPosition>-1)
            tempUserName= tempUserName.substring(0,blankPosition);

        if (tempUserName.length()>20)
            tempUserName=tempUserName.substring(0,20);

        if (this._type.equals("m1"))
        {
            message = tempUserName + ", ya estas proximo a llegar a tu destino";
        }
        if (_type.equals("stop"))
        {
                //TODO: El mensaje  Gracias por usar nuestro servicio no debe ir para cada usuario . Solo cuando se ejecute el lote
                message = tempUserName + ", ya llegaste a tu destino. Te puedes bajar aqui.";
        }

        return message;
    }
}
