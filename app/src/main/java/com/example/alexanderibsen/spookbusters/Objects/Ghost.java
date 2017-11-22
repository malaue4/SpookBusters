package com.example.alexanderibsen.spookbusters.Objects;


import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Random;

public class Ghost {
    public int ID;
    Random r = new Random();
    double meterDegree = 0.0000089;
    public Location location;
    Marker marker;
    LatLng mapsLocation;
    float ghostMoveSpeed = 0.2f;
    //MoveDirection
    double metersLat = 0.1f;
    double metersLong = 0.1f;

    public Ghost(int id,Location l, LatLng ml, Marker m){
        ID = id;
        location = l;
        marker = m;
        mapsLocation = ml;
    }

    public void changeDirection(){
        metersLat =  r.nextFloat()*ghostMoveSpeed-ghostMoveSpeed/2;
        metersLong =  r.nextFloat()*ghostMoveSpeed-ghostMoveSpeed/2;
    }

    public void moveGhost(){
        location.setLatitude(location.getLatitude()+metersLat*meterDegree);
        location.setLongitude(location.getLongitude()+metersLong*meterDegree);
        marker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));

    }

}