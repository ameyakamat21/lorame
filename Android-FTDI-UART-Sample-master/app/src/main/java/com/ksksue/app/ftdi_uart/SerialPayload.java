package com.ksksue.app.ftdi_uart;
import android.location.Location;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SerialPayload {
	private static int NEW_BUFFER_MAX_SIZE = 1024;
	private static String PRINTF_FORMAT_BYTE = "%02X";
	private static String PRINTF_FORMAT_NODE_ID = "%08X";

	public static byte TYPE_NODEDATA_REQ = 0x00;
	public static byte TYPE_NODEDATA     = 0x01;
	public static byte TYPE_FORWARD      = 0x02;

	private ByteBuffer buffer;

	public SerialPayload() {
		buffer = ByteBuffer.allocate(NEW_BUFFER_MAX_SIZE);
	}

	public SerialPayload(String payload) {
		int index = 0;
		buffer = ByteBuffer.allocate(payload.length());

		if ((payload.length() % 2) != 0) {
			// we need to consume a nibble
			buffer.put((byte) Integer.parseInt(payload.substring(index, index + 1), 16));
			index++;
		}
		for (; index < (payload.length() - 1); index += 2) {
			buffer.put((byte) Integer.parseInt(payload.substring(index, index + 2), 16));
		}
		buffer.limit(buffer.position());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		buffer.position(0);
		for (int index = 0; index < buffer.limit(); index++) {
			sb.append(String.format(PRINTF_FORMAT_BYTE, buffer.get(index)));
		}
		return sb.toString();
	}

	public byte getType() {
		return buffer.get(0);
	}

	public void putNodeData(List<NodeData> nodedata) {
		// [index] --> nodeAddr
		int[] nodeIdDictionary;
		// nodeAddr --> NodeData
		HashMap<Integer, NodeData> nodeDataMap = new HashMap<Integer, NodeData>();
		// nodeAddr --> index
		HashMap<Integer, Integer> nodeIdMap = new HashMap<Integer, Integer>();

		int nodeIndex = 0;
		for (NodeData d : nodedata) {
			nodeDataMap.put(SerialNodeID.DecodeNodeId(d.id), d);
			for (NodeData sd : d.neighbors) {
				nodeDataMap.put(SerialNodeID.DecodeNodeId(sd.id), sd);
			}
		}
		nodeIdDictionary = new int[nodeDataMap.size()];

		// Generate Map Of NodeID to Dictionary Index
		for (NodeData d : nodeDataMap.values()) {
			nodeIdDictionary[nodeIndex] = SerialNodeID.DecodeNodeId(d.id);
			nodeIdMap.put(SerialNodeID.DecodeNodeId(d.id), nodeIndex);
			nodeIndex++;
		}

		buffer.position(0);

		// Place Payload Type
		buffer.put(TYPE_NODEDATA); // payload type

		// Place Number of Node IDs
		buffer.put((byte) nodeDataMap.size());

		// Place Node IDs in Dictionary
		for (int id : nodeIdDictionary) {
			buffer.putInt(id);
		}

		// Place Node Data
		for (int id : nodeIdDictionary) {
			buffer.putDouble(nodeDataMap.get(id).location.getLatitude());
			buffer.putDouble(nodeDataMap.get(id).location.getLongitude());
			buffer.put((byte) nodeDataMap.get(id).neighbors.size());
			for (NodeData d : nodeDataMap.get(id).neighbors) {
				buffer.put((byte) (int) nodeIdMap.get(SerialNodeID.DecodeNodeId(d.id)));
			}
		}

		buffer.limit(buffer.position());
	}

	public List<NodeData> getNodeData() {
		List<NodeData> nodeData = new LinkedList<NodeData>();
		// nodeAddr --> NodeData
		HashMap<Integer, NodeData> nodeDataMap = new HashMap<Integer, NodeData>();

		// Check message type
		if (getType() != TYPE_NODEDATA) {
			return null;
		}

		buffer.position(1);

		// Parse Dictionary Size
		byte nodeDictionarySize = buffer.get();
		int[] dictionary = new int[nodeDictionarySize];

		// Parse Dictionary
		for (byte index = 0; index < nodeDictionarySize; index++) {
			dictionary[index] = buffer.getInt();

			NodeData d = new NodeData();
			d.id = String.format(PRINTF_FORMAT_NODE_ID, dictionary[index]);
			d.neighbors = new LinkedList<NodeData>();
			d.location = new Location("");
			d.rssi = "0";
			nodeDataMap.put(dictionary[index], d);
			nodeData.add(d);
		}

		for (int id : dictionary) {
			NodeData d = nodeDataMap.get(id);
			d.location.setLatitude(buffer.getDouble());
			d.location.setLongitude(buffer.getDouble());
			byte neighborCount = buffer.get();
			for (byte i = 0; i < neighborCount; i++) {
				int neighbor_id = dictionary[buffer.get()];
				d.neighbors.add(nodeDataMap.get(neighbor_id));
			}
		}

		return nodeData;
	}

	public int putForwardMessage(ForwardMessage fmsg) {
		byte[] msg = fmsg.message.getBytes();
		buffer.position(0);

		buffer.put(TYPE_FORWARD); // payload type
		buffer.put((byte) fmsg.ids.size());
		for (String id : fmsg.ids) {
			buffer.putInt(SerialNodeID.DecodeNodeId(id));
		}

		// buffer.put(msg.length);
		buffer.put(msg);
		buffer.limit(buffer.position());
		return buffer.limit();
	}

	public ForwardMessage getForwardMessage() {
		ForwardMessage fmsg = new ForwardMessage();
		StringBuilder sb = new StringBuilder();

		// Check message type
		if (getType() != TYPE_FORWARD) {
			return null;
		}

		buffer.position(1);
		byte idCount = buffer.get();
		fmsg.ids = new LinkedList<String>();
		for (byte index = 0; index < idCount; index++) {
			fmsg.ids.add(SerialNodeID.EncodeNodeId(buffer.getInt()));
		}

		for (int index = buffer.position(); index < buffer.limit(); index++) {
			sb.append((char) buffer.get());
		}
		fmsg.message = sb.toString();

		return fmsg;
	}

}
