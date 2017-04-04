package com.cmu.ece.build18.firstapp;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.analytics.Logger;

//google play services imports
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
import java.util.List;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    public static final String STATE_LATITUDE = "STATE_LAT";
    public static final String STATE_LONGITUDE = "STATE_LON";
    private GoogleApiClient mGoogleApiClient;
    private double currLatitude, currLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize google play services interface

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if(savedInstanceState != null) {
            currLatitude = savedInstanceState.getDouble(STATE_LATITUDE);
            currLongitude = savedInstanceState.getDouble(STATE_LONGITUDE);
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
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

    private void showToast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
    }

    public void sendUartMessage(View view) {

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

// Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

// Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            byte buffer[] = new byte[16];
            int numBytesRead = port.read(buffer, 1000);
            Log.d("LOG", "Read " + numBytesRead + " bytes.");
        } catch (IOException e) {
            // Deal with error.
        } finally {
            try {
                port.close();
            } catch (IOException e) {
                Log.e("ERROR", "Got IOException on port.close(): " + e.getMessage());
            }
        }
    }

    public void getLocationBtnCallback(View view) {

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        Log.e("LOG", "mGoogleApiClient.isConnected(): " + mGoogleApiClient.isConnected());

        if (mLastLocation != null) {
            currLatitude = mLastLocation.getLatitude();
            currLongitude = mLastLocation.getLongitude();
            showToast("Last loc: (" + currLatitude + ", " + currLongitude + ")");
        } else  {
            showToast("Last location was null.");
            Log.e("LOCERR", "!!!!!!!! Last location was null.");
        }
        //showToast("(" + currLatitude + ", " + currLongitude + ")");
    }

}
