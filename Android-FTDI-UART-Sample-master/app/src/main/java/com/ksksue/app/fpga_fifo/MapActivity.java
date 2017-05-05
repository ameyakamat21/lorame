package com.ksksue.app.fpga_fifo;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ksksue.app.ftdi_uart.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location selfLocation;
    private List<Location> locList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        locList = new ArrayList<Location>();
        Intent intent = getIntent();
        for(String key : intent.getExtras().keySet()) {
            Location currLoc = intent.getParcelableExtra(key);
            locList.add(currLoc);
        }

        selfLocation = (Location) intent.getParcelableExtra(
                MainActivity.SELF_LOCATION_KEY);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(selfLocation == null) {
            return;

        }

        for(Location currLoc : locList) {
            LatLng currLatLng =
                    new LatLng(currLoc.getLatitude(), currLoc.getLongitude());
            mMap.addMarker(new MarkerOptions()
                .position(currLatLng)
                .title("" + Math.random())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }

        LatLng selfLatLng = new LatLng(selfLocation.getLatitude(),
                        selfLocation.getLongitude());
//        mMap.addMarker(new MarkerOptions()
//                .position(selfLatLng)
//                .title("Myself")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(selfLatLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo((float) 15.0));

    }
}
