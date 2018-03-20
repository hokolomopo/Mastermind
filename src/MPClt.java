
public enum MPClt implements MP{
	NEW_GAME(0), PROPOSITION(1), LIST(2);
	
	private int value;
	
	private MPClt(int value) {
		this.value = value;
	}
	
	@Override
	public byte getValue() {
		return (byte)this.value;
	}
	
	//Return the value of the acknowledgment of this type of client request
	public int acknowledgmentValue() {
		switch(this) {
			case NEW_GAME :
				return MPSrv.NEW_GAME_STARTED.getValue();
			case LIST :
				return MPSrv.LIST_REQUEST_RECEIVED.getValue();
			case PROPOSITION :
				return MPSrv.COMBINAISON_RECEIVED.getValue();
			default :
				return MPSrv.ERROR.getValue();
		}
	}
	
	//Return the type of the acknowledgment of this type of client request
	public MPSrv acknowledgmentType() {
		switch(this) {
			case NEW_GAME :
				return MPSrv.NEW_GAME_STARTED;
			case LIST :
				return MPSrv.LIST_REQUEST_RECEIVED;
			case PROPOSITION :
				return MPSrv.COMBINAISON_RECEIVED;
			default :
				return MPSrv.ERROR;
		}
	}


	//Convert a value to a type of client request
	public static MPClt valueToClientRequest(byte value) {
		for(MPClt m : MPClt.values())
			if(m.getValue() == value)
				return m;
		return null;
	}
	
	@Override
	public int getMsgLenght() {
		switch(this) {
			case NEW_GAME :
			case LIST :
				return 0;
			case PROPOSITION :
				return MPSrv.COMBINATIONS_LENGHT;
		}
		return -1;
	}
	
}
