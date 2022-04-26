/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.Courier;

import java.sql.ResultSet;
import java.sql.SQLException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import Shopify.DB.DB;
import Shopify.Log.Console;
import java.sql.CallableStatement;
import java.sql.Statement;

/**
 *
 * @author Faisal
 */
public class callCourier {
    private DB db = null;
    
    private int getRowCount(ResultSet resultSet) {
        if (resultSet == null) {
            return 0;
        }

        try {
            resultSet.last();
            return resultSet.getRow();
        } catch (SQLException exp) {
            exp.printStackTrace();
        } finally {
            try {
                resultSet.beforeFirst();
            } catch (SQLException exp) {
                exp.printStackTrace();
            }
        }

        return 0;
    }

    public callCourier(DB pdp) {
        db = pdp;
    }
    
    public allCities[] FetchAllCallCourierCities() {
        allCities[] allCitiesDataArray = null;
        
        ///API call
        try{
            
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
        .url("http://cod.callcourier.com.pk/API/CallCourier/GetCityListByService?serviceID=7")
        .method("GET", null)
        .build();
        Response response = client.newCall(request).execute();
         //JSON parser object to parse read file
           JSONParser jsonParser = new JSONParser();
            //Read JSON file
            Object obj = jsonParser.parse(response.body().string());
            JSONArray cityArr = (JSONArray) obj;
            // Array
            if (cityArr.size() > 0) {
                //Set Array Size 
                allCitiesDataArray = new allCities[cityArr.size()];

                for (int ldx = 0; ldx < cityArr.size(); ldx++) {
                    JSONObject cityValue = (JSONObject) cityArr.get(ldx);

                    //Get Current City 
                    allCities l_allCities = null;

                    if (cityValue != null) {
                        ///New Object 
                        l_allCities = new allCities();

                        l_allCities.CityId = (String) cityValue.get("CityID").toString();
                        l_allCities.CityName = (String) cityValue.get("CityName").toString();
                        l_allCities.CityCode = (String) cityValue.get("CityShortDesc").toString();
                        l_allCities.Area = (String) cityValue.get("ProvinceName").toString();
                        l_allCities.CompanyName = "Call Courier";
                    }
                    allCitiesDataArray[ldx] = l_allCities;
                }
            }
         } catch (Exception ex) {
            Console.write("Unexpected error while cities ! " + ex.getMessage());
            return allCitiesDataArray;
        }
        ///
        return allCitiesDataArray;
    
    }///Cities ENd 
    
    
     public void generateCN() {
        try {
            Statement Dstmt = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet Drs = Dstmt.executeQuery("select COMPANY_NAME, ORDER_ID,COST_CENTER_CODE,consignee_Name , CONSIGNEE_ADDRESS,CONSIGNEE_MOB_NO,CONSIGNEE_EMAIL,DESTINATION_CITY_id,ORIGIN_CITY_NAME,DESTINATION_CITY_NAME,WEIGHT,PIECES,COD_AMOUNT,CUSTOMER_REFERENCE_NO,case when COD_AMOUNT > 0 then 7 else 1 end  SERVICES,PRODUCT_DETAILS,FRAGILE,REMARKS,CUSTOMER_REFERENCE_NO,CN_DETAILS_ID,INSURANCE_VALUE  from MF_Shopify_CN_details where organization_id = 299 and  STATUS is null and CN_NUMBER is null and CN_GENERATION_ERROR is null and COMPANY_NAME ='Call Courier'");
            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 
                Console.write("Call Courier CN generation Start for Order ID :  " + Drs.getString(2));
               
                String URL = "http://cod.callcourier.com.pk/api/CallCourier/SaveBooking?"
                        + "loginId=lhr-00848"
                        + "&ConsigneeName="+Drs.getString("consignee_Name")
                        + "&ConsigneeRefNo="+Drs.getString("CUSTOMER_REFERENCE_NO")
                        + "&ConsigneeCellNo="+Drs.getString("CONSIGNEE_MOB_NO")
                        + "&Address="+Drs.getString("CONSIGNEE_ADDRESS")
                        + "&Origin=Lahore"
                        + "&DestCityId="+Drs.getString("DESTINATION_CITY_id")
                        + "&ServiceTypeId="+Drs.getString("SERVICES")
                        + "&Pcs="+Drs.getString("PIECES")
                        + "&Weight="+Drs.getString("WEIGHT")
                        + "&Description="+Drs.getString("PRODUCT_DETAILS")
                        + "&SelOrigin=Domestic"
                        + "&CodAmount="+Drs.getString("COD_AMOUNT")
                        + "&SpecialHandling=false"
                        + "&MyBoxId=1"
                        + "&Holiday=false"
                        + "&remarks="+Drs.getString("REMARKS")
                        + "&ShipperName=test RETAIL LIMITED"
                        + "&ShipperCellNo=11"
                        + "&ShipperArea=1"
                        + "&ShipperCity=1"
                        + "&ShipperAddress=1-"
                        + "&ShipperLandLineNo=+92(42) 111-738-245"
                        + "&ShipperEmail=wecare@eonline.pk";
                String responseString = null;
                ///Get Data CallCourier
                    try {
                   OkHttpClient client = new OkHttpClient().newBuilder().build();
                   Request request     = new Request.Builder()
                                        .url(URL)
                                        .method("GET", null)
                                        .build();
                                        Response response = client.newCall(request).execute();
                         
                        Console.show(URL);
                        
                        //JSON parser object to parse read file
                        JSONParser jsonParser = new JSONParser();
                        responseString= response.body().string();
                        //Read JSON file
                        Object obj = jsonParser.parse(responseString);
                        JSONObject tcsResponceObj = (JSONObject) obj;
                        
                        if (tcsResponceObj.get("Response").toString().equals("true")) {
                            //add CN information Successfully
                           CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                           dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                           dCall.setString(2, "SUCCESS");
                           dCall.setString(3,tcsResponceObj.get("CNNO").toString() );
                           dCall.execute();
                           dCall.close();
                        
                        } else {
                            //add CN information Successfully
                           CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                           dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                           dCall.setString(2, "FAIL");
                           dCall.setString(3,tcsResponceObj.get("Response").toString());
                           dCall.execute();
                           dCall.close();
                        }

                    } catch (Exception ex) {
                        Console.write("CN generation  Error found at " + ex.getMessage());
                         //add CN information Successfully
                           CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                           dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                           dCall.setString(2, "FAIL");
                           dCall.setString(3,responseString);
                           dCall.execute();
                           dCall.close();
                    }

            }
            Dstmt.close();
        } catch (SQLException ex) {
            Console.write("CN Generation Error : " + ex.getMessage());
        }
    }

    
}
