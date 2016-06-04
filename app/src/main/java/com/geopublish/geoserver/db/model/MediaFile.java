package com.geopublish.geoserver.db.model;

/**
 * Created by edgar on 5/4/2016.
 */
public class MediaFile {
    public int id;
    public String name;

    public MediaFile(int id,String name)
    {
        this.id=id;
        this.name=name;
    }
}
