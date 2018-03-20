import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class MastermindClient {

	private final static int TIME_OUT_DELAY = 10000;

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
		System.out.println("Welcome to tha MasterMind game !\n"
				+ "The supported colors are : " + MPColors.getColorList());

	}

	//Main loop of the client
	private void run(){

		//The whole thing is in a while loop to be able to try to reconnect to the server if an exception occurs
		while(true) {
			try {
				this.connectToServer();

				while(true) {
					switch(this.getInput()) {
						//Send a proposition to the server
						case 1 :

							//Send proposition to server
							this.sendProposition(this.getProposition());

							//Parse server response
							this.manageResponse(this.receiveMsg(MPClt.PROPOSITION), MPClt.PROPOSITION);
							break;
						//List already tried propositions
						case 2 :

							//Send request to server
							this.sendMessage(MPClt.LIST);

							//Parse server response
							this.manageResponse(this.receiveMsg(MPClt.LIST), MPClt.LIST);
							break;
						//Close the client
						case 3 :
							this.close();
							break;
					}

					//Check if game is won/lost
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

	//Get user choice in the main menu (Propose a combination/List combinations proposed/Quit)
	private int getInput() {

		int input;

		System.out.println("What do you want to do ?\n\n"
				+ "1. Propose a combination ("+trysLeft+" trys left)\n"
				+ "2. See already proposed combinations\n"
				+ "3. Quit the game");


		while(true) {
			try {
				//Get input and parse it to int
				String s = sc.next();
				input = Integer.parseInt(s);
			}catch(Exception  e) {
				input = 0;
			}

			if(input > 0 && input < 4) {
				break;
			}


			System.out.println("Please enter a valid number");
			sc.nextLine();

		}

		System.out.println("");

		//Clean the scanner
		sc.nextLine();

		return input;
	}

	//Get user proposition converted into array of byte formatted according to the Mastermind Protocol
	private byte[] getProposition() {
		System.out.println("Enter a proposition : ");

		int currLen = 0, value;
		byte[] colors = new byte[MP.COMBINATIONS_LENGHT];
		String input;

		while(currLen < MP.COMBINATIONS_LENGHT) {

			input = sc.next();
			value = MPColors.getColorValue(input);

			if(value == -1) {
				System.out.println(input+" isn't a valid color name");
				sc.nextLine();
				currLen = 0;
				continue;
			}

			colors[currLen++] = (byte) value;

		}

		System.out.println("");

		//Clean the scanner
		sc.nextLine();

		return colors;
	}

	/*
	 * Connect to server localhost at port 2340
	 * Will loop until a connection is established
	 * After the connection is established, send a START_SERVER message to the server
	 * 	and receive the expected answer from the server
	 *
	 * Throws IOException if it fails to send the message or receive the message
	*/
	public int connectToServer() throws IOException {
		boolean connected = false;

		System.out.println("Starting a new game ...");

		//Loop until a connection to the server is established
		while(!connected) {
			try {
				socket = new Socket("localhost", 2340);

				output = socket.getOutputStream () ;
				input = socket.getInputStream () ;

				socket.setSoTimeout(TIME_OUT_DELAY);

				connected = true;
			}catch(Exception e) {
				try {
				System.out.print("Unable to connecto to server : "+e.getMessage()+" ...Trying again");

				//Wait a bit before the next try to connect to server
				for(int j = 0;j < 3;j++) {
					System.out.print(".");
						Thread.sleep(1000);
					}
				System.out.println("");
				}
				catch (InterruptedException e1) {
				}
			}
		}

		//Send and receive message to create a new game
		this.sendMessage(MPClt.NEW_GAME);
		this.receiveMsg(MPClt.NEW_GAME);

		System.out.println("Connected to server !\n");

		return 0;

	}


	/*
	 * Send a proposition to the server
	 * The combination to send should be in the prop byte array
	 *
	 *  Throws IOException if send failed
	 */
	private void sendProposition(byte[] prop) throws IOException {

		byte[] msg = new byte[MPSrv.HEADER_LENGHT + MPSrv.COMBINATIONS_LENGHT];

		MP.setupHeader(msg, MPClt.PROPOSITION);

		for(int i = 0;i < MP.COMBINATIONS_LENGHT;i++)
			msg[MP.HEADER_LENGHT + i] = prop[i];

		output.write(msg);
		output.flush();

	}

	/*
	 * Send a message to the server of type msgType according to the Mastermind Protocol
	 *
	 *  Throws IOException if send failed
	 */
	private void sendMessage(MPClt msgType) throws IOException {

		byte[] msg = new byte[MP.HEADER_LENGHT];

		MP.setupHeader(msg, msgType);

		output.write(msg);
		output.flush();

	}

	/*
	 * Receive a message from the server with the expected answer being an answer
	 * 	to a message of type msgType
	 *
	 *  Throws IOException if receive failed
	 *  Will close the program if the response from the server isn't what was expected
	 */
	private byte[] receiveMsg(MPClt msgType) throws IOException{

		//Create a byte array of the size of the message we are expecting
		int expectedLenght = MP.HEADER_LENGHT + msgType.acknowledgmentType().getMsgLenght();

		byte[] msg = readMsg(expectedLenght);

		if(msg[0] != MPSrv.PROTOCOL_VERSION) {
			System.err.println("Error : The server isn't using the same protocol version as the client...closing the client");
			this.close();
		}

		if(msg[1] != msgType.acknowledgmentValue()) {
			System.err.println("Error : The server isn't sending the expected response");
			this.close();
		}

		return msg;

	}

	/*
	 * Read and return a message from the input stream of size msgLen
	 * Will wait until msgLen bytes ares read from the stream or the stream is closed
	 *
	 * Throws IOException if the end of the input stream is reached or if we were unable to read from the stream
	 */
	private byte[] readMsg(int msgLen) throws IOException {
		int i = 0;
		byte[] msg = new byte[msgLen];

		//Loop until we read the expected number of elements on the string
		while(i < msgLen) {

			//Try to read the remaining elements
			int read = input.read(msg, i, msgLen - i);
			i += read;
			//End of stream
			if(read < 0)
				throw new IOException("EOF reached");
		}
		return msg;
	}


	// Manage a message (msg) received from the server, the request to the server should be of type msgType
	private void manageResponse(byte[] msg, MPClt msgType) {

		switch(msgType) {
			case PROPOSITION :
				System.out.println("You found "+msg[MP.HEADER_LENGHT]+" colors at the right place, "
						+ "and "+msg[MP.HEADER_LENGHT+1]+" right colors at the wrong place");

				//Check if it's a victory or a loss
				if(msg[MP.HEADER_LENGHT] == MP.COMBINATIONS_LENGHT) {
					gameWon = true;
					return;
				}
				else if(--this.trysLeft == 0) {
					gameLost = true;
					return;
				}

				System.out.println("Try lefts : " + trysLeft + "\n");
				break;
			case LIST :

				if(msg[MP.HEADER_LENGHT] == 0)
					System.out.println("You haven't tried any combinations yet...\n");

				for(int i = 0;i < msg[MP.HEADER_LENGHT];i++) {

					System.out.print("Try number "+i+" was : ");

					for(int j = 0;j < MPSrv.COMBINATIONS_LENGHT;j++)
						System.out.print(MPColors.getColorName((int)msg[MP.HEADER_LENGHT + 1 + i * (MP.COMBINATIONS_LENGHT + 2) + j]) + " ");

					System.out.println("\nWith : -"+msg[MP.HEADER_LENGHT + 1 + i* (MP.COMBINATIONS_LENGHT + 2) + MP.COMBINATIONS_LENGHT]+" colors at the right place \n       -"+
						msg[MP.HEADER_LENGHT + 1 + i* (MP.COMBINATIONS_LENGHT + 2) + MP.COMBINATIONS_LENGHT + 1]+" right colors at the wrong place\n");

				}
				break;
			default :
				break;

		}
	}

	/*
	 * Display the win message and ask if the user want to play again
	 *
	 * Throws IOException if the user wanted to play again but the connection failed
	 * Quit the game if the user didn't want to play again
	 */
	private void win() throws IOException {
		System.out.println("Congratulations ! You found the right combination !");

		this.playAgain();
	}

	/*
	 * Display the defeat message and ask if the user want to play again
	 *
	 * Throws IOException if the user wanted to play again but the connection failed
	 * Quit the game if the user didn't want to play again
	 */
	private void loose() throws IOException {
		System.out.println("How sad ... You lost..");

		this.playAgain();
	}

	/*
	 * Ask if the user want to play again
	 *
	 * Throws IOException if the user wanted to play again but the connection failed
	 * Quit the game if the user didn't want to play again
	 */
	private void playAgain() throws IOException {
		System.out.println("Do you want to play again ?\n"
				+ "1. Yes\n"
				+ "2. No\n");

		String s = sc.next();

		if(!s.equals("1"))
			this.close();

		this.reset();
		this.connectToServer();
	}

	//Reset game parameters and close the socket
	private void reset() {
		this.gameLost = false;
		this.gameWon = false;
		this.trysLeft = MP.NUMBER_OF_TRYS;
		try {
			socket.close();
		} catch (IOException e) {
		}
	}

	//Close the program, closing the process and the scanner
	private void close() {
		System.out.println("See you soon !");
		sc.close();
		try {
			socket.close();
		} catch (IOException e) {
		}
		System.exit(0);

	}
}
