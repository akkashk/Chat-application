package uk.ac.cam.aks73.fjava.tick2;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class TestMessageReadWrite {

	public static void main(String args[]) {
		String testurl = "http://www.cl.cam.ac.uk/teaching/current/FJava/testmessage-aks73.jobj";
		System.out.println(readMessage(testurl));
	}
	
	
	public static boolean writeMessage(String message, String filename) {
		//Create a TestMessage object with the given message
		TestMessage msg = new TestMessage();
		msg.setMessage(message);
		
		try {
			//Write the message object into given filename
			FileOutputStream out = new FileOutputStream(filename);
			ObjectOutputStream objout = new ObjectOutputStream (out);
			objout.writeObject(msg);
			objout.close();
			return true;
		} 
		
		catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException in writing message");
			return false;
		}
		catch (IOException e) {
			System.out.println("IOException in writing message");
			return false;
		}
		
	}
	
	public static String readMessage(String location) {
		try {
			if (location.startsWith("http")) {
				URL destination = new URL(location);
				URLConnection connection = destination.openConnection();
				ObjectInputStream data = new ObjectInputStream(connection.getInputStream());
				TestMessage obj = (TestMessage) data.readObject();
				data.close();
				return obj.getMessage();
			}
			else {
				BufferedInputStream file = new BufferedInputStream((new FileInputStream(location)));
				ObjectInputStream data = new ObjectInputStream(file);
				TestMessage obj = (TestMessage) data.readObject();
				data.close();
				return obj.getMessage();
			}
			
		}
		
		catch (IOException e) {
			System.out.println("IOExp found in reading message");
			e.printStackTrace();
			return null;
		}
		
		catch (ClassNotFoundException e) {
			System.out.println("CNFE found in reading message");
			return null;
		}
	}
	
}
