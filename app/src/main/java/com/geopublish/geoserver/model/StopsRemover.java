package com.geopublish.geoserver.model;

import android.os.AsyncTask;

import org.mapsforge.map.layer.Layers;

import java.util.List;

/**
 * Created by edgar on 9/21/2015.
 * Clase encargada de quitar los iconos de las paradas de usuario
 */
public class StopsRemover extends AsyncTask<List<UserMessage>, Void, List<UserMessage>> {
    private Exception thrownException;
    private Layers layers;

    public StopsRemover(Layers layers) {
        this.layers = layers;
    }

    @Override
    protected List<UserMessage> doInBackground(List<UserMessage>... data) {
        //Esperamos 15 segundos despues de anunciada la parada y simplemente pasamos a onPostExecute los datos para borrar los puntos
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return data[0];
    }


    @Override
    public void onPostExecute(List<UserMessage> userMessages) {
        if (thrownException == null)
            for (UserMessage userMessage : userMessages) {
                System.out.println("MENSAJE PARA EL USUARIO: " + userMessage.getTextMessage());

                if (userMessage.getStatus().equals("stop")) {
                    for (int i = 0; i < layers.size(); i++) {
                        try
                        {

                        //TODO: Esto se tiene que mejorar y quitar el try catch
                        //Por si acaso hay varios iconos que eliminar y la lista de iconos va cambiando esto garantiza
                        // que al intentar obtener un indic y este no se encuentra entonces no tumba al server

                            if (layers.get(i) instanceof UserMarker) {
                                if (((UserMarker) layers.get(i)).deviceId.equals(userMessage.getId())) {
                                    layers.remove(i);
                                    //TODO: agregar un break o salida de todos los cclos
                                    System.out.println("Marcador eliminado ---------------------------");
                                }
                            }
                        }
                        catch (Exception ex)
                        {

                        }

                    }
                }
            }
    }

}
