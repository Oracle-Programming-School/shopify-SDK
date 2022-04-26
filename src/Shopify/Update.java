/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify;

import Shopify.DB.DB;
import FND.Email;
import Shopify.Log.Console;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 *
 * @author abdul.ahad1
 *
 */
public class Update {

   DB db = null;

   public Update(DB pdp) {
      db = pdp;
   }

  
   private final String responseSuccess = "Tag Updated Succeccfully on Shopify";
   private final String responseFail = "Failed to Updated Tag on Shopify";
   private HashMap < String, Integer > hash_map = new HashMap < String, Integer > ();

   public int updateTags() throws SQLException, InterruptedException {

     
      /********************************************************************************/
      String select_candela = "SELECT  ISNULL(SUM( sli.qty ), 0) net_qty, s1.adjustment_comments from dbo.tblSalesLineItems sli , dbo.tblSales s1\n" +
         "                   where sli.sale_id = s1.sale_id and sli.shop_id = s1.shop_id \n" +
         "                  and s1.shop_id = 13\n" +
         "                  AND CONVERT( date  , s1.TransactionTime ,104) > CONVERT( date  , getdate() - 216 ,104)\n" +
         "                  group by s1.adjustment_comments";

      String selectQuery = "select * from srl_shopify_tags_mutation_v002 tmv";
      int retCode;
      long start;
      long end;
      System.out.println("Candela parsing start ");
      start = System.currentTimeMillis();
      PreparedStatement psCandela;
      ResultSet rsCandela;

      psCandela = db.getCandelaConnection().prepareStatement(
         select_candela,
         ResultSet.TYPE_FORWARD_ONLY,
         ResultSet.CONCUR_READ_ONLY);
      rsCandela = psCandela.executeQuery();
      if (!rsCandela.next()) {
         rsCandela.close();
         psCandela.close();
      }
      do {
         hash_map.put(rsCandela.getString("adjustment_comments"), rsCandela.getInt("net_qty"));
      }
      while (rsCandela.next());
      rsCandela.close();
      psCandela.close();
      end = System.currentTimeMillis();
      System.out.println("Candela parsing takes " + (end - start) + " and hash map size is " + hash_map.size());

      PreparedStatement psSelectOrderDetail;
      ResultSet rsOrderDetail;
      ArrayList < MapOrderRow > orderRow = new ArrayList < MapOrderRow > ();
      MapOrderRow row = null;

      psSelectOrderDetail = db.getDBConnection().prepareStatement(
         selectQuery,
         ResultSet.TYPE_FORWARD_ONLY,
         ResultSet.CONCUR_READ_ONLY);

      rsOrderDetail = psSelectOrderDetail.executeQuery();

      //if ORA resultset is null
      if (!rsOrderDetail.next()) {

         rsOrderDetail.close();
         psSelectOrderDetail.close();
         return 0;
      } else {
         do { 
            row = new MapOrderRow(rsOrderDetail.getString("orderName"),
               rsOrderDetail.getString("orderId"),
               rsOrderDetail.getString("Status"),
               rsOrderDetail.getString("fulfilmentId"),
               rsOrderDetail.getString("trans_date"),
               rsOrderDetail.getString("store_id"),
               rsOrderDetail.getString("shop_id"),
               rsOrderDetail.getString("swaped_shopID"), 
               rsOrderDetail.getString("fulfillment_type")
            );
            orderRow.add(row);
         } while (rsOrderDetail.next());

      }

      rsOrderDetail.close();
      psSelectOrderDetail.close();
      int count = 0;
      for (MapOrderRow ldx: orderRow) {

         /*case 1: order with processed status*/
         if (ldx.status.equalsIgnoreCase("Processed")) {
            count++;
            retCode = this.setDispatchedTag(ldx.orderId, ldx.orderName, ldx.status, ldx.transDate, ldx.storeID, ldx.fulfillmentType);
            System.out.println("Total " + count + " Orders Proceed");
         }

         /************End of Orders having processed status*********/

         /*case 2: order with returned  status*/
         else if (ldx.status.equalsIgnoreCase("Returned")) {
         count++;
          

            retCode = setReturnedTag(ldx.orderId, ldx.orderName, ldx.status, ldx.transDate, ldx.fullfillmentID, ldx.shopID, ldx.swaped_shopID, ldx.storeID, ldx.fulfillmentType);
        System.out.println("Total " + count + " Orders Proceed");
         }

         /**************End of Orders having return status*************/

      } //end of for each loop

      return 0;

   }

