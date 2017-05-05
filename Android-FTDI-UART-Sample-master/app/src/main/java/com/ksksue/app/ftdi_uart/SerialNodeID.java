import java.util.LinkedList;

public class SerialNodeID {
		
	public static int DecodeNodeId(String id) {
		return Integer.parseUnsignedInt(id, 16);
	}
	
	public static String EncodeNodeId(int id) {
		return String.format("%08X", id);
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
		

		System.out.printf("%02X\n", (byte)Integer.parseUnsignedInt("9A", 16));
		ForwardMessage fmsg2 = new SerialPayload(sp.toString()).getForwardMessage();
		System.out.println(fmsg2);
		System.out.println("Message = \"" + fmsg2.message + "\"");
		for(String id : fmsg2.ids) {
			System.out.println(id);
		}
	}

}
