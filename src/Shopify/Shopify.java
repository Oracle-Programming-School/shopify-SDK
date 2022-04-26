/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify;

import Shopify.DB.DB;
import FND.Global;
import Shopify.Log.Console;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.nio.CharBuffer;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author M.Faisal1521
 *
 *
 * ********************************************************************
 * User Date Log
 * ******************************************************************** mFaisal
 * 27-Jun-2020 added @dev27062020
 *
 *
 *
 *
 *
 */
public class Shopify {
// comment for test

    OkHttpClient client = new OkHttpClient();
    private String shopifyAuthorizationKey;
    String urlString;
    DB db = null;
//Test

    public Shopify(DB P_db, String PauthorizationKey) {
        client = new OkHttpClient.Builder()
                .connectTimeout(160, TimeUnit.SECONDS)
                .writeTimeout(160, TimeUnit.SECONDS)
                .readTimeout(160, TimeUnit.SECONDS)
                .build();

        urlString = Global.ShopifyAppURL + "/admin/";
        db = P_db;
        this.shopifyAuthorizationKey = PauthorizationKey;
    }

    public Request getRequest(String pURL) {

        Console.write(urlString + pURL);

//        try {
//            Thread.sleep(40000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Shopify.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return new Request.Builder().url(urlString + pURL)
                .method("GET", null)
                .addHeader("Authorization", this.shopifyAuthorizationKey)
                .build();
    }

    //@SuppressWarnings("unchecked")
    public void FetchAllOrders(String parameters, String TransactionType) {

        try {

            Response response = client.newCall(getRequest("api/2020-07/orders.json?" + parameters)).execute();

            //JSON parser object to parse read file
            JSONParser jsonParser = new JSONParser();

            String resultString = response.body().string();

            //console.write(resultString);
            //Read JSON file
            Object obj = jsonParser.parse(resultString);
            JSONObject OrdersObj = (JSONObject) obj;
            JSONArray OrdersList = (JSONArray) OrdersObj.get("orders");
            orderDetails l_orderDetails;

            for (int ldx = 0; ldx < OrdersList.size(); ldx++) {

                l_orderDetails = (orderDetails) parseOrdersList((JSONObject) OrdersList.get(ldx));

                if (TransactionType.equals(Global.Synchronize_ERP)) {
                    //

                    //  console.allLog.info("Check Synchronization...");
                    //console.write("Check Synchronization...");
                    //Get Connection
                    try {
                        CallableStatement stmt = (CallableStatement) db.getDBConnection().prepareCall("begin :result := app_utl.is_order_updatable(p_order_id => :p_order_id,p_updated_at => :p_updated_at,p_organization_id => 299); end;");
                        stmt.registerOutParameter(1, Types.VARCHAR);
                        stmt.setString(2, l_orderDetails.OrderId);
                        stmt.setString(3, l_orderDetails.updated_at);
                        stmt.execute();
                        String result = stmt.getString(1);
                        stmt.close();

                        // console.write("Check Synchronization Order Id " +l_orderDetails.OrderId +" "+result);
                        if (result.equals("FALSE")) {
                            continue;
                        }

                    } catch (Exception e) {
                        Console.write("Check Synchronization Error ..." + e.getMessage());
                        Console.write("Error in Check Synchronization..." + e.getMessage());
                    }
                }

                // InsertData to Database table
                this.insertOrderHeader(l_orderDetails, false);
                Console.write(" Order ID " + " :" + l_orderDetails.OrderId + " Insert / Updated to DB" + " B = " + Global.Synchronize_ERP);
            }

        } catch (IOException | ParseException e) {
            Console.write("FetchAllOrders / error " + e.getMessage());
        }
    }

    //@SuppressWarnings("unchecked")
    public void FetchSingleOrder(String parameters, boolean isFulfilmentOrders) {

        try {
            Response response = client.newCall(getRequest("orders/" + parameters + ".json")).execute();
            String responseString = null;
            //JSON parser object to parse read file
            JSONParser jsonParser = new JSONParser();
            responseString = response.body().string();

            // Console.write("FetchSingleOrder // Responce "+responseString);
            //Read JSON file
            Object obj = jsonParser.parse(responseString);
            JSONObject OrdersObj = (JSONObject) obj;

            OrdersObj = (JSONObject) OrdersObj.get("order");
            if (OrdersObj == null) {
                Console.write("no Data found against this Parameter :" + parameters);
                return;
            }

            orderDetails l_orderDetails = (orderDetails) parseOrdersList((JSONObject) OrdersObj);

            //InsertData to Database table
            this.insertOrderHeader(l_orderDetails, isFulfilmentOrders);
            // Console.write("Log    " + parameters);
        } catch (IOException | ParseException e) {
            Console.show(" FetchSingleOrder / Error " + e.getMessage());
        }

    }

