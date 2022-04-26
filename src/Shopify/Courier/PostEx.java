/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.Courier;

import Shopify.DB.DB;
import FND.Global;
import Shopify.Log.Console;
import java.sql.ResultSet;
import java.sql.SQLException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.sql.Types;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 *
 * @author Faisal
 */
public class PostEx {
    
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

    public PostEx(DB pdp) {
      db = pdp; 
    }

    public String nvl(Object v) {
        if (v == null) {
            return "null";
        } else {
            return v.toString();
        }
    }

    public void generateCN() {
        try {
            Statement Dstmt = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet Drs = Dstmt.executeQuery("select COMPANY_NAME, ORDER_ID,COST_CENTER_CODE,consignee_Name , CONSIGNEE_ADDRESS,CONSIGNEE_MOB_NO,CONSIGNEE_EMAIL,DESTINATION_CITY_id,ORIGIN_CITY_NAME,DESTINATION_CITY_NAME,WEIGHT,PIECES,COD_AMOUNT,REPLACE(CUSTOMER_REFERENCE_NO,'#','') CUSTOMER_REFERENCE_NO,case when COD_AMOUNT > 0 then 1 else 2 end  SERVICES,PRODUCT_DETAILS,FRAGILE,REMARKS,CUSTOMER_REFERENCE_NO,CN_DETAILS_ID,INSURANCE_VALUE  from MF_Shopify_CN_details where organization_id = 299 and STATUS is null and CN_NUMBER is null and CN_GENERATION_ERROR is null and COMPANY_NAME ='POSTEX' and rownum < 100");
            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 
                Console.write("PostEx CN generation Start for Order ID :  " + Drs.getString(2));

                String responseString = null;

                ///Get Data Trax
                try {
                    ////////////////////////////////
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, "{\n    \"cityName\": \""+Drs.getString("DESTINATION_CITY_NAME")+"\",\n    \"customerName\": \""+Drs.getString("consignee_Name")+"\",\n    \"customerPhone\": \""+Drs.getString("CONSIGNEE_MOB_NO")+"\",\n    \"deliveryAddress\": \""+Drs.getString("CONSIGNEE_ADDRESS")+"\",\n    \"invoiceDivision\": 1,\n    \"invoicePayment\": "+Drs.getString("COD_AMOUNT")+",\n    \"items\": "+Drs.getString("PIECES")+",\n    \"orderDetail\": \""+Drs.getString("PRODUCT_DETAILS")+"\",\n    \"orderRefNumber\": \""+Drs.getString("CUSTOMER_REFERENCE_NO")+"\",\n    \"transactionNotes\": \"\"\n}");
                    Request request = new Request.Builder()
                            .url(Global.PosteXBaseurl+"/order/v1/create-order")
                            .method("POST", body)
                            .addHeader("token", Global.PosteXToken)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    Response response = client.newCall(request).execute();
                    ////////////////////////////////


                    //JSON parser object to parse read file
                    JSONParser jsonParser = new JSONParser();
                    //Read JSON file
                    responseString = (String) response.body().string();
                    Console.show(" Response from Trax " + responseString);
                    Object obj = jsonParser.parse(responseString);

                    JSONObject PostExResponceObj = (JSONObject) obj;

                    if (PostExResponceObj.get("dist") != null) {
                        
                        JSONObject CNObject = (JSONObject)PostExResponceObj.get("dist");
                        
                        //add CN information Successfully
                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "SUCCESS");
                        dCall.setString(3, CNObject.get("trackingNumber").toString());
                        dCall.execute();
                        dCall.close();

                    } else {
                        //add CN information Successfully
                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "FAIL");
                        dCall.setString(3, PostExResponceObj.get("message").toString());
                        dCall.execute();
                        dCall.close();
                    }

                } catch (Exception ex) {
                    Console.write("PostEx CN generation  Error found at " + ex.getMessage());
                    //add CN information Successfully
                    CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                    dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                    dCall.setString(2, "FAIL");
                    dCall.setString(3, "Error Found");
                    dCall.execute();
                    dCall.close();
                }

            }
            Dstmt.close();
            Drs.close();
        } catch (SQLException ex) {
            Console.write("PostEx CN Generation Error : " + ex.getMessage());
        }
    }

    public allCities[] FetchAllCities() {
        allCities[] allCitiesDataArray = null;

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(Global.PosteXBaseurl+"/order/v1/get-operational-city")
                    .method("GET", null)
                    .addHeader("token", Global.PosteXToken)
                    .build();
            Response response = client.newCall(request).execute();

            //JSON parser object to parse read file
            JSONParser jsonParser = new JSONParser();

            String requestResponse = new String();
            requestResponse = response.body().string();
            Console.show(requestResponse);
            //Read JSON file
            Object obj = jsonParser.parse(requestResponse);
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

                        l_allCities.CityId = "0";
                        l_allCities.CityName = (String) cityValue.get("operationalCityName").toString();
                        l_allCities.CityCode = null;//(String) cityValue.get("cityCode").toString();
                        l_allCities.Area = (String) cityValue.get("countryName").toString();
                        l_allCities.CompanyName = "POSTEX";
                    }
                    allCitiesDataArray[ldx] = l_allCities;
                }
            }

        } catch (Exception ex) {
            Console.write("Unexpected error while cities ! " + ex.getMessage());
            return allCitiesDataArray;
        }

        return allCitiesDataArray;
    }

}
