package uk.ac.cam.aks73.fjava.tick1star;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class ImageChatClient extends JFrame{

	private String server;
	private int port;
	private static Socket socket;
	private Canvas canvas;
	private Button b;
	private BufferedImage image;
	private Dimension d;
	private static InputStream in;
	
	public ImageChatClient(String s, int p) throws IOException {
		server = s;
		port = p;
		socket = new Socket(server, port);
		in = socket.getInputStream();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
		setLayout(new BorderLayout());								
		setVisible(true);										
		
		canvas = new Canvas() {
			@Override
			public void paint(Graphics g) {
				g.drawImage(image, 0, 0, this);
			}
		};
		
		add(canvas,BorderLayout.NORTH);					
		
		b = new Button("Upload");
		add(b,BorderLayout.SOUTH);						
		pack();											
	}
	
	public static void main(String[] args) {
		try {
			final ImageChatClient img = new ImageChatClient(args[0],Integer.parseInt(args[1]));

			Thread t = new Thread() {
				@Override
				public void run() {
					img.b.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JFileChooser jf = new JFileChooser();
							int returnVal = jf.showOpenDialog(img);					
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								File f = jf.getSelectedFile();
								try {
									socket.getOutputStream().write(Files.readAllBytes(f.toPath()));
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
							
							
						}
					});	
				}
			};
			t.start();
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf;
			int a1 = in.read();
			int a2 = in.read();
			while (true) {
				if ((byte) a1 == -1 && (byte) a2 == -39) {
					out.write(a1);
					out.write(a2);
					buf = out.toByteArray();
					img.image = ImageIO.read(new ByteArrayInputStream(buf));
					out.reset();
					img.canvas.repaint();
					int width = img.image.getWidth();
					int height = img.image.getHeight();
					img.d = new Dimension(width,height);
					img.canvas.setPreferredSize(img.d);
					img.pack();										
					a1 = in.read();
					a2 = in.read();
				}
				else { //end of file not reached
					out.write(a1);
					a1 = a2;
					a2 = in.read();
				}
			}
			
		}
		catch (NumberFormatException e) {
			System.err.println("This application requires two arguments: <machine> <port>");
		}
		catch (IOException e) {
			System.err.println("Cannot connect to "+args[0]+" on port "+args[1]);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("This application requires two arguments: <machine> <port>");
		}

	}
	
}