    ///
    //@SuppressWarnings("unchecked")
    public void SetOrderCity(String parameters, String pCity) throws IOException, ParseException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n"
                + "  \"order\": {\n"
                + "    \"id\": " + parameters + ",\n"
                + "    \"shipping_address\": {\n"
                + "      \"city\": \"" + pCity + "\"\n"
                + "    }\n"
                + "  }\n"
                + "}");
        Request request = new Request.Builder()
                .url(Global.ShopifyAppURL + "/admin/orders/" + parameters + ".json")
                .method("PUT", body)
                .addHeader("Authorization", this.shopifyAuthorizationKey)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        //Read JSON file
        Object obj = jsonParser.parse(response.body().string());
        JSONObject OrdersObj = (JSONObject) obj;

        JSONObject l_OrdersObj = (JSONObject) OrdersObj.get("order");

        if (l_OrdersObj == null) {
            Console.write(OrdersObj.toString());
            return;

        }

        orderDetails l_orderDetails = (orderDetails) parseOrdersList((JSONObject) l_OrdersObj);
        //InsertData to Database table
        this.insertOrderHeader(l_orderDetails, false);

    }

    //nvl
    public String nvl(Object v) {
        if (v == null) {
            return "null";
        } else {
            return v.toString();
        }
    }

    private orderDetails parseOrdersList(JSONObject Order) throws IOException, ParseException {
        //Get employee object within list
        //Get employee first name
        orderDetails l_orderDetails = new orderDetails();
        l_orderDetails.OrderId = nvl(Order.get("id"));

        ///----Verify Transactions of Order 
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url(Global.ShopifyAppURL + "/admin/api/2020-07/orders/" + l_orderDetails.OrderId + "/transactions.json")
                    .method("GET", null)
                    .addHeader("Authorization", this.shopifyAuthorizationKey)
                    .build();
            Response response = client.newCall(request).execute();
            String responseString = response.body().string();

            //Console.write(responseString);
            //JSON parser object to parse read file
            JSONParser jsonParser = new JSONParser();
            //Read JSON file
            Object obj = jsonParser.parse(responseString);
            JSONObject OrdersTransactionObj = (JSONObject) obj;

            JSONArray OrdersTransactionArry = (JSONArray) OrdersTransactionObj.get("transactions");

            //Transaction array working
            if (OrdersTransactionArry != null) {
                if (OrdersTransactionArry.size() > 0) {
                    l_orderDetails.initializationTransactions(OrdersTransactionArry.size());

                    for (int ldx = 0; ldx < OrdersTransactionArry.size(); ldx++) {
                        //Local Variable for store Json record
                        transactions ltransactions = new transactions();

                        //get Transaction from Array of Shopify Data
                        JSONObject jsonTransaction = (JSONObject) OrdersTransactionArry.get(ldx);

                        ltransactions.id = nvl(jsonTransaction.get("id"));
                        ltransactions.order_id = nvl(jsonTransaction.get("order_id"));
                        ltransactions.kind = nvl(jsonTransaction.get("kind"));
                        ltransactions.gateway = nvl(jsonTransaction.get("gateway"));
                        ltransactions.status = nvl(jsonTransaction.get("status"));
                        ltransactions.message = nvl(jsonTransaction.get("message"));
                        ltransactions.processed_at = nvl(jsonTransaction.get("processed_at"));
                        ltransactions.error_code = nvl(jsonTransaction.get("error_code"));
                        ltransactions.source_name = nvl(jsonTransaction.get("source_name"));
                        ltransactions.amount = nvl(jsonTransaction.get("amount"));
                        ltransactions.currency = nvl(jsonTransaction.get("currency"));
                        ltransactions.authorization = nvl(jsonTransaction.get("authorization"));

                        ///add LocalData  into Order details Array
                        l_orderDetails.transactions[ldx] = ltransactions;
                    }
                }
            }

        } catch (IOException e) {
            Console.write("Erron in Order Transactions : " + l_orderDetails.OrderId);
            throw e;
        }

        ///--------------------------------------------------------------------
        l_orderDetails.email = nvl(Order.get("email"));
        l_orderDetails.closed_at = nvl(Order.get("closed_at"));
        l_orderDetails.created_at = nvl(Order.get("created_at"));
        l_orderDetails.updated_at = nvl(Order.get("updated_at"));
        l_orderDetails.number = nvl(Order.get("number"));
        l_orderDetails.note = nvl(Order.get("note"));
        l_orderDetails.token = nvl(Order.get("token"));
        l_orderDetails.gateway = nvl(Order.get("gateway"));
        l_orderDetails.test = nvl(Order.get("test"));
        l_orderDetails.total_price = nvl(Order.get("total_price"));
        l_orderDetails.subtotal_price = nvl(Order.get("subtotal_price"));
        l_orderDetails.total_weight = nvl(Order.get("total_weight"));
        l_orderDetails.total_tax = nvl(Order.get("total_tax"));
        l_orderDetails.taxes_included = nvl(Order.get("taxes_included"));
        l_orderDetails.currency = nvl(Order.get("currency"));
        l_orderDetails.financial_status = nvl(Order.get("financial_status"));
        l_orderDetails.confirmed = nvl(Order.get("confirmed"));
        l_orderDetails.total_discounts = nvl(Order.get("total_discounts"));
        l_orderDetails.total_line_items_price = nvl(Order.get("total_line_items_price"));
        l_orderDetails.cart_token = nvl(Order.get("cart_token"));
        l_orderDetails.buyer_accepts_marketing = nvl(Order.get("buyer_accepts_marketing"));
        l_orderDetails.name = nvl(Order.get("name"));
        l_orderDetails.referring_site = nvl(Order.get("referring_site"));
        l_orderDetails.landing_site = nvl(Order.get("landing_site"));
        l_orderDetails.cancelled_at = nvl(Order.get("cancelled_at"));
        l_orderDetails.cancel_reason = nvl(Order.get("cancel_reason"));
        l_orderDetails.total_price_usd = nvl(Order.get("total_price_usd"));
        l_orderDetails.checkout_token = nvl(Order.get("checkout_token"));
        l_orderDetails.reference = nvl(Order.get("reference"));
        l_orderDetails.user_id = nvl(Order.get("user_id"));
        l_orderDetails.location_id = nvl(Order.get("location_id"));
        l_orderDetails.source_identifier = nvl(Order.get("source_identifier"));
        l_orderDetails.source_url = nvl(Order.get("source_url"));
        l_orderDetails.processed_at = nvl(Order.get("processed_at"));
        l_orderDetails.device_id = nvl(Order.get("device_id"));
        l_orderDetails.phone = nvl(Order.get("phone"));
        l_orderDetails.customer_locale = nvl(Order.get("customer_locale"));
        l_orderDetails.app_id = nvl(Order.get("app_id"));
        l_orderDetails.browser_ip = nvl(Order.get("browser_ip"));
        l_orderDetails.landing_site_ref = nvl(Order.get("landing_site_ref"));
        l_orderDetails.order_number = nvl(Order.get("order_number"));
        l_orderDetails.processing_method = nvl(Order.get("processing_method"));
        l_orderDetails.checkout_id = nvl(Order.get("checkout_id"));
        l_orderDetails.source_name = nvl(Order.get("source_name"));
        l_orderDetails.fulfillment_status = nvl(Order.get("fulfillment_status"));
        l_orderDetails.tags = nvl(Order.get("tags"));
        l_orderDetails.contact_email = nvl(Order.get("contact_email"));
        l_orderDetails.order_status_url = nvl(Order.get("order_status_url"));
        l_orderDetails.presentment_currency = nvl(Order.get("presentment_currency"));

        ///discount_applications
        JSONArray discount_applications_Arr = (JSONArray) Order.get("discount_applications");
        if (discount_applications_Arr.size() > 0) {
            ///Read Object
            JSONObject discount_applications_obj = (JSONObject) discount_applications_Arr.get(0);

            l_orderDetails.is_discount_applications = true;
            l_orderDetails.discount_applications_detail.allocation_method = nvl(discount_applications_obj.get("allocation_method"));
            l_orderDetails.discount_applications_detail.code = nvl(discount_applications_obj.get("code"));
            l_orderDetails.discount_applications_detail.target_selection = nvl(discount_applications_obj.get("target_selection"));
            l_orderDetails.discount_applications_detail.target_type = nvl(discount_applications_obj.get("target_type"));
            l_orderDetails.discount_applications_detail.type = nvl(discount_applications_obj.get("type"));
            l_orderDetails.discount_applications_detail.value = nvl(discount_applications_obj.get("value"));
            l_orderDetails.discount_applications_detail.value_type = nvl(discount_applications_obj.get("value_type"));
        }

        ///discount_codes
        JSONArray discount_codes_Arr = (JSONArray) Order.get("discount_codes");
        if (discount_codes_Arr.size() > 0) {
            ///Read Object 
            JSONObject discount_codes_Obj = (JSONObject) discount_codes_Arr.get(0);

            l_orderDetails.is_discount_codes = true;
            l_orderDetails.discount_codes_detail.amount = nvl(discount_codes_Obj.get("amount"));
            l_orderDetails.discount_codes_detail.code = nvl(discount_codes_Obj.get("code"));
            l_orderDetails.discount_codes_detail.type = nvl(discount_codes_Obj.get("type"));

        }

        //@Dev27062020
        //fulfillments
        JSONArray fulfillments_Arr = (JSONArray) Order.get("fulfillments");

        if (fulfillments_Arr.size() > 0) {

            fulfillments[] lfulfillmentsArray = new fulfillments[fulfillments_Arr.size()];

            for (int ldx = 0; ldx < fulfillments_Arr.size(); ldx++) {

                ///Read Object 
                JSONObject fulfillments_Obj = (JSONObject) fulfillments_Arr.get(ldx);
                fulfillments lfulfillments = new fulfillments();

                lfulfillments.created_at = nvl(fulfillments_Obj.get("created_at"));
                lfulfillments.id = nvl(fulfillments_Obj.get("id"));
                lfulfillments.location_id = nvl(fulfillments_Obj.get("location_id"));
                lfulfillments.order_id = nvl(fulfillments_Obj.get("order_id"));
                lfulfillments.service = nvl(fulfillments_Obj.get("service"));
                lfulfillments.shipment_status = nvl(fulfillments_Obj.get("shipment_status"));
                lfulfillments.status = nvl(fulfillments_Obj.get("status"));
                lfulfillments.tracking_company = nvl(fulfillments_Obj.get("tracking_company"));
                lfulfillments.tracking_url = nvl(fulfillments_Obj.get("tracking_url"));
                lfulfillments.tracking_number = nvl(fulfillments_Obj.get("tracking_number"));
                lfulfillments.updated_at = nvl(fulfillments_Obj.get("updated_at"));
                lfulfillmentsArray[ldx] = lfulfillments;

            }
            l_orderDetails.Orderfulfillments = lfulfillmentsArray;
        }

        ///total_line_items_price_set
        JSONObject total_line_items_price_set_obj = (JSONObject) Order.get("total_line_items_price_set");

        //Shop Money
        JSONObject total_line_items_price_set_shop_obj = (JSONObject) total_line_items_price_set_obj.get("shop_money");
        l_orderDetails.total_line_items_price_set.shop_money.amount = nvl(total_line_items_price_set_shop_obj.get("amount"));
        l_orderDetails.total_line_items_price_set.shop_money.currency_code = nvl(total_line_items_price_set_shop_obj.get("currency_code"));

        ///presentment_money
        JSONObject total_line_items_price_set_presentment_obj = (JSONObject) total_line_items_price_set_obj.get("presentment_money");
        l_orderDetails.total_line_items_price_set.presentment_money.amount = nvl(total_line_items_price_set_presentment_obj.get("amount"));
        l_orderDetails.total_line_items_price_set.presentment_money.currency_code = nvl(total_line_items_price_set_presentment_obj.get("currency_code"));

        ///total_discounts_set
        JSONObject total_discounts_set_obj = (JSONObject) Order.get("total_discounts_set");

        //Shop Money
        JSONObject total_discounts_set_shop_obj = (JSONObject) total_discounts_set_obj.get("shop_money");
        l_orderDetails.total_discounts_set.shop_money.amount = nvl(total_discounts_set_shop_obj.get("amount"));
        l_orderDetails.total_discounts_set.shop_money.currency_code = nvl(total_discounts_set_shop_obj.get("currency_code"));

        ///presentment_money
        JSONObject total_discounts_set_presentment_obj = (JSONObject) total_discounts_set_obj.get("presentment_money");
        l_orderDetails.total_discounts_set.presentment_money.amount = nvl(total_discounts_set_presentment_obj.get("amount"));
        l_orderDetails.total_discounts_set.presentment_money.currency_code = nvl(total_discounts_set_presentment_obj.get("currency_code"));

        ///total_shipping_price_set
        JSONObject total_shipping_price_set_obj = (JSONObject) Order.get("total_shipping_price_set");

        //Shop Money
        JSONObject total_shipping_price_set_shop_obj = (JSONObject) total_shipping_price_set_obj.get("shop_money");
        l_orderDetails.total_shipping_price_set.shop_money.amount = nvl(total_shipping_price_set_shop_obj.get("amount"));
        l_orderDetails.total_shipping_price_set.shop_money.currency_code = nvl(total_shipping_price_set_shop_obj.get("currency_code"));

        ///presentment_money
        JSONObject total_shipping_price_set_presentment_obj = (JSONObject) total_shipping_price_set_obj.get("presentment_money");
        l_orderDetails.total_shipping_price_set.presentment_money.amount = nvl(total_shipping_price_set_presentment_obj.get("amount"));
        l_orderDetails.total_shipping_price_set.presentment_money.currency_code = nvl(total_shipping_price_set_presentment_obj.get("currency_code"));

        ///subtotal_price_set
        JSONObject subtotal_price_set_obj = (JSONObject) Order.get("subtotal_price_set");

        //Shop Money
        JSONObject subtotal_price_set_shop_obj = (JSONObject) subtotal_price_set_obj.get("shop_money");
        l_orderDetails.subtotal_price_set.shop_money.amount = nvl(subtotal_price_set_shop_obj.get("amount"));
        l_orderDetails.subtotal_price_set.shop_money.currency_code = nvl(subtotal_price_set_shop_obj.get("currency_code"));

        ///presentment_money
        JSONObject subtotal_price_set_presentment_obj = (JSONObject) subtotal_price_set_obj.get("presentment_money");
        l_orderDetails.subtotal_price_set.presentment_money.amount = nvl(subtotal_price_set_presentment_obj.get("amount"));
        l_orderDetails.subtotal_price_set.presentment_money.currency_code = nvl(subtotal_price_set_presentment_obj.get("currency_code"));

        ///total_price_set
        JSONObject total_price_set_obj = (JSONObject) Order.get("total_price_set");

        //Shop Money
        JSONObject total_price_set_shop_obj = (JSONObject) total_price_set_obj.get("shop_money");
        l_orderDetails.total_price_set.shop_money.amount = nvl(total_price_set_shop_obj.get("amount"));
        l_orderDetails.total_price_set.shop_money.currency_code = nvl(total_price_set_shop_obj.get("currency_code"));

        ///presentment_money
        JSONObject total_price_set_presentment_obj = (JSONObject) total_price_set_obj.get("presentment_money");
        l_orderDetails.total_price_set.presentment_money.amount = nvl(total_price_set_presentment_obj.get("amount"));
        l_orderDetails.total_price_set.presentment_money.currency_code = nvl(total_price_set_presentment_obj.get("currency_code"));

        ///total_tax_set
        JSONObject total_tax_set_obj = (JSONObject) Order.get("total_tax_set");

        //Shop Money
        JSONObject total_tax_set_shop_obj = (JSONObject) total_tax_set_obj.get("shop_money");
        l_orderDetails.total_tax_set.shop_money.amount = nvl(total_tax_set_shop_obj.get("amount"));
        l_orderDetails.total_tax_set.shop_money.currency_code = nvl(total_tax_set_shop_obj.get("currency_code"));

        ///presentment_money
        JSONObject total_tax_set_presentment_obj = (JSONObject) total_tax_set_obj.get("presentment_money");
        l_orderDetails.total_tax_set.presentment_money.amount = nvl(total_tax_set_presentment_obj.get("amount"));
        l_orderDetails.total_tax_set.presentment_money.currency_code = nvl(total_tax_set_presentment_obj.get("currency_code"));

        ///line_items   get JSON Line
        JSONArray line_items_arr = (JSONArray) Order.get("line_items");
        line_items line_items;

        // If Line Exists 
        if (line_items_arr.size() > 0) {
            l_orderDetails.isLinesExists = true;
            //Initialization of Lines in Header 
            l_orderDetails.initializationLinesItem(line_items_arr.size());
            ///Read JSON Lines and Load 
            for (int ldx = 0; ldx < line_items_arr.size(); ldx++) {
                /// New line 
                line_items = new line_items();

                //Read Line Object from Array
                JSONObject line_items_Obj = (JSONObject) line_items_arr.get(ldx);

                //Load in Local Variables 
                line_items.id = nvl(line_items_Obj.get("id"));

                line_items.variant_id = nvl(line_items_Obj.get("variant_id"));
                line_items.title = nvl(line_items_Obj.get("title"));
                line_items.quantity = nvl(line_items_Obj.get("quantity"));
                line_items.sku = nvl(line_items_Obj.get("sku"));
                line_items.variant_title = nvl(line_items_Obj.get("variant_title"));
                line_items.vendor = nvl(line_items_Obj.get("vendor"));
                line_items.fulfillment_service = nvl(line_items_Obj.get("fulfillment_service"));
                line_items.product_id = nvl(line_items_Obj.get("product_id"));
                line_items.requires_shipping = nvl(line_items_Obj.get("requires_shipping"));
                line_items.taxable = nvl(line_items_Obj.get("taxable"));
                line_items.gift_card = nvl(line_items_Obj.get("gift_card"));
                line_items.name = nvl(line_items_Obj.get("name"));

                line_items.variant_inventory_management = nvl(line_items_Obj.get("variant_inventory_management"));
                line_items.fulfillable_quantity = nvl(line_items_Obj.get("fulfillable_quantity"));
                line_items.grams = nvl(line_items_Obj.get("grams"));
                line_items.price = nvl(line_items_Obj.get("price"));
                line_items.total_discount = nvl(line_items_Obj.get("total_discount"));
                line_items.fulfillment_status = nvl(line_items_Obj.get("fulfillment_status"));
                line_items.pre_tax_price = nvl(line_items_Obj.get("pre_tax_price"));

                //price_set
                JSONObject price_set_Obj = (JSONObject) line_items_Obj.get("price_set");

                ///Shop
                JSONObject price_set_shop = (JSONObject) price_set_Obj.get("shop_money");
                line_items.price_set.shop_money.amount = nvl(price_set_shop.get("amount"));
                line_items.price_set.shop_money.currency_code = nvl(price_set_shop.get("currency_code"));

                ///presentment_mone
                JSONObject price_set_presentment = (JSONObject) price_set_Obj.get("presentment_money");
                line_items.price_set.presentment_money.amount = nvl(price_set_presentment.get("amount"));
                line_items.price_set.presentment_money.currency_code = nvl(price_set_presentment.get("currency_code"));

                //pre_tax_price_set
                JSONObject pre_tax_price_set_Obj = (JSONObject) line_items_Obj.get("pre_tax_price_set");
                if (pre_tax_price_set_Obj != null) {
                    ///Shop
                    JSONObject pre_tax_price_set_shop = (JSONObject) pre_tax_price_set_Obj.get("shop_money");
                    line_items.pre_tax_price_set.shop_money.amount = nvl(pre_tax_price_set_shop.get("amount"));
                    line_items.pre_tax_price_set.shop_money.currency_code = nvl(pre_tax_price_set_shop.get("currency_code"));

                    ///presentment_money
                    JSONObject pre_tax_price_set_presentment = (JSONObject) pre_tax_price_set_Obj.get("presentment_money");
                    line_items.pre_tax_price_set.presentment_money.amount = nvl(pre_tax_price_set_presentment.get("amount"));
                    line_items.pre_tax_price_set.presentment_money.currency_code = nvl(pre_tax_price_set_presentment.get("currency_code"));
                }

                //total_discount_set
                JSONObject total_discount_set_Obj = (JSONObject) line_items_Obj.get("total_discount_set");

                ///Shop
                JSONObject total_discount_set_shop = (JSONObject) total_discount_set_Obj.get("shop_money");
                line_items.total_discount_set.shop_money.amount = nvl(total_discount_set_shop.get("amount"));
                line_items.total_discount_set.shop_money.currency_code = nvl(total_discount_set_shop.get("currency_code"));

                ///presentment_money
                JSONObject total_discount_set_presentment = (JSONObject) total_discount_set_Obj.get("presentment_money");
                line_items.total_discount_set.presentment_money.amount = nvl(total_discount_set_presentment.get("amount"));
                line_items.total_discount_set.presentment_money.currency_code = nvl(total_discount_set_presentment.get("currency_code"));

                ///discount_allocations
                JSONArray discount_allocations_arr = (JSONArray) line_items_Obj.get("discount_allocations");
                if (discount_allocations_arr.size() > 0) {
                    JSONObject discount_allocations_Obj = (JSONObject) discount_allocations_arr.get(0);
                    line_items.discount_allocations.amount = nvl(discount_allocations_Obj.get("discount_allocations_Obj"));
                    line_items.discount_allocations.discount_application_index = nvl(discount_allocations_Obj.get("discount_application_index"));

                    //amount_set
                    JSONObject amount_set_Obj = (JSONObject) discount_allocations_Obj.get("amount_set");

                    ///Shop
                    JSONObject amount_set_shop = (JSONObject) amount_set_Obj.get("shop_money");
                    line_items.discount_allocations.amount_set.shop_money.amount = nvl(amount_set_shop.get("amount"));
                    line_items.discount_allocations.amount_set.shop_money.amount = nvl(amount_set_shop.get("currency_code"));

                    ///presentment_money
                    JSONObject amount_set_presentment = (JSONObject) amount_set_Obj.get("presentment_money");
                    line_items.discount_allocations.amount_set.presentment_money.amount = nvl(amount_set_presentment.get("amount"));
                    line_items.discount_allocations.amount_set.presentment_money.currency_code = nvl(amount_set_presentment.get("currency_code"));
                }

                //tax_lines
                JSONArray tax_lines_arr = (JSONArray) line_items_Obj.get("tax_lines");
                if (tax_lines_arr.size() > 0) {

                    JSONObject tax_lines_Obj = (JSONObject) tax_lines_arr.get(0);
                    line_items.tax_lines.title = nvl(tax_lines_Obj.get("title"));
                    line_items.tax_lines.price = nvl(tax_lines_Obj.get("price"));
                    line_items.tax_lines.rate = nvl(tax_lines_Obj.get("rate"));

                    /// tax_lines   //tax_price_set_Obj
                    JSONObject tax_price_set_Obj = (JSONObject) tax_lines_Obj.get("price_set");

                    //Shop
                    JSONObject tax_price_set_Shop_Obj = (JSONObject) tax_price_set_Obj.get("shop_money");
                    line_items.tax_lines.price_set.shop_money.amount = nvl(tax_price_set_Shop_Obj.get("amount"));
                    line_items.tax_lines.price_set.shop_money.currency_code = nvl(tax_price_set_Shop_Obj.get("currency_code"));

                    //presentment
                    JSONObject tax_price_set_presentment_Obj = (JSONObject) tax_price_set_Obj.get("presentment_money");
                    line_items.tax_lines.price_set.presentment_money.amount = nvl(tax_price_set_presentment_Obj.get("amount"));
                    line_items.tax_lines.price_set.presentment_money.currency_code = nvl(tax_price_set_presentment_Obj.get("currency_code"));
                }

                ///origin_location
                JSONObject origin_location_Obj = (JSONObject) line_items_Obj.get("origin_location");
                if (origin_location_Obj != null) {
                    line_items.origin_location.id = nvl(origin_location_Obj.get("id"));
                    line_items.origin_location.country_code = nvl(origin_location_Obj.get("country_code"));
                    line_items.origin_location.province_code = nvl(origin_location_Obj.get("province_code"));
                    line_items.origin_location.name = nvl(origin_location_Obj.get("name"));
                    line_items.origin_location.address1 = nvl(origin_location_Obj.get("address1"));
                    line_items.origin_location.address2 = nvl(origin_location_Obj.get("address2"));
                    line_items.origin_location.city = nvl(origin_location_Obj.get("city"));
                    line_items.origin_location.zip = nvl(origin_location_Obj.get("zip"));
                }
                ///add new Line in Header
                l_orderDetails.line_items[ldx] = line_items;
            }
        }

        //shipping_lines
        JSONArray shipping_lines_arr = (JSONArray) Order.get("shipping_lines");

        if (shipping_lines_arr.size() > 0) {
            JSONObject shipping_lines_Obj = (JSONObject) shipping_lines_arr.get(0);

            l_orderDetails.shipping_lines.id = nvl(shipping_lines_Obj.get("id"));
            l_orderDetails.shipping_lines.title = nvl(shipping_lines_Obj.get("title"));
            l_orderDetails.shipping_lines.price = nvl(shipping_lines_Obj.get("price"));
            l_orderDetails.shipping_lines.code = nvl(shipping_lines_Obj.get("code"));
            l_orderDetails.shipping_lines.source = nvl(shipping_lines_Obj.get("source"));
            l_orderDetails.shipping_lines.phone = nvl(shipping_lines_Obj.get("phone"));
            l_orderDetails.shipping_lines.requested_fulfillment_service_id = nvl(shipping_lines_Obj.get("requested_fulfillment_service_id"));
            l_orderDetails.shipping_lines.delivery_category = nvl(shipping_lines_Obj.get("delivery_category"));
            l_orderDetails.shipping_lines.carrier_identifier = nvl(shipping_lines_Obj.get("carrier_identifier"));
            l_orderDetails.shipping_lines.discounted_price = nvl(shipping_lines_Obj.get("discounted_price"));

            //Shipping_price_set
            JSONObject Shipping_price_set_obj = (JSONObject) shipping_lines_Obj.get("price_set");
            //Shop 
            JSONObject Shipping_price_set_shop = (JSONObject) Shipping_price_set_obj.get("shop_money");
            l_orderDetails.shipping_lines.price_set.shop_money.amount = nvl(Shipping_price_set_shop.get("amount"));
            l_orderDetails.shipping_lines.price_set.shop_money.currency_code = nvl(Shipping_price_set_shop.get("currency_code"));
            //presentment_money
            JSONObject Shipping_price_set_presentment = (JSONObject) Shipping_price_set_obj.get("presentment_money");
            l_orderDetails.shipping_lines.price_set.presentment_money.amount = nvl(Shipping_price_set_presentment.get("amount"));
            l_orderDetails.shipping_lines.price_set.presentment_money.currency_code = nvl(Shipping_price_set_presentment.get("currency_code"));

            //Shipping_discounted_price_set_obj discounted_price_set
            JSONObject Shipping_discounted_price_set_obj = (JSONObject) shipping_lines_Obj.get("discounted_price_set");
            //Shop 
            JSONObject SShipping_discounted_price_set_shop = (JSONObject) Shipping_discounted_price_set_obj.get("shop_money");
            l_orderDetails.shipping_lines.discounted_price_set.shop_money.amount = nvl(SShipping_discounted_price_set_shop.get("amount"));
            l_orderDetails.shipping_lines.discounted_price_set.shop_money.currency_code = nvl(SShipping_discounted_price_set_shop.get("currency_code"));
            //presentment_money
            JSONObject Shipping_discounted_price_set_presentment = (JSONObject) Shipping_discounted_price_set_obj.get("presentment_money");
            l_orderDetails.shipping_lines.discounted_price_set.presentment_money.amount = nvl(Shipping_discounted_price_set_presentment.get("amount"));
            l_orderDetails.shipping_lines.discounted_price_set.presentment_money.currency_code = nvl(Shipping_discounted_price_set_presentment.get("currency_code"));

        }

        //billing_address
        JSONObject billing_address_obj = (JSONObject) Order.get("billing_address");
        if (billing_address_obj != null) {
            l_orderDetails.billing_address.first_name = nvl(billing_address_obj.get("first_name"));
            l_orderDetails.billing_address.address1 = nvl(billing_address_obj.get("address1"));
            l_orderDetails.billing_address.phone = nvl(billing_address_obj.get("phone"));
            l_orderDetails.billing_address.city = nvl(billing_address_obj.get("city"));
            l_orderDetails.billing_address.zip = nvl(billing_address_obj.get("zip"));
            l_orderDetails.billing_address.province = nvl(billing_address_obj.get("province"));
            l_orderDetails.billing_address.country = nvl(billing_address_obj.get("country"));
            l_orderDetails.billing_address.last_name = nvl(billing_address_obj.get("last_name"));
            l_orderDetails.billing_address.address2 = nvl(billing_address_obj.get("address2"));
            l_orderDetails.billing_address.company = nvl(billing_address_obj.get("company"));
            l_orderDetails.billing_address.latitude = nvl(billing_address_obj.get("latitude"));
            l_orderDetails.billing_address.longitude = nvl(billing_address_obj.get("longitude"));
            l_orderDetails.billing_address.name = nvl(billing_address_obj.get("name"));
            l_orderDetails.billing_address.country_code = nvl(billing_address_obj.get("country_code"));
            l_orderDetails.billing_address.province_code = nvl(billing_address_obj.get("province_code"));
        }

        //shipping_address
        JSONObject shipping_address_obj = (JSONObject) Order.get("shipping_address");
        if (shipping_address_obj != null) {
            l_orderDetails.shipping_address.first_name = nvl(shipping_address_obj.get("first_name"));
            l_orderDetails.shipping_address.address1 = nvl(shipping_address_obj.get("address1"));
            l_orderDetails.shipping_address.phone = nvl(shipping_address_obj.get("phone"));
            l_orderDetails.shipping_address.city = nvl(shipping_address_obj.get("city"));
            l_orderDetails.shipping_address.zip = nvl(shipping_address_obj.get("zip"));
            l_orderDetails.shipping_address.province = nvl(shipping_address_obj.get("province"));
            l_orderDetails.shipping_address.country = nvl(shipping_address_obj.get("country"));
            l_orderDetails.shipping_address.last_name = nvl(shipping_address_obj.get("last_name"));
            l_orderDetails.shipping_address.address2 = nvl(shipping_address_obj.get("address2"));
            l_orderDetails.shipping_address.company = nvl(shipping_address_obj.get("company"));
            l_orderDetails.shipping_address.latitude = nvl(shipping_address_obj.get("latitude"));
            l_orderDetails.shipping_address.longitude = nvl(shipping_address_obj.get("longitude"));
            l_orderDetails.shipping_address.name = nvl(shipping_address_obj.get("name"));
            l_orderDetails.shipping_address.country_code = nvl(shipping_address_obj.get("country_code"));
            l_orderDetails.shipping_address.province_code = nvl(shipping_address_obj.get("province_code"));
        }

        //shipping_address 
        JSONObject customer_obj = (JSONObject) Order.get("customer");
        if (customer_obj != null) {
            l_orderDetails.customer.id = nvl(customer_obj.get("id"));
            l_orderDetails.customer.email = nvl(customer_obj.get("email"));
            l_orderDetails.customer.accepts_marketing = nvl(customer_obj.get("accepts_marketing"));
            l_orderDetails.customer.created_at = nvl(customer_obj.get("created_at"));
            l_orderDetails.customer.updated_at = nvl(customer_obj.get("updated_at"));
            l_orderDetails.customer.first_name = nvl(customer_obj.get("first_name"));
            l_orderDetails.customer.last_name = nvl(customer_obj.get("last_name"));
            l_orderDetails.customer.orders_count = nvl(customer_obj.get("orders_count"));
            l_orderDetails.customer.state = nvl(customer_obj.get("state"));
            l_orderDetails.customer.total_spent = nvl(customer_obj.get("total_spent"));
            l_orderDetails.customer.last_order_id = nvl(customer_obj.get("last_order_id"));
            l_orderDetails.customer.note = nvl(customer_obj.get("note"));
            l_orderDetails.customer.verified_email = nvl(customer_obj.get("verified_email"));
            l_orderDetails.customer.multipass_identifier = nvl(customer_obj.get("multipass_identifier"));
            l_orderDetails.customer.tax_exempt = nvl(customer_obj.get("tax_exempt"));
            l_orderDetails.customer.phone = nvl(customer_obj.get("phone"));
            l_orderDetails.customer.tags = nvl(customer_obj.get("tags"));
            l_orderDetails.customer.last_order_name = nvl(customer_obj.get("last_order_name"));
            l_orderDetails.customer.currency = nvl(customer_obj.get("currency"));

            //Console.show("Fetch Process Start 0001 012 + ");
            ///default_address
            JSONObject customer_default_address_obj = (JSONObject) customer_obj.get("default_address");
            if (customer_default_address_obj != null) {
                l_orderDetails.customer.default_address.id = nvl(customer_default_address_obj.get("id"));
                l_orderDetails.customer.default_address.customer_id = nvl(customer_default_address_obj.get("customer_id"));
                l_orderDetails.customer.default_address.first_name = nvl(customer_default_address_obj.get("first_name"));
                l_orderDetails.customer.default_address.last_name = nvl(customer_default_address_obj.get("last_name"));
                l_orderDetails.customer.default_address.company = nvl(customer_default_address_obj.get("company"));
                l_orderDetails.customer.default_address.address1 = nvl(customer_default_address_obj.get("address1"));
                l_orderDetails.customer.default_address.address2 = nvl(customer_default_address_obj.get("address2"));
                l_orderDetails.customer.default_address.city = nvl(customer_default_address_obj.get("city"));
                l_orderDetails.customer.default_address.province = nvl(customer_default_address_obj.get("province"));
                l_orderDetails.customer.default_address.country = nvl(customer_default_address_obj.get("country"));
                l_orderDetails.customer.default_address.zip = nvl(customer_default_address_obj.get("zip"));
                l_orderDetails.customer.default_address.phone = nvl(customer_default_address_obj.get("phone"));
                l_orderDetails.customer.default_address.name = nvl(customer_default_address_obj.get("name"));
                l_orderDetails.customer.default_address.province_code = nvl(customer_default_address_obj.get("province_code"));
                l_orderDetails.customer.default_address.country_code = nvl(customer_default_address_obj.get("country_code"));
                l_orderDetails.customer.default_address.country_name = nvl(customer_default_address_obj.get("country_name"));
            }
        }

        ///Return 
        return l_orderDetails;
    }

    private void insertOrderHeader(orderDetails l_orderDetails, boolean isFulfilmentOrders) {
        String phase = new String("Start");

        CallableStatement stmt = null;
        try {

            phase = "Insert Data into Header";

            //Insert Headers
            stmt = db.getDBConnection().prepareCall("begin\n"
                    + "  -- Call the procedure\n"
                    + "  app_utl.post_order_header(p_order_id => :p_order_id,\n"
                    + "                            p_email => :p_email,\n"
                    + "                            p_closed_at => :p_closed_at,\n"
                    + "                            p_created_at => :p_created_at,\n"
                    + "                            p_updated_at => :p_updated_at,\n"
                    + "                            p_order_number => :p_order_number,\n"
                    + "                            p_note => :p_note,\n"
                    + "                            p_total_price => :p_total_price,\n"
                    + "                            p_subtotal_price => :p_subtotal_price,\n"
                    + "                            p_total_weight => :p_total_weight,\n"
                    + "                            p_total_tax => :p_total_tax,\n"
                    + "                            p_taxes_included => :p_taxes_included,\n"
                    + "                            p_currency => :p_currency,\n"
                    + "                            p_financial_status => :p_financial_status,\n"
                    + "                            p_confirmed => :p_confirmed,\n"
                    + "                            p_total_discounts => :p_total_discounts,\n"
                    + "                            p_total_line_items_price => :p_total_line_items_price,\n"
                    + "                            p_order_name => :p_order_name,\n"
                    + "                            p_cancelled_at => :p_cancelled_at,\n"
                    + "                            p_cancel_reason => :p_cancel_reason,\n"
                    + "                            p_total_price_usd => :p_total_price_usd,\n"
                    + "                            p_phone => :p_phone,\n"
                    + "                            p_discount_code => :p_discount_code,\n"
                    + "                            p_discount_amount => :p_discount_amount,\n"
                    + "                            p_discount_type => :p_discount_type,\n"
                    + "                            p_total_shipping_price => :p_total_shipping_price,\n"
                    + "                            p_tags     => :p_tags,\n"
                    + "                            P_tracking_number => :P_tracking_number,\n"
                    + "                            p_tracking_url => :p_tracking_url,\n"
                    + "                            p_fulfilment_id => :p_fulfilment_id,\n"
                    + "                            p_organization_id => 299"
                    + ");\n"
                    + "end;");

            stmt.setString(1, l_orderDetails.OrderId);
            stmt.setString(2, l_orderDetails.email);
            stmt.setString(3, l_orderDetails.closed_at);
            stmt.setString(4, l_orderDetails.created_at);
            stmt.setString(5, l_orderDetails.updated_at);
            stmt.setString(6, l_orderDetails.order_number);
            stmt.setString(7, l_orderDetails.note);
            stmt.setString(8, l_orderDetails.total_price);
            stmt.setString(9, l_orderDetails.subtotal_price);
            stmt.setString(10, l_orderDetails.total_weight);
            stmt.setString(11, l_orderDetails.total_tax);
            stmt.setString(12, l_orderDetails.taxes_included);
            stmt.setString(13, l_orderDetails.currency);
            stmt.setString(14, l_orderDetails.financial_status);
            stmt.setString(15, l_orderDetails.confirmed);
            stmt.setString(16, l_orderDetails.total_discounts);
            stmt.setString(17, l_orderDetails.total_line_items_price);
            stmt.setString(18, l_orderDetails.name);
            stmt.setString(19, l_orderDetails.cancelled_at);
            stmt.setString(20, l_orderDetails.cancel_reason);
            stmt.setString(21, l_orderDetails.total_price_usd);
            stmt.setString(22, l_orderDetails.phone);
            stmt.setString(23, nvl(l_orderDetails.discount_codes_detail.code));
            stmt.setString(24, nvl(l_orderDetails.discount_codes_detail.amount));
            stmt.setString(25, nvl(l_orderDetails.discount_codes_detail.type));
            stmt.setString(26, nvl(l_orderDetails.total_shipping_price_set.shop_money.amount));
            stmt.setString(27, nvl(l_orderDetails.tags));
            stmt.setString(28, nvl(null));
            stmt.setString(29, nvl(null));
            stmt.setString(30, nvl(null));
            stmt.execute();

            stmt.close();

            phase = "Insert Data into Transactions";
            //Console.write("Number of Transactions: " + l_orderDetails.transactions.length);
            if (l_orderDetails.transactions != null) {
                for (int ldx = 0; ldx < l_orderDetails.transactions.length; ldx++) {
                    ///get Lines array
                    transactions ltransactions = (transactions) l_orderDetails.transactions[ldx];

                    stmt = db.getDBConnection().prepareCall("begin\n"
                            + "  -- Call the procedure\n"
                            + "  app_utl.post_order_transactions(p_id => :p_id,\n"
                            + "                                  p_order_id => :p_order_id,\n"
                            + "                                  p_kind => :p_kind,\n"
                            + "                                  p_gateway => :p_gateway,\n"
                            + "                                  p_status => :p_status,\n"
                            + "                                  p_message => :p_message,\n"
                            + "                                  p_processed_at => :p_processed_at,\n"
                            + "                                  p_error_code => :p_error_code,\n"
                            + "                                  p_source_name => :p_source_name,\n"
                            + "                                  p_amount => :p_amount,\n"
                            + "                                  p_currency => :p_currency,\n"
                            + "                                  p_organization_id => 299,\n"
                            + "                                  P_authorization => :P_authorization);\n"
                            + "end;");
                    stmt.setString(1, nvl(ltransactions.id));
                    stmt.setString(2, nvl(ltransactions.order_id));
                    stmt.setString(3, nvl(ltransactions.kind));
                    stmt.setString(4, nvl(ltransactions.gateway));
                    stmt.setString(5, nvl(ltransactions.status));
                    stmt.setString(6, nvl(ltransactions.message));
                    stmt.setString(7, nvl(ltransactions.processed_at));
                    stmt.setString(8, nvl(ltransactions.error_code));
                    stmt.setString(9, nvl(ltransactions.source_name));
                    stmt.setString(10, nvl(ltransactions.amount));
                    stmt.setString(11, nvl(ltransactions.currency));
                    stmt.setString(12, nvl(ltransactions.authorization));

                    stmt.execute();
                    stmt.close();

                }
            }

            phase = "Insert Data into Fulfillment";
            Console.show("Insert Data into Fulfillment");
            if (l_orderDetails.Orderfulfillments != null) {
                for (int ldx = 0; ldx < l_orderDetails.Orderfulfillments.length; ldx++) {
                    Console.show("Insert Data into Fulfillment : " + l_orderDetails.Orderfulfillments.length);
                    ///get Lines array
                    fulfillments lfulfillments = (fulfillments) l_orderDetails.Orderfulfillments[ldx];

                    stmt = db.getDBConnection().prepareCall("begin\n"
                            + "  app_utl.post_fulfilment_lines(p_order_id => :p_order_id,\n"
                            + "                                p_company => :p_company,\n"
                            + "                                p_tracking_number => :p_tracking_number,\n"
                            + "                                p_tracking_url => :p_tracking_url,\n"
                            + "                                p_fulfillment_id => :p_fulfillment_id,\n"
                            + "                                p_status => :p_status,\n"
                            + "                                p_organization_id => :p_organization_id,\n"
                            + "                                p_create_at => :p_create_at,\n"
                            + "                                p_update_at => :p_update_at);\n"
                            + "end;");
                    stmt.setString(1, nvl(lfulfillments.order_id));
                    stmt.setString(2, nvl(lfulfillments.tracking_company));
                    stmt.setString(3, nvl(lfulfillments.tracking_number));
                    stmt.setString(4, nvl(lfulfillments.tracking_url));
                    stmt.setString(5, nvl(lfulfillments.id));
                    stmt.setString(6, nvl(lfulfillments.status));
                    stmt.setString(7, nvl(299));
                    stmt.setString(8, nvl(lfulfillments.created_at));
                    stmt.setString(9, nvl(lfulfillments.updated_at));
                    stmt.execute();
                    stmt.close();

                    Console.show("execute ");
                }
            }

            if (isFulfilmentOrders) {
                return;
            }

            phase = "Insert Data into Lines";
            for (int ldx = 0; ldx < l_orderDetails.line_items.length; ldx++) {
                ///get Lines array
                line_items l_line_items = (line_items) l_orderDetails.line_items[ldx];
                //Insert Lines
                stmt = db.getDBConnection().prepareCall("begin\n"
                        + "  -- Call the procedure\n"
                        + "  App_UTL.post_Order_lines(P_Order_id => :P_Order_id,\n"
                        + "                           p_Line_id => :p_Line_id,\n"
                        + "                           p_variant_id => :p_variant_id,\n"
                        + "                           p_Product_title => :p_Product_title,\n"
                        + "                           p_quantity => :p_quantity,\n"
                        + "                           p_sku => :p_sku,\n"
                        + "                           p_variant_title => :p_variant_title,\n"
                        + "                           p_vendor => :p_vendor,\n"
                        + "                           p_fulfillment_service => :p_fulfillment_service,\n"
                        + "                           p_product_id => :p_product_id,\n"
                        + "                           p_taxable => :p_taxable,\n"
                        + "                           p_gift_card => :p_gift_card,\n"
                        + "                           p_Product_name => :p_Product_name,\n"
                        + "                           p_variant_inventory_management => :p_variant_inventory_management,\n"
                        + "                           p_fulfillable_quantity => :p_fulfillable_quantity,\n"
                        + "                           p_grams => :p_grams,\n"
                        + "                           p_price => :p_price,\n"
                        + "                           p_total_discount => :p_total_discount,\n"
                        + "                           p_fulfillment_status => :p_fulfillment_status,\n"
                        + "                           p_tax_title => :p_tax_title,\n"
                        + "                           p_tax_price => :p_tax_price,\n"
                        + "                           p_tax_rate => :p_tax_rate,\n"
                        + "                           P_line_discount => :P_line_discount,\n"
                        + "                           p_pre_tax_price=> :p_pre_tax_price,\n"
                        + "                           p_organization_id => 299);\n"
                        + "end;");

                stmt.setString(1, nvl(l_orderDetails.OrderId));
                stmt.setString(2, nvl(l_line_items.id));
                stmt.setString(3, nvl(l_line_items.variant_id));
                stmt.setString(4, nvl(l_line_items.title));
                stmt.setString(5, nvl(l_line_items.quantity));
                stmt.setString(6, nvl(l_line_items.sku));
                stmt.setString(7, nvl(l_line_items.variant_title));
                stmt.setString(8, nvl(l_line_items.vendor));
                stmt.setString(9, nvl(l_line_items.fulfillment_service));
                stmt.setString(10, nvl(l_line_items.product_id));
                stmt.setString(11, nvl(l_line_items.taxable));
                stmt.setString(12, nvl(l_line_items.gift_card));
                stmt.setString(13, nvl(l_line_items.name));
                stmt.setString(14, nvl(l_line_items.variant_inventory_management));
                stmt.setString(15, nvl(l_line_items.fulfillable_quantity));
                stmt.setString(16, nvl(l_line_items.grams));
                stmt.setString(17, nvl(l_line_items.price));
                stmt.setString(18, nvl(l_line_items.total_discount));
                stmt.setString(19, nvl(l_line_items.fulfillment_status));
                stmt.setString(20, nvl(l_line_items.tax_lines.title));
                stmt.setString(21, nvl(l_line_items.tax_lines.price));
                stmt.setString(22, nvl(l_line_items.tax_lines.rate));
                stmt.setString(23, nvl(l_line_items.discount_allocations.amount));
                stmt.setString(24, nvl(l_line_items.pre_tax_price));

                stmt.execute();
                stmt.close();
            }

            if (l_orderDetails.shipping_address != null) {
                phase = "Insert Data into post_shipment_lines";

                //Insert Lines
               stmt = db.getDBConnection().prepareCall("begin\n"
                        + "  -- Call the procedure\n"
                        + "  App_UTL.post_shipment_lines(P_Order_id => :P_Order_id,\n"
                        + "                              P_first_name => :P_first_name,\n"
                        + "                              P_address1 => :P_address1,\n"
                        + "                              P_phone => :P_phone,\n"
                        + "                              P_city => :P_city,\n"
                        + "                              P_zip => :P_zip,\n"
                        + "                              P_province => :P_province,\n"
                        + "                              P_country => :P_country,\n"
                        + "                              P_last_name => :P_last_name,\n"
                        + "                              P_address2 => :P_address2,\n"
                        + "                              P_company => :P_company,\n"
                        + "                              P_full_name => :P_full_name,\n"
                        + "                              P_country_code => :P_country_code,\n"
                        + "                              P_province_code => :P_province_code,\n"
                        + "                              p_organization_id => 299,\n"
                        + "                              P_CUSTOMER_ID => :P_CUSTOMER_ID"
                        + ");\n"
                        + "end;");

                stmt.setString(1, nvl(l_orderDetails.OrderId));
                stmt.setString(2, nvl(l_orderDetails.shipping_address.first_name));
                stmt.setString(3, nvl(l_orderDetails.shipping_address.address1));
                stmt.setString(4, nvl(l_orderDetails.shipping_address.phone));
                stmt.setString(5, nvl(l_orderDetails.shipping_address.city));
                stmt.setString(6, nvl(l_orderDetails.shipping_address.zip));
                stmt.setString(7, nvl(l_orderDetails.shipping_address.province));
                stmt.setString(8, nvl(l_orderDetails.shipping_address.country));
                stmt.setString(9, nvl(l_orderDetails.shipping_address.last_name));
                stmt.setString(10, nvl(l_orderDetails.shipping_address.address2));
                stmt.setString(11, nvl(l_orderDetails.shipping_address.company));
                stmt.setString(12, nvl(l_orderDetails.shipping_address.name));
                stmt.setString(13, nvl(l_orderDetails.shipping_address.country_code));
                stmt.setString(14, nvl(l_orderDetails.shipping_address.province_code));
                stmt.setString(15, nvl(l_orderDetails.customer.id));
                stmt.execute();
                stmt.close();   
 
                phase = "Insert Data into post_Customer";

                stmt = db.getDBConnection().prepareCall("begin\n"
                        + "  app_utl.post_customer(p_customer_id => :p_customer_id,\n"
                        + "                        p_email => :p_email,\n"
                        + "                        p_accepts_marketing => :p_accepts_marketing,\n"
                        + "                        p_created_at => :p_created_at,\n"
                        + "                        p_updated_at => :p_updated_at,\n"
                        + "                        p_first_name => :p_first_name,\n"
                        + "                        p_last_name => :p_last_name,\n"
                        + "                        p_orders_count => :p_orders_count,\n"
                        + "                        p_state => :p_state,\n"
                        + "                        p_total_spent => :p_total_spent,\n"
                        + "                        p_last_order_id => :p_last_order_id,\n"
                        + "                        p_note => :p_note,\n"
                        + "                        p_verified_email => :p_verified_email,\n"
                        + "                        p_multipass_identifier => :p_multipass_identifier,\n"
                        + "                        p_tax_exempt => :p_tax_exempt,\n"
                        + "                        p_phone => :p_phone,\n"
                        + "                        p_tags => :p_tags,\n"
                        + "                        p_last_order_name => :p_last_order_name,\n"
                        + "                        p_currency => :p_currency,\n"
                        + "                        p_accepts_marketing_updated_at => :p_accepts_marketing_updated_at,\n"
                        + "                        p_marketing_opt_in_level => :p_marketing_opt_in_level,\n"
                        + "                        p_tax_exemptions => :p_tax_exemptions,\n"
                        + "                        p_admin_graphql_api_id => :p_admin_graphql_api_id,\n"
                        + "                        p_df_id => :p_df_id,\n"
                        + "                        p_df_first_name => :p_df_first_name,\n"
                        + "                        p_df_last_name => :p_df_last_name,\n"
                        + "                        p_df_company => :p_df_company,\n"
                        + "                        p_df_address1 => :p_df_address1,\n"
                        + "                        p_df_address2 => :p_df_address2,\n"
                        + "                        p_df_city => :p_df_city,\n"
                        + "                        p_df_province => :p_df_province,\n"
                        + "                        p_df_country => :p_df_country,\n"
                        + "                        p_df_zip => :p_df_zip,\n"
                        + "                        p_df_phone => :p_df_phone,\n"
                        + "                        p_df_name => :p_df_name,\n"
                        + "                        p_df_province_code => :p_df_province_code,\n"
                        + "                        p_df_country_code => :p_df_country_code,\n"
                        + "                        p_df_country_name => :p_df_country_name,\n"
                        + "                        p_df_default_ => :p_df_default_,\n"
                        + "                        p_organization_id => :p_organization_id);\n"
                        + "end;");
                stmt.setString(1, nvl(l_orderDetails.customer.id));
                stmt.setString(2, nvl(l_orderDetails.customer.email));
                stmt.setString(3, nvl(l_orderDetails.customer.accepts_marketing));
                stmt.setString(4, nvl(l_orderDetails.customer.created_at));
                stmt.setString(5, nvl(l_orderDetails.customer.updated_at));
                stmt.setString(6, nvl(l_orderDetails.customer.first_name));
                stmt.setString(7, nvl(l_orderDetails.customer.last_name));
                stmt.setString(8, nvl(l_orderDetails.customer.orders_count));
                stmt.setString(9, nvl(l_orderDetails.customer.state));
                stmt.setString(10, nvl(l_orderDetails.customer.total_spent));
                stmt.setString(11, nvl(l_orderDetails.customer.last_order_id));
                stmt.setString(12, nvl(l_orderDetails.customer.note));
                stmt.setString(13, nvl(l_orderDetails.customer.verified_email));
                stmt.setString(14, nvl(l_orderDetails.customer.multipass_identifier));
                stmt.setString(15, nvl(l_orderDetails.customer.tax_exempt));
                stmt.setString(16, nvl(l_orderDetails.customer.phone));
                stmt.setString(17, nvl(l_orderDetails.customer.tags));
                stmt.setString(18, nvl(l_orderDetails.customer.last_order_name));
                stmt.setString(19, nvl(l_orderDetails.customer.currency));
                stmt.setString(20, nvl(l_orderDetails.customer.accepts_marketing_updated_at));
                stmt.setString(21, nvl(l_orderDetails.customer.marketing_opt_in_level));
                stmt.setString(22, nvl(l_orderDetails.customer.tax_exemptions));
                stmt.setString(23, nvl(l_orderDetails.customer.admin_graphql_api_id));
                stmt.setString(24, nvl(l_orderDetails.customer.default_address.id));
                stmt.setString(25, nvl(l_orderDetails.customer.default_address.first_name));
                stmt.setString(26, nvl(l_orderDetails.customer.default_address.last_name));
                stmt.setString(27, nvl(l_orderDetails.customer.default_address.company));
                stmt.setString(28, nvl(l_orderDetails.customer.default_address.address1));
                stmt.setString(29, nvl(l_orderDetails.customer.default_address.address2));
                stmt.setString(30, nvl(l_orderDetails.customer.default_address.city));
                stmt.setString(31, nvl(l_orderDetails.customer.default_address.province));
                stmt.setString(32, nvl(l_orderDetails.customer.default_address.country));
                stmt.setString(33, nvl(l_orderDetails.customer.default_address.zip));
                stmt.setString(34, nvl(l_orderDetails.customer.default_address.phone));
                stmt.setString(35, nvl(l_orderDetails.customer.default_address.name));
                stmt.setString(36, nvl(l_orderDetails.customer.default_address.province_code));
                stmt.setString(37, nvl(l_orderDetails.customer.default_address.country_code));
                stmt.setString(38, nvl(l_orderDetails.customer.default_address.country_name));
                stmt.setString(39, nvl(l_orderDetails.customer.default_address.default_));
                stmt.setString(40, "299");
                stmt.execute();
                stmt.close();
            }

        } catch (SQLException | NullPointerException e) {
            Console.write("Error while perform Transaction against Order ID " + l_orderDetails.OrderId + " Error:" + e.getMessage() + " Phase:" + phase);
             
            try {
                stmt.close();
            } catch (SQLException ex) {
            }
        }

    }

    private int getRowCount(ResultSet resultSet) {
        if (resultSet == null) {
            return 0;
        }
        try {
            resultSet.last();
            return resultSet.getRow();
        } catch (SQLException exp) {
           // exp.printStackTrace();
        } finally {
            try {
                resultSet.beforeFirst();
            } catch (SQLException exp) {
             //   exp.printStackTrace();
            }
        }

        return 0;
    }

    private String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    ///make Fullfilments
    public void CreateSingleOrderFullfilment(int storeID) {
        try {
            Statement Dstmt = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet Drs = Dstmt.executeQuery("select f.COMPANY_NAME,f.ORDER_ID,f.CN_DETAILS_ID,f.CN_NUMBER,f.TRACKING_URLS, f.LOCATION_ID,f.UPDATED_AT,f.STORE_ID,f.FULFILLMENT_TYPE,f.CREATED_AT from SRL_single_fulfilment_V f WHERE f.STORE_ID = " + storeID);
            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 
                Console.show("Row ID " + D + " = " + this.getDate());
                Console.show("Fullfilment  Start for Order ID :  " + Drs.getString("ORDER_ID") + " / " + this.getDate());

                String errorMessage = null;
                try {
                    String JsonString = "{\r\n  \"fulfillment\": {\r\n\t\"location_id\": " + Drs.getString("location_id") + ",\r\n\t\"tracking_company\": \"" + Drs.getString("COMPANY_NAME") + "\",\r\n    \"tracking_number\": \"" + Drs.getString("CN_NUMBER") + "\",\r\n    \"tracking_url\": \"" + Drs.getString("TRACKING_URLS") + "\",\r\n    \"updated_at\": \"" + Drs.getString("updated_at") + "\"\r\n  }\r\n}\r\n";
                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, JsonString);
                    Console.write(Global.ShopifyAppURL + "/admin/api/2020-07/orders/" + Drs.getString("ORDER_ID") + "/fulfillments.json");
                    Request request = new Request.Builder()
                            .url(Global.ShopifyAppURL + "/admin/api/2020-07/orders/" + Drs.getString("ORDER_ID") + "/fulfillments.json")
                            .method("POST", body)
                            .addHeader("content-type", "application/json")
                            .addHeader("Authorization", this.shopifyAuthorizationKey)
                            .build();

                    //    Console.write("2 - Build Completed");
                    //Console.write("JSON "+JsonString);
                    Response response = client.newCall(request).execute();

                    //      Console.write("3 - response Executed");
                    errorMessage = response.body().string();

                    Console.show("Fullfilment  Response " + Drs.getString("ORDER_ID") + " / " + this.getDate());
                    //      Console.write("4 - Response Body String");
                    //Console.write("response "+ errorMessage);

                    //add fullfilment information
                    CallableStatement dCall = db.getDBConnection().prepareCall("begin   App_UTL.post_fullfilment(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_STATUS => :P_STATUS); end;");
                    dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                    dCall.setString(2, errorMessage);
                    dCall.execute();
                    dCall.close();

                    Console.show("Fullfilment  post_fullfilment  / " + this.getDate());
                    this.FetchSingleOrder(Drs.getString("ORDER_ID"), true);

                    Console.show("After FullfilmentFetchSingleOrder  / " + this.getDate());

                } catch (Exception ex) {
                    Console.write("response " + ex.getMessage());
                }

            }
            Dstmt.close();
        } catch (SQLException ex) {
            Console.write("Fullfilment error " + ex.getMessage());
        }
    }

    ///make Fullfilments
    public void CreateMixOrderFullfilment(int storeID) throws SQLException {

        Statement Dstmt = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet Drs = Dstmt.executeQuery("select m.COMPANY_NAME,m.ORDER_ID,m.CN_DETAILS_ID,m.CN_NUMBER,m.TRACKING_URLS,m.LOCATION_ID,m.UPDATED_AT,m.STORE_ID,m.FULFILLMENT_TYPE,m.CREATED_AT,m.order_lines_count from SRL_MIX_FULFILMENT_V m where m.STORE_ID = " + storeID);
        for (int D = 1; D <= getRowCount(Drs); D++) {
            Drs.absolute(D); //MOVE TO ROW 

            try {
                Console.show("Fullfilment  Start for Order ID :  " + Drs.getString("ORDER_ID"));
                String errorMessage = null;
                errorMessage = new String(
                        this.partialFullfillmet(Drs.getString("ORDER_ID"),
                                Drs.getString("COMPANY_NAME"),
                                Drs.getString("CN_NUMBER"),
                                Drs.getString("TRACKING_URLS"),
                                Drs.getInt("order_lines_count"),
                                299,
                                Drs.getInt("STORE_ID")
                        ));

                /*/add fullfilment information*/
                CallableStatement dCall = db.getDBConnection().prepareCall("begin   App_UTL.post_fullfilment(p_CN_DETAILS_ID => :p_CN_DETAILS_ID,P_STATUS => :P_STATUS); end;");
                dCall.setString(1, Drs.getString("CN_DETAILS_ID"));
                dCall.setString(2, errorMessage);
                dCall.execute();
                dCall.close();

                this.FetchSingleOrder(Drs.getString("ORDER_ID"), true);

            } catch (SQLException ex) {
                Console.write("response " + ex.getMessage());
            }
        }
        Drs.close();
        Dstmt.close();
    }

    public String partialFullfillmet(String orderId, String trackingCompany, String trackingNumber, String trackingURL, int totalLines, int orgID, int storeID) {
        String jResponse = null;
        String orderFulfillmentID = null;
        String fulfillmentLineItemID = null;
        String sKU = null;
        CallableStatement stmt = null;
        Object document;
        HashMap< String, Integer> skuMap = new HashMap<>();
        JSONObject object;
        ArrayList< SKURow> skuRow = new ArrayList< SKURow>(); //orderLinesRow
        SKURow row = null;
        String fulfillmentID = null;
        String fulfillmentStatus = null;
        String lineItem = null;
        int qty;

        try {
            String status;

            jResponse = graphQlMutation("{\"query\":\"{\\r\\norder(id:\\\"gid://shopify/Order/" + orderId + "\\\") {\\r\\n    fulfillmentOrders (first:2) {\\r\\n      edges {\\r\\n        node {\\r\\n          id\\r\\n          status\\r\\n       \\r\\n        }\\r\\n      }\\r\\n    }\\r\\n  }\\r\\n}\\r\\n\\r\\n\",\"variables\":{}}");

            document = Configuration.defaultConfiguration().jsonProvider().parse(jResponse);
            Console.show("First Response " + " ORDER ID IS " + orderId + " " + jResponse);

            status = JsonPath.read(document, "$.data.order.fulfillmentOrders.edges[0].node.status");

            if (status.equalsIgnoreCase("CLOSED")) {
                orderFulfillmentID = getDigitsOnly(JsonPath.read(document, "$.data.order.fulfillmentOrders.edges[1].node.id"));
            } else {
                orderFulfillmentID = getDigitsOnly(JsonPath.read(document, "$.data.order.fulfillmentOrders.edges[0].node.id"));
            }

            Console.show("First Response " + orderFulfillmentID);

            // System.out.println("orderFulfillmentID= "+orderFulfillmentID);
            jResponse = graphQlMutation("{\"query\":\"query {\\r\\n  fulfillmentOrder(id:\\\"gid://shopify/FulfillmentOrder/" + orderFulfillmentID + "\\\") {\\r\\n    lineItems (first:"+Integer.toString(totalLines)+") {\\r\\n      edges {\\r\\n        node {\\r\\n          id\\r\\n          \\tlineItem {\\r\\n                  name\\r\\n                  sku\\r\\n                  vendor\\r\\n                }\\r\\n          remainingQuantity\\r\\n          totalQuantity\\r\\n        }\\r\\n      }\\r\\n    }\\r\\n  }\\r\\n}\",\"variables\":{}}");
            document = Configuration.defaultConfiguration().jsonProvider().parse(jResponse);

            Console.show("Second  Response " + jResponse + " " + totalLines);

            for (int ldx = 0; ldx < totalLines; ldx++) {

                Console.show("Inside Loop  " + ldx);

                sKU = JsonPath.read(document, "$.data.fulfillmentOrder.lineItems.edges[" + ldx + "].node.lineItem.sku");
                /* if(sKU.equalsIgnoreCase("3PC21WNTV326")){
            continue;
            }*/
                Console.show("Inside Loop  " + sKU);

                lineItem = JsonPath.read(document, "$.data.fulfillmentOrder.lineItems.edges[" + ldx + "].node.lineItem.vendor");
                fulfillmentLineItemID = getDigitsOnly(JsonPath.read(document, "$.data.fulfillmentOrder.lineItems.edges[" + ldx + "].node.id"));
                qty = JsonPath.read(document, "$.data.fulfillmentOrder.lineItems.edges[" + ldx + "].node.totalQuantity");

                if (storeID == 2 && lineItem.trim().equalsIgnoreCase("Unstitched")) {

                    row = new SKURow(fulfillmentLineItemID, sKU, qty);
                    skuRow.add(row);

                } else if (storeID == 1 && (!lineItem.trim().equalsIgnoreCase("Unstitched"))) {
                    // System.out.println(sKU + " yes i am in loop");
                    row = new SKURow(fulfillmentLineItemID, sKU, qty);
                    skuRow.add(row);

                }
            }

            System.out.println("size is " + skuRow.size());
            object = new JSONObject();
            JSONObject fulfillment = new JSONObject();
            fulfillment.put("notifyCustomer", true);
            JSONObject trackingInfo = new JSONObject();
            trackingInfo.put("company", trackingCompany);
            trackingInfo.put("number", trackingNumber);
            trackingInfo.put("url", trackingURL);
            fulfillment.put("trackingInfo", trackingInfo);

            JSONObject lineItemObject = new JSONObject();
            lineItemObject.put("fulfillmentOrderId", "gid://shopify/FulfillmentOrder/" + orderFulfillmentID);
            org.json.JSONArray fulfillmentOrderLineItems = new org.json.JSONArray();

            JSONObject[] obj = new JSONObject[skuRow.size()];
            int count = 0;
            for (SKURow ldx : skuRow) {

                obj[count] = new JSONObject();
                obj[count].put("id", "gid://shopify/FulfillmentOrderLineItem/" + ldx.lineItemID);
                obj[count].put("quantity", ldx.qty);
                fulfillmentOrderLineItems.put(obj[count]);
                count++;
            }

            // for (JSONObject ldx: obj) {}
            lineItemObject.put("fulfillmentOrderLineItems", fulfillmentOrderLineItems);
            // lineItemsByFulfillmentOrder.put(lineItemObject);

            fulfillment.put("lineItemsByFulfillmentOrder", lineItemObject);

            object.put("fulfillment", fulfillment);

            System.out.println(object);
            // String query = "{\"query\":\"mutation fulfillmentCreateV2($fulfillment: FulfillmentV2Input!) {\\r\\n  fulfillmentCreateV2(fulfillment: $fulfillment) {\\r\\n    fulfillment {\\r\\n      id\\r\\n      status\\r\\n      trackingInfo {\\r\\n        company\\r\\n        number\\r\\n        url\\r\\n      }\\r\\n    }\\r\\n    userErrors {\\r\\n      field\\r\\n      message\\r\\n    }\\r\\n  }\\r\\n}\"";

            JSONObject createfullFillment = new JSONObject();
            createfullFillment.put("query", "mutation fulfillmentCreateV2($fulfillment: FulfillmentV2Input!) {\n"
                    + "  fulfillmentCreateV2(fulfillment: $fulfillment) {\n"
                    + "    fulfillment {\n"
                    + "      id\n"
                    + "      status\n"
                    + "      trackingInfo {\n"
                    + "        company\n"
                    + "        number\n"
                    + "        url\n"
                    + "      }\n"
                    + "    }\n"
                    + "    userErrors {\n"
                    + "      field\n"
                    + "      message\n"
                    + "    }\n"
                    + "  }\n"
                    + "}");
            createfullFillment.put("variables", object);

            jResponse = graphQlMutation(createfullFillment.toString());

            document = Configuration.defaultConfiguration().jsonProvider().parse(jResponse);
            System.out.println("objjj  " + object);
            System.out.println("errrrrrrrrrrrrrrrrr  " + jResponse);
            fulfillmentID = getDigitsOnly(JsonPath.read(document, "$.data.fulfillmentCreateV2.fulfillment.id"));
            fulfillmentStatus = JsonPath.read(document, "$.data.fulfillmentCreateV2.fulfillment.status");
            System.out.println("Fulfillment created");
            System.out.println("fulfillmentID = " + fulfillmentID);
            System.out.println("fulfillmentStatus = " + fulfillmentStatus);
            return jResponse;

        } catch (Exception ex) {
            Console.write("Partial Fullfilment error " + ex.getMessage() + " Error Json: " + jResponse);
           // ex.printStackTrace();
            return "Partial Fullfilment error " + ex.getMessage() + " Error Json: " + jResponse;
        }

    }

    public String graphQlMutation(String GraphQuery) {
        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                    .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                    .readTimeout(5, TimeUnit.MINUTES) // read timeout
                    .retryOnConnectionFailure(true)
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, GraphQuery);
            Request request = new Request.Builder()
                    .url(Global.ShopifyAppURL + "/admin/api/2021-07/graphql.json")
                    .method("POST", body)
                    .addHeader("Authorization", this.shopifyAuthorizationKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();

        } catch (Exception ex) {
            Console.write("Partial Fullfilment Mutation Error " + ex.getMessage());
          //  ex.printStackTrace();
            return null;
        }

    }

    public static String getDigitsOnly(CharSequence input) {
        if (input == null) {
            return null;
        }
        if (input.length() == 0) {
            return "";
        }

        char[] result = new char[input.length()];
        int cursor = 0;
        CharBuffer buffer = CharBuffer.wrap(input);

        while (buffer.hasRemaining()) {
            char chr = buffer.get();
            if (chr > 47 && chr < 58) {
                result[cursor++] = chr;
            }
        }

        return new String(result, 0, cursor);
    }
}

