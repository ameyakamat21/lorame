package com.ksksue.app.ftdi_uart;

import android.widget.AdapterView;
import android.view.View;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

/**
 * Created by aruln on 4/30/2017.
 */

public class CustomOnItemSelectedListener implements OnItemSelectedListener {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        Toast.makeText(parent.getContext(),
                "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
