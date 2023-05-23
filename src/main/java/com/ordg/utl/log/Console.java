package com.ordg.utl.log;

import javax.swing.JOptionPane;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.ordg.utl.configurations.AppDataUtils;

public class Console {

    private static final String APP_NAME = "PointOfSale";
    

    public static void showError(String message, Class<?> clazz) {
        writeLog("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "]" + "[ERROR] [" + clazz.getName() + "] " + message);
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showError(String message, boolean exitApp, Class<?> clazz) {
        writeLog("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "]" + "[ERROR] [" + clazz.getName() + "] " + message);
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        if (exitApp) {
            System.exit(1);
        }
    }

    public static void showNote(String message) {
        String logMessage = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] NOTE: " + message;
        writeLog(logMessage);
        JOptionPane.showMessageDialog(null, message, APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showMessage(String message) {
        String logMessage = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] MESSAGE: " + message;
        writeLog(logMessage);
        JOptionPane.showMessageDialog(null, message, APP_NAME, JOptionPane.PLAIN_MESSAGE);
    }

    public static void showCaution(String message) {
        String logMessage = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] CAUTION: " + message;
        writeLog(logMessage);
        JOptionPane.showMessageDialog(null, message, APP_NAME + " - Caution", JOptionPane.WARNING_MESSAGE);
    }

    private static void writeLog(String message) {
        AppDataUtils.writeLog(message);
    }

    public static void write(String message) {
        AppDataUtils.writeLog(message);
    }

    public static void write(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        write(stackTrace);
    }
    
    
    public static boolean getUserConfirmation(String message) {
        int response = JOptionPane.showConfirmDialog(null, message, "Confirm", JOptionPane.YES_NO_OPTION);
        return response == JOptionPane.YES_OPTION;
    }

}
