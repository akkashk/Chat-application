package uk.ac.cam.aks73.fjava.tick5;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

public class ClientHandler implements Runnable {
	
	private Socket socket;
	private MultiQueue<Message> multiQueue;	//Sends to all clients currently online
	private String nickname;
	private SafeMessageQueue<Message> clientMessages;	//Send to current client
	private Thread t1;
	private Database database;
	
	public ClientHandler(Socket s, MultiQueue<Message> q, Database d) {
		socket = s;
		multiQueue = q;
		clientMessages = new SafeMessageQueue<Message>();
		Random r = new Random();
		nickname = "Anonymous"+(10000+r.nextInt(90000));	//nextInt can return a value between 0 and <90000 so adding 10000 always keeps it at 5-digits
		database = d;
		try {
			d.incrementLogins();
		} 
		
		catch (SQLException e) {
			System.out.println("SQL Exception when incrementing database login count");
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {	
			String host = socket.getInetAddress().getHostName();
			String finalstring = nickname+" connected from "+host+".";
			//Output the last 10 messages from database
			List<RelayMessage> ls = database.getRecent();
			for (RelayMessage m: ls) clientMessages.put(m);
			
			StatusMessage statusMessage = new StatusMessage(finalstring);
			multiQueue.register(clientMessages);
			multiQueue.put(statusMessage); 	//Sends a status update to all clients
			
			//multiQueue.register(clientMessages); //Register here so above status message isn't sent to connecting client
			OutputStream out = socket.getOutputStream();
			final ObjectOutputStream output = new ObjectOutputStream(out);
			t1 = new Thread() {
				@Override
				public void run() {
					while(true) {	
						try {
							Message toSend = clientMessages.take();
							output.writeObject(toSend);
						} 
						//Thread was interrupted, socket was closed and if this thread is waiting then interrupted
						catch (InterruptedException e) { 
							return;
						}
						
						catch (IOException e) {
							System.out.println("IO Exception in output");
						}
						
					}
				}
			};
			t1.start();
			
			
			InputStream in = socket.getInputStream();
			ObjectInputStream input = new ObjectInputStream(in);
			while (true){
				Message obj = (Message) input.readObject();
				if (obj instanceof ChangeNickMessage) {
					ChangeNickMessage object = (ChangeNickMessage) obj;
					String newname = object.name;
					finalstring = nickname+" is now known as "+newname+".";
					nickname = newname;
					StatusMessage status = new StatusMessage(finalstring);
					multiQueue.put(status);
				}
				else if (obj instanceof ChatMessage) {
					ChatMessage object = (ChatMessage) obj;
					RelayMessage newMsg = new RelayMessage(nickname, object);
					multiQueue.put(newMsg);
					database.addMessage(newMsg);
				}
			}
		}
		
		catch (IOException e)  {
			multiQueue.deregister(clientMessages);
			String finalstring = nickname+" has disconnected.";
			StatusMessage finalMsg = new StatusMessage(finalstring);
			multiQueue.put(finalMsg);
			//If Thread t1 is in in wait() method, causes it to terminate
			t1.interrupt();		 
			//To return from current Thread
			return;  
		}
		
		catch (ClassNotFoundException e) {
			System.out.println("Class not found from input");
		}
		
		catch (SQLException e1) {
			System.out.println("SQL Exception from getRecent()");
			e1.printStackTrace();
		}
		
	} 


}
