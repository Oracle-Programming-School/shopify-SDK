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

/**
 *
 * @author Faisal
 */
public class TPL {
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

    public TPL(DB pdp) {
     db = pdp;
    }
 
    public void generateCN() {
        try {
            Statement Dstmt = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet Drs = Dstmt.executeQuery("select COMPANY_NAME, ORDER_ID,COST_CENTER_CODE,consignee_Name , CONSIGNEE_ADDRESS,CONSIGNEE_MOB_NO,CONSIGNEE_EMAIL,DESTINATION_CITY_id,ORIGIN_CITY_NAME,DESTINATION_CITY_NAME,WEIGHT,PIECES,COD_AMOUNT,REPLACE(CUSTOMER_REFERENCE_NO,'#','') CUSTOMER_REFERENCE_NO,case when COD_AMOUNT > 0 then 1 else 2 end  SERVICES,PRODUCT_DETAILS,FRAGILE,REMARKS,CUSTOMER_REFERENCE_NO,CN_DETAILS_ID,INSURANCE_VALUE  from MF_Shopify_CN_details where organization_id = 299 and STATUS is null and CN_NUMBER is null and CN_GENERATION_ERROR is null and COMPANY_NAME ='TPL'");
            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 
                Console.write("TPL CN generation Start for Order ID :  " + Drs.getString(2));

                String URL = "http://api.withrider.com/rider/v2/SaveBooking?"
                        + "loginId=258"
                        + "&ConsigneeName=" + Drs.getString("consignee_Name")
                        + "&ConsigneeRefNo=" + Drs.getString("CUSTOMER_REFERENCE_NO")
                        + "&ConsigneeCellNo=" + Drs.getString("CONSIGNEE_MOB_NO")
                        + "&Address=" + Drs.getString("CONSIGNEE_ADDRESS")
                        + "&DestCityId="+ Drs.getString("DESTINATION_CITY_ID")
                        + "&OriginCityId=2"
                        + "&ServiceTypeId=" + Drs.getString("SERVICES")
                        + "&DeliveryTypeId=2"
                        + "&Pcs=" + Drs.getString("PIECES")
                        + "&Weight=" + Drs.getString("WEIGHT")
                        + "&Description=" + Drs.getString("PRODUCT_DETAILS")
                        + "&CodAmount=" + Drs.getString("COD_AMOUNT")
                        + "&remarks=" + Drs.getString("REMARKS")
                        + "&ShipperAddress=e"
                        + "&apikey=" + Global.TPL_APIkey;
                 String responseString = null;
                ///Get Data TPL
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder().build();
                    Request request = new Request.Builder()
                            .url(URL)
                            .method("GET", null)
                            .build();
                    Response response = client.newCall(request).execute();

                    Console.show(URL);

                    //JSON parser object to parse read file
                    JSONParser jsonParser = new JSONParser();
                    //Read JSON file
                     responseString = (String) response.body().string();
                    Console.show(" Response from TPL " + responseString);
                    Object obj = jsonParser.parse(responseString);

                    JSONObject TPLResponceObj = (JSONObject) obj;

                    if (TPLResponceObj.get("status").toString().equals("ÖK")) {
                        //add CN information Successfully
                        CallableStatement dCall = db.getDBConnection().prepareCall("begin App_UTL.post_CN_iformation(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_CN_STATUS => :P_CN_STATUS,p_CN_GENERATION_MESSGAE => :p_CN_GENERATION_MESSGAE); end;");
                        dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                        dCall.setString(2, "SUCCESS");
                        dCall.setString(3, TPLResponceObj.get("CNUM").toString());
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

}
