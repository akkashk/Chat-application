package uk.ac.cam.aks73.fjava.tick3star;

import java.util.concurrent.atomic.AtomicReference;

public class NoLockConcurrentQueue<T> implements ConcurrentQueue<T> {

	private class Node {
		T value;
		AtomicReference<Node> next;
	}
	
	AtomicReference<Node> head;
	AtomicReference<Node> tail;
	
	public NoLockConcurrentQueue() {
		Node n = new Node();						
		n.next = new AtomicReference<Node>();		  
		head = new AtomicReference<Node>(n);		
		tail = new AtomicReference<Node>(n);
	}
	
	@Override
	public void offer(T message) {
		Node n = new Node();			//Create a new Node with message
		n.value = message;
		n.next = new AtomicReference<Node>();
		Node t;
		
		while (true) {
			t = tail.get(); 					//A snapshot of the current tail
			Node next = t.next.get();			//Node next to tail node, potentially null
			if (t == tail.get()) {				//Testing to see if another thread has changed tail position
				if (next == null) {				//tail (& t) were pointing at the last node
					if (tail.get().next.compareAndSet(next, n)) break; //Add new node to last node.next pointer 
				}
				else {							//Tail wasn't pointing at last node, move it along to next node
					tail.compareAndSet(t, next);
				}
			}
		}					
		tail.compareAndSet(t, n);				//Enqueue operation successfully done, move tail pointer 
	}

	@Override
	public T poll() {
		T value;
		while (true) {
			Node h = head.get();
			Node t = tail.get();
			Node next = h.next.get();			//Next node Head is pointing to, potentially null
			if (h == head.get()) {				//Head pointer hasn't changed
				if (h == t) { 					//Empty queue
					if (next == null) {
						return null;
					}
					tail.compareAndSet(t, next);//Advance tail
				}
				else {
					value = next.value;			//Dequeue operation value
					if (head.compareAndSet(h, next)) break;		 
				}
			}
		}
		return value;
	}

}
