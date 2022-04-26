/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package InventorySync;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
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
 * @author abdul.ahad1
 */
public class Mutation { //ParseResponse
    
        public static String getResponse(String itm_id, String qty) {
                try {
                        OkHttpClient client = new OkHttpClient().newBuilder()
                                .build();
                        MediaType mediaType = MediaType.parse("application/json");
                        RequestBody body = RequestBody.create(mediaType, "{\"query\":\"mutation {\\r\\n  inventoryAdjustQuantity (input: {inventoryLevelId: \\\"" + itm_id + "\\\", availableDelta:" + qty + " }) {\\r\\n    inventoryLevel {\\r\\n      available\\r\\n    }\\r\\n    userErrors {\\r\\n      field\\r\\n      message\\r\\n    }\\r\\n  }\\r\\n}\",\"variables\":{}}");
                        Request request = new Request.Builder()
                                .url("https://sapphire-online.myshopify.com/admin/api/graphql.json")
                                .method("POST", body)
                                .addHeader("Authorization", "Basic TA3YzViZGRiZDcwNzJjNmNmM2QzZWY=")
                                .addHeader("Content-Type", "application/json")
                                .build();
                        Response response = client.newCall(request).execute();
                        return response.body().string();
                } catch (Exception ex) {
                        ex.printStackTrace();
                        return "";
                }
        }
        
        public static String adjustquantity(String itm_id, String qty) {
               
                int quantity = -777;
                String jstring = null;
                try {
                        boolean isvalidjson = true;

                        jstring = getResponse(itm_id, qty);
                        isvalidjson = isJSONValid(jstring);
                        if (isvalidjson == true) {
                                Object document = Configuration.defaultConfiguration().jsonProvider().parse(jstring);
                                quantity = JsonPath.read(document, "$.data.inventoryAdjustQuantity.inventoryLevel.available");

                        }
                        return "Quantity Adjuested successfully on Shopify";
                } catch (Exception ex) {
                        //   System.err.println(itm_id);
                        //   System.out.println(jstring);
                        ex.printStackTrace();
                        return ex.toString();
                }
        }

        public static boolean isJSONValid(String test) {
                try {
                        new JSONObject(test);
                } catch (JSONException ex) {
                        // edited, to include @Arthur's comment
                        // e.g. in case JSONArray is valid as well...
                        try {
                                new JSONArray(test);
                        } catch (JSONException ex1) {
                                return false;
                        }
                }
                return true;
        }
}