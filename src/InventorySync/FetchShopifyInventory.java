/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package InventorySync;
import okhttp3.*;
import java.io.IOException;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.json.*;
/**
 *
 * @author abdul.ahad1 
 */
public class FetchShopifyInventory {
   
   public static String getproductdetails(String sku) throws IOException {
   /*
        OkHttpClient client = new OkHttpClient();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(15, TimeUnit.SECONDS); 
        builder.readTimeout(15, TimeUnit.SECONDS); 
        builder.writeTimeout(15, TimeUnit.SECONDS); 
        client = builder.build();  
       */
       OkHttpClient client = new OkHttpClient().newBuilder()
         .build();
        

      MediaType mediaType = MediaType.parse("application/json");
      RequestBody body = RequestBody.create(mediaType, "{\"query\":\"{\\r\\n  productVariants(query: \\\"sku:" + sku + "\\\", first: 1) {\\r\\n    edges {\\r\\n      cursor\\r\\n      node {\\r\\n        displayName\\r\\n        createdAt\\r\\n        id\\r\\n        inventoryManagement\\r\\n        price\\r\\n        inventoryQuantity\\r\\n        sku\\r\\n        storefrontId\\r\\n        taxCode\\r\\n        inventoryItem {\\r\\n          id\\r\\n          inventoryLevels(first: 10) {\\r\\n            edges {\\r\\n              cursor\\r\\n              node {\\r\\n                id\\r\\n              }\\r\\n            }\\r\\n          }\\r\\n        }\\r\\n      }\\r\\n    }\\r\\n  }\\r\\n}\\r\\n\",\"variables\":{}}");
      Request request = new Request.Builder()
         .url("https://sapphire-online.myshopify.com/admin/api/graphql.json")
         .method("POST", body)
         .addHeader("Authorization", "Basic ZWY=")
         .addHeader("Content-Type", "application/json")
         .build();
       
      Response response = client.newCall(request).execute();
      return response.body().string();
      // System.out.println(response.body().string());

   }

   public static Parser JParser(String sku) throws IOException, InterruptedException {
      String sku1 = null;
      String displayName = null;
      String skuid = null;
      String price = null;
      int inventoryQuantity = -10;
      String invlevelid = null;
      int errorResponseCount = 1;
      boolean isvalidjson = true;
      String jstring;
      jstring = getproductdetails(sku);
      isvalidjson = isJSONValid(jstring);
      while (isvalidjson == false && errorResponseCount < 3) {
          
         jstring = getproductdetails(sku);
         isvalidjson = isJSONValid(jstring);
         errorResponseCount++;
       //  Thread.sleep(500);
      }
      if (isvalidjson == true) {
         try {
            List < String > displayName_list = new ArrayList < String > ();
            List < String > skuid_list = new ArrayList < String > ();
            List < String > price_list = new ArrayList < String > ();
            List < Integer > inventoryQuantity_list = new ArrayList < Integer > ();
            List < String > sku_list = new ArrayList < String > ();
            List < String > invlevelid_list = new ArrayList < String > ();

            Object document = Configuration.defaultConfiguration().jsonProvider().parse(jstring);
            sku_list = JsonPath.read(document, "$.data.productVariants.edges[*].node.sku");
            displayName_list = JsonPath.read(document, "$.data.productVariants.edges[*].node.displayName");
            skuid_list = JsonPath.read(document, "$.data.productVariants.edges[*].node.id");
            sku_list = JsonPath.read(document, "$.data.productVariants.edges[*].node.sku");
            price_list = JsonPath.read(document, "$.data.productVariants.edges[*].node.price");
            inventoryQuantity_list = JsonPath.read(document, "$.data.productVariants.edges[*].node.inventoryQuantity");
            invlevelid_list = JsonPath.read(document, "$.data.productVariants.edges[*].node.inventoryItem.inventoryLevels.edges[*].node.id");
            if (sku_list.size() <= 0) {
               displayName = "-";
               sku1 = sku;
               skuid = "-";
               price = "-";
               inventoryQuantity = 0;
               invlevelid = "-";
            } else {
               displayName = displayName_list.get(0);
               sku1 = sku_list.get(0);
               skuid = skuid_list.get(0);
               price = price_list.get(0);
               inventoryQuantity = inventoryQuantity_list.get(0);
               invlevelid = invlevelid_list.get(0);
            }
         } catch (Exception ex) {
            System.out.println(sku);
            System.out.println(jstring);

            ex.printStackTrace();
         }
         return new Parser(sku1, displayName, skuid, price, inventoryQuantity, invlevelid);
      } //end if
      else {
         return new Parser(sku, "-", "-", "-", 0, "-");
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
final class Parser {
   public String sku;
   public String displayName;
   public String skuid;
   public String price;
   public String invlevelid;
   public int inventoryQuantity;

   public Parser(String sku, String displayName, String skuid, String price, int inventoryQuantity, String invlevelid) {
      this.sku = sku;
      this.displayName = displayName;
      this.skuid = skuid;
      this.price = price;
      this.invlevelid = invlevelid;
      this.inventoryQuantity = inventoryQuantity;
   }
  
}