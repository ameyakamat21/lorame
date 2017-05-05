/*
 * Copyright (C) 2013 Keisuke SUZUKI
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * This code is checked by Galaxy S II and FT232RL
 */
package com.ksksue.app.ftdi_uart;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.google.android.gms.common.api.GoogleApiClient;
import com.ksksue.app.fpga_fifo.MapActivity;
import com.ksksue.app.fpga_fifo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity implements
        ConnectionCallbacks, OnConnectionFailedListener {
    private final static String TAG = "FPGA_FIFO Activity";

    private static D2xxManager ftD2xx = null;
    public static String SELF_LOCATION_KEY = "SELF_LOCATION";
    private FT_Device ftDev;

    static final int READBUF_SIZE  = 256;
    byte[] rbuf  = new byte[READBUF_SIZE];
    char[] rchar = new char[READBUF_SIZE];
    int mReadSize=0;
    private GoogleApiClient mGoogleApiClient;
    private double currLatitude, currLongitude;


    TextView tvRead;
    TextView logRead;
    EditText etWrite;
    Button btWrite;
    Button joinBtn;
    Button debuger;
    Spinner spinner;
    ArrayAdapter<String> spinnerDataAdapter;

    boolean mThreadIsStopped = true;
    Handler mHandler = new Handler();
    Thread mThread;

    NetworkManager network = new NetworkManager();
    LoraState state = new LoraState();
    int msgCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize GoogleApiClient instance for location
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API).build();

        tvRead = (TextView) findViewById(R.id.tvRead);
        tvRead.setMovementMethod(new ScrollingMovementMethod());

        logRead = (TextView) findViewById(R.id.logRead);
        logRead.setMovementMethod(new ScrollingMovementMethod());

        etWrite = (EditText) findViewById(R.id.etWrite);

        btWrite = (Button) findViewById(R.id.btWrite);
        debuger = (Button) findViewById(R.id.debug);
        joinBtn = (Button) findViewById(R.id.joinBtn);

        List<String> list = new ArrayList<String>();
        list.add("Everyone");
        spinnerDataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        makeSpinner(spinnerDataAdapter);

        updateView(false);

        try {
            ftD2xx = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException ex) {
            Log.e(TAG,ex.toString());
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

    }

    private void makeSpinner(ArrayAdapter<String> dataAdapter) {
        spinner = (Spinner) findViewById(R.id.spinner);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void onShowMessages(View v) {
        logRead.setVisibility(View.INVISIBLE);
        tvRead.setVisibility(View.VISIBLE);
    }

    public void onShowLog(View v) {
        logRead.setVisibility(View.VISIBLE);
        tvRead.setVisibility(View.INVISIBLE);
    }

    public void onDebug(View v) {
        Toast.makeText(MainActivity.this,
                "State ID: "+ state.id,
                Toast.LENGTH_SHORT).show();
        logRead.append("Another one!\n");
    }

    public void onJoin(View v) {
        String wString = "J\n";
        byte[] writeByte = wString.getBytes();
        ftDev.write(writeByte, wString.length());

    }

    public void onClickWrite(View v) {
        if(ftDev == null) {
            return;
        }

        synchronized (ftDev) {
            if(ftDev.isOpen() == false) {
                Log.e(TAG, "onClickWrite : Device is not open");
                return;
            }

            ftDev.setLatencyTimer((byte)16);

            String id = spinner.getSelectedItem().toString(); //this should be string id
            //TODO network manager get path to selected node. Send to first node in path
            ForwardMessage fm = new ForwardMessage();
            fm.ids = new LinkedList<>();
            fm.message = etWrite.getText().toString();
            SerialPayload sp = new SerialPayload();
            sp.putForwardMessage(fm);
            String wString = "S " +id+ " "+ sp.toString() + "\n";
            byte[] writeByte = wString.getBytes();
            ftDev.write(writeByte, wString.length());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }

//    public void onClickRecv(View v) {
//        if(ftDev == null) {
//            return;
//        }
//
//        synchronized (ftDev) {
//            if(ftDev.isOpen() == false) {
//                Log.e(TAG, "onClickWrite : Device is not open");
//                return;
//            }
//
//            ftDev.setLatencyTimer((byte)16);
//
//            String writeString = "recva\n";
//            byte[] writeByte = writeString.getBytes();
//            ftDev.write(writeByte, writeString.length());
//        }
//    }

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
        Log.e("LOCERR", "The connection has failed!: " + result);
    }

    @Override
    public void onConnectionSuspended(int someInt) {
        Log.e("LOCERR", "The connection was suspended.");
    }

    public Location getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            Log.e("PERMISSION", "Does not have location permission!");

            String[] permisionsToRequest = {Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permisionsToRequest, 1);
        }

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        return mLastLocation;
    }

    private void getAndDisplayLocation() {

        Location mLastLocation = getLocation();

        if (mLastLocation != null) {
            currLatitude = mLastLocation.getLatitude();
            currLongitude = mLastLocation.getLongitude();
            showToast("Last loc: (" + currLatitude + ", " + currLongitude + ")");
        } else {
            showToast("Last location was null.");
        }
    }

    public void getLocationBtnCallback(View view) {

        getAndDisplayLocation();
    }

    private void showToast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();
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

