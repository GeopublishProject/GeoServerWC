package com.geopublish.geoserver.utils;

/**
 * Created by edgar on 1/15/2016.
 * Contiene varios metodos para el manejo de cadenas de texto.
 * Estos metodos son especificos para el sistema GeoPublish y no son usados para procesos de proposito general en el manejo de cadenas
 */
public class StringHelper {

    /**
     * Obtiene el nombre reducido del usuario. la importancia de este metodo radica es que
     * el nombre de uusario se presenta en pantalla y es reproducido por el sistema de voz
     * TTS por lo que trabajar con nombres largos representaria muchos problemas
     *
     * @param userName    Nombre de usuario
     * @param allowSpaces Valor booleano que indica si se permiten espacios en el nombre obtenido
     * @param maxChars    Cantidad maxima de carcteres que puede tener el nombre de usuario aun recortandolo por los espacios en blanco
     * @return Cadena de texto con el nomnbre de usuario recortado
     */
    public static String getShortUserName(String userName, boolean allowSpaces, int maxChars) {
        int blankPosition = userName.indexOf(" ");

        if (!allowSpaces)
            if (blankPosition > -1)
                userName = userName.substring(0, blankPosition);

        if (userName.length() > maxChars)
            userName = userName.substring(0, maxChars);

        return userName;
    }
}
