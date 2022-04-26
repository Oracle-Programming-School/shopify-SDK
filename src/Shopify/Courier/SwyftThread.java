/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.Courier;

import FND.Global;
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
import okhttp3.MediaType;
import okhttp3.RequestBody;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Faisal
 */
public class SwyftThread {

   private DB db = null;
   private HashMap < String, String > hash_map = new HashMap < String, String > ();
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
   public void generateCN() {
      try {
         Statement Dstmt = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
         ResultSet Drs = Dstmt.executeQuery("select COMPANY_NAME, ORDER_ID,COST_CENTER_CODE,consignee_Name , CONSIGNEE_ADDRESS,CONSIGNEE_MOB_NO,CONSIGNEE_EMAIL,DESTINATION_CITY_id,ORIGIN_CITY_NAME,DESTINATION_CITY_NAME,destination_city_code,to_char(WEIGHT,'990D9') WEIGHT,PIECES,COD_AMOUNT,REPLACE(CUSTOMER_REFERENCE_NO,'#','') CUSTOMER_REFERENCE_NO,case when COD_AMOUNT > 0 then 'COD' else 'NONCOD' end  SERVICES,PRODUCT_DETAILS,FRAGILE,DESTINATION_CITY_NAME,REMARKS,CUSTOMER_REFERENCE_NO,CN_DETAILS_ID,INSURANCE_VALUE  from MF_Shopify_CN_details where organization_id = 299 and STATUS is null and CN_NUMBER is null and CN_GENERATION_ERROR is null and COMPANY_NAME ='SWYFT'");
         for (int D = 1; D <= getRowCount(Drs); D++) {
            Drs.absolute(D); //MOVE TO ROW 
            Console.write("SWYFT CN generation Start for Order ID :  " + Drs.getString(2));

            ////////////////////
            String responseString = null;
            try {
               OkHttpClient client = new OkHttpClient().newBuilder().build();
               MediaType mediaType = MediaType.parse("application/json");
               
               /*@23-03-22 by abdul ahad 
               In JSON String Change CONSIGNEE_CITY parameter value 
               from destination_city_name column to destination_city_code column */
               
               String jsonData = new String("[ { \"ORDER_ID\":\"" + Drs.getString("CUSTOMER_REFERENCE_NO") + "\", \"ORDER_TYPE\":\"" + Drs.getString("SERVICES") + "\" , \"CONSIGNEE_FIRST_NAME\":\"" + Drs.getString("CONSIGNEE_NAME") + "\", \"CONSIGNEE_LAST_NAME\":\"" + Drs.getString("CONSIGNEE_NAME") + "\", \"CONSIGNEE_EMAIL\":\"" + Drs.getString("CONSIGNEE_EMAIL") + "\", \"CONSIGNEE_PHONE\":\"" + Drs.getString("CONSIGNEE_MOB_NO") + "\", \"CONSIGNEE_CITY\":\"" + Drs.getString("destination_city_code") + "\" , \"CONSIGNEE_ADDRESS\":\"" + Drs.getString("CONSIGNEE_ADDRESS") + "\", \"PACKAGING\":\"Flyer\", \"ORIGIN_CITY\" : \"LHE\", \"PIECES\":" + Drs.getString("PIECES") + ", \"COD\" : " + Drs.getString("COD_AMOUNT") + ", \"DESCRIPTION\":\"" + Drs.getString("PRODUCT_DETAILS") + "\", \"WEIGHT\":" + Drs.getString("WEIGHT") + ", \"SHIPPER_ADDRESS_ID\" : \"PL-697852\" } ]");
               RequestBody body = RequestBody.create(mediaType, jsonData);
               Request request = new Request.Builder()
                  .url("https://vendor-api.swyftlogistics.com:3000/api/10de/api-upload")
                  .method("POST", body)
                  .addHeader("Content-Type", "application/json")
                  .addHeader("Authorization", "d1de")
                  .build();
               Response response = client.newCall(request).execute();
               ///////////////////
               Console.show(jsonData);

               //JSON parser object to parse read file
               JSONParser jsonParser = new JSONParser();
               //Read JSON file
               responseString = (String) response.body().string();
               Console.show(" Response from Swft " + responseString);
               Object obj = jsonParser.parse(responseString);

               JSONObject WebResponceObj = (JSONObject) obj;

               JSONArray dataArry = (JSONArray) WebResponceObj.get("data");

               if (dataArry != null) {

                  JSONObject DataObj = (JSONObject) dataArry.get(0);

                  //add CN information Successfully
                  CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                  dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                  dCall.setString(2, "SUCCESS");
                  dCall.setString(3, DataObj.get("parcelId").toString());
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
               //add CN information failure
               CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
               dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
               dCall.setString(2, "FAIL");
               dCall.setString(3, responseString);
               dCall.execute();
               dCall.close();
            }
         }
         Dstmt.close();
      } catch (SQLException ex) {
         Console.write("Swyft CN Generation Error : " + ex.getMessage());
      }
   }

   public SwyftThread(DB pdp) {
      db = pdp;
   }

   public allCities[] FetchAllCallCourierCities() {
      allCities[] allCitiesDataArray = null;

      ///API call
      try {

         OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
         Request request = new Request.Builder()
            .url("https://api.swyftlogistics.com:3000/api/5ee0de/get-cities")
            //    .url("https://vendor-api.swyftlogistics.com:3000/api/Cities")
            .method("GET", null)
            .addHeader("Authorization", "d17ba222-4c24")
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

                  l_allCities.CityId = "0";
                  l_allCities.CityName = (String) cityValue.get("name").toString();
                  l_allCities.CityCode = (String) cityValue.get("code").toString();
                  l_allCities.Area = "None";
                  l_allCities.CompanyName = "Swyft";
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

   } ///Cities ENd 
   public void fetchNewCities() {

      try {
         int arrySize = 0;
         OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
         Request request = new Request.Builder()
            .url("https://vendor-api.swyftlogistics.com:3000/api/Cities")
            .method("GET", null)
            .addHeader("Content-Type", "application/json")
            .build();

         Response response = client.newCall(request).execute();
         String jsonResponse = response.body().string();

         System.out.println("jsonResponse  " + jsonResponse);

         if (response.code() == 200) {

            Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonResponse);
            arrySize = JsonPath.read(document, "$.length()");

            for (int ldx = 0; ldx < arrySize; ldx++) {
               String city_code = JsonPath.read(document, "$.[" + ldx + "].code");
               String city_name = JsonPath.read(document, "$.[" + ldx + "].name");
               hash_map.put(city_name, city_code);
               int cityArray = JsonPath.read(document, "$.[" + ldx + "].cities.length()");

               for (int ldx2 = 0; ldx2 < cityArray; ldx2++) {

                  city_name = JsonPath.read(document, "$.[" + ldx + "].cities[" + ldx2 + "].name");
                  hash_map.put(city_name, city_code);

               }

            }

            for (Map.Entry < String, String > entry: hash_map.entrySet()) {
              // String city = entry.getKey();
              // String code = entry.getValue();
             //  System.out.println("City Name: " + city + " City Code " + code);
               try {
                 
                  CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.Post_City_Name(p_Company_Name => :p_Company_Name,p_city_ID => :p_city_ID,p_city_Name => :p_city_Name,p_city_Code => :p_city_Code,p_area => :p_area); end;");
                  dCall.setString(1, "Swyft");
                  dCall.setString(2, "0");
                  dCall.setString(3, entry.getKey());
                  dCall.setString(4, entry.getValue());
                  dCall.setString(5, "None");
                  dCall.execute();
                  dCall.close();
                 Console.write("Swyft Cities fetched successfully...!");
               } catch (Exception ex) {
                  Console.write("Error while run App_UTL.Post_City_Name " + ex.getMessage());
               }

            }

         }

      } catch (Exception ex) {
         Console.write("Swyft fetch citites has error -> " + ex.getMessage());
         ex.printStackTrace();
      }
    
   }

}