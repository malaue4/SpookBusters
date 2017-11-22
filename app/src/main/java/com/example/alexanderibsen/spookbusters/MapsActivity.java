package com.example.alexanderibsen.spookbusters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexanderibsen.spookbusters.Objects.Ghost;
import com.example.alexanderibsen.spookbusters.Objects.GhostSimple;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.support.v4.app.ActivityCompat.requestPermissions;
import static android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Gson gson = new Gson();
    LocationManager locationManager;
    private static final int MY_PERMISSIONS_FINE_LOC = 1;
    Button btnGhostCam;
    TextView txtSeek;
    TextView textView;
    Random r = new Random();
    double meterDegree = 0.0000089; //A meter in degrees. Used to move around Lat and Long.
    String ghostsJson;
    List<GhostSimple> ghostInRange;

    //Player
    Location playerLoc;
    LatLng playerLocMaps;
    Marker playerMarker;

    //Ghost
    List<Ghost> ghosts;
    Boolean ghostSpawned = false;
    int ghostSpawnDiameter = 20;
    private GoogleMap mMap;

    //Icons
    BitmapDescriptor iconGhosts;
    BitmapDescriptor iconPlayer;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        iconGhosts = BitmapDescriptorFactory.fromResource(R.drawable.ghost2);
        iconPlayer = BitmapDescriptorFactory.fromResource(R.drawable.gbicon2);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        ghostsJson = preferences.getString("ghostsJson", "");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnGhostCam = (Button) findViewById(R.id.btnGhostCam);
        txtSeek = (TextView) findViewById(R.id.txtSeekGhosts);
        btnGhostCam.setVisibility(View.INVISIBLE);
        txtSeek.setVisibility(View.VISIBLE);
        txtSeek.setText("Searching for PARANORMAL ACTIVITY...");
        ghostInRange = new ArrayList<>();
        ghosts = new ArrayList<>();

        //Location Service
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!hasFineLocPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestFineLocPermission();
            } else {
                Toast.makeText(this, "This app needs camera permission", Toast.LENGTH_SHORT).show();
            }
        } else {
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, locationListener);
        }

        if(getIntent().hasExtra("FromGhostCam")){
            updatePlayerLoc(locationManager.getLastKnownLocation(NETWORK_PROVIDER));
        }

        final Handler handler = new Handler();
        class MyRunnable implements Runnable {
            private Handler handler;

            public MyRunnable(Handler handler) {
                this.handler = handler;
            }

            @Override
            public void run() {
                this.handler.postDelayed(this, 50);
                moveGhosts();
                if(ghosts.size() < 3){
                    generateGhost(playerLoc, ghostSpawnDiameter);
                }
                for (Ghost g: ghosts) {
                    if(g.location.distanceTo(playerLoc) > 30){

                    }
                }
            }
        }
        handler.post(new MyRunnable(handler));
    }

    private boolean hasFineLocPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

    }

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            playerLoc = location;
            if (!ghostSpawned){
                if(getIntent().hasExtra("FromGhostCam")) {
                    GhostSimple[] transferedGhosts = gson.fromJson(ghostsJson, GhostSimple[].class);
                    for (GhostSimple g : transferedGhosts) {
                        Location mLoc = new Location(NETWORK_PROVIDER);
                        mLoc.setLatitude(g.lat);
                        mLoc.setLongitude(g.lng);
                        generateGhost(mLoc, 0);

                    }
                    ghostSpawned = true;
                } else {
                    for (int i = 0; i < 4; i++) {
                        generateGhost(location, ghostSpawnDiameter);
                    }
                    ghostSpawned = true;
                }

                txtSeek.setText("SEEK OUT GHOSTS!");

            }
            updatePlayerLoc(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    @RequiresApi(23)
    private void requestFineLocPermission(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, "Need GPS to show ghosts", Toast.LENGTH_LONG).show();
            } else { // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_FINE_LOC);
            }
        }

    }

    private void updatePlayerLoc(Location location){
        playerLocMaps = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playerLocMaps, 20f));
        if(playerMarker == null) {
            MarkerOptions markPlayer = new MarkerOptions().position(playerLocMaps).title("Spookbuster").icon(iconPlayer);
            playerMarker = mMap.addMarker(markPlayer);
        }
        playerMarker.setPosition(playerLocMaps);

    }

    private void generateGhost(Location location, int spawnRange) {

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        if(spawnRange >0) {
            double direction = r.nextDouble()*Math.PI*2;
            double distance = 5 + r.nextDouble()*(spawnRange-5);

            double metersLat = cos(direction)*distance;
            double metersLong = sin(direction)*distance;
            /*
            double metersLat = r.nextInt(spawnRange) - spawnRange / 2;
            double metersLong = r.nextInt(spawnRange) - spawnRange / 2;
            while (Math.abs(metersLat) < 5 || Math.abs(metersLong) < 5) {
                metersLat = r.nextInt(spawnRange) - spawnRange / 2;
                metersLong = r.nextInt(spawnRange) - spawnRange / 2;
            }*/
            double coefLat = metersLat * meterDegree;  //1 meter i grader rundt om jorden = 0,000008983
            double coefLong = metersLong * meterDegree;
            lat = lat + coefLat;
            lon = lon + coefLong / cos(lat * 0.018);
        }

        Location loc = new Location(NETWORK_PROVIDER);
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        LatLng mapsLoc = new LatLng(lat, lon);
        MarkerOptions mark = new MarkerOptions().position(mapsLoc).title("Ghostie "+(ghosts.size()+1)).icon(iconGhosts);

        Marker marker = mMap.addMarker(mark);
        ghosts.add(new Ghost(ghosts.size()+1,loc, mapsLoc, marker));
    }

    private void moveGhosts() {
        for (Ghost g : ghosts) {
            if (r.nextInt(30) < 1) {
                g.changeDirection();
            }
            g.moveGhost();
        }
        ghostInRange.clear();
        for (Ghost g : ghosts) {
            if (playerLoc.distanceTo(g.location) < 10) {
                ghostInRange.add(new GhostSimple(g.ID,g.location.getLatitude()-playerLoc.getLatitude(),g.location.getLongitude()-playerLoc.getLongitude()));
            }
        }
        if(ghostInRange.isEmpty()){
            txtSeek.setVisibility(View.VISIBLE);
            btnGhostCam.setVisibility(View.INVISIBLE);
        } else {
            txtSeek.setVisibility(View.INVISIBLE);
            btnGhostCam.setVisibility(View.VISIBLE);
        }
    }

    public void switchGhostCam(View view){
        Intent intent = new Intent(this, GhostCamActivity.class).putExtra("ghostData", gson.toJson(ghostInRange));
        startActivity(intent);
    }

    public void centerCamera(View view){
        if(playerLocMaps != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playerLocMaps, 20f));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        List<GhostSimple> transferGhosts = new ArrayList<>();
        for(Ghost g : ghosts){
            transferGhosts.add(new GhostSimple(g.ID,g.location.getLatitude(),g.location.getLongitude()));
        }
        ghostsJson = gson.toJson(transferGhosts);
        Log.d("GHOST", ghostsJson);
        if(ghostsJson.length() > 4) {
            editor.putString("ghostsJson", ghostsJson);
            editor.apply();
        } else {
            editor.putString("ghostsJson", "");
            editor.apply();
        }
    }
}


