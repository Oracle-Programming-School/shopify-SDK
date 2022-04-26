/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.Log;

import Main.InventoryMainPage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author M.Faisal1521
 */
public class Console{
    
    private static Logger allLog = null;
    private static int sequence =0;

    public static void init() {
        //ConsoleObject
        //get File with CurrentDateTime
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy hh_mm");
        String logfileName = dtf.format(LocalDateTime.now());

        try {
            //All-Console 
            allLog = (Logger) Logger.getLogger("JavaAPP_Log");
            FileHandler fh = new FileHandler("C:\\Oracle\\Shopify\\log\\" + logfileName + ".log");
            allLog.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());

        } catch (SecurityException e) {
            Console.show(e.getMessage());
        } catch (IOException ex) {
            Console.show(ex.getMessage());
        }

    }

    public static void show(String pText) {
        if (!InventoryMainPage.isLogStop)
        {
        String logText = (sequence++) + ") " + pText + "\n";
        InventoryMainPage.getConsoleText().append(logText);
        allLog.log(Level.SEVERE, pText);
        }
    }

    public static void write(String pText) {
        String logText = (sequence++) + ") " + pText + "\n";
        
        
        if (!InventoryMainPage.isLogStop)
        {
            InventoryMainPage.getConsoleText().append(logText);
        }
        
        allLog.log(Level.SEVERE, pText);
    }
    
    
    
}
