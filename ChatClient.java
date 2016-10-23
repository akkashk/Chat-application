package uk.ac.cam.aks73.fjava.tick2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.DynamicObjectInputStream;
import uk.ac.cam.cl.fjava.messages.Execute;
import uk.ac.cam.cl.fjava.messages.NewMessageType;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

@FurtherJavaPreamble(
author = "Akkash Kumar",
date = "October 2015",
crsid = "aks73",
summary = "ChatClient",
ticker = FurtherJavaPreamble.Ticker.D)

public class ChatClient {
	
	private static String server = null;
	private static int port = 0;
	private static Socket s = null;
	private static SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss");
	
	
	public static void main (String[] args) {
		try {
			server = args[0];
			port = Integer.parseInt(args[1]);
			s = new Socket(server, port);
			Date date = new Date();
			System.out.println(form.format(date)+" [Client] Connected to "+server+" on port "+port+".");
			Thread output = new Thread() {
				@Override
				//This thread is used to deserialise data from the server
				public void run() {	
					try{
						@SuppressWarnings("resource")
						DynamicObjectInputStream obstream = new DynamicObjectInputStream(s.getInputStream());
						while (true) {
							Object obmsg = obstream.readObject();
							if (obmsg instanceof NewMessageType) {
								//A new message type has been received
								NewMessageType newmsg = (NewMessageType) obmsg;
								obstream.addClass(newmsg.getName(), newmsg.getClassData());
								Date d = new Date();
								System.out.println(form.format(d)+" [Client] New class "+newmsg.getName()+" loaded.");
							}
							else if (obmsg instanceof RelayMessage) {
								//Server sends a chat message to all clients 
								RelayMessage message = (RelayMessage) obmsg;
								System.out.println(form.format(message.getCreationTime())+" ["+message.getFrom()+"] "+message.getMessage());
							}
							else if (obmsg instanceof StatusMessage) {
								//Server sends a status message about any new connections/terminations 
								StatusMessage message = (StatusMessage) obmsg;
								System.out.println(form.format(message.getCreationTime())+" [Server] "+message.getMessage());
							}
							else {
								//Using ? since we do not know what the type of object will be
								Class<?> someclass = obmsg.getClass();
								String classname = someclass.getName();
								classname = someclass.getSimpleName();
								Field[] fieldlist = someclass.getDeclaredFields();
								Date d = new Date();
								System.out.print(form.format(d)+" [Client] "+classname+": ");
								for (int i=0; i<fieldlist.length; i++) {
									//allows to access private fields too
									fieldlist[i].setAccessible(true); 
									System.out.print(fieldlist[i].getName()+"("+fieldlist[i].get(obmsg)+")");
									//To get output in required format
									if (i != fieldlist.length-1) System.out.print(", ");   
								}
								System.out.println();
								Method[] methodslist = someclass.getDeclaredMethods();
								for (Method m: methodslist) {
									if (m.getParameterTypes().length == 0) {
										Annotation[] anno = m.getDeclaredAnnotations();
										for (Annotation a: anno) {
											//Execute method with Execute annotation
											if (a.annotationType() == Execute.class) m.invoke(obmsg, (Object[]) null);
										}
									}
								}
							}
						}
					}
					
					catch (IOException e) {
						//when socket is closed by \quit return causes this thread to quit
						return;
					}
					
					catch (ClassNotFoundException e) {
						System.out.println("CNFException in inside thread run method");
					} 
					
					catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					
					catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					
					catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			};
			output.setDaemon(true); 
			output.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			while (true) {
				String line = r.readLine();
				//Checks if special instruction is sent
				if (line.startsWith("\\")) {
					if (line.startsWith("\\nick")) {
						//Split the string into two words and send the second word as new nickname
						String[] inputstrings = line.split("\\s");
						ChangeNickMessage nickname = new ChangeNickMessage(inputstrings[1]);
						out.writeObject(nickname);
					}
					else {
						if (line.startsWith("\\quit")) {
							Date d = new Date();
							System.out.println(form.format(d)+" [Client] Connection terminated.");
							s.close();
							return;
						}
						else { 
							// Line starts with a \ but no known command
							//To split a string by backslash need to use \\\\ as regex
							String[] inputstrings = line.split("\\\\");
							//Another split for words after \ to get unknown command
							String[] inputstrings2 = inputstrings[1].split("\\s");
							Date d = new Date();
							System.out.println(form.format(d)+" [Client] Unknown command \""+inputstrings2[0]+"\"");
						}
					}
				}
				else {
					//Send a ChatMessage object
					ChatMessage message = new ChatMessage(line);
					out.writeObject(message);
				}
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
