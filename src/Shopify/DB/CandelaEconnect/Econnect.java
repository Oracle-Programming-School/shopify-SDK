/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.DB.CandelaEconnect;

import Shopify.DB.DB;
import Shopify.Log.Console;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author abdul.ahad1
 */
public class Econnect {

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

    public Econnect(DB P_db) {
        db = P_db;
    }

    public void Post_Order() {
        try {
            String select_hdr = "select distinct to_char(t.ORDER_ID) od, t.ORDER_NAME,\n"
                    + "                     shopid, \n"
                    + "                     to_char(t.CREATED_AT,'RRRR-MM-DD') ORDER_DATE, \n"
                    + "                     t.FIRST_NAME, \n"
                    + "                     t.EMAIL, \n"
                    + "                     t.ADDRESS, \n"
                    + "                     t.CITY, \n"
                    + "                     t.COUNTRY, \n"
                    + "                     'None' state, \n"
                    + "                     t.PHONE, \n"
                    + "                     'Processing' status, \n"
                    + "                     '0' TOTAL_SHIPPING_PRICE, \n"
                    + "                     0 customerno, \n"
                    + "                     t.COMMENTS,  \n"
                    + "                     CN_NUMBER CourierNumber, \n"
                    + "                     t.WEIGHT, 'Local' Locality, t.ORGANIZATION_ID , t.store_id \n"
                    + "                                from SRL_ECONNECT_new_ORDERS_V t ";

            String select_det = "select t.SKU product_code,\n"
                    + "t.QUANTITY,\n"
                    + "t.DISCOUNT_PERECNTAGE,\n"
                    + "t.PRICE,\n"
                    + "t.PRE_TAX_PRICE\n"
                    + " from SRL_ECONNECT_new_ORDERS_V t";

            PreparedStatement ps_hdr = db.getDBConnection().prepareStatement(select_hdr, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs_hdr = ps_hdr.executeQuery();

            for (int ldx = 1; ldx <= getRowCount(rs_hdr); ldx++) {
                rs_hdr.absolute(ldx);

                ProductHeader ph = new ProductHeader();
                ph.setAppId("12");
                ph.setAppKey("FID5re2F8oENk2");
                ph.setOrderId(rs_hdr.getString("ORDER_NAME"));
                ph.setShopId(rs_hdr.getString("shopid"));
                ph.setOrderDate(rs_hdr.getString("ORDER_DATE"));
                ph.setFirstName(rs_hdr.getString("FIRST_NAME"));
                ph.setCustomerEmai(rs_hdr.getString("EMAIL"));
                ph.setAddress(rs_hdr.getString("ADDRESS"));
                ph.setCity(rs_hdr.getString("CITY"));
                ph.setCountry(rs_hdr.getString("COUNTRY"));
                ph.setState(rs_hdr.getString("state"));
                ph.setTelephone(rs_hdr.getString("PHONE"));
                ph.setStatus(rs_hdr.getString("status"));
                ph.setShippingCost(rs_hdr.getString("TOTAL_SHIPPING_PRICE"));
                ph.setCustomerNo("");
                ph.setComments(rs_hdr.getString("COMMENTS"));
                ph.setWeight(rs_hdr.getString("WEIGHT"));
                ph.setLocality(rs_hdr.getString("Locality"));

                // Call + > 
                PreparedStatement Q_Lines = db.getDBConnection().prepareStatement(select_det + " where order_id = " + rs_hdr.getString("od") + " and shopid=" + rs_hdr.getString("shopid"), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet RQ_Lines = (ResultSet) Q_Lines.executeQuery();
                List< Products> productsdetais = new ArrayList< Products>();
                for (int LDX_lines = 1; LDX_lines <= getRowCount(RQ_Lines); LDX_lines++) {
                    RQ_Lines.absolute(LDX_lines);
                    Products pro = new Products();
                    pro.setProductCode(RQ_Lines.getString("product_code"));
                    pro.setQty(Integer.toString(RQ_Lines.getInt("QUANTITY")));
                    pro.setDiscountPerc((RQ_Lines.getString("DISCOUNT_PERECNTAGE")));
                    pro.setItemAmount((RQ_Lines.getString("PRICE")));
                    pro.setItemTotal((RQ_Lines.getString("PRE_TAX_PRICE")));
                    productsdetais.add(pro);
                }
                Q_Lines.close();

                ph.setProducts(productsdetais);
                ObjectMapper objectMapper = new ObjectMapper();
                String nestedJsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ph);
                String jstring = nestedJsonPayload;

                 Console.show(jstring);
                 System.out.println(rs_hdr.getString("od")+" json is "+jstring);
                /// Request Started
                OkHttpClient client = new OkHttpClient().newBuilder().build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, jstring);

                Request request = new Request.Builder()
                        .url("http://192.168.1.1:96/api/orders/PostOrder")
                        .method("POST", body)
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                String jresponse = response.body().string();
                Object document = Configuration.defaultConfiguration().jsonProvider().parse(jresponse);
                String resp = JsonPath.read(document, "$.msg");

                post_new_order(rs_hdr.getString("od"), rs_hdr.getString("ORGANIZATION_ID"), rs_hdr.getInt("store_id"), resp);

            } //end for loop 
            rs_hdr.close();
            ps_hdr.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void post_new_order(String order_id, String organization_id, int storeID, String return_status) throws SQLException {
        CallableStatement dCall = db.getDBConnection().prepareCall("begin srl_ec_post_new_order(p_order_id => :p_order_id,\n"
                + "                        p_organization_id => :p_organization_id, p_store_id => :p_store_id,\n"
                + "                        p_api_return_status => :p_api_return_status); end;");
        dCall.setString(1, order_id);
        dCall.setString(2, organization_id);
        dCall.setInt(3, storeID);
        dCall.setString(4, return_status);
        dCall.execute();
        dCall.close();
    }

    public void update_order() {
        PreparedStatement ps_hdr = null;
        try {
            String select_hdr = "select distinct to_char(t.ORDER_ID) od, t.ORDER_NAME,\n"
                    + "                    shopid,\n"
                    + "                    to_char(t.CREATED_AT,'RRRR-MM-DD') ORDER_DATE,\n"
                    + "                    t.FIRST_NAME,\n"
                    + "                    t.EMAIL,\n"
                    + "                    t.ADDRESS,\n"
                    + "                    t.CITY,\n"
                    + "                    t.COUNTRY,\n"
                    + "                    'None' state,\n"
                    + "                    t.PHONE,\n"
                    + "                    'Processing' status,\n"
                    + "                    '0' TOTAL_SHIPPING_PRICE,\n"
                    + "                    '0' customerno,\n"
                    + "                    t.COMMENTS,\n"
                    + "                    T.COMPANY_NAME courier_company,\n"
                    + "                    T.CN_NUMBER CourierNumber,\n"
                    + "                    t.WEIGHT,\n"
                    + "                    'Local' Locality, t.ORGANIZATION_ID , ORDER_COUNT, store_id \n"
                    + "                    from SRL_ECONNECT_UPDATE_ORDERS_V t ";

            String select_det = "select t.SKU product_code,\n"
                    + "t.QUANTITY,\n"
                    + "t.DISCOUNT_PERECNTAGE,\n"
                    + "t.PRICE,\n"
                    + "t.PRE_TAX_PRICE\n"
                    + " from SRL_ECONNECT_UPDATE_ORDERS_V t";

            ps_hdr = db.getDBConnection().prepareStatement(select_hdr, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs_hdr = ps_hdr.executeQuery();

            for (int ldx = 1; ldx <= getRowCount(rs_hdr); ldx++) {
                rs_hdr.absolute(ldx);

                ProductHeader ph = new ProductHeader();
                ph.setAppId("12");
                ph.setAppKey("FIJc0");
                ph.setOrderId(rs_hdr.getString("ORDER_NAME"));
                ph.setShopId(rs_hdr.getString("shopid"));
                ph.setOrderDate(rs_hdr.getString("ORDER_DATE"));
                ph.setFirstName(rs_hdr.getString("FIRST_NAME"));
                ph.setCustomerEmai(rs_hdr.getString("EMAIL"));
                ph.setAddress(rs_hdr.getString("ADDRESS"));
                ph.setCity(rs_hdr.getString("CITY"));
                ph.setCountry(rs_hdr.getString("COUNTRY"));
                ph.setState(rs_hdr.getString("state"));
                ph.setTelephone(rs_hdr.getString("PHONE"));
                ph.setStatus(rs_hdr.getString("status"));
                ph.setShippingCost(rs_hdr.getString("TOTAL_SHIPPING_PRICE"));
                ph.setCustomerNo("");
                ph.setComments(rs_hdr.getString("COMMENTS"));
                ph.setCourierCompany(rs_hdr.getString("courier_company"));
                ph.setCourierNumber(rs_hdr.getString("CourierNumber"));
                ph.setWeight(rs_hdr.getString("WEIGHT"));
                ph.setLocality(rs_hdr.getString("Locality"));

                System.out.println(" Before CN Posting " + rs_hdr.getString("ORDER_NAME"));

                // Call + > 
                PreparedStatement Q_Lines = db.getDBConnection().prepareStatement(select_det + " where order_id = " + rs_hdr.getString("od") + " and store_id=" + rs_hdr.getString("store_id"), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet RQ_Lines = (ResultSet) Q_Lines.executeQuery();

                //incase orders are not equal 
                /*if (getRowCount(RQ_Lines) != rs_hdr.getInt("ORDER_COUNT")) {
                    Console.show("Order count is not equal to lines");
                    continue;
                }*/
                //Mark CN First
                String CN_Message = new String("None");
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, "{\r\n\"AppId\":\"12\",\r\n\"AppKey\":\"FIJc8oENk2\",\r\n\"OrderId\":\"" + rs_hdr.getString("ORDER_NAME") + "\",\r\n\"shopId\":\"" + rs_hdr.getString("shopId") + "\",\r\n\"CourierCompany\":\"" + rs_hdr.getString("courier_company") + "\",\r\n\"CourierNumber\": \"" + rs_hdr.getString("CourierNumber") + "\",\r\n\"Weight\": \"" + rs_hdr.getString("WEIGHT") + "\",\r\n\"Locality\": \"Local\"\r\n}");
                    Request request = new Request.Builder()
                            .url("http://192.168.1.1:96/api/Orders/UpdateOrdersCNDetails")
                            .method("POST", body)
                            .addHeader("Authorization", "Basic TaDEx")
                            .addHeader("Content-Type", "application/json")
                            .build();
                    Response response = client.newCall(request).execute();
                    String jresponse = response.body().string();
                    Object document = Configuration.defaultConfiguration().jsonProvider().parse(jresponse);
                    CN_Message = JsonPath.read(document, "$.msg");
                    System.err.println("UpdateOrdersCNDetails JResponse \n" + jresponse);
                } catch (Exception ex) {
                    CN_Message = ex.getMessage();
                }
                //End CN Details
                System.out.println(" After CN Posting " + rs_hdr.getString("ORDER_NAME"));

                List< Products> productsdetais = new ArrayList< Products>();

                for (int LDX_lines = 1; LDX_lines <= getRowCount(RQ_Lines); LDX_lines++) {
                    RQ_Lines.absolute(LDX_lines);
                    Products pro = new Products();
                    pro.setProductCode(RQ_Lines.getString("product_code"));
                    pro.setQty(Integer.toString(RQ_Lines.getInt("QUANTITY")));
                    pro.setDiscountPerc((RQ_Lines.getString("DISCOUNT_PERECNTAGE")));
                    pro.setItemAmount((RQ_Lines.getString("PRICE")));
                    pro.setItemTotal((RQ_Lines.getString("PRE_TAX_PRICE")));
                    productsdetais.add(pro);
                }

                ph.setProducts(productsdetais);
                ObjectMapper objectMapper = new ObjectMapper();
                String nestedJsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ph);
                System.out.println(nestedJsonPayload);
                String jstring = nestedJsonPayload;

                OkHttpClient client = new OkHttpClient().newBuilder().build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, jstring);
                Request request = new Request.Builder()
                        .url("http://192.168.1.1:96/api/orders/UpdateOrder")
                        .method("POST", body)
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                String jresponse = response.body().string();
                Object document = Configuration.defaultConfiguration().jsonProvider().parse(jresponse);
                String resp = JsonPath.read(document, "$.msg");
                System.err.println("UpdateOrder JResponse \n" + jresponse);
                //*/
                post_update_order(rs_hdr.getString("od"), rs_hdr.getString("ORGANIZATION_ID"), CN_Message, rs_hdr.getString("CourierNumber"), rs_hdr.getInt("store_id"));

                System.out.println(" After post_update_order " + rs_hdr.getString("ORDER_NAME"));
                RQ_Lines.close();
                Q_Lines.close();
            } //end for loop 
            rs_hdr.close();
            ps_hdr.close();
        } catch (InvalidJsonException | IOException | SQLException ex) {
            Console.show("Update Candela Error : " + ex.getMessage()
            );
            try {
                ps_hdr.close();
            } catch (SQLException qx) {
            }
        }

    }

