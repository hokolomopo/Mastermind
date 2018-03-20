
public interface MP {
	
	public final static int HEADER_LENGHT = 2;
	public final static byte PROTOCOL_VERSION = 1;
	public final static int COMBINATIONS_LENGHT = 4;
	public final static int NUMBER_OF_TRYS = 12;

	//Get the value of the enum element
	public byte getValue() ;
	
	//Get the message length of the enum element 
	public int getMsgLenght() ;
	
	//Set up the header of the msg byte array
	public static void setupHeader(byte[] msg, MP msgType) {
		if(msg.length < HEADER_LENGHT)
			return;
		msg[0] =  MPSrv.PROTOCOL_VERSION;
		msg[1] =  msgType.getValue();
	}
	
	//Convert to string an array of bits (mainly used for testing if we wantto print the message send)
	public static String print(byte[] b) {
		String ret = new String();
		for(int i = 0;i < b.length;i++)
			ret += String.valueOf(((int)b[i]));
		return ret;
	}
	

	
}
