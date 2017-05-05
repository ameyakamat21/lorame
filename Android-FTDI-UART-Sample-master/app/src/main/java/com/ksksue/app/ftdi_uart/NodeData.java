package com.ksksue.app.ftdi_uart;

import java.util.List;

/**
 * Created by aruln on 4/30/2017.
 */

public class NodeData {
    public String id;
    public String rssi;
    public List<NodeData> neighbors;
    public double gpslat;
    public double gpslong;
}