final class SKURow {

    public String lineItemID;
    public String SKU;
    public int qty;

    public SKURow(String lineItemID, String SKU, int qty) {
        this.lineItemID = lineItemID;
        this.SKU = SKU;
        this.qty = qty;
    }

}

///Additional Classes
class orderDetails {

    protected void initializationLinesItem(int p) {
        line_items = new line_items[p];
    }

    protected void initializationTransactions(int p) {
        transactions = new transactions[p];
    }

    String OrderId;
    String email;
    String closed_at;
    String created_at;
    String updated_at;
    String number;
    String note;
    String token;
    String gateway;
    String test;
    String total_price;
    String subtotal_price;
    String total_weight;
    String total_tax;
    String taxes_included;
    String currency;
    String financial_status;
    String confirmed;
    String total_discounts;
    String total_line_items_price;
    String cart_token;
    String buyer_accepts_marketing;
    String name;
    String referring_site;
    String landing_site;
    String cancelled_at;
    String cancel_reason;
    String total_price_usd;
    String checkout_token;
    String reference;
    String user_id;
    String location_id;
    String source_identifier;
    String source_url;
    String processed_at;
    String device_id;
    String phone;
    String customer_locale;
    String app_id;
    String browser_ip;
    String landing_site_ref;
    String order_number;
    String processing_method;
    String checkout_id;
    String source_name;
    String fulfillment_status;
    String tax_lines;
    String tags;
    String contact_email;
    String order_status_url;
    String presentment_currency;
    fulfillments[] Orderfulfillments;

