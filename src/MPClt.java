
public enum MPClt implements MP{
	CLIENT_NEW_GAME(0), CLIENT_PROPOSITION(1), CLIENT_LIST(2);
	
	private int value;
	
	private MPClt(int value) {
		this.value = value;
	}
	
	@Override
	public int getValue() {
		return this.value;
	}
	
	public int acknowledgmentValue() {
		switch(this) {
			case CLIENT_NEW_GAME :
				return MPSrv.SRV_NEW_GAME_STARTED.getValue();
			case CLIENT_LIST :
				return MPSrv.SRV_LIST_REQUEST_RECEIVED.getValue();
			case CLIENT_PROPOSITION :
				return MPSrv.SRV_COMBI_RECEIVED.getValue();
			default :
				return MPSrv.SRV_ERROR.getValue();
		}
	}
	
	public MPSrv acknowledgmentType() {
		switch(this) {
			case CLIENT_NEW_GAME :
				return MPSrv.SRV_NEW_GAME_STARTED;
			case CLIENT_LIST :
				return MPSrv.SRV_LIST_REQUEST_RECEIVED;
			case CLIENT_PROPOSITION :
				return MPSrv.SRV_COMBI_RECEIVED;
			default :
				return MPSrv.SRV_ERROR;
		}
	}


	public static MPClt valueToClientRequest(int value) {
		for(MPClt m : MPClt.values())
			if(m.getValue() == value)
				return m;
		return null;
	}
	
	@Override
	public int getMsgLenght() {
		switch(this) {
			case CLIENT_NEW_GAME :
			case CLIENT_LIST :
				return 0;
			case CLIENT_PROPOSITION :
				return MPSrv.COMBINAISON_LENGHT;
		}
		return -1;
	}
		

}
