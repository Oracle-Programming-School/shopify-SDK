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
import org.json.JSONObject;



/**
 *
 * @author abdul.ahad1
 */
public class Jomo {

   DB db = null;

   public Jomo(DB pdp) {
      db = pdp;
   }

   public void generateCN(int storeID) {

      PreparedStatement ps = null;
      ResultSet rs = null;
      String responseString = null;
      String cnNumber = null;
      String errors = null;
      String orderId = null;

      try {

         String selectQuery = "select jcd.* from srl_shopify_jomo_cn_details_v jcd where 1=1 and jcd.STORE_ID  = ?";
         ps = db.getDBConnection().prepareStatement(
            selectQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
         ps.setInt(1, storeID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            Console.write("Nothing to proceed for JOMO");
         } else {
            try {

               do {
                  try {
                     orderId = rs.getString("ORDER_ID");
                     Console.write("JOMO CN generation Start for Order ID :  " + orderId);

                     
                     JSONArray jomoJson = new JSONArray();
                     JSONObject jomoArrayObject = new JSONObject();
                     jomoArrayObject.put("orderNo", rs.getString("CUSTOMER_REFERENCE_NO"));
                     jomoArrayObject.put("orderTime", rs.getString("order_Time"));
                     jomoArrayObject.put("distributionCenter", "Lahore");
                     jomoArrayObject.put("pickupCity", "Lahore");
                     jomoArrayObject.put("pickupApartment", rs.getString("pickup_address"));
                     jomoArrayObject.put("packageWeight", rs.getString("WEIGHT"));
                     jomoArrayObject.put("numberOfItems", rs.getString("PIECES"));
                     jomoArrayObject.put("paymentType", "COD");
                     jomoArrayObject.put("packageValue", rs.getString("COD_AMOUNT"));
                     jomoArrayObject.put("deliverEmail", rs.getString("CONSIGNEE_EMAIL"));
                     jomoArrayObject.put("deliverPhoneNumber", rs.getString("CONSIGNEE_MOB_NO"));
                     jomoArrayObject.put("deliverAccountName", rs.getString("consignee_Name"));
                     jomoArrayObject.put("deliverApartment", rs.getString("CONSIGNEE_ADDRESS"));
                     jomoArrayObject.put("deliverCity", rs.getString("DESTINATION_CITY_NAME"));
                     jomoArrayObject.put("deliverNotes", "NA");
                     jomoArrayObject.put("productDetail", rs.getString("PRODUCT_DETAILS"));
                     jomoArrayObject.put("domain", "jomo.pk");
                     jomoArrayObject.put("fromDomain", "sapphireonline.pk");
                     jomoArrayObject.put("toDomain", "jomo.pk");
                     JSONArray itemsArray = new JSONArray();
                     JSONObject itemsArrayObject = new JSONObject();
                     itemsArrayObject.put("itemCode", "null");
                     itemsArrayObject.put("itemName", "null");
                     itemsArrayObject.put("itemPrice", 0);
                     itemsArrayObject.put("itemQuantity", 0);
                     itemsArrayObject.put("itemType", "");
                     itemsArrayObject.put("itemWeight", 0);
                     itemsArray.put(itemsArrayObject);
                     jomoArrayObject.put("items", itemsArray);
                     jomoJson.put(jomoArrayObject);
                    
                     Console.write(jomoJson.toString());

                     

                     OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(5, TimeUnit.MINUTES)
                        .writeTimeout(5, TimeUnit.MINUTES)
                        .readTimeout(5, TimeUnit.MINUTES)
                        .retryOnConnectionFailure(true)
                        .build();

                     MediaType mediaType = MediaType.parse("application/json");
                     RequestBody body = RequestBody.create(mediaType, jomoJson.toString());
                     Request request = new Request.Builder()
                        .url("https://shipbox.pk/api-shipping/orders")
                        .method("POST", body)
                        .addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLP3rYMEU0oM")
                        .addHeader("Content-Type", "application/json")
                        .build();
                     Response response = client.newCall(request).execute();

                     String jsonResponse = response.body().string();

                     if (response.code() == 200 && this.isJSONValid(jsonResponse) == true) {
                        cnNumber = null;
                        Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonResponse);
                        cnNumber = JsonPath.read(document, "$.data[0].trackingNumber");

                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, rs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "SUCCESS");
                        dCall.setString(3, cnNumber);
                        dCall.execute();
                        dCall.close();

                        Console.show("Jomo CN generated successfully for OrderId: " + orderId + " CN Number: " + cnNumber);

                     } else if (response.code() != 200 && this.isJSONValid(jsonResponse) == true) {
                        cnNumber = null;
                        Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonResponse);
                        responseString = JsonPath.read(document, "$.data[0].message");

                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, rs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "FAIL");
                        dCall.setString(3, responseString);
                        dCall.execute();
                        dCall.close();

                        Console.show("Jomo CN generation failed for OrderId: " + orderId + " Error: " + responseString);

                     } else {
                        cnNumber = null;

                        Console.write("JOMO CN generation failed for OrderId: " + orderId + " --> Invalid Data");

                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, rs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "FAIL");
                        dCall.setString(3, "Invalid Data -> " + jomoJson.toString());
                        dCall.execute();
                        dCall.close();
                     }

                  } catch (Exception ex) {

                     Console.write("JOMO CN generation failed for OrderId: " + orderId + " --> " + ex.getMessage());

                     CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                     dCall.setString(1, rs.getString("CN_DETAILS_ID"));
                     dCall.setString(2, "FAIL");
                     dCall.setString(3, ex.getMessage());
                     dCall.execute();
                     dCall.close();
                     // ex.printStackTrace();

                  }
               } while (rs.next());
               rs.close();
               ps.close();

            } catch (Exception ex) {
               Console.show("JOMO CN Generation exception# 001 " + ex.getMessage());
               if (rs != null) {
                  rs.close();
               }
               if (ps != null) {
                  ps.close();
               }
            }
         }
      } catch (Exception ex) {
         Console.show("JOMO CN Generation exception# 002 " + ex.getMessage());
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