    /// 1) Array
    boolean is_discount_applications;
    discount_applications discount_applications_detail = new discount_applications();

    /// 2) Array
    boolean is_discount_codes;
    discount_codes discount_codes_detail = new discount_codes();

    ///Data Objects 
    amountCurrencyType total_line_items_price_set = new amountCurrencyType();
    amountCurrencyType total_discounts_set = new amountCurrencyType();
    amountCurrencyType total_shipping_price_set = new amountCurrencyType();
    amountCurrencyType subtotal_price_set = new amountCurrencyType();
    amountCurrencyType total_price_set = new amountCurrencyType();
    amountCurrencyType total_tax_set = new amountCurrencyType();

    //Lines 
    boolean isLinesExists;
    line_items[] line_items;
    shipping_lines shipping_lines = new shipping_lines();
    billing_address billing_address = new billing_address();
    shipping_address shipping_address = new shipping_address();
    customer customer = new customer();

    //transactions
    transactions[] transactions;

}

class discount_applications {

    String type;
    String value;
    String value_type;
    String allocation_method;
    String target_selection;
    String target_type;
    String code;
}

class discount_codes {

    String code;
    String amount;
    String type;
}

class amountCurrencyType {

    amount_Currency shop_money = new amount_Currency();
    amount_Currency presentment_money = new amount_Currency();
}

