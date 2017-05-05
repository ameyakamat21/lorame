package com.ksksue.app.ftdi_uart;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

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


    public List<String> getShortestPath(String src, String dest){
        NodeData s = this.network.get(src);
        NodeData d = this.network.get(dest);
        List<NodeData> spath = getShortestPath(s,d);
        List<String> spath_ids = new ArrayList<>();
        if(spath==null){
            return  spath_ids;
        }
        for (NodeData nd: spath){
            spath_ids.add(nd.id);
        }
        return  spath_ids;
    }

    public List<NodeData> getShortestPath(NodeData src, NodeData dest) {

        if(src.equals(dest)) {
            List<NodeData> returnList = new ArrayList<NodeData>();
            returnList.add(src);
            return returnList;
        }
        List<NodeData> path = new ArrayList<NodeData>();
        path.add(src);
        List<NodeData> seen = new ArrayList<NodeData>();
        seen.add(src);
        return shortestPathHelp(src, dest, path, seen);
    }

    public static List<NodeData> shortestPathHelp(NodeData src,
                                           NodeData dest, List<NodeData> path,
                                           List<NodeData> seen) {
        List<List<NodeData>> foundPaths = new ArrayList<List<NodeData>>();
        for(NodeData neighbor : src.neighbors) {

            if(seen.contains(neighbor)) {
                continue;
            }
            seen.add(neighbor);
            List<NodeData> currPath = new ArrayList<NodeData>();
            currPath.addAll(path);
            currPath.add(neighbor);
            if(neighbor.equals(dest)) {
                foundPaths.add(currPath);
            } else {
                List<NodeData> foundPath = shortestPathHelp(neighbor, dest, currPath, seen);
                if(foundPath != null) {
                    foundPaths.add(foundPath);
                }
            }
        }

        if(foundPaths.isEmpty()) {
            return null;
        }

        //return smallest path
        int smalletPathLength = foundPaths.get(0).size();
        int smallestPathIdx = 0;

        for(int i=0; i<foundPaths.size(); i++) {
            int currLength = foundPaths.get(i).size();
            if(currLength < smalletPathLength) {
                smalletPathLength = currLength;
                smallestPathIdx = i;
            }
        }

        return foundPaths.get(smallestPathIdx);
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
