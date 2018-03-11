
public class MastermindClient {

	
	public static void main(String args[]) throws Exception{
		GameClient client = new GameClient();
		
		
		while(true) {
			switch(client.update()) {
			case 1 : 
				System.out.println("Send");
				client.createConnection();
				break;
			case 2 :
				System.out.println( "Combi");
				break;
			case 3 : 
				System.exit(0);
				break;
			}
		}

	}
	
}