class amount_Currency {

    String amount;
    String currency_code;
}

class discount_allocations {

    amountCurrencyType amount_set = new amountCurrencyType();
    String amount;
    String discount_application_index;
}

class tax_lines {

    amountCurrencyType price_set = new amountCurrencyType();
    String title;
    String price;
    String rate;
}

class origin_location {

    String id;
    String country_code;
    String province_code;
    String name;
    String address1;
    String address2;
    String city;
    String zip;

}

class line_items {

    String id;
    String variant_id;
    String title;
    String quantity;
    String sku;
    String variant_title;
    String vendor;
    String fulfillment_service;
    String product_id;
    String requires_shipping;
    String taxable;
    String gift_card;
    String name;
    String variant_inventory_management;
    String fulfillable_quantity;
    String grams;
    String price;
    String total_discount;
    String fulfillment_status;
    String pre_tax_price;
    amountCurrencyType price_set = new amountCurrencyType();
    amountCurrencyType pre_tax_price_set = new amountCurrencyType();
    amountCurrencyType total_discount_set = new amountCurrencyType();
    discount_allocations discount_allocations = new discount_allocations();
    tax_lines tax_lines = new tax_lines();
    origin_location origin_location = new origin_location();
}

class shipping_lines {

    String id;
    String title;
    String price;
    String code;
    String source;
    String phone;
    String requested_fulfillment_service_id;
    String delivery_category;
    String carrier_identifier;
    String discounted_price;
    amountCurrencyType price_set = new amountCurrencyType();
    amountCurrencyType discounted_price_set = new amountCurrencyType();
}

