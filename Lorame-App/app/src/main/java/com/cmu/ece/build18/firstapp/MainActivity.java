package com.cmu.ece.build18.firstapp;

import android.Manifest;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.content.Context;
import android.widget.Toast;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.CompoundButton;

import com.google.android.gms.analytics.Logger;

//google play services imports
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.GoogleApiAvailability;
import android.location.Location;
import android.util.Log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import android.hardware.usb.UsbManager;
import com.hoho.android.usbserial.driver.UsbSerialDriver;

import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.driver.UsbSerialPort;


import android.hardware.usb.UsbDeviceConnection;
import android.widget.ToggleButton;

import java.nio.charset.Charset;
import java.util.List;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    public static final String STATE_LATITUDE = "STATE_LAT";
    public static final String STATE_LONGITUDE = "STATE_LON";
    private GoogleApiClient mGoogleApiClient;
    private double currLatitude, currLongitude;
    private TextView textView;
    private UsbSerialDriver activeUsbDriver;
    private UsbDeviceConnection activeUsbDeviceConnection;
    private UsbSerialPort activeUsbDevicePort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        ToggleButton connectBtn = (ToggleButton) findViewById(R.id.usb_switch);

        connectBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    if(!initializeUsbDeviceConnection()) {
                        buttonView.setChecked(false);
                    }

                } else {
                    powerOffUsbConnection();
                    Log.e("TAG", "Connection disabled");
                }
            }
        });

        //initialize google play services interface

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API).build();

        if (savedInstanceState != null) {
            currLatitude = savedInstanceState.getDouble(STATE_LATITUDE);
            currLongitude = savedInstanceState.getDouble(STATE_LONGITUDE);
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        getAndDisplayLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e("LOCERR", "!!!!!!!! The connection has failed!: " + result);
    }

    @Override
    public void onConnectionSuspended(int someInt) {
        Log.e("LOCERR", " !!!!!!!!  The connection was suspended.");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putDouble(STATE_LATITUDE, currLatitude);
        savedInstanceState.putDouble(STATE_LONGITUDE, currLongitude);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        for(int i=0; i<grantResults.length; i++) {
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                showToast("Did not get permission for " + permissions[i] + ":(");
                return;
            }
        }

        showToast("Got all permissions!");
    }


    private void getAndDisplayLocation() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.e("PERMISSION", "Does not have location permission!");
            showToast("Does not have location permission!");


            String[] permisionsToRequest = {Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permisionsToRequest, 1);
        }

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            currLatitude = mLastLocation.getLatitude();
            currLongitude = mLastLocation.getLongitude();
            showToast("Last loc: (" + currLatitude + ", " + currLongitude + ")");
        } else  {
            showToast("Last location was null.");
        }
    }

    private void showToast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }

    public boolean initializeUsbDeviceConnection() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        UsbSerialProber dflProber = UsbSerialProber.getDefaultProber();

        List<UsbSerialDriver> availableDrivers = dflProber.findAllDrivers(manager);
        if(availableDrivers == null) {
            Log.e("DRIVER", "Available drivers list is null.");
            showToast("Available drivers list is null.");
        } else if (availableDrivers.isEmpty()) {
            Log.e("DRIVER", "Available drivers list is empty...");
            showToast("Available driver list is empty..");
            return false;
        }

        for(UsbSerialDriver availableDriver : availableDrivers) {
            showToast(availableDriver.toString());
            Log.e("DRIVER", "Found driver: " + availableDriver);
        }

// Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());

        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            showToast("UsbDeviceConnection was null.");
            return false;
        }

        this.activeUsbDriver = driver;
        this.activeUsbDeviceConnection = connection;
        this.activeUsbDevicePort = activeUsbDriver.getPorts().get(0);

        try {
            this.activeUsbDevicePort.open(this.activeUsbDeviceConnection);
            this.activeUsbDevicePort.setParameters(115200, 8,
                    UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        } catch(IOException e) {
            showToast("Exception opening port: " + e.getMessage());
        }
        //showToast("Connection established with driver.");
        return true;
    }

    public boolean powerOffUsbConnection() {
        try {
            this.activeUsbDevicePort.close();
        } catch (IOException e) {
            showToast("Error closing port: " + e.getMessage());
            Log.e("ERROR", "Got IOException on port.close(): " + e.getMessage());
            return false;
        }

        return true;
    }

    public void sendUartMessage(View view) {

        if(this.activeUsbDriver == null || this.activeUsbDeviceConnection == null) {
            if(!initializeUsbDeviceConnection()) {
                showToast("Driver or connection is null. Aborting.");
                return;
            }
        }

        if(this.activeUsbDriver == null || this.activeUsbDeviceConnection == null) {
            showToast("Driver or connection is null. Aborting.");
            return;
        }

        try {

            byte []sendBuf = "recvc".getBytes(Charset.forName("UTF-8"));

            this.activeUsbDevicePort.write(sendBuf, 1000);
            textView.append("\n Sent:" + "recvc");

        } catch (IOException e) {
            // Deal with error.
            showToast("Error in port operation: " + e.getMessage());
        }
    }


    public void receiveUartMessage(View view) {

        if(this.activeUsbDriver == null || this.activeUsbDeviceConnection == null) {
            if(!initializeUsbDeviceConnection()) {
                showToast("Driver or connection is null. Aborting.");
                return;
            }
        }

        if(this.activeUsbDriver == null || this.activeUsbDeviceConnection == null) {
            showToast("Driver or connection is null. Aborting.");
            return;
        }

        try {

            byte buffer[] = new byte[1000];

            //read with timeout = 1000ms
            int numBytesRead = this.activeUsbDevicePort.read(buffer, 1000);

            textView.append("\n Received: " + new String(buffer, "UTF-8"));
            Log.d("LOG", "Read " + numBytesRead + " bytes.");
        } catch (IOException e) {
            // Deal with error.
            showToast("Error in port operation: " + e.getMessage());
        }
    }

    public void getLocationBtnCallback(View view) {

       getAndDisplayLocation();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
}
