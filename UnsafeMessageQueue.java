package uk.ac.cam.aks73.fjava.tick3;

public class UnsafeMessageQueue<T> implements MessageQueue<T> {
	
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
	
	public void put(T val) {
		//Given a new "val", create a new Link<T>
		// element to contain it and update "first" and
		// "last" as appropriate
		Link<T> value = new Link<T>(val);
		if (first == null) {
			first = value;
			last = value;
		}
		else {
			last.next = value;
			last = value;
		}
	}

	public T take() {
		while (first == null) {	//use a loop to block thread until data is available
			try {
				Thread.sleep(100);
			} 
			catch(InterruptedException ie) {
				
			}
		}
		//Retrieve "val" from "first", update "first" to refer
		// to next element in list (if any). Return "val"
		Link<T> value = first;
		first = value.next;
		if (first == null) {
			last = null;
		}
		return value.val;
	}

}
