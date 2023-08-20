package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
	private String driver = "com.mysql.jdbc.Driver";
	
	private String server = "jdbc:mysql://localhost:3325";
	//private String server = "jdbc:mysql://localhost:3306";
	
	private String databaseName = "scrap";
	private String username = "root";
	
	private String password = "";
	
	private String unicode = "?useUnicode=yes&characterEncoding=UTF-8";
	//"useUnicode=true&amp;characterEncoding=UTF-8";
	
	private Connection connection;
	private Statement statement;
	
	public DBConnection() throws ClassNotFoundException, SQLException {
		Class.forName(driver);
		connection = DriverManager.getConnection(server + "/" + databaseName + "?" + unicode, username, password);
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public int executeUpdate(String sql) throws SQLException {
		if(statement != null) statement.close();
		statement = connection.createStatement();
		return statement.executeUpdate(sql);
	}
	
	public ResultSet executeQuery(String query) throws SQLException {
		if(statement != null) statement.close();
		statement = connection.createStatement();
		return statement.executeQuery(query);
	}
	
	public void close() {
		try {
			if(statement != null) statement.close();
		} catch(SQLException e) { }
		try {
			connection.close();
		} catch(SQLException e) { }
	}
}