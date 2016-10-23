package uk.ac.cam.aks73.fjava.tick5;

public interface MessageQueue<T> {//A FIFO queue of items of type T

	public abstract void put(T msg); //place msg on back of queue
	
	public abstract T take() throws InterruptedException; //block until queue length >0; return head of queue

}
