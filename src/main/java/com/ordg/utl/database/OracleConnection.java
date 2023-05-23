package com.ordg.utl.database;

import com.ordg.utl.collections.Constants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ordg.utl.log.Console;

public class OracleConnection {
    private static Connection connection;

    public static Connection getConnection() {
        try {
                if (connection == null || connection.isClosed()) 
                {
                    Class.forName("oracle.jdbc.driver.OracleDriver");
                    connection = DriverManager.getConnection(Constants.get("DBA_STRING"), Constants.get("DBA_USER_NAME"), Constants.get("DBA_PASSWORD"));
                }
            }
        catch (Exception ex) {
            Console.showError("Database connection issue", OracleConnection.class);
            Console.write(ex);
        }
    
     return connection;
        }
        
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }
    
}
