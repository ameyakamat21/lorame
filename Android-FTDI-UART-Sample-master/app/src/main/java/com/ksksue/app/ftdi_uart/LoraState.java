package com.ksksue.app.ftdi_uart;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aruln on 5/5/2017.
 */

public class LoraState {
    public String id;
    public boolean busySending = false;
    public List<String> pendingNeighbors = new ArrayList<>();

    public LoraState(){
    }
}
