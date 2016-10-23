package uk.ac.cam.aks73.fjava.tick5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import uk.ac.cam.cl.fjava.messages.RelayMessage;

public class Database {
	
	private Connection connection;
	
	public Database(String database) throws SQLException, ClassNotFoundException {
		if (database == null) {
			System.err.println("Usage: java uk.ac.cam.aks73.fjava.tick5.Database <database name>");
			return;
		}
		
		Class.forName("org.hsqldb.jdbcDriver");
		connection = DriverManager.getConnection("jdbc:hsqldb:file:"+database,"SA","");
		Statement delayStmt = connection.createStatement();
		try {
			delayStmt.execute("SET WRITE_DELAY FALSE");
		}
		
		finally {
			delayStmt.close();
		}
		connection.setAutoCommit(false);
		
		Statement sqlStmt = connection.createStatement();
		try {
			sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+"message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
		}
		
		catch (SQLException e) {
			System.out.println("Warning: Database table \"messages\" already exists.");
		}
		
		//Separate statement to get separate warning for both tables
		try {
			sqlStmt.execute("CREATE TABLE statistics(key VARCHAR(255),value INT)");
			sqlStmt.executeUpdate("INSERT INTO statistics(key,value) VALUES ('Total messages',0)");
			sqlStmt.executeUpdate("INSERT INTO statistics(key,value) VALUES ('Total logins',0)");
		}
		
		catch (SQLException e) {
			System.out.println("Warning: Database table \"statistics\" already exists.");
		}
		
		finally{
			sqlStmt.close();
		}
	}
	
	public static void main (String[] args) throws ClassNotFoundException, SQLException  {
		if (args.length==0) {
			System.err.println("Usage: java uk.ac.cam.aks73.fjava.tick5.Database <database name>");
			return;
		}
		
		Class.forName("org.hsqldb.jdbcDriver");
		Connection connection = DriverManager.getConnection("jdbc:hsqldb:file:"+args[0],"SA","");
		Statement delayStmt = connection.createStatement();
		
		try {
			delayStmt.execute("SET WRITE_DELAY FALSE");
		}
		
		finally {
			delayStmt.close();
		}
		connection.setAutoCommit(false);
		
		Statement sqlStmt = connection.createStatement();
		try {
			sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+"message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
		}
		
		catch (SQLException e) {
			System.out.println("Warning: Database table \"messages\" already exists.");
		}
		
		finally{
			sqlStmt.close();
		}
		
		String stmt = "INSERT INTO messages(nick,message,timeposted) VALUES (?,?,?)";
		PreparedStatement insertMessage = connection.prepareStatement(stmt);
		try {
			insertMessage.setString(1, "Alastair");
			insertMessage.setString(2, "Hello, Andy");
			insertMessage.setLong(3, System.currentTimeMillis());
			insertMessage.executeUpdate();
		}
		
		finally {
			insertMessage.close();
		}
		connection.commit();
		
		stmt = "SELECT nick,message,timeposted FROM messages ORDER BY timeposted DESC LIMIT 10";
		PreparedStatement recentMessages = connection.prepareStatement(stmt);
		try {
			ResultSet rs = recentMessages.executeQuery();
			try {
				while (rs.next()) System.out.println(rs.getString(1)+": "+rs.getString(2)+" ["+rs.getLong(3)+"]");
			}
			
			finally {
				rs.close();
			}
		}
		
		finally {
			recentMessages.close();
		}
		
		connection.close();
		
	}
	
	public void close() throws SQLException {
		connection.close();
	}
	
	public void incrementLogins() throws SQLException {
		//Since same string will be used many times, better to use PreparedStatements
		String update = "UPDATE statistics SET value = value+1 WHERE key='Total logins'";
		PreparedStatement stmt = connection.prepareStatement(update);
		stmt.executeUpdate();
		connection.commit();
	}
	
	public void addMessage(RelayMessage m) throws SQLException {
		String insert = "INSERT INTO messages(nick,message,timeposted) VALUES (?,?,?)";
		String update = "UPDATE statistics SET value = value+1 WHERE key='Total messages'";
		PreparedStatement stmt = connection.prepareStatement(insert);
		
		String message = m.getMessage();
		String name = m.getFrom();
		long time = m.getCreationTime().getTime();
		try {
			stmt.setString(1, name);
			stmt.setString(2, message);
			stmt.setLong(3, time);
			stmt.executeUpdate();
			stmt = connection.prepareStatement(update);
			stmt.executeUpdate();
		}
		
		finally {
			stmt.close();
		}
		connection.commit();
	}
	
	public List<RelayMessage> getRecent() throws SQLException {
		String stmt = "SELECT nick,message,timeposted FROM messages ORDER BY timeposted DESC LIMIT 10"; 
		PreparedStatement retrieve = connection.prepareStatement(stmt);
		LinkedList<RelayMessage> resultList = new LinkedList<RelayMessage>();
		RelayMessage temp;
		try {
			ResultSet rs = retrieve.executeQuery();
			try {
				while (rs.next()) {
					String from = rs.getString(1);
					String message = rs.getString(2);
					long time = rs.getLong(3);
					Date d = new Date(time);
					temp = new RelayMessage(from,message,d);
					resultList.addFirst(temp); //The bottom 10 messages in database are latest and adding them first to list outputs in received order
				}
			}
			
			finally {
				rs.close();
			}
		}
		
		finally {
			retrieve.close();
		}
		
		return resultList;
	}

}