class billing_address {

    String first_name;
    String address1;
    String phone;
    String city;
    String zip;
    String province;
    String country;
    String last_name;
    String address2;
    String company;
    String latitude;
    String longitude;
    String name;
    String country_code;
    String province_code;
}

class shipping_address {

    String first_name;
    String address1;
    String phone;
    String city;
    String zip;
    String province;
    String country;
    String last_name;
    String address2;
    String company;
    String latitude;
    String longitude;
    String name;
    String country_code;
    String province_code;
}

class default_address {

    String id;
    String customer_id;
    String first_name;
    String last_name;
    String company;
    String address1;
    String address2;
    String city;
    String province;
    String country;
    String zip;
    String phone;
    String name;
    String province_code;
    String country_code;
    String country_name;
    String default_;
}

class customer {

    String id;
    String email;
    String accepts_marketing;
    String created_at;
    String updated_at;
    String first_name;
    String last_name;
    String orders_count;
    String state;
    String total_spent;
    String last_order_id;
    String note;
    String verified_email;
    String multipass_identifier;
    String tax_exempt;
    String phone;
    String tags;
    String last_order_name;
    String currency;
    String accepts_marketing_updated_at;
    String marketing_opt_in_level;
    String tax_exemptions;
    String admin_graphql_api_id;
    default_address default_address = new default_address();
}

///
class fulfillments {

    String id;
    String order_id;
    String status;
    String created_at;
    String service;
    String updated_at;
    String tracking_company;
    String shipment_status;
    String location_id;
    String tracking_number;
    String tracking_url;
}

///
class transactions {

    String id;
    String order_id;
    String kind;
    String gateway;
    String status;
    String message;
    String processed_at;
    String error_code;
    String source_name;
    String amount;
    String currency;
    String authorization;
}
