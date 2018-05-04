package com.mst.karsac.connections;

import java.io.Serializable;

public class Mode implements Serializable{

    public String mode;
    public String lat_lon;
    public int radius;

    public Mode() {
    }

    public Mode(String mode, String lat_lon, int radius) {
        this.mode = mode;
        this.lat_lon = lat_lon;
        this.radius = radius;
    }

    public Mode(String mode) {
        this.mode = mode;
    }
}
