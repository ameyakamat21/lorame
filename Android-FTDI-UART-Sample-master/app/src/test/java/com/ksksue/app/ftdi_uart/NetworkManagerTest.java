package com.ksksue.app.ftdi_uart;

import junit.framework.TestCase;

import org.w3c.dom.Node;

import java.util.List;

/**
 * Created by ameya on 5/5/17.
 */
public class NetworkManagerTest extends TestCase {
    public void testGetShortestPath() throws Exception {

        /*
        NodeData n1 = new NodeData();
        n1.id = "1";
        NodeData n2 = new NodeData();
        n2.id = "2";
        NodeData n3 = new NodeData();
        n3.id = "3";
        NodeData n4 = new NodeData();
        n3.id = "4";

        addUndirectedEdge(n1,n2);
        addUndirectedEdge(n2, n3);
        addUndirectedEdge(n3, n4);
        addUndirectedEdge(n2, n4);



        List<NodeData> receivedShortestPath = NetworkManager.getShortestPath(n1, n3);
        System.out.println(" -- Received shortest path from" + n1.id + " -> " +
                n3.id + ": " + receivedShortestPath);
        receivedShortestPath = NetworkManager.getShortestPath(n2, n1);
        System.out.println(" -- Received shortest path from" + n2.id + " -> " +
                n2.id + ": " + receivedShortestPath);
                */
    }


//    public String printLog(List<NodeData>)

    public void addUndirectedEdge(NodeData n1, NodeData n2) {
        n1.neighbors.add(n2);
        n2.neighbors.add(n1);
    }

}