package uk.ac.cam.aks73.fjava.tick1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.lang.Thread;

public class StringChat {
	
	private static String server = null;
	private static int  port = 0;

	public static void main(String[] args) {
		try {
			server = args[0];
			port = Integer.parseInt(args[1]);
			//Declaring a variable as final here means it cannot be assigned to another instance of Socket object
			@SuppressWarnings("resource")
			final Socket s = new Socket(server, port); 
			Thread output = new Thread() {
			@Override
			public void run() {
				byte[] buffer = new byte[1024];
				int val = 0;
				while (val != -1) {
					try {
						val = s.getInputStream().read(buffer);
						String a = new String(buffer,0,val-1);
						System.out.println(a);
					} 
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			};
			//A daemon thread prevents JVM from exiting if the program finishes but thread is still running
			output.setDaemon(true); 
			output.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				String line = r.readLine();
				byte[] temp = line.getBytes();
				s.getOutputStream().write(temp);
			}
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
