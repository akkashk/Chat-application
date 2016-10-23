package uk.ac.cam.aks73.fjava.tick3star;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TwoLockConcurrentQueue<T> implements ConcurrentQueue<T> {

	Lock headl = new ReentrantLock();
	Lock taill = new ReentrantLock();
	Node head;
	Node tail;
	
	private class Node {
		T value;
		Node next;
	}
	
	public TwoLockConcurrentQueue() {
		Node n = new Node();
		n.next = null;
		head = n;
		tail = n;
	}
	
	@Override
	public void offer(T message) {
		Node n = new Node();
		n.value = message;
		n.next = null;
		taill.lock();
		tail.next = n;
		tail = n;
		taill.unlock();
		
	}

	@Override
	public T poll() {
		headl.lock();
		Node n = head;
		Node nextn = n.next;
		if (nextn == null) { //Empty queue
			headl.unlock();
			return null;
		}
		T message = nextn.value;
		head = nextn;
		headl.unlock();
		return message;
	}

}