   public int setDispatchedTag(String orderID, String orderName, String status, String transDate, String storeID, String fulfillmentType) {
      try {
         int retCode;
         int tagResponse;
         int currentTag;
         if (fulfillmentType.equalsIgnoreCase("F3")) {
            currentTag = getTag(orderName);

            if (currentTag != 1) {
               tagResponse = this.TagUpdate(orderID, "Dispatched");
               retCode = saveMutationRecord(orderName, status, "Dispatched", responseSuccess, transDate, storeID);

               //System.err.println("mutation done and not ok");
               //retCode = saveMutationRecord(orderName, status, "", responseFail, transDate, storeID);
               //System.err.println("record save in oracle");

            } else if (currentTag == 1) {
               retCode = saveMutationRecord(orderName, status, "Partially Returned", responseSuccess, transDate, storeID);
            }
         } else {
            tagResponse = this.TagUpdate(orderID, "Dispatched");
            retCode = saveMutationRecord(orderName, status, "Dispatched", responseSuccess, transDate, storeID);

         }
         return 0;
      } catch (Exception ex) {
        // ex.printStackTrace();
        Console.show("Error --> TagUpdate --> set dispatch tag "+ex.getMessage());
         return 1;
      }

   }

   public int setReturnedTag(String orderID, String orderName, String status, String transDate, String fullfillmentID, String shopID, String swapdShopID, String storeID, String fulfillmentType) {
      int fullFillmentResponse = -1;
      int cancelOrderResponse = -1;
      int tagResponse = -1;
      int retCode;
      int orderShopifyQty;
      int swapdOrderQtyCandela;
      int orderFullQtyCandela;
      int orderQtyCandela;
      int currentTag = 0;
      long start;
      long end;

      try {

         start = System.currentTimeMillis();

         if (fulfillmentType.equalsIgnoreCase("F3")) {

            System.out.println("yes i am fulfilment type F3 order");

            orderFullQtyCandela = getCandelaOrderFullQty(orderName);
            Thread.sleep(500);
            orderQtyCandela = getCandelaOrderQty(orderName, shopID);

            System.out.println("orderFullQtyCandela " + orderFullQtyCandela);
            System.out.println("orderQtyCandela " + orderQtyCandela);

            orderShopifyQty = this.getShopifyOrderQty(orderName);
            System.out.println("orderShopifyQty " + orderShopifyQty);

            swapdOrderQtyCandela = getCandelaOrderQty(orderName, swapdShopID);
            System.out.println("swapdOrderQtyCandela " + swapdOrderQtyCandela);

            orderShopifyQty = orderShopifyQty - swapdOrderQtyCandela;
            System.out.println("Net orderShopifyQty " + orderShopifyQty);

            currentTag = getTag(orderName);
            System.out.println("currentTag " + currentTag);

            if (swapdOrderQtyCandela != 0 && orderQtyCandela != 0) {
               int qtyMatched = orderShopifyQty - orderQtyCandela;
               if (qtyMatched != 0) {
                  System.out.println("yes i am reverse pikup order ");
                  retCode = this.setDispatchedTag(orderID, orderName, status, transDate, storeID, fulfillmentType);
                  return 2;
               }
            }

            fullFillmentResponse = this.cancelFullfilment(orderID, fullfillmentID);
            if (fullFillmentResponse == 0) {
               System.out.println("fullFillment is cancelled for storeID  " + storeID);
               if (currentTag == 0) {

                  tagResponse = this.TagUpdate(orderID, "Partially Returned");

                  System.out.println("tag is set for storeID  " + storeID);
                  retCode = saveMutationRecord(orderName, status, "Partially Returned", responseSuccess, transDate, storeID);

               } else if (currentTag == 1) {

                  cancelOrderResponse = this.cancelOrder(orderID);
                  tagResponse = this.TagUpdate(orderID, "Returned");
                  retCode = saveMutationRecord(orderName, status, "Returned", responseSuccess, transDate, storeID);

               }

            } else if (fullFillmentResponse == 1) {

               retCode = saveMutationRecord(orderName, status, "Returned", "Unable to Cancel Order Fullfillment", transDate, storeID);

            }

         } else {
            end = System.currentTimeMillis();
            System.out.println("master if takes " + (end - start));

            System.out.println("No i am not a split order");

            start = System.currentTimeMillis();
            if (hash_map.containsKey(orderName) == true) {
               orderQtyCandela = hash_map.get(orderName);
               System.out.println("yes i am from hash map");
            } else {
               orderQtyCandela = getCandelaOrderQty(orderName, shopID);
            }
            end = System.currentTimeMillis();
            System.out.println("orderQtyCandela takes " + (end - start));

            start = System.currentTimeMillis();
            orderShopifyQty = this.getShopifyOrderQty(orderName);
            end = System.currentTimeMillis();
            System.out.println("orderShopifyQty takes " + (end - start));

            int qtyMatched = orderShopifyQty - orderQtyCandela;

            if (qtyMatched != 0) {
               System.out.println("i am in qtyMatched");
               start = System.currentTimeMillis();
               //  retCode = this.setDispatchedTag(orderID, orderName, status, transDate, storeID, fulfillmentType);
               tagResponse = this.TagUpdate(orderID, "Dispatched");
               retCode = saveMutationRecord(orderName, status, "Dispatched", responseSuccess, transDate, storeID);
               end = System.currentTimeMillis();
               System.out.println("qtyMatched if takes " + (end - start));
               return 2;
            }

            start = System.currentTimeMillis();
            fullFillmentResponse = this.cancelFullfilment(orderID, fullfillmentID);
            end = System.currentTimeMillis();
            System.out.println("cancelFullfilment takes " + (end - start));

            start = System.currentTimeMillis();
            cancelOrderResponse = this.cancelOrder(orderID);
            end = System.currentTimeMillis();
            System.out.println("cancelOrder takes " + (end - start));

            start = System.currentTimeMillis();
            tagResponse = this.TagUpdate(orderID, "Returned");
            end = System.currentTimeMillis();
            System.out.println("TagUpdate takes " + (end - start));

            start = System.currentTimeMillis();
            retCode = saveMutationRecord(orderName, status, "Returned", responseSuccess, transDate, storeID);
            end = System.currentTimeMillis();
            System.out.println("saveMutationRecord takes " + (end - start));

         }

         return 0;
      } catch (Exception ex) {
        // ex.printStackTrace();
         Console.show("Error --> TagUpdate --> set return tag "+ex.getMessage());
         return 1;
      }

   }

