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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author abdul.ahad1
 */
public class BlueX {

    private DB db = null;

    public BlueX(DB pdp) {
       db = pdp;
    }

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

    public void addcity() {
        try {
            String jstring;
            List< String> CITY_NAME = new ArrayList< String>();
            List< String> CITY_CODE = new ArrayList< String>();
            HashMap< String, String> Cities = new HashMap< String, String>();
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"country_code\":\"PK\"}\r\n");
            Request request = new Request.Builder()
                    .url("https://bigazure.com/api/json_v3/cities/get_cities.php")
                    .method("POST", body)
                    .addHeader("Authorization", "Basic TEhFLTAxNjgzOjExMGprdXllaDExaGtqc3RnaDEx")
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            jstring = response.body().string();
            Object document = Configuration.defaultConfiguration().jsonProvider().parse(jstring);
            CITY_CODE = JsonPath.read(document, "$.response[*].CITY_CODE");
            CITY_NAME = JsonPath.read(document, "$.response[*].CITY_NAME");

            for (int ldx = 0; ldx < CITY_CODE.size(); ldx++) {
                Cities.put(CITY_CODE.get(ldx), CITY_NAME.get(ldx));
            }

            System.out.println("total cities " + Cities.size());
            for (String i : Cities.keySet()) {
                System.out.println("key: " + i + " value: " + Cities.get(i));
                CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.Post_City_Name(p_Company_Name => :p_Company_Name,p_city_ID => :p_city_ID,p_city_Name => :p_city_Name,p_city_Code => :p_city_Code,p_area => :p_area); end;");
                dCall.setString(1, "BLUEX");
                dCall.setString(2, "0");
                dCall.setString(3, Cities.get(i));
                dCall.setString(4, i);
                dCall.setString(5, "None");
                dCall.execute();
                dCall.close();
                System.out.print("okay done");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void generateCN() {
        try {
            String jstring, response_string = null;
            int status, order_code, cn_id = 0, success = 0;
            boolean IsJsonValid;

            Statement Dstmt = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet Drs = Dstmt.executeQuery("select COMPANY_NAME, ORDER_ID,COST_CENTER_CODE,consignee_Name , CONSIGNEE_ADDRESS,CONSIGNEE_MOB_NO,CONSIGNEE_EMAIL,DESTINATION_CITY_id,ORIGIN_CITY_NAME,destination_city_code,DESTINATION_CITY_NAME,to_char(WEIGHT,'990D9') WEIGHT,PIECES,COD_AMOUNT,REPLACE(CUSTOMER_REFERENCE_NO,'#','') CUSTOMER_REFERENCE_NO,case when COD_AMOUNT > 0 then 'COD' else 'NONCOD' end  SERVICES,PRODUCT_DETAILS,FRAGILE,DESTINATION_CITY_NAME,REMARKS,CUSTOMER_REFERENCE_NO,CN_DETAILS_ID,INSURANCE_VALUE  from MF_Shopify_CN_details where organization_id = 299 and STATUS is null and CN_NUMBER is null and CN_GENERATION_ERROR is null and COMPANY_NAME ='BLUEX'");

            Console.show("Query Performed");
            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 
                Console.write("BlueX CN generation Start for Order ID :  " + Drs.getString(2));
                // START: POST HTTP REQUEST TO BLUEX API FOR CN GENERATION

                try {

                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, "{\r\n  \"shipper_name\": \"Test Retail Limited\",\r\n  \"shipper_email\": \"WECARE@Testonline.pk\",\r\n  \"shipper_contact\": \"920\",\r\n  \"shipper_address\": \"Test Retaile.\",\r\n  \"shipper_city\": \"LHE\",\r\n  \"customer_name\":\"" + Drs.getString("CONSIGNEE_NAME") + "\",\r\n  \"customer_email\":\"" + Drs.getString("CONSIGNEE_EMAIL") + "\", \r\n  \"customer_contact\":\"" + Drs.getString("CONSIGNEE_MOB_NO") + "\",\r\n  \"customer_address\":\"" + Drs.getString("CONSIGNEE_ADDRESS") + "\",\r\n  \"customer_city\":\"" + Drs.getString("destination_city_code") + "\" ,\r\n  \"customer_country\": \"PK\",\r\n  \"customer_comment\": \"None\",\r\n  \"shipping_charges\": 0 ,\r\n  \"payment_type\": \"COD\",\r\n  \"service_code\": \"BE\",\r\n  \"total_order_amount\": " + Drs.getString("COD_AMOUNT") + ",\r\n  \"total_order_weight\":" + Drs.getString("WEIGHT") + ", \r\n  \"order_refernce_code\":\"" + Drs.getString("CUSTOMER_REFERENCE_NO") + "\",\r\n  \"fragile\": \"N\",\r\n  \"parcel_type\": \"P\",\r\n  \"insurance_require\" : \"N\",\r\n  \"insurance_value\" : \"0\",\r\n  \"testbit\": \"Y\",\r\n  \"cn_generate\": \"Y\",\r\n  \"multi_pickup\": \"Y\",\r\n  \"products_detail\": [\r\n    {\r\n      \"product_code\": \"0\",\r\n      \"product_name\":\"" + Drs.getString("PRODUCT_DETAILS") + "\",\r\n      \"product_price\": \"0\",\r\n      \"product_weight\": \"0\",\r\n      \"product_quantity\":\"" + Drs.getString("PIECES") + "\",\r\n      \"product_variations\": \"None\",\r\n      \"sku_code\": \"None\"\r\n    }\r\n  ]\r\n}");
                    Request request = new Request.Builder()
                            .url("https://bigazure.com/api/json_v3/shipment/create_shipment.php")
                            .method("POST", body)
                            .addHeader("Authorization", "Basic TDEx")
                            .addHeader("Content-Type", "application/json")
                            .build();
                    Response response = client.newCall(request).execute();
                    jstring = response.body().string();

                    Console.show(" BlueX :  " + jstring);

                    //JSON parser object to parse read file
                    JSONParser jsonParser = new JSONParser();

                    //Read JSON file
                    Object obj = jsonParser.parse(jstring);
                    JSONObject BlueXResponceObj = (JSONObject) obj;

                    String cnMessage = null;

                    if (BlueXResponceObj.get("status").toString().equals("1")) {
                        //add CN information
                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "SUCCESS");
                        dCall.setString(3, BlueXResponceObj.get("cn").toString());
                        dCall.execute();
                        dCall.close();
                    } else {
                        //add CN information
                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "FAIL");
                        dCall.setString(3, BlueXResponceObj.get("response").toString());
                        dCall.execute();
                        dCall.close();
                    }

                } catch (Exception ex) {
                    Console.write("BlueX CN generation  Error found at " + ex.getMessage());
                    //add CN information failure
                    PutCN(Drs.getString("CN_DETAILS_ID"), "FAIL", response_string);
                }
            }
            Dstmt.close();
        } catch (SQLException ex) {
            Console.write("Bluex CN Generation Error : " + ex.getMessage());
        }
    }

    public void PutCN(String CN_det_id, String status, String cn_id) throws SQLException {
        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
        dCall.setString(1, CN_det_id);
        dCall.setString(2, status);
        dCall.setString(3, cn_id);
        dCall.execute();
        dCall.close();
    }

}
