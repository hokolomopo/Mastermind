import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MastermindServer {
	
	public static void main(String args[]) throws Exception{
		
		ServerSocket server = new ServerSocket(2340);
		
		while(true) {
			Socket socket = server.accept();
			
			OutputStream output = socket.getOutputStream () ;
			InputStream input = socket.getInputStream () ;
			
			byte msg[] = new byte [64] ;
			
			output.write("General Kenobi".getBytes());
			
			while(true){
				int len = input.read(msg); // get bytes (max 64)
				
				if (len <=0) 
					break ; // connection closed by peer ?
				
				output.write(msg,0,len); // send them away .
				output.flush(); // donft wait for more .
			}
			
			socket.close();

		}
	}

}
