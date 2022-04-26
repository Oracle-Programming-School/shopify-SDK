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
public class Trax {

    DB db = null;
    
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

    public Trax(DB pdp) {
     db = pdp;
    }

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
            ResultSet Drs = Dstmt.executeQuery("select COMPANY_NAME, ORDER_ID,COST_CENTER_CODE,consignee_Name , CONSIGNEE_ADDRESS,CONSIGNEE_MOB_NO,CONSIGNEE_EMAIL,DESTINATION_CITY_id,ORIGIN_CITY_NAME,DESTINATION_CITY_NAME,WEIGHT,PIECES,COD_AMOUNT,REPLACE(CUSTOMER_REFERENCE_NO,'#','') CUSTOMER_REFERENCE_NO,case when COD_AMOUNT > 0 then 1 else 2 end  SERVICES,PRODUCT_DETAILS,FRAGILE,REMARKS,CUSTOMER_REFERENCE_NO,CN_DETAILS_ID,INSURANCE_VALUE  from MF_Shopify_CN_details where organization_id = 299 and STATUS is null and CN_NUMBER is null and CN_GENERATION_ERROR is null and COMPANY_NAME = 'TRAX' and store_id = "+ storeID +" and rownum < 100");
            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 
                Console.write("Trax CN generation Start for Order ID :  " + Drs.getString(2));

                String responseString = null;
               // String URL = "{\r\n    \"service_type_id\": 1,   \r\n    \"pickup_address_id\": 22,\r\n    \"information_display\": 1, \r\n    \"delivery_type_id\" : 1,\r\n    \"consignee_city_id\": \"" + Drs.getString("DESTINATION_CITY_id") + "\", \r\n    \"consignee_name\": \"" + Drs.getString("consignee_Name") + "\", \r\n    \"consignee_address\": \"" + Drs.getString("CONSIGNEE_ADDRESS") + "\", \r\n    \"consignee_phone_number_1\": \"" + Drs.getString("CONSIGNEE_MOB_NO") + "\",\r\n    \"order_id\": \"" + Drs.getString("CUSTOMER_REFERENCE_NO") + "\", \r\n    \"item_product_type_id\": 24, \r\n    \"item_description\":\"" + Drs.getString("PRODUCT_DETAILS") + "\",\r\n    \"item_quantity\": \"" + Drs.getString("PIECES") + "\", \r\n    \"item_insurance\": 0, \r\n    \"estimated_weight\": \"" + Drs.getString("WEIGHT") + "\",\r\n    \"shipping_mode_id\": 1, \r\n    \"amount\":  \"" + Drs.getString("COD_AMOUNT") + "\",    \r\n    \"payment_mode_id\": 1,\r\n    \"charges_mode_id\": 3 \r\n}";
                
                String URL = "{\r\n    \"service_type_id\": 1,   \r\n    \"pickup_address_id\": "+Drs.getString("COST_CENTER_CODE")+",\r\n    \"information_display\": 1, \r\n    \"delivery_type_id\" : 1,\r\n    \"consignee_city_id\": \"" + Drs.getString("DESTINATION_CITY_id") + "\", \r\n    \"consignee_name\": \"" + Drs.getString("consignee_Name") + "\", \r\n    \"consignee_address\": \"" + Drs.getString("CONSIGNEE_ADDRESS") + "\", \r\n    \"consignee_phone_number_1\": \"" + Drs.getString("CONSIGNEE_MOB_NO") + "\",\r\n    \"order_id\": \"" + Drs.getString("CUSTOMER_REFERENCE_NO") + "\", \r\n    \"item_product_type_id\": 24, \r\n    \"item_description\":\"" + Drs.getString("PRODUCT_DETAILS") + "\",\r\n    \"item_quantity\": \"" + Drs.getString("PIECES") + "\", \r\n    \"item_insurance\": 0, \r\n    \"estimated_weight\": \"" + Drs.getString("WEIGHT") + "\",\r\n    \"shipping_mode_id\": 1, \r\n    \"amount\":  \"" + Drs.getString("COD_AMOUNT") + "\",    \r\n    \"payment_mode_id\": 1,\r\n    \"charges_mode_id\": 3 \r\n}";

