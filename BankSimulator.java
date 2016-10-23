package uk.ac.cam.aks73.fjava.tick3;

import java.util.Random;

public class BankSimulator {
	
	private int capital;
	private BankAccount[] account;
	private RoboTeller[] teller;
	
	private class BankAccount {
		private int balance;
		private int acc;
		
		BankAccount(int accountNumber, int deposit) {
			balance = deposit;
			acc = accountNumber;
		}
		
		public int getAccountNumber() {
			return acc;
		}
		
		public void transferTo(BankAccount b, int amount) {
			if (this.getAccountNumber() < b.getAccountNumber()) {
				synchronized (this) {
					balance -= amount;
					synchronized (b) {
						b.balance += amount;
					}
				}
			}
			else {			
				synchronized (b) {
					b.balance += amount;
					synchronized (this) {
						balance -= amount;
					}
				}
			}
		}
	}
	
	public BankSimulator(int capital, int accounts, int tellers) {
    	this.capital = capital;
    	this.account = new BankAccount[accounts];
    	this.teller = new RoboTeller[tellers];
		for(int i=0; i<account.length; i++) {
			account[i]  = new BankAccount(i,capital/account.length);
		}
    }
	
	public static void main(String[] args) {
		BankSimulator javaBank = new BankSimulator(10000,10,100);
		javaBank.runDay();
		System.out.println("Capital at close: £"+javaBank.getCapital());
	}


	private static Random r = new Random();
	
	private class RoboTeller extends Thread {
		public void run() {
			//Robots work from 9am until 5pm; one customer per second
			for(int i=9*60*60; i<17*60*60; i++) {
				int a = r.nextInt(account.length);
				int b = r.nextInt(account.length);
				account[a].transferTo(account[b], r.nextInt(100));							
			}
		}
	}

	
	public int getCapital() {
		return capital;
	}
	
	public void runDay() {
		for(int i=0; i<teller.length; i++) {
			teller[i] = new RoboTeller();
		}
		for(int i=0; i<teller.length; i++) {
			teller[i].start();
		}
		
		int done = 0;
		while(done < teller.length)
			try {
				teller[done].join();done++;
			} 
			catch(InterruptedException e) {
				
			}
			
		int finalCapital = 0; 
		for(int i=0; i<account.length; i++)
		finalCapital += account[i].balance;
		capital = finalCapital;
	}
	
}
