package uk.ac.cam.aks73.fjava.tick3star;


public class OneLockConcurrentQueue<T> implements ConcurrentQueue<T> {

	private Link<T> first = null;
	private Link<T> last = null;
	
	private static class Link<L> {
		L val;
		Link<L> next;
		
		Link(L val) { 
			this.val = val; this.next = null; 
		}
	}
	
	@Override
	public synchronized void offer(T message) {
		Link<T> value = new Link<T>(message);
		if (first == null) {
			first = value;
			last = value;
		}
		else {	//first != null, queue already has 1+ element(s) in it
			last.next = value;
			last = value;
		}
	}

	@Override
	public synchronized T poll() {
		if (first == null) return null;
		Link<T> value = this.first;
		first = value.next;
		if (first == null) last = null;
		return value.val;
	}

}
