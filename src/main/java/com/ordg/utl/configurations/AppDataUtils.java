package com.ordg.utl.configurations;

import com.ordg.utl.log.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.ordg.ApplicationStartPoint;
import com.ordg.utl.collections.Constants;
import com.ordg.utl.database.OracleConnection;

public class AppDataUtils {

    private static final String ORADIGITALS_DIR = System.getenv("APPDATA") + "\\Oradigitals";
    private static final String POS_DIR = ORADIGITALS_DIR + "\\PointOfSale";
    private static final String DATA_DIR = POS_DIR + "\\Data";
    private static final String REPORTS_DIR = POS_DIR + "\\Reports";
    private static final String CONFIG_DIR = POS_DIR + "\\Config";
    private static final String LOG_DIR = POS_DIR + "\\Log";
    private static final List<String> DIRECTORIES = Arrays.asList(ORADIGITALS_DIR, POS_DIR, DATA_DIR, REPORTS_DIR, CONFIG_DIR, LOG_DIR);
    private static PrintWriter logWriter;

    public static String getReportTmpPath() {
        return LOG_DIR;
    }

    public static String getConfigPath() {
        return CONFIG_DIR + "\\";
    }

    public static boolean isOradigitalsDirectoryExists() {
        File directory = new File(ORADIGITALS_DIR);
        return directory.exists();
    }

    public static boolean isDataDirectoryExists() {
        File directory = new File(DATA_DIR);
        return directory.exists();
    }

    public static boolean isReportsDirectoryExists() {
        File directory = new File(REPORTS_DIR);
        return directory.exists();
    }

    public static boolean isConfigDirectoryExists() {
        File directory = new File(CONFIG_DIR);
        return directory.exists();
    }

    public static boolean isLogDirectoryExists() {
        File directory = new File(LOG_DIR);
        return directory.exists();
    }

    public static boolean isConfigurationFileExists() {

        File configFile = new File(CONFIG_DIR, "Configuration.xml");

        File TestconfigFile = new File(CONFIG_DIR, "PostTestConfiguration.xml");

        try {

            if (TestconfigFile.exists()) {
                Constants.initializeConfigValues(CONFIG_DIR + File.separator + "PostTestConfiguration.xml");
                return TestconfigFile.exists();
            } else {

                Constants.initializeConfigValues(CONFIG_DIR + File.separator + "Configuration.xml");
                return configFile.exists();
            }

        } catch (Exception ex) {
            Console.showError("Error in File Parsing : " + "Configuration xml", true, AppDataUtils.class);
        }
        
        return false;
    }

    public static void createDirectories() {
        for (String directory : DIRECTORIES) {
            File file = new File(directory);
            if (!file.exists()) {
                boolean created = file.mkdirs();
                if (!created) {
                    Console.showError("Failed to create directory: " + directory, true, AppDataUtils.class);
                }
            }
        }
    }

    public static List<String> getDirectories() {
        return new ArrayList<>(DIRECTORIES);
    }

    public static boolean areAllDirectoriesExists() {
        for (String directory : DIRECTORIES) {
            if (!new File(directory).exists()) {
                return false;
            }
        }
        createDirectories();
        return true;
    }

    public static void writeLog(String message) {
        if (logWriter == null) {
            try {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String fileName = LOG_DIR + "\\" + "POS_" + formatter.format(now) + ".log";
                logWriter = new PrintWriter(new FileWriter(fileName, true));
            } catch (IOException e) {
                Console.showMessage("Failed to create log file: " + e.getMessage());
            }
        }
        logWriter.println(message);
        logWriter.flush(); // Flush the log buffer to ensure the log message is immediately written to the log file
    }

}
