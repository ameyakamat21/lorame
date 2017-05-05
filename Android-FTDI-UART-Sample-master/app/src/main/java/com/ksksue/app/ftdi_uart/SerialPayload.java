package com.ksksue.app.ftdi_uart;

import com.ksksue.app.ftdi_uart.NodeData;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SerialPayload {
	private static int NEW_BUFFER_MAX_SIZE = 1024;
	private static String PRINTF_FORMAT_BYTE = "%02X";
	private static String PRINTF_FORMAT_NODE_ID = "%08X";

	public static byte TYPE_NODEDATA = 0x00;
	public static byte TYPE_FORWARD = 0x01;
	
	private ByteBuffer buffer;
	private int count;
	
	public SerialPayload() {
		buffer = ByteBuffer.allocate(NEW_BUFFER_MAX_SIZE);
		count = 0;
	}
	
	public SerialPayload(String payload) {
		int index = 0;
		buffer = ByteBuffer.allocate(payload.length());
		count = 0;
		
		if ((payload.length() % 2) != 0) {
			// we need to consume a nibble
			buffer.put((byte)Integer.parseInt(payload.substring(index, index+1), 16));
			index++;
			count++;
		}
		for(; index < (payload.length()-1); index+=2) {
			buffer.put((byte)Integer.parseInt(payload.substring(index, index+2), 16));
			count++;
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int index = 0; index < count; index++) {
			sb.append(String.format(PRINTF_FORMAT_BYTE,	buffer.get(index)));
		}
		return sb.toString();
	}
	
	
	public void putNodeData(List<NodeData> nodedata) {
		Set<Integer> nodeids = new HashSet<Integer>();
		for (NodeData d : nodedata) {
			nodeids.add(SerialNodeID.DecodeNodeId(d.id));
			for (NodeData sd : d.neighbors) {
				nodeids.add(SerialNodeID.DecodeNodeId(sd.id));
			}
		}
		buffer.put(TYPE_NODEDATA); // payload type 
		// global table
		// node data blobs
		
	}
	
	public int putForwardMessage(ForwardMessage fmsg) {
		byte[] msg = fmsg.message.getBytes();
		count = 0;
		buffer.position(0);
		
		buffer.put(TYPE_FORWARD); // payload type
		buffer.put((byte)fmsg.ids.size());
		for (String id : fmsg.ids) {
			buffer.putInt(SerialNodeID.DecodeNodeId(id));
		}
		
//		buffer.put(msg.length);
		buffer.put(msg);
		count = buffer.position();
		return count;
	}
	
	public byte getType() {
		return buffer.get(0);
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
		
		for(int index = buffer.position(); index < count; index++) {
			sb.append((char)buffer.get());
		}
		fmsg.message = sb.toString();
		
		return fmsg;
	}
	
}
