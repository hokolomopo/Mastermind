import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;

public class MastermindServer implements Runnable{

	private final static int TIME_OUT_DELAY = 120000;

	private Socket socket;
	private int number;

	OutputStream output;
	InputStream input;

	//Memory to remember previous try from the client
	Vector<byte[]> previousTrys = new Vector<byte[]>();

	//Correct combination of colors of this thread instance
	byte[] combinaison = new byte[MP.COMBINATIONS_LENGHT];


	/*
	 * Main loop of the server, creating new threads when a connection occurs
	 *
	 * SuppressWarnings because the server doesn't provide "clean" way to close the server and
	 * 		the ServerSocket
	 */
	@SuppressWarnings("resource")
	public static void main(String args[])  {

		//Open the serverSocket
		ServerSocket server;
		try {
			server = new ServerSocket(2340);
		} catch (IOException e) {
			System.err.println("Unable to open server socket at port 2340 : " + e.getMessage());
			return;
		}

		int i = 0;

		//Loop and create a new thread when there's a connection request
		while(true) {
			try {
			Socket socket = server.accept();
			socket.setSoTimeout(TIME_OUT_DELAY);
			Thread thread = new Thread(new MastermindServer(socket, i++));
			thread.start();
			}catch(IOException e) {
				System.err.println("Unable to create connection to the client");
			}
		}

	}

	//Thread creation
	private MastermindServer(Socket s, int n) {
		this.number = n;
		System.out.println("Creating connection thread number "+n);
		socket = s;

	}

	//Main loop of a thread
	@Override
	public void run() {
		try {
			output = socket.getOutputStream () ;
			input = socket.getInputStream () ;

			while(true) {
				//Read the header of the message
				byte msg[] = readMsg(MP.HEADER_LENGHT);

				if(msg[0] != MP.PROTOCOL_VERSION) {
					System.err.println("Error : Wrong protocol version from client");
					MP.setupHeader(msg, MPSrv.ERROR);
				}
				else
					switch(MPClt.valueToClientRequest(msg[1])) {
						//Client asked for a new game
						case NEW_GAME :
							//Setup the header of the answer and create a new combination
							MP.setupHeader(msg, MPClt.NEW_GAME.acknowledgmentType());
							this.generateCombination();
							break;
						//Client submitted a proposition
						case PROPOSITION :
							//Read the proposition
							byte[] combi = readMsg(MP.COMBINATIONS_LENGHT);

							//Setup the header and body of the answer
							msg = this.manageReceivedCombination(combi);
							MP.setupHeader(msg, MPClt.PROPOSITION.acknowledgmentType());
							break;
						//Client asked to list the submitted combinations
						case LIST :
							//Create the answer msg and setup it's header
							msg = new byte[MP.HEADER_LENGHT + MPSrv.LIST_REQUEST_RECEIVED.getMsgLenght()];
							MP.setupHeader(msg, MPClt.LIST.acknowledgmentType());

							//Put the number of tries combinations in the answer msg
							msg[MP.HEADER_LENGHT] = (byte) this.previousTrys.size();

							//Put the previous combinations in the answer msg
							for(int i = 0;i < this.previousTrys.size();i++)
								for(int j = 0;j < MP.COMBINATIONS_LENGHT + 2;j++)
									msg[MP.HEADER_LENGHT + 1 + i*(MP.COMBINATIONS_LENGHT +2) + j] = this.previousTrys.get(i)[j];
							break;
						default :
							System.err.println("Error : Wrong request value from client");
							MP.setupHeader(msg, MPSrv.ERROR);
							break;
					}

				output.write(msg);
				output.flush();
			}

		}catch(Exception e) {
			System.err.println(e.getMessage()+" : server thread "+number+" stopped");
			try {
				socket.close();
			} catch (IOException e1) {
			}
		}

	}

	//Generate a new random combination of colors and displays it on the screen
	private void generateCombination() {
		Random rand = new Random();

		System.out.print("The right combinaison is : ");
		for(int i = 0;i < MP.COMBINATIONS_LENGHT;i++) {
			this.combinaison[i] = (byte)rand.nextInt(MPColors.values().length);
			System.out.print(MPColors.getColorName(this.combinaison[i])+" ");
		}
		System.out.println("");
	}

	/*
	 * Manage received combination and put the received combination to memory
	 *
	 * Return a byte array of size MP.HEADER_LENGHT + 2 whose element
	 * 	- MP.HEADER_LENGHT is the number of right colors at the right place
	 *  - MP.HEADER_LENGHT + 1 is the number of right colors at the wrong place
	 */
	private byte[] manageReceivedCombination(byte[] receivedCombi) {

		byte[] comparison = new byte[MP.HEADER_LENGHT + 2];
		comparison[MP.HEADER_LENGHT] = 0;
		comparison[MP.HEADER_LENGHT + 1] = 0;

		for(int i = 0;i < MP.COMBINATIONS_LENGHT;i++) {

			//Count correct and right place
			if(this.combinaison[i] == receivedCombi[i])
				comparison[MP.HEADER_LENGHT]++;

			//Count correct but wrong place
			else
				for(int j = 0;j < MP.COMBINATIONS_LENGHT;j++)
					if(receivedCombi[i] == this.combinaison[j]) {
						comparison[MP.HEADER_LENGHT+1]++;
						break;
					}
		}

		//Remember that the client tried this combination
		byte[] memory = new byte[MP.COMBINATIONS_LENGHT + 2];
		for(int i =0;i < MP.COMBINATIONS_LENGHT;i++)
			memory[i] = receivedCombi[i];
		memory[MP.COMBINATIONS_LENGHT] = comparison[MP.HEADER_LENGHT];
		memory[MP.COMBINATIONS_LENGHT + 1] = comparison[MP.HEADER_LENGHT + 1];

		this.previousTrys.addElement(memory);

		return comparison;
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
}
