package uk.ac.cam.aks73.fjava.tick5;

public class SafeMessageQueue<T> implements MessageQueue<T> {
	private Link<T> first = null;
	private Link<T> last = null;
	
	private static class Link<L> {
		L val;
		Link<L> next;
		
		Link(L val) { 
			this.val = val; 
			this.next = null; 
		}
	}
	
	public synchronized void put(T val) {
		Link<T> value = new Link<T>(val);
		if (first == null) {
			first = value;
			last = value;
		}
		//first != null, queue already has 1+ element(s) in it
		else {	
			last.next = value;
			last = value;
		}
		notify();
	}

	public synchronized T take() throws InterruptedException {
		//use a loop to block thread until data is available
		while (first == null) {	
			wait();
		}
		Link<T> value = first;
		first = value.next;
		if (first == null) {
			last = null;
		}
		return value.val;
	}

}

