package uk.ac.cam.aks73.fjava.tick5;

import java.util.Set;
import java.util.HashSet;

public class MultiQueue<T> {
	 
	private Set<MessageQueue<T>> outputs = new HashSet<MessageQueue<T>>();
	
	public synchronized void register(MessageQueue<T> q) { 
		outputs.add(q);
	}
	
	public synchronized void deregister(MessageQueue<T> q) {
		outputs.remove(q);
	}
	
	public synchronized void put(T message) {
		for (MessageQueue<T> m:outputs) { //Since method is synchronised no need for iterator
			m.put(message);
		}
	}

}
