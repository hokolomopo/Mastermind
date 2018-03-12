
public enum MPSrv implements  MP{
	SRV_NEW_GAME_STARTED(1), SRV_COMBI_RECEIVED(2), SRV_LIST_REQUEST_RECEIVED(3), SRV_ERROR(4);

	
	private int value;
	
	private MPSrv(int value) {
		this.value = value;
	}
	
	@Override
	public int getValue() {
		return this.value;
	}
	
	@Override
	public int getMsgLenght() {
		switch(this) {
			case SRV_NEW_GAME_STARTED :
			case SRV_ERROR :
				return 0;
			case SRV_COMBI_RECEIVED :
				return 2;
			case SRV_LIST_REQUEST_RECEIVED :
				return HEADER_LENGHT + 1 + (COMBINAISON_LENGHT + 2) * NUMBER_OF_TRYS;
		}
		return -1;
	}

}
