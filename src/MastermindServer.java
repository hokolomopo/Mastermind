import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;

public class MastermindServer implements Runnable{
	
	private Socket socket;
	private int number;
	
	Vector<byte[]> previousTrys = new Vector<byte[]>();
	byte[] combinaison = new byte[MP.COMBINAISON_LENGHT];
	

	@SuppressWarnings("resource")
	public static void main(String args[])  {
				
		ServerSocket server;
		try {
			server = new ServerSocket(2340);
		} catch (IOException e) {
			System.err.println("Unable to open server socket at port 2340 : " + e.getMessage());
			return;
		}
		
		int i = 0;
		
		while(true) {
			try {
			Socket socket = server.accept();
			Thread thread = new Thread(new MastermindServer(socket, i++));
			thread.start();
			}catch(IOException e) {
				System.err.println("Unable to create connection to the client");
			}
		}			
					
	}
	
	private MastermindServer(Socket s, int n) {
		this.number = n;
		System.out.println("Creating connection thread number "+n);
		socket = s;	
		
	}

	@Override
	public void run() {
		try {
			OutputStream output = socket.getOutputStream () ;
			InputStream input = socket.getInputStream () ;
			
			while(true) {
				byte msg[] = new byte [MP.HEADER_LENGHT] ;
				input.read(msg);
				System.out.println("Server read header "+MP.print(msg));
	
				if(msg[0] != MPSrv.PROTOCOL_VERSION) {
					System.err.println("Wrong protocol version from client");
					MP.setupHeader(msg, MPSrv.SRV_ERROR);
					output.write(msg);
					output.flush();
				}
			
				switch(MPClt.valueToClientRequest(msg[1])) {
					case CLIENT_NEW_GAME : 
						MP.setupHeader(msg, MPSrv.SRV_NEW_GAME_STARTED);
						this.generateCombinaison();
						break;
					case CLIENT_PROPOSITION :
						byte[] combi = new byte[MP.COMBINAISON_LENGHT];
						System.out.print("Proposition : try to read...");
						input.read(combi);
						System.out.println(MP.print(combi));
						msg = this.manageCombinaison(combi);
						MP.setupHeader(msg, MPSrv.SRV_COMBI_RECEIVED);
						break;
					case CLIENT_LIST :
						msg = new byte[MPSrv.SRV_LIST_REQUEST_RECEIVED.getMsgLenght()];
						MP.setupHeader(msg, MPSrv.SRV_LIST_REQUEST_RECEIVED);
						msg[MP.HEADER_LENGHT] = (byte) this.previousTrys.size();
						
						for(int i = 0;i < this.previousTrys.size();i++)
							for(int j = 0;j < MP.COMBINAISON_LENGHT + 2;j++)
								msg[MP.HEADER_LENGHT + 1 + i*(MP.COMBINAISON_LENGHT +2) + j] = this.previousTrys.get(i)[j];
						break;
					default :
						break;
				}
				
				System.out.println("Server send msg "+MP.print(msg)+" of length "+msg.length );
				output.write(msg); // send them away .
				output.flush(); // donft wait for more .
			}
			
		}catch(Exception e) {
			System.out.println("Server thread exception " +number+" "+e.getMessage());
		}
		
		System.out.println("End of server thread "+number+"\n");
		
	}
	
	private void generateCombinaison() {
		Random rand = new Random();
		
		System.out.print("The right combinaison is : ");
		for(int i = 0;i < MP.COMBINAISON_LENGHT;i++) {
			this.combinaison[i] = (byte)rand.nextInt(MPColors.values().length);
			System.out.print(MPColors.getColorName(this.combinaison[i])+" ");
		}
		System.out.println("");
	}

	private byte[] manageCombinaison(byte[] receivedCombi) {

		byte[] comparison = new byte[MP.HEADER_LENGHT + 2];
		comparison[MP.HEADER_LENGHT] = 0;
		comparison[MP.HEADER_LENGHT + 1] = 0;
		for(int i = 0;i < MP.COMBINAISON_LENGHT;i++) {
			
			//Correct and right place
			if(this.combinaison[i] == receivedCombi[i])
				comparison[MP.HEADER_LENGHT]++;
			
			//Correct and wrong place
			else
				for(int j = 0;j < MP.COMBINAISON_LENGHT;j++)
					if(receivedCombi[i] == this.combinaison[j]) {
						comparison[MP.HEADER_LENGHT+1]++;
						break;
					}
		}

		byte[] memory = new byte[MP.COMBINAISON_LENGHT + 2];
		for(int i =0;i < MP.COMBINAISON_LENGHT;i++)
			memory[i] = receivedCombi[i];
		memory[MP.COMBINAISON_LENGHT] = comparison[MP.HEADER_LENGHT];
		memory[MP.COMBINAISON_LENGHT + 1] = comparison[MP.HEADER_LENGHT + 1];
		
		System.out.println("Memory : "+MP.print(memory));
		
		this.previousTrys.addElement(memory);

		return comparison;
	}
}
