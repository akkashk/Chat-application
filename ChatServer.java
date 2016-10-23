package uk.ac.cam.aks73.fjava.tick5;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

import uk.ac.cam.cl.fjava.messages.Message;

public class ChatServer {
	
	private static int port = 0;
	private static ServerSocket soc = null;

	public static void main(String[] args) {
		try {
			port = Integer.parseInt(args[0]);
			String filepath = args[1];
			soc = new ServerSocket(port);
			MultiQueue<Message> q = new MultiQueue<Message>();
			Database database = new Database(filepath);
			while (true) {
				Socket newconnection = soc.accept();
				Thread newThread = new Thread(new ClientHandler(newconnection,q,database));
				newThread.start();
			}
		}
		
		catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			System.out.println("Usage: java ChatServer <port>");
		}
		
		catch (IOException e) {
			System.out.println("Cannot use port number "+args[0]);
		}
		
		catch (ClassNotFoundException e) {
			System.out.println("CNF Exception from ChatServer from database instantiation");
			e.printStackTrace();
		}
		
		catch (SQLException e) {
			System.out.println("SQL Exception from ChatServer from database instantiation");
			e.printStackTrace();
		}

	}
}
