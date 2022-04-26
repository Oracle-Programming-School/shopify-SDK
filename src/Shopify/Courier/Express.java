/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.Courier;

import FND.Global;
import Shopify.DB.DB;
import Shopify.Log.Console;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Faisal
 */
public class Express {
    
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

    public Express(DB pdp) {
          db = pdp;
    }
    
    public allCities[] FetchAllExpressCities()
    {
        
        allCities[] allCitiesDataArray = null;
         try {
             OkHttpClient client = new OkHttpClient().newBuilder().build();
             Request request = new Request.Builder().url(Global.ExpressURL+"/cod/getcodcities.php?login="+Global.ExpressUserName+"&pass="+Global.ExpressPassword)
                .method("GET", null)
                .build();
             Response response = client.newCall(request).execute();
             //JSON parser object to parse read file
            JSONParser jsonParser = new JSONParser();
            //Read JSON file
            Object obj = jsonParser.parse(response.body().string());
            JSONObject citySetObj = (JSONObject) obj;
            JSONArray cityArr = (JSONArray) citySetObj.get("Cities");
            // Array
            if (cityArr.size() > 0) {
                //Set Array Size 
                allCitiesDataArray = new allCities[cityArr.size()];
                    
                for (int ldx = 0; ldx < cityArr.size(); ldx++) {
                    JSONObject cityValue = (JSONObject) cityArr.get(ldx);
                    
                   //Get Current City Record
                    allCities l_allCities = new allCities();
                    
                    if (cityValue != null) {
                        l_allCities.CityId ="0"; //(String) cityValue.get("id").toString();
                        l_allCities.CityName = (String) cityValue.get("CityName").toString();
                        l_allCities.CityCode = (String) cityValue.get("CityCode").toString();
                        l_allCities.Area = (String) cityValue.get("CityZone").toString();
                        l_allCities.CompanyName = "EXPRESS";
                    }
                   
                    
                    allCitiesDataArray[ldx] = l_allCities;
                }
                
            }
            return allCitiesDataArray;
             
         } catch (Exception ex) {
            Console.write("Unexpected error while cities ! " + ex.getMessage());
            return allCitiesDataArray;
        }
    
    }

    
    public void generateCN() {
        try {
            Statement Dstmt = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet Drs = Dstmt.executeQuery("select COMPANY_NAME, ORDER_ID,COST_CENTER_CODE,consignee_Name , CONSIGNEE_ADDRESS,CONSIGNEE_MOB_NO,CONSIGNEE_EMAIL,DESTINATION_CITY_id,ORIGIN_CITY_NAME,destination_city_code,DESTINATION_CITY_NAME,WEIGHT,PIECES,COD_AMOUNT,REPLACE(CUSTOMER_REFERENCE_NO,'#','') CUSTOMER_REFERENCE_NO,PRODUCT_DETAILS,FRAGILE,REMARKS,CN_DETAILS_ID,INSURANCE_VALUE  from MF_Shopify_CN_details where organization_id = 299 and STATUS is null and CN_NUMBER is null and CN_GENERATION_ERROR is null and COMPANY_NAME ='EXPRESS'");
            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 
                Console.write("Express CN generation Start for Order ID :  " + Drs.getString(2));

                String responseString = null;
                String URL = Global.ExpressURL+"/cod/cod_booking.php?login="+Global.ExpressUserName+"&pass="+Global.ExpressPassword+"&origincode=LHE&refno="+Drs.getString("CUSTOMER_REFERENCE_NO")+"&dest="+Drs.getString("destination_city_code")+"&consigneename="+Drs.getString("consignee_Name")+"&consigneeaddress="+Drs.getString("CONSIGNEE_ADDRESS")+"&consigneecontact="+Drs.getString("CONSIGNEE_MOB_NO")+"&weight="+Drs.getString("WEIGHT")+"&pcs="+Drs.getString("PIECES")+"&goods="+Drs.getString("PRODUCT_DETAILS")+"&codvalue="+Drs.getString("COD_AMOUNT")+"&remarks=None";

                ///Get Data Trax
                try {
OkHttpClient client = new OkHttpClient().newBuilder()
  .build();
Request request = new Request.Builder()
  .url(URL)
  .method("GET", null)
  .build();
                    Response response = client.newCall(request).execute();
                    ////////////////////////////////

                    Console.show(URL);

                    //JSON parser object to parse read file
                    JSONParser jsonParser = new JSONParser();
                    //Read JSON file
                    responseString = (String) response.body().string();
                    Console.show(" Response from Express " + responseString);
                    Object obj = jsonParser.parse(responseString);

                    JSONObject ExpressResponceObj = (JSONObject) obj;

                    if (ExpressResponceObj.get("OrderDetails") != null) {
                        
                        JSONObject ExpressOrderDetailsObj = (JSONObject) ExpressResponceObj.get("OrderDetails");
                        
                        //add CN information Successfully
                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "SUCCESS");
                        dCall.setString(3, ExpressOrderDetailsObj.get("ConsignmentNumber").toString());
                        dCall.execute();
                        dCall.close();

                    } else {
                        //add CN information Successfully
                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "FAIL");
                        dCall.setString(3, responseString);
                        dCall.execute();
                        dCall.close();
                    }

                } catch (Exception ex) {
                    Console.write("TPL CN generation  Error found at " + ex.getMessage());
                    //add CN information Successfully
                    CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                    dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                    dCall.setString(2, "FAIL");
                    dCall.setString(3, responseString);
                    dCall.execute();
                    dCall.close();
                }

            }
            Dstmt.close();
            Drs.close();
        } catch (SQLException ex) {
            Console.write("TPL CN Generation Error : " + ex.getMessage());
        }
    }



}


