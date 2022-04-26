/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.Courier;

import Shopify.DB.DB;
import FND.Global;
import Shopify.Log.Console;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author M.Faisal1521
 */
public class TCS {

    private OkHttpClient client;
    private String urlString;
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

    public TCS(DB pdp) {
        db = pdp;
        client = new OkHttpClient().newBuilder().build();
        urlString = Global.TCS_URL+"/v1/cod/";
    }

    private Request getRequest(String pURL) {
        return new Request.Builder().url(urlString + pURL)
                .method("GET", null)
                .addHeader("x-ibm-client-id", Global.TCSclient_id).build();
    }

    public allCities[] FetchAllTCScities() {
        allCities[] allCitiesDataArray = null;

        try {
            Response response = client.newCall(getRequest("cities")).execute();

            //JSON parser object to parse read file
            JSONParser jsonParser = new JSONParser();
            //Read JSON file
            Object obj = jsonParser.parse(response.body().string());
            JSONObject citySetObj = (JSONObject) obj;
            JSONArray cityArr = (JSONArray) citySetObj.get("allCities");
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

                        l_allCities.CityId = (String) cityValue.get("cityID").toString();
                        l_allCities.CityName = (String) cityValue.get("cityName").toString();
                        l_allCities.CityCode = (String) cityValue.get("cityCode").toString();
                        l_allCities.Area = (String) cityValue.get("area").toString();
                        l_allCities.CompanyName = "TCS";
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

    //nvl
    public String nvl(Object v) {
        if (v == null) {
            return "null";
        } else {
            return v.toString();
        }
    }

    public void generateCN(int storeID) {
        try {
            Statement Dstmt = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
          
            ResultSet Drs = Dstmt.executeQuery("select COMPANY_NAME, ORDER_ID,COST_CENTER_CODE,substr(consignee_Name,0,75)consignee_Name , CONSIGNEE_ADDRESS,CONSIGNEE_MOB_NO,CONSIGNEE_EMAIL,ORIGIN_CITY_NAME,DESTINATION_CITY_NAME,WEIGHT,PIECES,COD_AMOUNT,CUSTOMER_REFERENCE_NO,SERVICES,PRODUCT_DETAILS,FRAGILE, null REMARKS,CUSTOMER_REFERENCE_NO,CN_DETAILS_ID,INSURANCE_VALUE,STORE_ID  from MF_Shopify_CN_details where ORGANIZATION_ID = 299 and STATUS is null  and CN_NUMBER is null and CN_GENERATION_ERROR is null and COMPANY_NAME ='TCS' and store_id = "+ storeID +" and rownum < 100 ");
           
            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 
                Console.write("TCS CN generation Start for Order ID :  " + Drs.getString(2));
                String ResponseString = null;
                ///Get Data TCS
                    try {
                        OkHttpClient client = new OkHttpClient().newBuilder().build();
                        MediaType mediaType = MediaType.parse("application/json");
                        String JsonData = "{\r\n  \"userName\": \"test\",\r\n  \"password\": \"123\",\r\n  \"costCenterCode\": \"" + Drs.getString("COST_CENTER_CODE") + "\",\r\n  \"consigneeName\": \"" + Drs.getString("consignee_Name") + "\",\r\n  \"consigneeAddress\": \"" + Drs.getString("CONSIGNEE_ADDRESS") + "\",\r\n  \"consigneeMobNo\": \"" + Drs.getString("CONSIGNEE_MOB_NO") + "\",\r\n  \"consigneeEmail\": \"" + Drs.getString("CONSIGNEE_EMAIL") + "\",\r\n  \"originCityName\": \"" + Drs.getString("ORIGIN_CITY_NAME") + "\",\r\n  \"destinationCityName\": \"" + Drs.getString("DESTINATION_CITY_NAME") + "\",\r\n  \"weight\": \"" + Drs.getString("WEIGHT") + "\",\r\n  \"pieces\": " + Drs.getString("PIECES") + ",\r\n  \"codAmount\": \"" + Drs.getString("COD_AMOUNT") + "\",\r\n  \"customerReferenceNo\": \"" + Drs.getString("CUSTOMER_REFERENCE_NO") + "\",\r\n  \"services\": \"" + Drs.getString("SERVICES") + "\",\r\n  \"productDetails\": \"" + Drs.getString("PRODUCT_DETAILS") + "\",\r\n  \"fragile\": \"" + Drs.getString("FRAGILE") + "\",\r\n  \"remarks\": \"" + Drs.getString("REMARKS") + "\",\r\n  \"insuranceValue\": \"" + Drs.getString("INSURANCE_VALUE") + "\"\r\n}";
                        RequestBody body = RequestBody.create(mediaType,JsonData );
                        Request request = new Request.Builder().url(Global.TCS_URL+"/v1/cod/create-order").method("POST", body)
                                .addHeader("x-ibm-client-id", Global.TCSclient_id)
                                .addHeader("accept", "application/json")
                                .addHeader("content-type", "application/json")
                                .build();
                        
                        Console.write(JsonData);
                        
                        Response response = client.newCall(request).execute();
                         
                        
                        //JSON parser object to parse read file
                        JSONParser jsonParser = new JSONParser();
                         ResponseString = response.body().string();
                        
                        Console.write(ResponseString);
                        //Read JSON file
                        Object obj = jsonParser.parse(ResponseString);
                        JSONObject tcsResponceObj = (JSONObject) obj;
                        JSONObject tcsReturnStatus = (JSONObject) tcsResponceObj.get("returnStatus");

                        String cnMessage = null;

                        if (tcsReturnStatus.get("status").toString().equals("SUCCESS")) {
                            JSONObject tcsBookingReply = (JSONObject) tcsResponceObj.get("bookingReply");
                            cnMessage = tcsBookingReply.get("result").toString();
                        } else {
                            cnMessage = tcsReturnStatus.get("message").toString();
                        }
                        
                        
                        //add CN information
                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, tcsReturnStatus.get("status").toString());
                        dCall.setString(3, cnMessage);
                        dCall.execute();
                        dCall.close();
                        
                        Console.write("TCS API : " + cnMessage);
                        
                    } catch (Exception ex) {
                        Console.write("CN generation  Error found at " + ex.getMessage());
                        //add CN information
                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "FAIL");
                        dCall.setString(3,ResponseString  );
                        dCall.execute();
                        dCall.close();
                        
                    }

            }
            Dstmt.close();
        } catch (SQLException ex) {
            Console.write("CN Generation Error : " + ex.getMessage());
        }
    }
    /*
    
    
    
     */
}
