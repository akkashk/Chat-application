package uk.ac.cam.aks73.fjava.tick3;

public class ProducerConsumer {
	private MessageQueue<Character> m = new UnsafeMessageQueue<Character>();
	
	public static void main(String[] args) {
		new ProducerConsumer().execute();
	}
	
	private class Producer implements Runnable {
		char[] cl = "Computer Laboratory".toCharArray();
	
		public void run() {
			for (int i = 0; i < cl.length; i++) {
				m.put(cl[i]);
				try {
					Thread.sleep(500);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class Consumer implements Runnable {
		public void run() {
			while (true) {
				System.out.print(m.take());
				System.out.flush();
			}
		}
	}
	
	void execute() {
		new Thread(new Producer()).start();
		new Thread(new Consumer()).start();
	}
	
}
