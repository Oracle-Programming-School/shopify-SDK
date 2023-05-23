package com.ordg;

import com.ordg.utl.collections.Constants;
import com.ordg.utl.configurations.AppDataUtils;
import com.ordg.utl.collections.AppThread;
import com.ordg.utl.log.Console;
import com.ordg.utl.dashboard.Dashboard;

public class ApplicationStartPoint {

    private static void init() {
        
        AppDataUtils.createDirectories();
        
         // check if Configuration.xml file exists
        if (!AppDataUtils.isConfigurationFileExists()) {
            Console.showError("Configuration.xml file not found. The application will now exit.", true,ApplicationStartPoint.class);
        }
        
        //Load Store configuration
        Constants.loadStoreConfiguration();
        
        AppThread at = new AppThread("loadItemsDetailsFromDatabase");
        at.start();
        
        Dashboard d = new Dashboard();
        
    }  
    
    public static void main(String[] args) {
        init();
    }
    
}
