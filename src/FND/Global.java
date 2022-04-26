package FND;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import net.sourceforge.barbecue.output.OutputException;

/**
 *
 * @author Faisal
 */
public class Global {
    
    public static String dbFilePath = "\\DatabasePassword.txt";
    public static String CandeladbPath = "jdbc:sqlserver://192.168.1.1\\mssqlserver;user=test;password=test";

    public static String SHOPIFY_SRL_IN_HOUSE_KEY =  "Basic MDI5ZDRkMDE5NDVjMmJmMDhkNGRkM2ZlN=";
    public static String SHOPIFY_SRL_FULFILLMENT_KEY = "Basic MmQ2YmE=";
    public static String ShopifyAppURL =  "https://mfaisal1521.myshopify.com";
    ///lhe0047
    public static String TCSclient_id =  "1000";
    public static String TCS_URL =  "https://apis.tcscourier.com/production";
    public static String TPL_APIkey = "testAPI";
    
    
    public static String Trax_APIkey ="test";
    // List of API calls 
    public static String Synchronize_ERP =  "Synchronize_ERP" ;
    public static String NewOrders       =  "NewOrders" ;
    
    //Express Courier
    public static String ExpressURL       =  "http://xpresscourierlink.com" ;
    public static String ExpressUserName       =  "Test" ;
    public static String ExpressPassword       =  "Test" ;
    
    public static String PosteXToken       =  "ZDEyOTE=" ;
    public static String PosteXBaseurl       =  "https://api.postex.pk/services/integration/api" ;

    public static void GenerateBarcode(String pOrderid,String Barcode) throws BarcodeException, OutputException
    {
        //Get 128B Barcode instance from the Factory
        Barcode barcode = BarcodeFactory.createCode128B(Barcode);
        barcode.setBarHeight(60);
        barcode.setBarWidth(2);
        File imgFile = new File("C:\\Oracle\\Shopify\\images\\"+pOrderid+".png");
        //Write the bar code to PNG file
        BarcodeImageHandler.savePNG(barcode, imgFile);
        
    }
    
    //nvl
    public static String nvl(Object v) {
        if (v == null) {
            return "null";
        } else {
            return v.toString();
        }
    }
    
}









