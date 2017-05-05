package com.ksksue.app.ftdi_uart;

import org.w3c.dom.Node;
import android.location.Location;
import java.util.List;

/**
 * Created by aruln on 4/30/2017.
 */

public class NodeData {
    public String id;
    public String rssi;
    public List<NodeData> neighbors;
    public Location location;

    @Override
    public boolean equals(Object ob) {
        if(!(ob instanceof  NodeData)) {
            return false;
        }

        NodeData thatNodeData = (NodeData) ob;
        return this.id.equals(thatNodeData.id);
    }
}
