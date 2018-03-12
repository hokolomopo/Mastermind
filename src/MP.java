
public interface MP {
	
	public final static int HEADER_LENGHT = 2;
	public final static int PROTOCOL_VERSION = 1;
	public final static int COMBINAISON_LENGHT = 4;
	public final static int NUMBER_OF_TRYS = 12;

	public int getValue() ;
	public int getMsgLenght() ;
	
	public static void setupHeader(byte[] msg, MP msgType) {
		if(msg.length < HEADER_LENGHT)
			return;
		msg[0] = (byte) MPSrv.PROTOCOL_VERSION;
		msg[1] = (byte) msgType.getValue();
	}
	
	public static String print(byte[] b) {
		String ret = new String();
		for(int i = 0;i < b.length;i++)
			ret += String.valueOf(((int)b[i]));
		return ret;
	}

}