   public int TagUpdate(String orderID, String Tag) {
      String jResponse;
      boolean isvalidjson;
      List < String > updatedTagList = new ArrayList < String > ();
      int apiCallCount = 0;
      boolean apiCall = true;
      try {
         //  while (apiCallCount < 3 && apiCall == true) {
         //     apiCallCount++;
         OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
         MediaType mediaType = MediaType.parse("application/json");
         RequestBody body = RequestBody.create(mediaType, "{\"query\":\"mutation orderUpdate($input: OrderInput!)" +
            " {\\r\\n  orderUpdate(input: $input) {\\r\\n    order {\\r\\n      id\\r\\n      tags\\r\\n    }\\r\\n " +
            "   userErrors {\\r\\n      field\\r\\n      message\\r\\n    }\\r\\n  }\\r\\n}\\r\\n\",\"variables\"" +
            ":{\"input\":{\"id\":\"gid://shopify/Order/" + orderID + "\"," +
            "\"tags\":[\"" + Tag + "\"]}}}");
         Request request = new Request.Builder()
            .url("https://sapphire-online.myshopify.com/admin/api/graphql.json")
            .method("POST", body)
            .addHeader("Authorization", "Basic DcwNzJjNmNmM2QzZWY=")
            .addHeader("Content-Type", "application/json")
            .build();
         Response response = client.newCall(request).execute();
         jResponse = response.body().string();
         isvalidjson = isJSONValid(jResponse);
         if (isvalidjson == true) {
            //      apiCall = false;
            Object document = Configuration.defaultConfiguration().jsonProvider().parse(jResponse);
            updatedTagList = JsonPath.read(document, "$.data.orderUpdate.order.tags");

         }
         // } //end while

         if (updatedTagList.get(0).equals(Tag)) {
            return 0;
         } else {
            return 1;
         }
      } catch (Exception ex) {
        // ex.printStackTrace();
         Console.show("Error --> TagUpdate "+ex.getMessage());
         return 1;
      }

   }

