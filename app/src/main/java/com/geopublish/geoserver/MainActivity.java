package com.geopublish.geoserver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.geopublish.geoserver.db.model.MediaFile;
import com.geopublish.geoserver.model.UserMessage;
import com.geopublish.geoserver.db.model.GeoServerDataSource;
import com.geopublish.geoserver.db.model.InterestingSite;
import com.geopublish.geoserver.db.model.SQLServerConnection;
import com.geopublish.geoserver.db.model.UserStopData;
import com.geopublish.geoserver.geometry.GeoUTMConverter;
import com.geopublish.geoserver.gps.emulator.GPSEmulatorListener;
import com.geopublish.geoserver.gps.emulator.GpxParser;
import com.geopublish.geoserver.maps.Utils;
import com.geopublish.geoserver.model.RegisterResult;
import com.geopublish.geoserver.model.StopsRemover;
import com.geopublish.geoserver.model.UpdatePositionResult;
import com.geopublish.geoserver.model.UserMarker;
import com.geopublish.geoserver.routing.BusRoute;
import com.geopublish.geoserver.routing.BusRoutePart;
import com.geopublish.geoserver.utils.StringHelper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import serialization.MessageData;
import serialization.PromoMessage;
import serialization.UserPromotionMessage;

public class MainActivity extends ActionBarActivity implements LocationListener, GPSEmulatorListener {

    protected LocationManager locationManager;
    //********************************************
    private ServerSocket serverSocket;
    private Handler updateConversationHandler;
    private Thread serverThread = null;
    private SQLServerConnection conn;
    private TextToSpeech tts;
    private MapView mapView;
    private TileCache tileCache;
    private TileRendererLayer tileRendererLayer;
    private Marker marker;
    private ObjectOutputStream oos;

    //Video
    private VideoView videoView;
    private MediaController mediaControls;
    private int index = -1;
    //7private File extStore;
    private List<File> files;
    private Marker busMarker;
    private File externalSdCard;
    private String VIDEO_PATH = "/storage/extSdCard/Videos/";
    private File extStore;
    private double lastLatitude;
    private double lastLongitude;
    private Boolean locationChangedDetected = false;
    private DataOutputStream dataOutputStream;

    private GeoUTMConverter utmConverter;
    private BusRoute busRoute;
    private ArrayList<Coordinate> busRoutePart1;
    private ArrayList<Coordinate> busRoutePart2;
    private int startReadingPoint = 1700;
    private ArrayList<UserStopData> usersStops;
    private GeometryFactory fact;
    private double lastX;
    private double lastY;
    private double tempLatitude;
    private double tempLongitude;
    private String downloadPath;
    private int playingIndex=0;
    private ArrayList<MediaFile> playList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Linea necesaria para el correcto funcionamiento de MapsForge
        AndroidGraphicFactory.createInstance(getApplication());
        setContentView(R.layout.activity_main);

        utmConverter = new GeoUTMConverter();
        usersStops = new ArrayList<>();
        fact = new GeometryFactory();

        //Inicializamos la conexion a la base de datos al principio para darle algo mas de tiempo mientras se conecta
        //Este sleep es importante para dar tiempo a que se de la conexion con el servidor de base de datos. No quitar

        //TODO: Excelente clase para obtener de un gpx la altitud, velocidad promedio, velocidad maxima, distancia total,
        //TODO: Opcvion para  zoom por defecto normal y amplio
        //TODO: parametro de ruta de bus a usar

