import java.io.IOException;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class GameClient {
	
	private int trysLeft = 12;
	private Scanner sc = new Scanner (System.in);

	public GameClient() {
		System.out.println("Welcome to tha MasterMind game !");
		
		System.out.println("Starting a new game ...");
	}
	
	public int update() {

		int input;
		
		while(true) {
			System.out.println("What do you want to do ?\n\n"
					+ "1. Propose a combinasion ("+trysLeft+" trys left)\n"
					+ "2. See already proposed combinasion\n"
					+ "3. Quit the game");
			
			try {
				input = Integer.parseInt(sc.nextLine());
			}catch(Exception  e) {
				input = 0;
			}
			
			if(input > 0 && input < 4)
				break;
			System.out.println("Please enter a valid number\n");
				
		}
		

		return input;
	}
	
	public int createConnection() throws UnknownHostException, IOException {
		Socket socket = new Socket("localhost", 2340);
		
		OutputStream output = socket.getOutputStream () ;
		InputStream input = socket.getInputStream () ;
		
		byte msg[] = new byte [64] ;
		
		output.write("Hello there".getBytes());
		
		while(true){
			if(input.read(msg) <=0)
				break;
			System.out.println(msg.toString());	
			}
		
		socket.close();
		
		return 0;
		
	}

}
