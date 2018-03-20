
public enum MPSrv implements  MP{
	NEW_GAME_STARTED(1), COMBINAISON_RECEIVED(2), LIST_REQUEST_RECEIVED(3), ERROR(4);

	
	private int value;
	
	private MPSrv(int value) {
		this.value = value;
	}
	
	@Override
	public byte getValue() {
		return (byte)this.value;
	}
	
	@Override
	public int getMsgLenght() {
		switch(this) {
			case NEW_GAME_STARTED :
			case ERROR :
				return 0;
			case COMBINAISON_RECEIVED :
				return 2;
			case LIST_REQUEST_RECEIVED :
				return HEADER_LENGHT + 1 + (COMBINATIONS_LENGHT + 2) * NUMBER_OF_TRYS;
		}
		return -1;
	}
	
}