        try {
            Thread.sleep(GeopublishServer.ExecutionDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        conn = new SQLServerConnection();

        //Obtenemos el path a la carpeta de download donde se encuentran los archivos de video y las tracks de las rutas
        extStore = Environment.getExternalStorageDirectory();
        downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        //set the media controller buttons
        if (mediaControls == null) {
            mediaControls = new MediaController(MainActivity.this);
        }

        //initialize the VideoView
        videoView = (VideoView) findViewById(R.id.video_view);


        try {
            if (GeopublishServer.AllowPlayAdvertising) {
                playList=conn.GetPlayList();



                        videoView.setVideoURI(Uri.parse(getNextVideo()));
                        videoView.requestFocus();

                        //we also set an setOnPreparedListener in order to know when the video file is ready for playback
                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                            public void onPrepared(MediaPlayer mediaPlayer) {
                                // close the progress bar and play the video
                                videoView.start();
                            }
                        });

                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                conn.AddMediaPlaying(playList.get(index).id, "ABX25", getCurrentDate());
                                videoView.setVideoURI(Uri.parse(getNextVideo()));

                            }
                        });

                        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                Log.e("VideoPlex", "Error playing video");
                                videoView.setVideoURI(Uri.parse(getNextVideo()));

                                //playingIndex++;
                                return true;
                                //TODO: Enviar error
                            }
                        });
                    }


        } catch (Exception e) {
            String message = e.getMessage();

            if (message == null) {
                message = "Error video block";
            }

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }

        //Movility============================================================================================================================

        //Mapa
        this.mapView = (MapView) findViewById(R.id.mapView);

        this.mapView.setClickable(true);
        this.mapView.getMapScaleBar().setVisible(false);
        this.mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
        this.mapView.getMapZoomControls().setZoomLevelMax((byte) 20);

        // create a tile cache of suitable size
        this.tileCache = AndroidUtil.createTileCache(this, "mapcache", mapView.getModel().displayModel.getTileSize(), 1f, this.mapView.getModel().frameBufferModel.getOverdrawFactor());

        //Inicializacion del motor de voz
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    Locale locSpanish = new Locale("spa", "MEX");
                    tts.setLanguage(locSpanish);
                }
            }
        });

        //Ejecucion del servidor
        updateConversationHandler = new Handler();

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
    }

    private static java.sql.Timestamp getCurrentDate() {

        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();

        return new java.sql.Timestamp(now.getTime());
    }


    //Obtiene el proximo archivo a reproducir
    private String getNextVideo() {
        String fileName = "";

        index ++;
        if (index == playList.size()) index =0;
            fileName = playList.get(index).name;
            File f = new File(downloadPath +"/" + fileName);

            if (f.exists())
            {
                return f.getAbsolutePath();
            }


        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        this.mapView.getModel().mapViewPosition.setCenter(new LatLong(10.93436111111111, -74.79205));
        this.mapView.getModel().mapViewPosition.setZoomLevel((byte) 16);

        // tile renderer layer using internal render theme
        MapDataStore mapDataStore = new MapFile(getMapFile());
        this.tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore, this.mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

        // only once a layer is associated with a mapView the rendering starts
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);

        if (conn.getConnection() != null) {
            //Creamos el overlay que almecenará y gestionará nuestros puntos
            // instantiating the paint object
            Paint paint1 = AndroidGraphicFactory.INSTANCE.createPaint();
            Paint paint2 = AndroidGraphicFactory.INSTANCE.createPaint();

            paint1.setColor(Color.BLUE);
            paint1.setStrokeWidth(4);
            paint1.setStyle(Style.STROKE);

            paint2.setColor(Color.RED);
            paint2.setStrokeWidth(4);
            paint2.setStyle(Style.STROKE);

            // instantiating the polyline object
            Polyline polyline1 = new Polyline(paint1, AndroidGraphicFactory.INSTANCE);
            Polyline polyline2 = new Polyline(paint2, AndroidGraphicFactory.INSTANCE);

            // set lat lng for the polyline
            List<LatLong> coordinateList1 = polyline1.getLatLongs();
            List<LatLong> coordinateList2 = polyline2.getLatLongs();

            ArrayList<InterestingSite> interestingSites = null;
            ResultSet rs;

            //Esto debe ir en una sola consulta
            rs = conn.GetRouteDetails("ABX25");
            interestingSites = conn.GetInterestingSites();

            if (rs != null) {
                try {
                    double lat;
                    double lon;
                    int i = 0;
                    int j = 0;
                    int k = 0;

                    GeoServerDataSource geoServerDataSource = new GeoServerDataSource(this);

                    //Eliminamos los puntos anteriores de la ruta
                    geoServerDataSource.DeleteRoutePoints();

                    busRoutePart1 = new ArrayList<Coordinate>();
                    busRoutePart2 = new ArrayList<Coordinate>();

                    while (rs.next()) {

                        lat = rs.getDouble(2);
                        lon = rs.getDouble(3);

                        //Se supone que los datos deben venir ordenados por grupo y orden de punto en la ruta
                        utmConverter.ToUTM(lat, lon);
                        geoServerDataSource.AddRoutePoint(rs.getInt(1), lat, lon, utmConverter.X, utmConverter.Y);

                        Coordinate coordinate = new Coordinate(utmConverter.X, utmConverter.Y);

                        switch (rs.getInt(1)) {
                            case 1:
                                coordinateList1.add(new LatLong(lat, lon));
                                busRoutePart1.add(coordinate);
                                j++;
                                break;

                            case 2:
                                coordinateList2.add(new LatLong(lat, lon));
                                busRoutePart2.add(coordinate);
                                k++;
                                break;
                        }
                        i++;
                    }

                    busRoute = new BusRoute(null, new BusRoutePart(1, busRoutePart1), new BusRoutePart(1, busRoutePart2));

                    // adding the layer to the mapview
                    mapView.getLayerManager().getLayers().add(polyline1);
                    mapView.getLayerManager().getLayers().add(polyline2);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (interestingSites != null) {
                if (interestingSites.size() > 0) {
                    for (int i = 0; i < interestingSites.size(); i++) {
                        org.mapsforge.core.graphics.Bitmap bitmap;

                        bitmap = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.stadium_32));

                        if (interestingSites.get(i).name.startsWith("CEMENTERIO")) {
                            bitmap = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.grave_32));
                        }

                        if (interestingSites.get(i).name.startsWith("SAO")) {
                            bitmap = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.sao_32));
                        }

                        if (interestingSites.get(i).name.startsWith("EXITO")) {
                            bitmap = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.exito_32));
                        }

                        marker = new Marker(new LatLong(interestingSites.get(i).lat, interestingSites.get(i).lon), bitmap, 0, 0);
                        mapView.getLayerManager().getLayers().add(marker);
                    }
                }
            }
        }

        //Lineas agregadas para repitar el mapa
        mapView.postInvalidate();

        tileRendererLayer.requestRedraw();
        mapView.repaint();

        //Inicializacion del GPS
        //Este codigo se ejecuta aqui debido a que toda la ruta debe estar cargada antes de realizar calculos espaciales.
        //La carga de las ruta y dibujado de las misma se ejecuta en OnStart debido a que MapsForge funciona de esta manera
        // y sobre los cual no se tiene control

        if (GeopublishServer.AllowEmulatedGPS) {
            //TODO: Corregir aqui porque el path esta hacia la ruta donde estan los videos y no donde esta el track del gps
            //Verificar si existe archivo. NO existe mostrar mensaje
            File busTrack = new File(extStore.getAbsolutePath() + "/Download/", "track_ruta_urbaplaya.gpx");
            FileInputStream fis = null;
            GpxParser gpxParse = null;

            try {
                fis = new FileInputStream(busTrack);

                GpxParser gpxParser = new GpxParser(fis, startReadingPoint);

                gpxParser.addListener(this);
                gpxParser.Start();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (gpxParse != null) {
                        if (gpxParse.executing == false)
                            gpxParse.release();

                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } else {
            //Real GPS - Si no se usa el GPS real entonces no hay problema en el listener
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 15, this);
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0,  this);
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000, 0,  this);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void newPosition(double latitude, double longitude) {
        if ((lastLatitude != latitude) && (lastLongitude != longitude)) {
            lastLatitude = latitude;
            lastLongitude = longitude;
            locationChangedDetected = true;
            getUserProximities(lastLatitude, lastLongitude);
        } else {
            Log.i("INFO", "Duplicated coords. Lat:  " + String.valueOf(lastLatitude) + ", Long: " + String.valueOf(lastLongitude));
        }
    }

    public void getUserProximities(double latitude, double longitude) {
        Log.i("INFO", "Reading point  " + String.valueOf(startReadingPoint));
        startReadingPoint++;
        utmConverter.ToUTM(latitude, longitude);

        if (busRoute != null) {
            //TODO: Aqui primero se calcula la poscion del bus y luego se mira si radialemte hay paradas en un radio de 500 mts. Mirar si
            //se usa la poscion bruta del bus y se traza el radio de 500 mts y si hay paradas en esa area alli si se calcula la posicion del bus. ANALIZAR
            //tambien depende de que demorar mas si obtener las posibles paradas en el area o caluclar la poscion del bus dentro de la ruta

            Coordinate busPosition = busRoute.getBusPositionInRoute(utmConverter.X, utmConverter.Y);

            if (busPosition != null) {
                utmConverter.ToLatLon(busPosition.x, busPosition.y, utmConverter.Zone, GeoUTMConverter.Hemisphere.Northern);

                tempLatitude = utmConverter.Latitude;
                tempLongitude = utmConverter.Longitude;
                lastX = busPosition.x;
                lastY = busPosition.y;
            }

            //if String.valueOf()latitude

            //TODO: El bloque de  codigo siguiente se puede meter en if anterior
            Point point = fact.createPoint(new Coordinate(lastX, lastY));
            Geometry isodecagon = point.buffer(500, 20);
            boolean existsUsers = false;

            for (int i = 0; i < usersStops.size(); i++) {
                if (fact.createPoint(new Coordinate(usersStops.get(i).assignedStopPointX, usersStops.get(i).assignedStopPointY)).within((isodecagon))) {
                    existsUsers = true;
                    break; //solo necesitamos un usuario para consultaren la BD
                }
            }

            UpdatePositionResult updatePositionResult = conn.GetDevicesInRange(tempLatitude, tempLongitude, !existsUsers);

            if (updatePositionResult.getUserMessages() != null) {
                if (updatePositionResult.getUserMessages().size() > 0) {
                    boolean stopFound = false;
                    int userIndex = -1;
                    System.out.println("Cantidad Registros " + updatePositionResult.getUserMessages().size());

                    for (UserMessage userMessage : updatePositionResult.getUserMessages()) {
                        System.out.println("MENSAJE PARA EL USUARIO: " + userMessage.getTextMessage());

                        tts.speak(userMessage.getTextMessage(), TextToSpeech.QUEUE_ADD, null);
                        tts.playSilence(500, TextToSpeech.QUEUE_ADD, null);

                        //Con solo un usuario que se encuentre en estado stop ya se puede iniciar el proceso de eliminacion del icono de parada
                        if (userMessage.getStatus().equals("stop")) {
                            sendStopNotification(userMessage.getId());
                            userIndex = searchUser(userMessage.getId());

                            if (userIndex > -1) {
                                //Esta ultima comprobacion aunque util puede no ser suficiente para que sea TjreadSafe
                                if (usersStops.size() > 0)
                                    usersStops.remove(searchUser(userMessage.getId()));

                                stopFound = true;
                            }
                        }
                    }

                    if (stopFound) {
                        //Se ejecuta en un hilo debido a que al itnentar manipular el mapa en el proceso hilo pricnipal se genera error
                        StopsRemover stopsRemover = new StopsRemover(mapView.getLayerManager().getLayers());
                        stopsRemover.execute(updatePositionResult.getUserMessages());
                    }
                }
            }

        }

        //Marker del bus en el mapa
        org.mapsforge.core.graphics.Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.bus_24));

        if (busMarker == null) {
            busMarker = new Marker(new LatLong(latitude, longitude), bitmap, 0, 0);
            mapView.getLayerManager().getLayers().add(busMarker);
        }

        //Si se encuentra el marcador entonces este se mueve
        busMarker.setLatLong(new LatLong(latitude, longitude));

        this.mapView.getModel().mapViewPosition.setCenter(new LatLong(latitude, longitude));

        //Lineas agregadas para repitar el mapa
        mapView.postInvalidate();

        tileRendererLayer.requestRedraw();
        mapView.repaint();
    }

    private void addUserStop(String deviceId, double assignedLocationX, double assignedLocationY) {
        int i = searchUser(deviceId);

        if (i > -1) {
            usersStops.get(i).assignedStopPointX = assignedLocationX;
            usersStops.get(i).assignedStopPointY = assignedLocationY;
        } else {
            usersStops.add(new UserStopData(deviceId, assignedLocationX, assignedLocationY));
        }
    }

    private int searchUser(String deviceId) {
        for (int i = 0; i < usersStops.size(); i++) {
            if (usersStops.get(i).deviceId.equals(deviceId)) {

                return i;
            }
        }
        return -1;
    }


    public void sendStopNotification(String deviceId) {
        String url = "http://geopublishproyct-001-site1.btempurl.com/GeopublishService.svc/notifyStopToUser?deviceId=" + deviceId;

        // RequestFuture<JSONObject> future = RequestFuture.newFuture();
        StringRequest req = new StringRequest(url,
                null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // VolleyLog.d(TAG, "Error: " + error.getMessage());

                if (error != null)
                    if (error.getMessage() != null)
                        Log.e("NOTIFICATION ERROR", error.getMessage());
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(5 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        GeopublishServer.getInstance().addToRequestQueue(req);
    }

    @Override
    public void onLocationChanged(Location location) {
        if ((lastLatitude != location.getLatitude()) && (lastLongitude != location.getLongitude())) {
            lastLatitude = location.getLatitude();
            lastLongitude = location.getLongitude();
            locationChangedDetected = true;
            getUserProximities(lastLatitude, lastLongitude);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void addUserMarker(Context context, String deviceId, String username, double latitude, double longitude, String stopMarkerId) {
        //Obtenemos el bitmap del recurso un bitmap
        Bitmap resourceBitmap = BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(stopMarkerId, "drawable", context.getPackageName()));

        //Volcamos el bitmap obtenido a un bitmap mutable
        Bitmap bitmapView = resourceBitmap.copy(Bitmap.Config.ARGB_8888, true);

        //Creamos el texto
        TextView bubbleView = new TextView(context);

        bubbleView.setDrawingCacheEnabled(true);
        bubbleView.buildDrawingCache();

        bubbleView.setTextColor(Color.WHITE);
        bubbleView.setEllipsize(TextUtils.TruncateAt.END);
        bubbleView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        bubbleView.setPadding(2, 2, 2, 2);
        bubbleView.setTextSize(9);

        if (stopMarkerId.startsWith("call")) {
            bubbleView.setMaxEms(15);
            bubbleView.setMaxLines(2);
            bubbleView.setText(StringHelper.getShortUserName(username, true, 15));
            bubbleView.setMaxWidth(resourceBitmap.getWidth() - 8);  //El ancho del texto no puede ser mayor o igual al de la imagen
        } else {
            bubbleView.setSingleLine();
            bubbleView.setMaxEms(10);
            bubbleView.setText(StringHelper.getShortUserName(username, false, 12));
            bubbleView.setMaxWidth(resourceBitmap.getWidth() - 1);  //El ancho del texto no puede ser mayor o igual al de la imagen

            bubbleView.setBackgroundResource(R.drawable.textview_round);
        }

        Bitmap bitmapText = Utils.viewToAndroidBitmap(context, bubbleView);

        //Procedemos a dibujar el texto
        Canvas canvas = new Canvas(bitmapView);

        if (stopMarkerId.startsWith("call")) {
            canvas.drawBitmap(bitmapText, (bitmapView.getWidth() - bitmapText.getWidth()) / 2, (bitmapView.getHeight() - bitmapText.getHeight() - 25) / 2, null);
        } else {
            canvas.drawBitmap(bitmapText, (bitmapView.getWidth() - bitmapText.getWidth()) / 2, bitmapView.getHeight() - bitmapText.getHeight(), null);
        }

        //Volcamos el bitmap nuevamente a la vista
        bubbleView.setText("");
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmapView);
        Utils.setBackground(bubbleView, bitmapDrawable);

        //Utils.setBackground(bubbleView, Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? getDrawable(R.drawable.balloon_overlay_unfocused) : getResources().getDrawable(R.drawable.balloon_overlay_unfocused));

        //Creamos el buuble bitmap en el formato especifico para nuestro control de mapa
        org.mapsforge.core.graphics.Bitmap bubble = Utils.viewToBitmap(context, bubbleView);

        bubble.incrementRefCount();
        UserMarker userMarker = new UserMarker(new LatLong(latitude, longitude), bubble, 0, -bubble.getHeight() / 2, deviceId);

        mapView.getLayerManager().getLayers().add(userMarker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mapView.destroyAll();
    }

    private File getMapFile() {
        File file = new File(Environment.getExternalStorageDirectory(), "/maps/colombia.map");
        return file;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ServerThread implements Runnable {

        public void run() {

            Socket socket;

            try {
                serverSocket = new ServerSocket(GeopublishServer.ListeningPort);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();

                    if (socket != null) {
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream objectInputStream;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;

            try {
                //dataOutputStream= new DataOutputStream(clientSocket.getOutputStream());
                objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //TODO. Mostrar toast wen el server para mensaje de parada. Hacer por configurator
        public void run() {

            Object objectData;
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    objectData = objectInputStream.readObject();

                    if (objectData instanceof MessageData) {
                        MessageData message = (MessageData) objectData;

                        //Probando instanciar el flujo de salida pr si acaso una condicion de error hizo que este fuera nulo

                        //if (dataOutputStream==null)
                        //{
                        //    dataOutputStream=new DataOutputStream(clientSocket.getOutputStream());
                        //}

                        switch (message.Action) {
                            case 1:
                                int result = conn.RegisterUser(message.deviceId, message.fullName, message.bornDate, message.ocupation, "");

                                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                                dataOutputStream.writeInt(result);
                                dataOutputStream.flush();

                                updateConversationHandler.post(new updateUIThread(message, null));

                                break;

                            case 2:
                                if (locationChangedDetected) {
                                    RegisterResult registerResult = conn.RegisterStop(tempLatitude, tempLongitude, message.deviceId, message.deviceToken, message.fullName, message.direction1 + ' ' + message.directionNumber1, message.direction2 + ' ' + message.directionNumber2, message.distance, message.stopMarkerId);

                                    //Si no se pudo registrar la parada entonces el resultado es nulo por eso es necesario la comprobacion
                                    //TODO: PARA TODOS LAS CONSULTAS DE BASE DE DATOS DEBE ESTABLECERSE EL REGISTERRESULT O CREARSE UN OBJETO DE ESTE TIPO PARA INDICAR QUE HUBO ERROR EN LA TRANSACCION
                                    if (registerResult != null) {

                                        //final GeoServerDataSource geoServerDataSource = new GeoServerDataSource(MainActivity.this);

                                        //geoServerDataSource.RegisterUserStop(message.deviceId, lastLatitude, lastLongitude);

                                        GeoUTMConverter geoUTMConverter = new GeoUTMConverter();
                                        geoUTMConverter.ToUTM(registerResult.latitude, registerResult.longitude);
                                        addUserStop(message.deviceId, geoUTMConverter.X, geoUTMConverter.Y);

                                        dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                                        dataOutputStream.writeInt(registerResult.resultCode);
                                        dataOutputStream.flush();

                                        updateConversationHandler.post(new updateUIThread(message, registerResult));
                                    }
                                } else {
                                    dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                                    //el codigo 50 hace referencia a que no se detecto posicion gps necesaria para registrar conocer donde se encuentra el bus y asi establecer correctamente la parada
                                    dataOutputStream.writeInt(50);
                                    dataOutputStream.flush();
                                }

                                //dataOutputStream.flush();

                                break;
                        }
                    }

                    if (objectData instanceof PromoMessage) {
                        PromoMessage promoMessage = (PromoMessage) objectData;

                        UserPromotionMessage userPromotionMessage = conn.AddDevicePromo(promoMessage.deviceId, promoMessage.code);

                        oos = new ObjectOutputStream(clientSocket.getOutputStream());

                        oos.writeObject(userPromotionMessage);
                        oos.flush();
                    }
                } catch (ClassNotFoundException e) {
                    Log.i("Snd_Msg() ERROR -> ", "" + "ClassNotFoundException");
                    //e.printStackTrace();
                } catch (IOException e) {
                    Log.i("Snd_Msg() ERROR -> ", "" + e.getMessage());
                    //e.printStackTrace();
                }
            }
        }
    }

    class updateUIThread implements Runnable {
        private MessageData msg;
        private RegisterResult registerResult;

        public updateUIThread(MessageData messageData, RegisterResult registerResult) {
            this.msg = messageData;
            this.registerResult = registerResult;
            //text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
        }

        @Override
        public void run() {

            if (registerResult != null) {
                if (registerResult.resultCode == 300 || registerResult.resultCode == 400) {
                    Layers layers = mapView.getLayerManager().getLayers();

                    if (layers != null) {
                        for (int i = 0; i < layers.size(); i++) {
                            if (layers.get(i) instanceof UserMarker) {
                                if (((UserMarker) layers.get(i)).deviceId.equals(msg.deviceId)) {
                                    layers.remove(i);
                                    //TODO: agregar un break o salida de todos los cilos
                                }
                            }
                        }

                        addUserMarker(MainActivity.this, msg.deviceId, msg.fullName, registerResult.latitude, registerResult.longitude, msg.stopMarkerId);
                    }
                }
            }
        }
    }
}
