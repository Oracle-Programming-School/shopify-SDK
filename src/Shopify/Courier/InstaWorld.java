/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.Courier;

import Shopify.DB.DB;
import Shopify.Log.Console;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author abdul.ahad1
 */
public class InstaWorld {

   DB db = null;

   public InstaWorld(DB pdp) {
      db = pdp;
   }

   public void generateCN(int storeID) {

      PreparedStatement ps = null;
      ResultSet rs = null;
      String responseString = null;
      String status = null;
      String cnNumber = null;
      String errors = null;
      String orderId = null;

      try {

         String selectQuery = "select COMPANY_NAME, ORDER_ID,COST_CENTER_CODE,substr(consignee_Name,0,75)consignee_Name , CONSIGNEE_ADDRESS,CONSIGNEE_MOB_NO,CONSIGNEE_EMAIL,ORIGIN_CITY_NAME,DESTINATION_CITY_NAME,WEIGHT,PIECES,COD_AMOUNT,CUSTOMER_REFERENCE_NO,SERVICES,PRODUCT_DETAILS,FRAGILE, null REMARKS,CUSTOMER_REFERENCE_NO,CN_DETAILS_ID,INSURANCE_VALUE,STORE_ID  from MF_Shopify_CN_details where ORGANIZATION_ID = 299 and STATUS is null  and CN_NUMBER is null and CN_GENERATION_ERROR is null and COMPANY_NAME ='INSTA' and store_id = " + storeID;

         //String selectQuery = "select COMPANY_NAME, ORDER_ID,COST_CENTER_CODE,substr(consignee_Name,0,75)consignee_Name , CONSIGNEE_ADDRESS,CONSIGNEE_MOB_NO,CONSIGNEE_EMAIL,ORIGIN_CITY_NAME,DESTINATION_CITY_NAME,WEIGHT,PIECES,COD_AMOUNT,CUSTOMER_REFERENCE_NO,SERVICES,PRODUCT_DETAILS,FRAGILE, null REMARKS,CUSTOMER_REFERENCE_NO,CN_DETAILS_ID,INSURANCE_VALUE,STORE_ID  from MF_Shopify_CN_details where ORGANIZATION_ID = 299 and STATUS = 'FAIL'  and CN_NUMBER is null and CN_GENERATION_ERROR is null and COMPANY_NAME ='INSTA' and order_id = 4188246114378 and store_id = " + storeID;
         ps = db.getDBConnection().prepareStatement(
            selectQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            Console.write("Nothing to proceed for InstaWorld thread ");
         } else {
            try {

               do {
                  try {
                     orderId = rs.getString("ORDER_ID");
                     Console.write("InstaWorld CN generation Start for Order ID :  " + orderId);

                     OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(5, TimeUnit.MINUTES)
                        .writeTimeout(5, TimeUnit.MINUTES)
                        .readTimeout(5, TimeUnit.MINUTES)
                        .retryOnConnectionFailure(true)
                        .build();
                     MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                     RequestBody body = RequestBody.create(mediaType, "service_type=COD&from_address=Pakistan&to_address=" + rs.getString("CONSIGNEE_ADDRESS").trim() + "&to_contact=" + rs.getString("CONSIGNEE_MOB_NO").trim() + "&to_name=" + rs.getString("consignee_Name").trim() + "&to_city=" + rs.getString("DESTINATION_CITY_NAME").trim() + "&type=package&customer_reference=" + rs.getString("CUSTOMER_REFERENCE_NO").trim() + "&to_interfon=" + rs.getString("CONSIGNEE_MOB_NO").trim() + "&to_phone=" + rs.getString("CONSIGNEE_MOB_NO").trim() + "&ramburs=" + rs.getString("COD_AMOUNT").trim() + "&weight=" + rs.getString("WEIGHT").trim() + "&content=" + rs.getString("PRODUCT_DETAILS").trim());
                     Request request = new Request.Builder()
                        .url("http://app.couriermanager.eu/cscourier/API/create_awb")
                        .method("POST", body)
                        .addHeader("api_key", "VgULJswAL")
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();
                     Response response = client.newCall(request).execute();
                     String jsonResponse = response.body().string();

                     if (this.isJSONValid(jsonResponse) == true) {
                        Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonResponse);
                        status = JsonPath.read(document, "$.data.status");
                        if (status.equalsIgnoreCase("uncollected")) {

                           cnNumber = JsonPath.read(document, "$.data.no");

                           CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                           dCall.setString(1, rs.getString("CN_DETAILS_ID"));
                           dCall.setString(2, "SUCCESS");
                           dCall.setString(3, cnNumber);
                           dCall.execute();
                           dCall.close();

                           System.out.println("cnNumber = " + cnNumber);
                           Console.show("InstaWorld CN generated successfully for OrderId: " + orderId + " CN Number: " + cnNumber);

                        } else if (status.equalsIgnoreCase("draft")) {

                           errors = JsonPath.read(document, "$.data.errors");

                           CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                           dCall.setString(1, rs.getString("CN_DETAILS_ID"));
                           dCall.setString(2, "FAIL");
                           dCall.setString(3, errors);
                           dCall.execute();
                           dCall.close();
                           System.out.println("CN Fail due to errors = " + errors);
                           Console.show("InstaWorld CN generation failed for OrderId " + orderId + " --> " + errors);

                        }
                     }
                  } catch (Exception ex) {

                     Console.show("InstaWorld CN generation failed for OrderId: " + orderId + " --> " + ex.getMessage());

                     CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                     dCall.setString(1, rs.getString("CN_DETAILS_ID"));
                     dCall.setString(2, "FAIL");
                     dCall.setString(3, ex.getMessage());
                     dCall.execute();
                     dCall.close();

                  }
               } while (rs.next());
               rs.close();
               ps.close();

            } catch (Exception ex) {
               Console.show("Instaword CN Generation exception# 001 " + ex.getMessage());
               if (rs != null) {
                  rs.close();
               }
               if (ps != null) {
                  ps.close();
               }
            }
         }
      } catch (Exception ex) {
         Console.show("Instaword CN Generation exception# 002 " + ex.getMessage());
      }

   }

   public static boolean isJSONValid(String jString) {
      try {
         new org.json.JSONObject(jString);
      } catch (JSONException ex) {

         try {
            new JSONArray(jString);
         } catch (JSONException ex1) {
            return false;
         }
      }
      return true;
   }

}