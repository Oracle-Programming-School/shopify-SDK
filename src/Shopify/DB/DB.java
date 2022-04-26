/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.DB;

import FND.Global;
import Main.InventoryMainPage;
import Shopify.Log.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author M.Faisal1521
 */
public class DB {

    String ConnectString;
    String ConnectUSER;
    String ConnectPassword;
    Connection ApplicationSession = null;
    Connection mssqlConnection = null;
    Connection    CandelaConnection = null;

    public DB() {
       // mssqlConnection=this.getSQLConnection();
       // CandelaConnection = this.getCandelaConnection();
    }

    private boolean isDBConfigFileExits() {
        /*Get Configration File*/
        File fileSoucrce = new File(System.getenv("APPDATA") + Global.dbFilePath);
        Scanner myReader;
        //this.console("Configration file loaded to program.");
        try {
            String[] fileText = new String[10];
            int i = 0;
            myReader = new Scanner(fileSoucrce);
            while (myReader.hasNextLine()) {
                fileText[i++] = myReader.nextLine();
            }
            myReader.close();

            ConnectString = fileText[0];
            ConnectUSER = fileText[1];
            ConnectPassword = fileText[2];

            // Configration Loaded
            return true;

        } catch (FileNotFoundException ex) {
            Console.write("Configration file not found at " + fileSoucrce.getAbsolutePath());
            return false;
        }

    } //isDBConfigFileExits

    /**
     * GetDBConnection
     */
    public Connection getDBConnection() {

        if (ApplicationSession != null) {

            try {
                if (!ApplicationSession.isClosed()) {
                    return ApplicationSession;
                }
            } catch (SQLException ex) {
                Console.write("Error ! Connection is not re-opening. " + ex.getMessage());
            }
        }

        String ReturnString = null;

        if (!isDBConfigFileExits()) {
            ReturnString = "Database connection not found due to configration file.";
        }

        try {
            //step1 load the driver class
            Class.forName("oracle.jdbc.driver.OracleDriver");
            //step2 create  the connection object
            ApplicationSession = DriverManager.getConnection(ConnectString, ConnectUSER, ConnectPassword);
            
            
           
            
            
            

        } catch (SQLException ex) {
            Logger.getLogger(InventoryMainPage.class.getName()).log(Level.SEVERE, null, ex);
            ReturnString = "Database connection not found : " + ex.getMessage();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(InventoryMainPage.class.getName()).log(Level.SEVERE, null, ex);
            ReturnString = "Java error database class not found.";
        }

        if (ReturnString == null) {
            Console.write("Database Connected");
        } else {
            Console.write(ReturnString);
          
        }        

        return ApplicationSession;    

    }
    /*//Comment For Imni Development 
    public Connection getSQLConnection() {
        
        if (mssqlConnection != null) {

            try {
                if (!mssqlConnection.isClosed()) {
                    return mssqlConnection;
                }
            } catch (SQLException ex) {
                this.console.write("SQL server Error ! Connection is not re-opening. " + ex.getMessage());
            }
        }
        
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            Connection conn = DriverManager.getConnection(Global.mySQLdbPath);
            
            if (conn != null) {
               mssqlConnection = conn;
            }
            
            this.console.write(" SQL server is connected.");
            
            return conn;
        } catch (SQLException ex) {
            this.console.write("SQL server connectivity Error ! " + ex.getMessage());
        }
        
        return mssqlConnection;
    }
    *///Comment For Imni Development 

    
    ///Candela Connection 
     public Connection getCandelaConnection() {
        
        if (CandelaConnection != null) {

            try {
                if (!CandelaConnection.isClosed()) {
                    return CandelaConnection;
                }
            } catch (SQLException ex) {
                Console.write("SQL server Error ! Connection is not re-opening. " + ex.getMessage());
            }
        }
        
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            Connection conn = DriverManager.getConnection(Global.CandeladbPath);
            
            if (conn != null) {
               CandelaConnection = conn;
            }
            
            Console.write(" Candela server is connected.");
            
            return conn;
        } catch (SQLException ex) {
            Console.write("Candela server connectivity Error ! " + ex.getMessage());
        }
        
        return CandelaConnection;
    }
}
