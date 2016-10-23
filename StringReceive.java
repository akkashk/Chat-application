package uk.ac.cam.aks73.fjava.tick1;

import java.io.IOException;
import java.net.Socket;


public class StringReceive {
	
	private static Socket socket = null;
	private static String server = null;
	private static int port = 0;
	
	public static void main(String[] args) {
		try {
				server = args[0];
				port = Integer.parseInt(args[1]);
				socket = new Socket(server, port);
				byte[] buffer = new byte[1024];
				int val = 0;
				String a = null;
				while (val != -1) {
					val = socket.getInputStream().read(buffer);
					a = new String(buffer,0,val-1);
					System.out.println(a);
				}
				socket.close();
			}
			catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("This application requires two arguments: <machine> <port>");
				return;
			}
			catch (NumberFormatException e) {
				System.err.println("This application requires two arguments: <machine> <port>");
				return;
			}
			catch (IOException e) {
				System.err.println("Cannot connect to "+args[0]+" on port "+args[1]);
				return;
			}
	}
		
}
