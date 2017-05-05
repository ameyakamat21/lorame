package com.ksksue.app.ftdi_uart;

import org.w3c.dom.Node;

import java.util.Hashtable;

/**
 * Created by aruln on 4/30/2017.
 */

public class NetworkManager {

    public Hashtable<String,NodeData> network;

    public NetworkManager(){
        this.network = new Hashtable<>();
    }

    public boolean addNeighbor(String id,NodeData d){
        this.network.put(id,d);
        return true;
    }

    public boolean addNeighbor(String id){
        NodeData d = new NodeData();
        d.id = id;
        this.network.put(id,d);
        return  true;
    }
    public boolean addNeighbor(String id, String rssi){
        NodeData d = new NodeData();
        d.id = id;
        d.rssi = rssi;
        this.network.put(id,d);
        return true;
    }



    public boolean updateNeighbor(String id, String rssi){
        if(this.network.containsKey(id)) {
            this.network.get(id).rssi = rssi;
            return true;
        }

        return false;
    }

    public boolean deleteNeighbor(String id){
        this.network.remove(id);
        for (NodeData node : this.network.values()){
            for (NodeData n : node.neighbors){
                if (n.id == id){
                    node.neighbors.remove(n);
                }
            }
        }
        return true;

    }

}