    public void post_update_order(String order_id, String organization_id, String return_status, String TrackingNumber, int storeID) throws SQLException {
        CallableStatement dCall = db.getDBConnection().prepareCall("begin\n"
                + "  srl_ec_post_update_order(p_order_id => :p_order_id,\n"
                + "                           p_organization_id => :p_organization_id,\n"
                + "                           p_api_return_status => :p_api_return_status,\n"
                + "                           p_tracking_number => :p_tracking_number, p_store_id => :p_store_id);\n"
                + "end;");
        dCall.setString(1, order_id);
        dCall.setString(2, organization_id);
        dCall.setString(3, return_status);
        dCall.setString(4, TrackingNumber);
        dCall.setInt(5, storeID);
        dCall.execute();
        dCall.close();
    }

    public void Cancel_order() {
        PreparedStatement ps_hdr = null;
        ResultSet rs_hdr = null;
        try {
            try {
                String select_hdr = "select  ORDER_ID , CANDELA_ORDER_ID , ORDER_STATUS, ShopId\n"
                        + "from SRL_ECONNECT_CANCEL_ORDERS_V ";

                ps_hdr = db.getDBConnection().prepareStatement(select_hdr, ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                rs_hdr = ps_hdr.executeQuery();

                for (int ldx = 1; ldx <= getRowCount(rs_hdr); ldx++) {
                    rs_hdr.absolute(ldx);

                    OkHttpClient client = new OkHttpClient().newBuilder().build();
                    MediaType mediaType = MediaType.parse("text/plain");
                    RequestBody body = RequestBody.create(mediaType, "");
                    Request request = new Request.Builder()
                            .url("http://192.168.1.1:96/api/Orders/CancelOrder?AppId=12&AppKey=Nk2&shopId="+rs_hdr.getString("ShopId").trim()+"&OrderId=" + rs_hdr.getString("CANDELA_ORDER_ID"))
                            .method("POST", body)
                            .addHeader("Authorization", "Basic TEhRnaDEx")
                            .build();
                    Response response = client.newCall(request).execute();
                    String jresponse = response.body().string();
                    Object document = Configuration.defaultConfiguration().jsonProvider().parse(jresponse);
                    String resp = JsonPath.read(document, "$.msg");

                    post_Cancel_order(rs_hdr.getString("ORDER_ID"), "299", resp, rs_hdr.getString("ORDER_STATUS"));

                } //end for loop 
                rs_hdr.close();
                ps_hdr.close();

            } catch (Exception ex) {
                Console.show("Cancel order Candela Error : " + ex.getMessage());
                if (rs_hdr != null) {
                    rs_hdr.close();
                }
                if (ps_hdr != null) {
                    ps_hdr.close();
                }

            }
        } catch (Exception ex) {
            Console.show("Cancel order --> Error : " + ex.getMessage());
        }

    }

    public void post_Cancel_order(String order_id, String organization_id, String return_status, String ORDER_STATUS) throws SQLException {
        CallableStatement dCall = db.getDBConnection().prepareCall("begin\n"
                + "  \n"
                + "  SRL_EC_POST_Cancel_ORDER(P_ORDER_ID => :P_ORDER_ID,\n"
                + "                           P_ORGANIZATION_ID => :P_ORGANIZATION_ID,\n"
                + "                           P_API_RETURN_STATUS => :P_API_RETURN_STATUS,\n"
                + "                           p_order_status => :p_order_status,\n"
                + "                           p_store_id => :p_store_id);\n"
                + "end;");
        dCall.setString(1, order_id);
        dCall.setString(2, organization_id);
        dCall.setString(3, return_status);
        dCall.setString(4, ORDER_STATUS);
        dCall.setInt(5, 1);
        dCall.execute();
        dCall.close();
    }

    public void updateTblSales() {

        String selectCandela = "select s.adjustment_comments order_Name, \n"
                + "s.shop_id,\n"
                + "s.sale_id ,\n"
                + "s.Additional_Comments from dbo.tblSales s\n"
                + "where 1=1\n"
                + "AND CONVERT( date    , s.TransactionTime ,104) >= '06-JAN-2022' \n"
                + "AND len(s.Additional_Comments) < 5\n"
                + "and  s.shop_id in( 13,75)\n"
                + "and s.GT_amount >= 0\n"
                + "and s.adjustment_comments != '' ";

        /**
         * ***************************************************
         */
        String selectOra = "select t.ORDER_NAME,\n"
                + "       t.COMMENTS, \n"
                + "       t.shopid\n"
                + "        from ECONNECT_UPDATE_V t\n"
                + "where t.ORGANIZATION_ID = 299\n"
                + "and t.ORDER_NAME = ?\n"
                + "and t.shopid = ?";
        /**
         * *****************************************************
         */
        String updateTblSales = "UPDATE [candelastml].[dbo].[tblSales]  \n"
                + "SET Additional_Comments = ? \n"
                + "WHERE adjustment_comments = ? \n"
                + "AND shop_id = ? \n"
                + "AND sale_id = ?";
        try {
            PreparedStatement ps = db.getCandelaConnection().prepareStatement(selectCandela, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = ps.executeQuery();
            Thread.sleep(1000);
            if (!rs.next()) {
                rs.close();
                ps.close();
                Console.show("Nothing to proceed in updateTblSales");
            } else {
                do {

                    /**
                     * Start fetching additional comments of an order from
                     * oracle *********************
                     */
                    PreparedStatement psOra = db.getDBConnection().prepareStatement(selectOra, ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_READ_ONLY);
                    psOra.setString(1, rs.getString("order_Name"));
                    psOra.setInt(2, rs.getInt("shop_id"));
                    ResultSet rsOra = psOra.executeQuery();

                    if (!rsOra.next()) {
                        rsOra.close();
                        psOra.close();
                        // Console.show("Record not found in oracle");
                    } else {
                        do {

                            /*start updating tblsales*/
                            Thread.sleep(100);
                            PreparedStatement updatePs = db.getCandelaConnection().prepareStatement(updateTblSales);

                            updatePs.setString(1, rsOra.getString("COMMENTS"));
                            updatePs.setString(2, rsOra.getString("ORDER_NAME"));
                            updatePs.setInt(3, rsOra.getInt("shopid"));
                            updatePs.setInt(4, rs.getInt("sale_id"));
                            int update = updatePs.executeUpdate();
                            updatePs.close();
                            Console.show("Update TblSales For " + rsOra.getString("ORDER_NAME"));
                        } while (rsOra.next());
                        rsOra.close();
                        psOra.close();
                    }
                    /*end updating tblsales*/

                    /**
                     * End fetching additional comments of an order from oracle***********************
                     */
                } while (rs.next());
                rs.close();
                ps.close();
            }

        } catch (Exception ignore) {
            // Console.show("Error in -> updateTblSales: " + ex.getMessage());
            // ex.printStackTrace();
        }

    }

}
