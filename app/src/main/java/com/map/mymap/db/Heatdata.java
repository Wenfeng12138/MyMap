package com.map.mymap.db;

import org.litepal.crud.DataSupport;

import java.security.Timestamp;

public class Heatdata extends DataSupport {

    private String timestamp;
    private Double latlng;
    private Double longitude;

    public Heatdata(String timestamp , Double latlng , Double longitude){
        this.timestamp = timestamp;
        this.latlng = latlng;
        this.longitude = longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatlng() {
        return latlng;
    }

    public void setLatlng(Double latlng) {
        this.latlng = latlng;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