   public int getTag(String orderID) {
      String jResponse;
      boolean isvalidjson;
      List < String > TagList = new ArrayList < String > ();
      int apiCallCount = 0;
      boolean apiCall = true;
      boolean partialReturnFound = false;
      try {
         //   while (apiCallCount < 3 && apiCall == true) {
         //      apiCallCount++;
         OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
         Request request = new Request.Builder()
            .url("https://sapphire-online.myshopify.com/admin/api/2021-10/orders/" + orderID + ".json")
            .method("GET", null)
            .addHeader("Authorization", "Basic iM2MyYmE=")
            .build();
         Response response = client.newCall(request).execute();
         jResponse = response.body().string();
         isvalidjson = isJSONValid(jResponse);
         if (isvalidjson == true) {
            //         apiCall = false;
            Object document = Configuration.defaultConfiguration().jsonProvider().parse(jResponse);
            TagList = JsonPath.read(document, "$.order.tags");

         }
         //   } //end while
         for (int ldx = 0; ldx < TagList.size(); ldx++) {
            if (TagList.get(ldx).contains("Partially Returned")) {

               partialReturnFound = true;
               break;
            }
         }
         if (partialReturnFound == true) {
            return 1;
         } else {
            return 0;
         }
      } catch (Exception ex) {
         //ex.printStackTrace();
          Console.show("Error --> TagUpdate --> getTag "+ex.getMessage());
         return 0;
      }

   }

   public int cancelFullfilment(String orderID, String fullFillmentId) {
      String jResponse;
      boolean isvalidjson;

      String fullFillmentStatus = "---";
      try {

         OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
         MediaType mediaType = MediaType.parse("text/plain");
         RequestBody body = RequestBody.create(mediaType, "");
         Request request = new Request.Builder()
            .url("https://sapphire-online.myshopify.com/admin/api/2021-07/orders/" + orderID + "/fulfillments/" + fullFillmentId + "/cancel.json")
            .method("POST", body)
            .addHeader("Authorization", "Basic NzJjNmNmM2QzZWY=")
            .build();
         Response response = client.newCall(request).execute();
         jResponse = response.body().string();
         isvalidjson = isJSONValid(jResponse);
         if (isvalidjson == true) {
            // System.out.println("orderID: "+orderID);
            //  System.out.println("fullFillmentId: "+fullFillmentId);
            //System.out.println(jResponse);
            if (jResponse.contains("errors")) {
               return 1;
            }
            Object document = Configuration.defaultConfiguration().jsonProvider().parse(jResponse);
            fullFillmentStatus = JsonPath.read(document, "$.fulfillment.status");
         }

         if (fullFillmentStatus.equals("cancelled")) {
            return 0;
         } else {
            return 1;
         }

      } catch (Exception ex) {
         //ex.printStackTrace();
          Console.show("Error --> TagUpdate --> cancelFulfilment "+ex.getMessage());
         return 1;
      }

   }

   public int cancelOrder(String orderID) {
      String jResponse;
      boolean isvalidjson;

      String orderStatus = null;
      try {

         OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
         MediaType mediaType = MediaType.parse("text/plain");
         RequestBody body = RequestBody.create(mediaType, "");
         Request request = new Request.Builder()
            .url("https://sapphire-online.myshopify.com/admin/api/2021-07/orders/" + orderID + "/cancel.json")
            .method("POST", body)
            .addHeader("Authorization", "Basic iZDcwNzJjNmNmM2QzZWY=")
            .build();
         Response response = client.newCall(request).execute();
         jResponse = response.body().string();
         isvalidjson = isJSONValid(jResponse);
         if (isvalidjson == true) {

            Object document = Configuration.defaultConfiguration().jsonProvider().parse(jResponse);
            orderStatus = JsonPath.read(document, "$.notice");

         }

         if (orderStatus.equals("Order has been canceled")) {
            return 0;
         } else {
            return 1;
         }

      } catch (Exception ex) {
         return 1;
      }

   }

