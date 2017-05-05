
package com.ksksue.app.ftdi_uart;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import android.location.Location;

public class SerialNodeID {
		
	public static int DecodeNodeId(String id) {
		return Integer.parseInt(id, 16);
	}
	
	public static String EncodeNodeId(int id) {
		return String.format("%X", id);
	}
	
	public static NodeData GenNodeData(String id, double lat, double lon) {
		NodeData d = new NodeData();
		d.id = id;
		d.location = new Location("");
		d.location.setLatitude(lat);
		d.location.setLongitude(lon);
		d.neighbors = new LinkedList<NodeData>();
		d.rssi = "0";
		return d;
	}
	
	public static void PrintNodeData(NodeData d) {
		System.out.println(d.id + ": ");
		System.out.println(d.location.getLatitude());
		System.out.println(d.location.getLongitude());
		for (NodeData nd : d.neighbors) {
			System.out.println("* " + nd.id);
		}
		System.out.println();
	}
	
	public static void test2() {
		System.out.println("## Test 2 ##");
		NodeData a = GenNodeData("111", -9999.22, -80.555);
		NodeData b = GenNodeData("222", 675.123, -1877.44);
		NodeData c = GenNodeData("333", 6213.668, -183.4);
		a.neighbors.add(b);
		a.neighbors.add(c);
		b.neighbors.add(a);
		c.neighbors.add(a);
		
		LinkedList<NodeData> l = new LinkedList<NodeData>();
		l.add(a);
		l.add(b);
		l.add(c);
		
		for (NodeData d : l) {
			PrintNodeData(d);
		}
		
		SerialPayload sp = new SerialPayload();
		sp.putNodeData(l);
		System.out.println("Payload: \"" + sp.toString() + "\" length=" + Integer.toString(sp.toString().length()));
		
		SerialPayload sp2 = new SerialPayload(sp.toString());
		List<NodeData> nodesList = sp2.getNodeData();
		for (NodeData d : nodesList) {
			PrintNodeData(d);
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ForwardMessage fmsg = new ForwardMessage();
		fmsg.ids = new LinkedList<String>();
		fmsg.ids.add("00112233");
		fmsg.message = "Hello World!";
		SerialPayload sp = new SerialPayload();
		sp.putForwardMessage(fmsg);
		System.out.println("Hello World!\n");
		System.out.println("Payload: \"" + sp.toString() + "\"");
		

		System.out.printf("%02X\n", (byte)Integer.parseInt("9A", 16));
		ForwardMessage fmsg2 = new SerialPayload(sp.toString()).getForwardMessage();
		System.out.println(fmsg2);
		System.out.println("Message = \"" + fmsg2.message + "\"");
		for(String id : fmsg2.ids) {
			System.out.println(id);
		}
		
		
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		ByteBuffer bb = ByteBuffer.allocate(100);
		System.out.printf("Position: %d\n", bb.position());
		bb.put((byte)'C'); bb.put((byte)'r'); bb.put((byte)'a'); bb.put((byte)'i'); bb.put((byte)'g');
		System.out.printf("Position: %d\n", bb.position());
//		bb.mark();
		bb.limit(bb.position());
//		bb.position(0);
		bb.limit(bb.limit()+1);
		bb.put((byte)'!');
		bb.position(0);
		System.out.printf("Position: %d\n", bb.position());
		for (int i = 0; i < bb.limit(); i++) {
			System.out.printf("%02X\n", bb.get());
		}
		System.out.printf("Position: %d\n", bb.position());
//		bb.reset();
//		System.out.printf("Position: %d\n", bb.position());
		
		test2();
	}

}