                ///Get Data Trax
                try {
                    ////////////////////////////////
                    OkHttpClient client = new OkHttpClient().newBuilder().build();
                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, URL);
                    Request request = new Request.Builder()
                            .url("https://sonic.pk/api/shipment/book")
                            .method("POST", body)
                            .addHeader("Authorization", Global.Trax_APIkey)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    Response response = client.newCall(request).execute();
                    ////////////////////////////////

                    Console.show(URL);

                    //JSON parser object to parse read file
                    JSONParser jsonParser = new JSONParser();
                    //Read JSON file
                    responseString = (String) response.body().string();
                    Console.show(" Response from Trax " + responseString);
                    Object obj = jsonParser.parse(responseString);

                    JSONObject TraxResponceObj = (JSONObject) obj;

                    if (TraxResponceObj.get("tracking_number") != null) {
                        //add CN information Successfully
                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "SUCCESS");
                        dCall.setString(3, TraxResponceObj.get("tracking_number").toString());
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

    public allCities[] FetchAllTraxCities() {
        allCities[] allCitiesDataArray = null;

        try {

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url("https://app.sonic.pk/api/cities").method("GET", null)
                    .addHeader("Authorization", Global.Trax_APIkey)
                    .build();
            Response response = client.newCall(request).execute();

            //JSON parser object to parse read file
            JSONParser jsonParser = new JSONParser();
            
            String requestResponse = new String();
            requestResponse=response.body().string();
            Console.show(requestResponse);
            //Read JSON file
            Object obj = jsonParser.parse(requestResponse);
            JSONObject citySetObj = (JSONObject) obj;
            JSONArray cityArr = (JSONArray) citySetObj.get("cities");
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

                        l_allCities.CityId = (String) cityValue.get("id").toString();
                        l_allCities.CityName = (String) cityValue.get("name").toString();
                        l_allCities.CityCode = null;//(String) cityValue.get("cityCode").toString();
                        l_allCities.Area = null;//(String) cityValue.get("area").toString();
                        l_allCities.CompanyName = "Trax";
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

    public void StartOrderTracking() {

        try {
            Statement Dstmt = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet Drs = Dstmt.executeQuery("select  COMPANY_NAME , ORDER_ID , CN_NUMBER \n"
                    + "from mf_shopify_cn_details scn  \n"
                    + "where COMPANY_NAME = 'TRAX' \n"
                    + "AND STATUS = 'SUCCESS' \n"
                    + "AND CN_COMPLETED_AT < SYSDATE-2 \n"
                    + "AND ROWNUM < 1000");

            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 
                
                String responseString = null;
                TrackingInfo l_TrackingInfo = new TrackingInfo();
                
                Console.write("Start Tracking Order ID :  " + Drs.getString("ORDER_ID"));
                
                l_TrackingInfo.setTrackingOrderId(Drs.getString("ORDER_ID"));
                l_TrackingInfo.setTrackingNumber(Drs.getString("CN_NUMBER"));
                l_TrackingInfo.setTrackingCompany(Drs.getString("COMPANY_NAME"));
                
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder().build();
                    Request request = new Request.Builder()
                            .url("https://sonic.pk/api/shipment/track?tracking_number=" + Drs.getString("CN_NUMBER") + "&type=1")
                            .method("GET", null)
                            .addHeader("Authorization", "Q09MNERzUVJUa0ZhcVBCSmY5Z3FtQjRtemV3NUt6NVJKYnpqYTlsZ09INHZRMzRLY2NxMmJMR2Q5Y0FV5f92bd590de40")
                            .build();
                    Response response = client.newCall(request).execute();
                    responseString = response.body().string();

                    JSONParser jsonParser = new JSONParser();
                    Object obj = jsonParser.parse(responseString);
                    JSONObject TraxObj = (JSONObject) obj;
                    
                    l_TrackingInfo.setRequestStatus(TraxObj.get("status").toString());

                    // for line 
                    JSONObject DetailsObj = null;

                    // check if details exists
                    if (TraxObj.get("details") != null) {

                        DetailsObj = (JSONObject) TraxObj.get("details");
                        
                        l_TrackingInfo.setRequestMessage((String) TraxObj.get("message").toString());
                        l_TrackingInfo.setConsignee((String) DetailsObj.get("consignee").toString());
                        l_TrackingInfo.setOrder_information((String) DetailsObj.get("order_information").toString());
                        
                        JSONObject shipperObj = (JSONObject) DetailsObj.get("shipper");
                        JSONObject pickupObj = (JSONObject) DetailsObj.get("pickup");
                        
                        l_TrackingInfo.setPickupOrigin((String) pickupObj.get("origin").toString());
                        l_TrackingInfo.setShipper_Name((String) shipperObj.get("name").toString());
                        
                    } else // error Transaction 
                    {
                        l_TrackingInfo.setRequestMessage((String) TraxObj.get("message").toString() + (String) TraxObj.get("error").toString());
                    }
                    
                    CallableStatement dCall = db.getDBConnection().prepareCall("begin\n"
                            + "  -- Call the function\n"
                            + "  :result := app_courier_pkg.add_tracking_header(p_order_id => :p_order_id,\n"
                            + "                                                 p_request_status => :p_request_status,\n"
                            + "                                                 p_request_message => :p_request_message,\n"
                            + "                                                 p_shipper_name => :p_shipper_name,\n"
                            + "                                                 p_pickup_origin => :p_pickup_origin,\n"
                            + "                                                 p_consignee => :p_consignee,\n"
                            + "                                                 p_order_information => :p_order_information,\n"
                            + "                                                 p_tracking_company => :p_tracking_company,\n"
                            + "                                                 p_tracking_number => :p_tracking_number);\n"
                            + "end;");
                    dCall.registerOutParameter(1, Types.INTEGER);
                    dCall.setString(2, l_TrackingInfo.getTrackingOrderId());
                    dCall.setString(3, l_TrackingInfo.getRequestStatus());
                    dCall.setString(4, l_TrackingInfo.getRequestMessage());
                    dCall.setString(5, l_TrackingInfo.getShipper_Name());
                    dCall.setString(6, l_TrackingInfo.getPickupOrigin());
                    dCall.setString(7, l_TrackingInfo.getConsignee());
                    dCall.setString(8, l_TrackingInfo.getOrder_information());
                    dCall.setString(9, l_TrackingInfo.getTrackingCompany());
                    dCall.setString(10, l_TrackingInfo.getTrackingNumber());
                    dCall.execute();
                    l_TrackingInfo.setTransactionID(dCall.getInt(1));
                    dCall.close();
                    
                    Console.show(" > " + l_TrackingInfo.getTransactionID());
                    /// Lines Details 
                    JSONArray TrackingArray = (JSONArray) DetailsObj.get("tracking_history");

                    if (TrackingArray != null) {
                        if (TrackingArray.size() > 0) {
                            for (int ldx = 0; ldx < TrackingArray.size(); ldx++) {
                                JSONObject HistoryTransaction = (JSONObject) TrackingArray.get(ldx);
                                CallableStatement HistoryCall = db.getDBConnection().prepareCall("begin  \n"
                                        + "  app_courier_pkg.add_tracking_lines(p_header_id => :p_header_id,\n"
                                        + "                                     p_date_time => :p_date_time,\n"
                                        + "                                     p_date_timestamp => :p_date_timestamp,\n"
                                        + "                                     p_status => :p_status,\n"
                                        + "                                     p_status_reason => :p_status_reason,\n"
                                        + "                                     p_sequence_no => :p_sequence_no);\n"
                                        + "end;");
                                HistoryCall.setInt(1, l_TrackingInfo.getTransactionID());
                                HistoryCall.setString(2, nvl(HistoryTransaction.get("date_time").toString()));
                                HistoryCall.setString(3, nvl(HistoryTransaction.get("timestamp").toString()));
                                HistoryCall.setString(4, nvl(HistoryTransaction.get("status").toString()));
                                if (HistoryTransaction.get("status_reason") != null) {
                                    HistoryCall.setString(5, HistoryTransaction.get("status_reason").toString());
                                } else {
                                    HistoryCall.setString(5, "NULL");
                                }
                                HistoryCall.setInt(6, TrackingArray.size() - ldx);
                                HistoryCall.execute();
                                HistoryCall.close();

                            }
                        }
                    }
                } catch (Exception ex) {
                    Console.write("CN Tracking Error Loop: " + ex.getMessage());
                    ex.printStackTrace();
                    continue;
                }

            }
            
        Dstmt.close();
        Drs.close();
    }
 catch (SQLException ex) {
            Console.write("CN Tracking Error : " + ex.getMessage());
    }

}
}