   public String getFullfillmentID(String orderName) {
      try {
         String jResponse;
         boolean isvalidjson;
         String idString = "";
         String fullfillmentID;

         OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
         MediaType mediaType = MediaType.parse("application/json");
         RequestBody body = RequestBody.create(mediaType, "{\"query\":\"query MyQuery {\\r\\n        orders(first:1 query:\\\"" + "name:" + orderName + "\\\") {\\r\\n          edges {\\r\\n            node {\\r\\n              id\\r\\n              name\\r\\n              displayFulfillmentStatus\\r\\n              fulfillments {\\r\\n          id\\r\\n          totalQuantity\\r\\n        }\\r\\n    \\r\\n              cancelledAt\\r\\n              createdAt\\r\\n              tags\\r\\n             \\r\\n            }\\r\\n          }\\r\\n        }\\r\\n      }\\r\\n\\r\\n\\r\\n\",\"variables\":{}}");
         Request request = new Request.Builder()
            .url("https://sapphire-online.myshopify.com/admin/api/graphql.json")
            .method("POST", body)
            .addHeader("Authorization", "Basic YzViZGRiZDcwNzJjNmNmM2QzZWY=")
            .addHeader("Content-Type", "application/json")
            .build();
         Response response = client.newCall(request).execute();
         jResponse = response.body().string();
         isvalidjson = isJSONValid(jResponse);
         if (isvalidjson == true) {

            Object document = Configuration.defaultConfiguration().jsonProvider().parse(jResponse);
            idString = JsonPath.read(document, "$.data.orders.edges[0].node.fulfillments[0].id");

         }
         fullfillmentID = idString.replaceAll("[^0-9]", "");
         return fullfillmentID;
      } catch (Exception ex) {
         //ex.printStackTrace();
          Console.show("Error --> TagUpdate --> getFulfilment ID "+ex.getMessage());
         return null;

      }

   }

   public int getShopifyOrderQty(String orderName) {
      String jResponse;
      boolean isvalidjson;
      int qty = 0;
      String orderQty;
      try {

         OkHttpClient client = new OkHttpClient().newBuilder()
            .build();
         MediaType mediaType = MediaType.parse("application/json");
         RequestBody body = RequestBody.create(mediaType, "{\"query\":\"query MyQuery {\\r\\n        orders(first:1 query:\\\"name:" + orderName + "\\\") {\\r\\n          edges {\\r\\n            node {\\r\\n              id\\r\\n              name\\r\\n              displayFulfillmentStatus\\r\\n              fulfillments {\\r\\n          id\\r\\n          totalQuantity\\r\\n        }\\r\\n    \\r\\n              cancelledAt\\r\\n              createdAt\\r\\n              tags\\r\\n             \\r\\n            }\\r\\n          }\\r\\n        }\\r\\n      }\\r\\n\\r\\n\\r\\n\",\"variables\":{}}");
         Request request = new Request.Builder()
            .url("https://sapphire-online.myshopify.com/admin/api/graphql.json")
            .method("POST", body)
            .addHeader("Authorization", "Basic SGRiZDcwNzJjNmNmM2QzZWY=")
            .addHeader("Content-Type", "application/json")
            .build();
         Response response = client.newCall(request).execute();
         jResponse = response.body().string();
         isvalidjson = isJSONValid(jResponse);
         if (isvalidjson == true) {

            Object document = Configuration.defaultConfiguration().jsonProvider().parse(jResponse);
            qty = JsonPath.read(document, "$.data.orders.edges[0].node.fulfillments[0].totalQuantity");

         }

         return qty;

      } catch (Exception ex) {
        // ex.printStackTrace();
         Console.show("Error --> TagUpdate --> getShopifyOrderQTy "+ex.getMessage());
         return qty;
      }

   }

   public static boolean isJSONValid(String jString) {
      try {
         new JSONObject(jString);
      } catch (JSONException ex) {
         // edited, to include @Arthur's comment
         // e.g. in case JSONArray is valid as well...
         try {
            new JSONArray(jString);
         } catch (JSONException ex1) {
            return false;
         }
      }
      return true;
   }

   private static java.sql.Date getCurrentDate() {
      java.util.Date today = new java.util.Date();
      return new java.sql.Date(today.getTime());
   }

   // save mutation record with success or fail status in ORA
   public int saveMutationRecord(String orderName, String status, String shopifyTag, String response, String transDate, String storeID) {
      try {
         String insertSQL = "insert into srl_shopify_tags_mutation\n" +
            "  ( order_name,status,updated_shopify_tag,response,mutation_date,TRANSACTION_DATE,store_id)\n" +
            "values (?,?,?,?,?,?,?)";
         PreparedStatement psOra = db.getDBConnection().prepareStatement(insertSQL);
         psOra.setString(1, orderName);
         psOra.setString(2, status);
         psOra.setString(3, shopifyTag);
         psOra.setString(4, response);
         psOra.setDate(5, getCurrentDate());
         psOra.setString(6, transDate); //storeID
         psOra.setString(7, storeID);
         psOra.addBatch();
         int[] insert = psOra.executeBatch();
         psOra.close();

         return 0;
      } catch (Exception ex) {
        // ex.printStackTrace();
         Console.show("Error --> TagUpdate --> saveMutationRecord "+ex.getMessage());
         return 1;
      }
   }

