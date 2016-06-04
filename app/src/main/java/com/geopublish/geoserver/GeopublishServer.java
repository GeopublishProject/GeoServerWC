package com.geopublish.geoserver;

import android.app.Application;
import android.os.Environment;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by edgar on 11/08/2015.
 */

@ReportsCrashes(
        formUri = "https://geopublish.cloudant.com/acra-geopublish/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin = "aryindestepightendishadm",
        formUriBasicAuthPassword = "14f7df3904d0e066df4ba74d70f009bc19fb919c",
        mode = ReportingInteractionMode.SILENT
)
public class GeopublishServer extends Application {
    public static final String TAG = GeopublishServer.class.getSimpleName();
    public static int ListeningPort = 7000;
    public static int ExecutionDelay = 5000;
    public static boolean AllowPlayAdvertising = true;
    public static boolean AllowEmulatedGPS = false;
    public static boolean AllowMultipleStops = false;
    public static boolean QueryUserStops = true;
    private static GeopublishServer mInstance;
    private String configFilePath;
    private RequestQueue mRequestQueue;

    public GeopublishServer() {
        File root = Environment.getExternalStorageDirectory();

        configFilePath = root.getAbsolutePath() + "/GeoPublishSettings/Server.xml";

        File file = new File(configFilePath);

        if (file.exists()) {
            readXML(configFilePath);
        }
    }

    public static synchronized GeopublishServer getInstance() {
        return mInstance;
    }

    private String getTextValue(Element doc, String tag) {
        String value = null;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);

        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }

        return value;
    }

    /**
     *
     * @param fileName
     */
    public void readXML(String fileName) {
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            File file = new File(fileName);
            InputStream is = new FileInputStream(file.getPath());
            Document doc = db.parse(new InputSource(is));
            doc.getDocumentElement().normalize();

            Element docElement = doc.getDocumentElement();

            ExecutionDelay = Integer.parseInt(getTextValue(docElement, "ExecutionDelay"));
            ListeningPort = Integer.parseInt(getTextValue(docElement, "ServerPort"));
            AllowPlayAdvertising = Integer.parseInt(getTextValue(docElement, "AllowPlayAdvertising")) != 0;
            AllowEmulatedGPS = Integer.parseInt(getTextValue(docElement, "AllowEmulatedGPS")) != 0;
            AllowMultipleStops = Integer.parseInt(getTextValue(docElement, "AllowMultipleStops")) != 0;
            QueryUserStops = Integer.parseInt(getTextValue(docElement, "QueryUserStops")) != 0;

        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        mInstance = this;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