//    public void onClickClose(View v) {
//        closeDevice();
//    }


//    public void clearTv(View view) {
//
//        tvRead.setText("");
//
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThreadIsStopped = true;
        unregisterReceiver(mUsbReceiver);
    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
*/

    private void openDevice() {
        if(ftDev != null) {
            if(ftDev.isOpen()) {
                if(mThreadIsStopped) {
                    updateView(true);
                    SetConfig(115200, (byte)8, (byte)1, (byte)0, (byte)0);
                    ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                    ftDev.restartInTask();
                    new Thread(mLoop).start();
                }
                //Try to send join?
                String wString = "J\n";
                byte[] writeByte = wString.getBytes();
                ftDev.write(writeByte, wString.length());
                return;
            }
        }

        int devCount = 0;
        devCount = ftD2xx.createDeviceInfoList(this);

        Log.d(TAG, "Device number : "+ Integer.toString(devCount));

        D2xxManager.FtDeviceInfoListNode[] deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
        ftD2xx.getDeviceInfoList(devCount, deviceList);

        if(devCount <= 0) {
            return;
        }

        if(ftDev == null) {
            ftDev = ftD2xx.openByIndex(this, 0);
        } else {
            synchronized (ftDev) {
                ftDev = ftD2xx.openByIndex(this, 0);
            }
        }

        if(ftDev.isOpen()) {
            if(mThreadIsStopped) {
                updateView(true);
                SetConfig(115200, (byte)8, (byte)1, (byte)0, (byte)0);
                ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                ftDev.restartInTask();
                new Thread(mLoop).start();
            }
        }
    }

    private Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            int i;
            int readSize;
            StringBuilder sb = new StringBuilder(READBUF_SIZE);
            mThreadIsStopped = false;
            while(true) {
                if(mThreadIsStopped) {
                    break;
                }

/*                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
*/
                synchronized (ftDev) {
                    readSize = ftDev.getQueueStatus();
                    if(readSize>0) {
                        mReadSize = readSize;
                        if(mReadSize > READBUF_SIZE) {
                            mReadSize = READBUF_SIZE;
                        }
                        int count = ftDev.read(rbuf,mReadSize);

                        // cannot use System.arraycopy
                        for(i=0; i<count; i++) {
                            rchar[i] = (char)rbuf[i];
                            sb.append((char)rbuf[i]);
                            if ((char)rbuf[i]=='\n'){
                                mHandler.post(new UartPoster(sb.toString()));
                                sb = new StringBuilder(READBUF_SIZE);
                            }
                        }

//                        for(i=0; i<mReadSize; i++) {
//                            rchar[i] = (char)rbuf[i];
//                        }



                    } // end of if(readSize>0)
                } // end of synchronized
            }
        }
    };

    public class UartPoster implements Runnable {
        String toSend = "";
        public UartPoster(String toSend){
            this.toSend = toSend;
        }
        @Override
        public void run() {
            logRead.append(toSend);
            handleInputFromFtDev(toSend);

        }
    }

    private void updateSpinnerList(){
        List<String> ids = new ArrayList<String>(this.network.network.keySet());
        Collections.sort(ids);
        spinnerDataAdapter.clear();
        spinnerDataAdapter.addAll(ids);
        spinnerDataAdapter.notifyDataSetChanged();
    }

    private synchronized void handleInputFromFtDev(String copyr) {
        String[] input = copyr.split("\\s+");
        logRead.append(">> " + input.length + " " + input[0] + "\n");
//        logRead.append("Recvd " + (msgCount++) + copyr.toString());

        if(input.length < 1){
            return;
        }
//        if (input.length < 3){
//            if(input.length < 3){
//                Toast.makeText(MainActivity.this,
//                        "Received message len less than len 3: " + copyr,
//                        Toast.LENGTH_SHORT).show();
//            }
//            return;
//        }
        switch (input[0]){
            case "A":
                //Add neighbor
                tvRead.append(copyr.toString());
                this.network.addNeighbor(input[1]);
                updateSpinnerList();
                //we should send something to get the network table of this neighbour

                break;
            case "D":
                //Delete neighbor
                this.network.deleteNeighbor(input[1]);
                updateSpinnerList();
                break;
            case "U":
                logRead.append("WHY ARE WE updating\n");
                this.network.updateNeighbor(input[1], input[2]);
                updateSpinnerList();
                break;
            case "P":
                tvRead.append(copyr.toString());
                //receive a payload
                //Check node id of final receiver, if me, display message, else forward it
                break;
            case "S":
                break;
            case "R":
                if (input.length<3){
                    tvRead.append("Received R of len less than 3");
                }else{
                    String payload = input[2];
                    SerialPayload sp = new SerialPayload(payload);
                    byte type = sp.getType();
                    if(type==SerialPayload.TYPE_FORWARD){
                        //check if you are the final receipient
                        ForwardMessage fm = sp.getForwardMessage();
                        if(fm.ids.get(fm.ids.size()-1)==this.state.id){
                            tvRead.append(input[1] + ": " + fm.message + "\n");
                        }else{
                            int index = fm.ids.lastIndexOf(this.state.id);
                            String wString = "S " + fm.ids.get(index+1)+ " "+ sp.toString() + "\n";
                            logRead.append("Forwarded message to "+ fm.ids.get(index+1));
                        }

                    }else if(type==SerialPayload.TYPE_NODEDATA){

                    }else if(type==SerialPayload.TYPE_NODEDATA_REQ){

                    }
                }
                break;
            case "J":
                //join
                //TODO send routing table
                break;
            case "K":
                //set status
                tvRead.append(copyr.toString());
                this.state.busySending=false;
                break;
            case "M":
                //set my address
                tvRead.append(copyr.toString());
                if (state.id == null) {
                    state.id = input[1];
                }
                if (!network.network.containsKey(input[1])){
                    network.addNeighbor(input[1]);
                    //TODO modify add neighbour to add to self
                }
                break;
            case "#":
                //debug
                break;


        }
    }

    private void closeDevice() {
        mThreadIsStopped = true;
        updateView(false);
        if(ftDev != null) {
            ftDev.close();
        }
    }

    private void updateView(boolean on) {
        if(on) {
//            btOpen.setEnabled(false);
            btWrite.setEnabled(true);
            joinBtn.setEnabled(true);
//            btClose.setEnabled(true);
        } else {
//            btOpen.setEnabled(true);
            btWrite.setEnabled(false);
            joinBtn.setEnabled(false);
//            btClose.setEnabled(false);
        }
    }

    public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        if (ftDev.isOpen() == false) {
            Log.e(TAG, "SetConfig: device not open");
            return;
        }

        // configure our port
        // reset to UART mode for 232 devices
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        ftDev.setBaudRate(baud);

        switch (dataBits) {
        case 7:
            dataBits = D2xxManager.FT_DATA_BITS_7;
            break;
        case 8:
            dataBits = D2xxManager.FT_DATA_BITS_8;
            break;
        default:
            dataBits = D2xxManager.FT_DATA_BITS_8;
            break;
        }

        switch (stopBits) {
        case 1:
            stopBits = D2xxManager.FT_STOP_BITS_1;
            break;
        case 2:
            stopBits = D2xxManager.FT_STOP_BITS_2;
            break;
        default:
            stopBits = D2xxManager.FT_STOP_BITS_1;
            break;
        }

        switch (parity) {
        case 0:
            parity = D2xxManager.FT_PARITY_NONE;
            break;
        case 1:
            parity = D2xxManager.FT_PARITY_ODD;
            break;
        case 2:
            parity = D2xxManager.FT_PARITY_EVEN;
            break;
        case 3:
            parity = D2xxManager.FT_PARITY_MARK;
            break;
        case 4:
            parity = D2xxManager.FT_PARITY_SPACE;
            break;
        default:
            parity = D2xxManager.FT_PARITY_NONE;
            break;
        }

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl) {
        case 0:
            flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
            break;
        case 1:
            flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
            break;
        case 2:
            flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
            break;
        case 3:
            flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
            break;
        default:
            flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
            break;
        }


        // TODO : flow ctrl: XOFF/XOM
        // TODO : flow ctrl: XOFF/XOM
        ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);
    }

    // done when ACTION_USB_DEVICE_ATTACHED
    @Override
    protected void onNewIntent(Intent intent) {
        openDevice();
    };

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals (action)) {
                // never come here(when attached, go to onNewIntent)
                openDevice();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                closeDevice();
            }
        }
    };

    public void openMapCallback(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(SELF_LOCATION_KEY, getLocation());

        startActivity(intent);
    }

}