   // method used only in case of returned order to verify revesr pick up order
   public int getCandelaOrderQty(String orderName, String shopID) {
      int qty = 0;
      try {
         PreparedStatement select_st = db.getCandelaConnection().
         prepareStatement(" SELECT ISNULL(SUM( sli.qty ), 0) net_qty from dbo.tblSalesLineItems sli , dbo.tblSales s1\n" +
            "       where sli.sale_id = s1.sale_id and sli.shop_id = s1.shop_id \n" +
            "       and s1.shop_id = ? \n" +
            "       and s1.adjustment_comments = ? ");
         select_st.setString(1, shopID);
         select_st.setString(2, orderName);
         ResultSet rs = select_st.executeQuery();
         while (rs.next()) {
            qty = rs.getInt("net_qty");
         }
         rs.close();
         select_st.close();
         return qty;
      } catch (Exception ex) {
        // ex.printStackTrace();
         Console.show("Error --> TagUpdate --> getCandelaOrderQty "+ex.getMessage());
         return -777;
      }

   }

   public int getCandelaOrderFullQty(String orderName) {
      int qty = 0;
      try {
         PreparedStatement select_st = db.getCandelaConnection().
         prepareStatement(" SELECT SUM( sli.qty ) net_qty from dbo.tblSalesLineItems sli , dbo.tblSales s1\n" +
            "       where sli.sale_id = s1.sale_id  and sli.shop_id = s1.shop_id \n" +
            "       and s1.shop_id in (13,75) \n" +
            "       and s1.adjustment_comments = ? ");
         select_st.setString(1, orderName);
         ResultSet rs = select_st.executeQuery();
         while (rs.next()) {
            qty = rs.getInt("net_qty");
         }
         rs.close();
         select_st.close();
         return qty;
      } catch (Exception ex) {
         //ex.printStackTrace();
          Console.show("Error --> TagUpdate --> getCandelaOrderFullQty "+ex.getMessage());
         return -777;
      }

   }

