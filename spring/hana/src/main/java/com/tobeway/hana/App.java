package com.tobeway.hana;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Hello world!
 *
 */
public class App 
{
	public static String connectionString = "jdbc:sap://192.168.1.171:39015";
	public static String user = "TOBEPRODSPEC";
	public static String password = "TOBEPRODSPEC";
    public static void main( String[] args )
    {
        System.out.println( "Hello HANA!" );
        Connection connection = null;
		try {
			connection = (Connection) DriverManager.getConnection(connectionString, user, password);
		} catch (SQLException e) {
			System.err.println("Connection Failed. User/Passwd Error? Message: " + e.getMessage());
			return;
		}
		if (connection != null) {
			try {
				System.out.println("Connection to HANA successful!");
				Statement stmt = connection.createStatement();
				ResultSet resultSet =  stmt.executeQuery("select 'hello world' from dummy");
				resultSet.next();
				String hello = resultSet.getString(1);
				System.out.println(hello);
			} catch (SQLException e) {
				System.err.println("Query failed!");
			}
		}
    }
}
