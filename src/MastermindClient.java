import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class MastermindClient {

	private Scanner sc = new Scanner (System.in);

	private int trysLeft = MP.NUMBER_OF_TRYS;
	
	private Socket socket;
	
	private boolean gameWon = false;
	private boolean gameLost = false;
	
	private OutputStream output;
	private InputStream input;
	
	public static void main(String args[]){
		MastermindClient client = new MastermindClient();
		
		client.run();
		
	}
	
	private MastermindClient() {
		System.out.println("Welcome to tha MasterMind game !");
		
	}
	
	
	private void run(){
		
		while(true) {
			try {
				this.connectToServer();
		
				while(true) {
					switch(this.getInput()) {
						case 1 : 
							this.sendProposition(this.getProposition());
							this.manageResponse(this.receiveMsg(MPClt.CLIENT_PROPOSITION), MPClt.CLIENT_PROPOSITION);
							break;
						case 2 :
							this.sendMessage(MPClt.CLIENT_LIST);
							this.manageResponse(this.receiveMsg(MPClt.CLIENT_LIST), MPClt.CLIENT_LIST);
							break;
						case 3 : 
							System.exit(0);
							break;
					}
					
					if(this.gameLost)
						this.loose();
					else if(this.gameWon)
						this.win();
				}
			}catch(IOException e) {
				System.err.println("An error occured : " + e.getMessage());
				this.reset();
			}
		}
	}
	
	private int getInput() {

		int input;
		
		while(true) {
			System.out.println("What do you want to do ?\n\n"
					+ "1. Propose a combinasion ("+trysLeft+" trys left)\n"
					+ "2. See already proposed combinasion\n"
					+ "3. Quit the game\n");
			
			try {
				String s = sc.next();
				input = Integer.parseInt(s);
			}catch(Exception  e) {
				input = 0;
			}
			
			if(input > 0 && input < 4)
				break;
			
			System.out.println("Please enter a valid number\n");
				
		}
		
		return input;
	}
	
	private byte[] getProposition() {
		System.out.println("Enter a proposition : ");
		
		int currLen = 0, value;
		byte[] colors = new byte[4];
		String input;
		
		while(currLen < 4) {
			
			input = sc.next();
			value = MPColors.getColorValue(input);
			
			if(value == -1) {
				System.out.println(input+" isn't a valid color name");
				currLen = 0;
				continue;
			}
			
			colors[currLen++] = (byte) value;

		}
				
		return colors;
	}
	
	public int connectToServer() throws IOException {
		boolean connected = false;
		
		System.out.println("Starting a new game ...");

		while(!connected) {
			try {
			socket = new Socket("localhost", 2340);
			
			output = socket.getOutputStream () ;
			input = socket.getInputStream () ;
			
			connected = true;
			}catch(Exception e) {
				System.out.print("Unable to connecto to server : "+e.getMessage()+" ...Trying again");
				try {
					for(int j = 0;j < 3;j++) {
					Thread.sleep(1000);
					System.out.print(".");
					}
					
				} catch (InterruptedException e1) {
				}
				System.out.println("");
			}
		}
		
		this.sendMessage(MPClt.CLIENT_NEW_GAME);
		this.receiveMsg(MPClt.CLIENT_NEW_GAME);
		
		System.out.println("Connected to server !");
						
		return 0;
		
	}

	private int sendProposition(byte[] prop) throws IOException {
		
		byte[] msg = new byte[MPSrv.HEADER_LENGHT + MPSrv.COMBINAISON_LENGHT];
		
		MP.setupHeader(msg, MPClt.CLIENT_PROPOSITION);
		
		for(int i = 0;i < 4;i++)
			msg[i+MP.HEADER_LENGHT] = prop[i];
		
		System.out.println("Client send "+MP.print(msg));
		output.write(msg);
		output.flush();
		
		return 0;
	}
	
	private int sendMessage(MPClt msgType) throws IOException {
		
		byte[] msg = new byte[2];

		MP.setupHeader(msg, msgType);
		
		System.out.println("Client send msg "+MP.print(msg));
		output.write(msg);
		output.flush();

		return 0;

	}
	
	private byte[] receiveMsg(MPClt msgType) throws IOException{
		
		byte[] msg = new byte[MP.HEADER_LENGHT + msgType.acknowledgmentType().getMsgLenght()];	
		System.out.println("Size "+msg.length+" "+msgType.acknowledgmentType()+" "+msgType);
		input.read(msg);
		System.out.println(msgType+" received, content "+MP.print(msg));
		
		if(msg[0] != MPSrv.PROTOCOL_VERSION) {
			System.err.println("Error : The server isn't using the same protocol version as the client...closing the client");
			System.exit(-1);
		}
		else if(msg[1] != msgType.acknowledgmentValue()) {
			System.err.println("Error : The server isn't sending the expected response");
			System.exit(-1);
		}

		return msg;
		
	}	

	private void manageResponse(byte[] msg, MPClt msgType) {
		
		switch(msgType) {
			case CLIENT_PROPOSITION :
				System.out.println("You found "+msg[2]+" colors at the right place, and "+msg[3]+" right colors at the wrong place");
				
				if(msg[2] == MP.COMBINAISON_LENGHT) {
					gameWon = true;
					return;
				}
				
				if(--this.trysLeft == 0) {
					gameLost = true;
					return;
				}
				
				System.out.println("Try lefts : " + trysLeft);
				break;
			case CLIENT_LIST :
				
				if(msg[2] == 0)
					System.out.println("You haven't tried any combinsaions yet");
				
				for(int i = 0;i < msg[2];i++) {
					
					System.out.print("Try number "+i+" was : ");
					
					for(int j = 0;j < MPSrv.COMBINAISON_LENGHT;j++)
						System.out.print(MPColors.getColorName((int)msg[MP.HEADER_LENGHT + 1 + i * (MP.COMBINAISON_LENGHT + 2) + j]) + " ");
					
					System.out.println("\nWith : -"+msg[MP.HEADER_LENGHT + 1 + i* (MP.COMBINAISON_LENGHT + 2) + MP.COMBINAISON_LENGHT]+" colors at the right place \n       -"+
						msg[MP.HEADER_LENGHT + 1 + i* (MP.COMBINAISON_LENGHT + 2) + MP.COMBINAISON_LENGHT + 1]+" right colors at the wrong place\n");
					
				}
				break;
			default : 
				break;
			
		}
	}
	
	private void win() throws IOException {
		System.out.println("Congratulations ! You found the right cominaison !");
		
		this.playAgain();
	}
	
	private void loose() throws IOException {
		System.out.println("How sad ... You lost..");
		
		this.playAgain();
	}
	
	private void playAgain() throws IOException {
		System.out.println("Do you want to play again ?\n"
				+ "1. Yes\n"
				+ "2. No\n");
		
		String s = sc.next();
		
		if(!s.equals("1")) {
			System.out.println("See you soon !");
			sc.close();
			try {
				socket.close();
			} catch (IOException e) {
			}
			System.exit(0);
		}
		
		this.reset();		
		this.connectToServer();
	}
	
	private void reset() {
		this.gameLost = false;
		this.gameWon = false;
		this.trysLeft = MP.NUMBER_OF_TRYS;
		try {
			socket.close();
		} catch (IOException e) {
		}
	}
}