   public void cancelOrder() {
      String query = "select cotv.transaction_id,\n" +
         "       cotv.tranaction_date,\n" +
         "       cotv.order_id,\n" +
         "       cotv.request_status,\n" +
         "       cotv.order_status,\n" +
         "       cotv.\"fulfilment_id1\",\n" +
         "       cotv.\"fulfilment_id2\",\n" +
         "       cotv.created_by,\n" +
         "       cotv.organization_id, cotv.order_name \n" +
         "       from srl_shopify_cancle_order_tmp_v  cotv";
      CallableStatement dCall = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      String transactionID;
      String orderID = null;
      String fulfilmentID1;
      String fulfilmentID2;
      String orderStatus;
      String requestStatus;
      int fulFillmentResponse1 = -1;
      int fulFillmentResponse2 = -1;
      int cancelOrderResponse = -1;
      int tagResponse = -1;
      String response = "failed";
      Email email = new Email();

      try {

         ps = db.getDBConnection().prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
         rs = ps.executeQuery();

         if (!rs.next()) {
            rs.close();
            ps.close();
            Console.show("--> Nothing to proceed for order cancel order shopify");
         } else {
            do {
               transactionID = rs.getString("transaction_id");
               orderID = rs.getString("order_id");
               fulfilmentID1 = rs.getString("fulfilment_id1");
               fulfilmentID2 = rs.getString("fulfilment_id2");
               orderStatus = rs.getString("order_status");
               requestStatus = rs.getString("request_status");

               if (requestStatus.equalsIgnoreCase("cancelled") && fulfilmentID2.equalsIgnoreCase("0") && !fulfilmentID1.equalsIgnoreCase("0")) {

                  fulFillmentResponse1 = this.cancelFullfilment(orderID, fulfilmentID1);
                  if (fulFillmentResponse1 == 0) {
                     cancelOrderResponse = this.cancelOrder(orderID);
                     if (cancelOrderResponse == 0) {
                        tagResponse = this.TagUpdate(orderID, orderStatus);
                       
                           response = orderStatus;
                        

                     }

                  }

               } //end validate request status cancelled
               else if (requestStatus.equalsIgnoreCase("cancelled") && (fulfilmentID2.equalsIgnoreCase("0") && fulfilmentID1.equalsIgnoreCase("0"))) {
                  cancelOrderResponse = this.cancelOrder(orderID);
                  if (cancelOrderResponse == 0) {
                     tagResponse = this.TagUpdate(orderID, orderStatus);
                     
                        response = orderStatus;
                     

                  }
               } else if (requestStatus.equalsIgnoreCase("cancelled") && (!fulfilmentID2.equalsIgnoreCase("0") && !fulfilmentID1.equalsIgnoreCase("0"))) {

                  fulFillmentResponse1 = this.cancelFullfilment(orderID, fulfilmentID1);
                  fulFillmentResponse2 = this.cancelFullfilment(orderID, fulfilmentID2);

                  if (fulFillmentResponse1 == 0 && fulFillmentResponse2 == 0) {
                     cancelOrderResponse = this.cancelOrder(orderID);
                     if (cancelOrderResponse == 0) {
                        tagResponse = this.TagUpdate(orderID, orderStatus);
                        
                           response = orderStatus;
                        

                     }

                  }
               }
               
               
               else if (requestStatus.equalsIgnoreCase("partially cancel") && !fulfilmentID1.equalsIgnoreCase("0")) {

                  fulFillmentResponse1 = this.cancelFullfilment(orderID, fulfilmentID1);

                  if (fulFillmentResponse1 == 0 && orderStatus.equalsIgnoreCase("partially cancelled")) {

                     tagResponse = this.TagUpdate(orderID, orderStatus);
                    
                        response = orderStatus;
                     
                  } else if (fulFillmentResponse1 == 0 && orderStatus.equalsIgnoreCase("cancelled")) {

                     cancelOrderResponse = this.cancelOrder(orderID);
                     if (cancelOrderResponse == 0) {
                        tagResponse = this.TagUpdate(orderID, orderStatus);
                        
                           response = orderStatus;
                        

                     }

                  }

               } //end validate request partially cancel
               else if (requestStatus.equalsIgnoreCase("partially cancel") && fulfilmentID1.equalsIgnoreCase("0")) {
                  cancelOrderResponse = this.cancelOrder(orderID);
                  if (cancelOrderResponse == 0) {
                     tagResponse = this.TagUpdate(orderID, orderStatus);
                     
                        response = orderStatus;
                     

                  }
               }
               dCall = db.getDBConnection().prepareCall("begin\n" +
                  " \n" +
                  "  srl_misc_utl.shopify_process_cancel_order(p_transaction_id => :p_transaction_id,\n" +
                  "                                            p_response => :p_response);\n" +
                  "end;");
               dCall.setString(1, transactionID);
               dCall.setString(2, response);
               dCall.execute();
               dCall.close();
               Console.show("--> Order Id " + orderID + " Cancel Request Response " + response);
               
              email.sendAlert(null,"test@test.com", "Cancel Order on Shopify", "This is an acknowledgement of the order "+rs.getString("order_name")+" is cancelled "+" by "+rs.getString("created_by") + " at "+rs.getString("tranaction_date"),true,null);
       
            } while (rs.next());
            rs.close();
            ps.close();

         }
      } catch (Exception ex) {
         System.err.println(ex.getMessage());
         Console.show("--> Error in Order Id " + orderID + " Cancel Request " + ex.getMessage());
         //ex.printStackTrace();
         try {
            if (rs != null) {
               rs.close();
            }
            if (ps != null) {
               ps.close();
            }
            if (dCall != null) {
               dCall.close();
            }
         } catch (Exception ignore) {}

      }

   }

}

final class MapOrderRow {

   public String orderName;
   public String orderId;
   public String status;
   public String fullfillmentID;
   public String transDate;
   public String storeID;
   public String shopID;
   public String swaped_shopID;
   public String fulfillmentType; //

   public MapOrderRow(String orderName, String orderId, String status, String fullfillmentID, String transDate, String storeID, String shopID, String swaped_shopID, String fulfillmentType) {
      this.orderName = orderName;
      this.orderId = orderId;
      this.status = status;
      this.fullfillmentID = fullfillmentID;
      this.transDate = transDate;
      this.storeID = storeID;
      this.shopID = shopID;
      this.swaped_shopID = swaped_shopID;
      this.fulfillmentType = fulfillmentType;
   }
}