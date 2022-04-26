package FND;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 *
 * @author M.Faisal1521
 */
public class FND {

    public static String getCurrentDate() {
       // /get File with CurrentDateTime
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String logfileName = dtf.format(LocalDateTime.now());
        System.out.println("Date ---------------" +  logfileName);
        return logfileName;
    }

}
