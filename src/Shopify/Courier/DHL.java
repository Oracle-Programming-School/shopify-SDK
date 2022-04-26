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
import java.io.File;
import java.io.FileOutputStream;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sun.misc.BASE64Decoder;
import FND.Email;

/**
 *
 * @author abdul.ahad1
 */
public class DHL {

    public DHL(DB pdp) {
        db = pdp;
    }

    private DB db = null;

    public void generateCN() {
        Statement st = null;
        ResultSet rs = null;

        try {
            st = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = st.executeQuery("select order_id,cn_details_id  from MF_Shopify_CN_details where ORGANIZATION_ID = 299 and STATUS is null  and CN_NUMBER is null and CN_GENERATION_ERROR is null and COMPANY_NAME = 'DHL' ");

            if (!rs.next()) {
                rs.close();
                st.close();
                Console.show("Nothing to proceed for DHL Thread...!");

            } else {
                do {

                    generateInvoice(rs.getString("order_id"), rs.getString("cn_details_id"));

                } while (rs.next());
                rs.close();
                st.close();
            }
        } catch (Exception ex) {

            Console.show("Error in DHL -> Cn-generation -> " + ex.getMessage());
            // ex.printStackTrace();
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ignore) {
                }
            }
            if (st != null) {
                try {
                    st.close();
                } catch (Exception ignore) {
                }
            }

        }
    }

    public void generateInvoice(String OrderId, String cnDetailId) {

        PreparedStatement ps;
        ResultSet rs;
        PreparedStatement psLines;
        ResultSet rsLines;
        JSONObject object = null;

        try {

            ps = db.getDBConnection().prepareStatement("select * from srl_dhl_pending_order_v t where t.order_id = ?",
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            try {
                ps.setString(1, OrderId);
                rs = ps.executeQuery();
                try {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();

                    } else {
                        do {

                            String orderId = rs.getString("ORDER_ID");

                            psLines = db.getDBConnection().prepareStatement("select rownum itemno,\n"
                                    + "       ORDER_ID,\n"
                                    + "       ITEM_DESCRIPTION,\n"
                                    + "       PER_UNIT_VALUE,\n"
                                    + "       HS_CODE,\n"
                                    + "       QTY,\n"
                                    + "       LINE_SUB_TOTAL,\n"
                                    + "       ORGANIZATION_ID,\n"
                                    + "       WEIGHT\n"
                                    + "  from srl_dhl_pending_order_lines_v l\n"
                                    + " where order_id = ?\n"
                                    + "   and ORGANIZATION_ID = 299", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                            psLines.setString(1, orderId);
                            rsLines = psLines.executeQuery();
                            //
                            String orderNumber = rs.getString("order_number");
                            String RefrenceNum = rs.getString("REFERENCE_NUM");
                            String ShipmentInvoiceDate = rs.getString("SHIPMENT_INVOICE_DATE");
                            double perUnitValue = rs.getDouble("PER_UNIT_VALUE");
                            double subTotalValue = rs.getDouble("SUB_TOTAL_VALUE");
                            double freightCost = rs.getDouble("FREIGHT_COST");
                            double totalInvoiceAmount = rs.getDouble("TOTAL_INVOICE_AMOUNT");
                            double totalGrossWeight = rs.getDouble("TOTAL_GROSS_WEIGHT");
                            double vatTax = rs.getDouble("vattaxusd");
                            String fullName = rs.getString("FULL_NAME");
                            String companyName = rs.getString("COMPANY_NAME");
                            String addressLine1 = rs.getString("ADDRESS_LINE1");
                            String addressLine2 = "null";

                            if (addressLine1.contains("|")) {
                                String[] splitAdd = addressLine1.split("\\|");
                                addressLine1 = splitAdd[0];
                                addressLine2 = splitAdd[1];
                            }

                            String addressLine3 = rs.getString("ADDRESS_LINE3");
                            String postalCode = rs.getString("POSTAL_CODE");
                            String cityName = rs.getString("CITY_NAME");
                            String email = rs.getString("email");
                            String phone = rs.getString("phone");
                            String countryCode = rs.getString("COUNTRY_CODE");
                            String currency = rs.getString("Currency");
                            String line_items = rs.getString("concated_line_items");
                            String incoTerms = rs.getString("term_of_trade");
                            double netGrossWeight = rs.getDouble("NET_GROSS_WEIGHT");
                            String COO = rs.getString("COO");
                            double totalUnits = rs.getDouble("TOTAL_UNITS");
                            int organizationID = rs.getInt("ORGANIZATION_ID");
                            String remarks = rs.getString("remarks");
                            String ukVatNumber = rs.getString("uk_vat_number");

                            object = new JSONObject();
                            object.put("productCode", "P");
                            object.put("localProductCode", "P");
                            object.put("getRateEstimates", false);
                            object.put("plannedShippingDateAndTime", ShipmentInvoiceDate + "T23:43:06 GMT+01:00");

                            JSONArray accounts = new JSONArray();
                            JSONObject accountsArrayObject = new JSONObject();
                            accountsArrayObject.put("typeCode", "shipper");
                            accountsArrayObject.put("number", "22222251");
                            accounts.put(accountsArrayObject);

                            if (countryCode.equalsIgnoreCase("CA")) {
                                JSONObject accountsArrayObject2 = new JSONObject();
                                accountsArrayObject2.put("typeCode", "duties-taxes");
                                accountsArrayObject2.put("number", "fffffff");
                                accounts.put(accountsArrayObject2);
                            }

                            object.put("accounts", accounts);
                            JSONObject pickupObject = new JSONObject();
                            pickupObject.put("isRequested", false);
                            object.put("pickup", pickupObject);

                            JSONObject outputImagePropertiesObject = new JSONObject();
                            outputImagePropertiesObject.put("encodingFormat", "pdf");
                            outputImagePropertiesObject.put("printerDPI", 200);
                            JSONArray imageOptionsArray = new JSONArray();
                            JSONObject imageOptionsArrayElement1 = new JSONObject();
                            imageOptionsArrayElement1.put("invoiceType", "commercial");
                            imageOptionsArrayElement1.put("templateName", "111111111");
                            imageOptionsArrayElement1.put("isRequested", true);
                            imageOptionsArrayElement1.put("typeCode", "invoice");
                            JSONObject imageOptionsArrayElement2 = new JSONObject();
                            imageOptionsArrayElement2.put("hideAccountNumber", false);
                            imageOptionsArrayElement2.put("fitLabelsToA4", true);
                            imageOptionsArrayElement2.put("isRequested", true);
                            imageOptionsArrayElement2.put("typeCode", "waybillDoc");
                            imageOptionsArray.put(imageOptionsArrayElement1);
                            imageOptionsArray.put(imageOptionsArrayElement2);
                            outputImagePropertiesObject.put("imageOptions", imageOptionsArray);
                            object.put("outputImageProperties", outputImagePropertiesObject);

                            JSONObject customerDetails = new JSONObject();
                            JSONObject shipperDetails = new JSONObject();
                            JSONObject postalAddress = new JSONObject();
                            postalAddress.put("cityName", "Lahore");
                            postalAddress.put("countryCode", "PK");
                            postalAddress.put("postalCode", "54000");
                            postalAddress.put("addressLine1", "1");
                            postalAddress.put("addressLine2", "Opposite University");
                            shipperDetails.put("postalAddress", postalAddress);

                            JSONObject contactInformation = new JSONObject();
                            contactInformation.put("phone", "+2222");
                            contactInformation.put("companyName", " RETAIL LIMITED");
                            contactInformation.put("fullName", " RETAIL");
                            shipperDetails.put("contactInformation", contactInformation);
                            shipperDetails.put("typeCode", "business");
                            customerDetails.put("shipperDetails", shipperDetails);

                            JSONObject receiverDetails = new JSONObject();
                            JSONObject postalAddressRcv = new JSONObject();
                            postalAddressRcv.put("cityName", cityName);
                            postalAddressRcv.put("countryCode", countryCode);
                            postalAddressRcv.put("postalCode", postalCode);
                            postalAddressRcv.put("addressLine1", addressLine1);

                            if (addressLine2.equalsIgnoreCase("null")) {
                                postalAddressRcv.put("addressLine2", addressLine3);
                            } else {
                                postalAddressRcv.put("addressLine2", addressLine2);
                                postalAddressRcv.put("addressLine3", addressLine3);
                            }
                            postalAddressRcv.put("addressLine2", addressLine2);
                            receiverDetails.put("postalAddress", postalAddressRcv);

                            JSONObject contactInformationRcv = new JSONObject();
                            contactInformationRcv.put("phone", phone);
                            contactInformationRcv.put("companyName", companyName);
                            contactInformationRcv.put("fullName", fullName);
                            contactInformationRcv.put("email", email);
                            receiverDetails.put("contactInformation", contactInformationRcv);
                            receiverDetails.put("typeCode", "private");

                            if (countryCode.equalsIgnoreCase("GB")) {
                                JSONArray vatArray = new JSONArray();
                                JSONObject vatArrayElement = new JSONObject();
                                vatArrayElement.put("typeCode", "VAT");
                                vatArrayElement.put("number", ukVatNumber);
                                vatArrayElement.put("issuerCountryCode", "PK");
                                vatArray.put(vatArrayElement);
                                receiverDetails.put("registrationNumbers", vatArray);
                            }
                            customerDetails.put("receiverDetails", receiverDetails);
                            object.put("customerDetails", customerDetails);

                            JSONObject content = new JSONObject();
                            content.put("unitOfMeasurement", "metric");
                            content.put("incoterm", incoTerms);
                            content.put("isCustomsDeclarable", true);
                            content.put("description", line_items);
                            content.put("declaredValue", totalInvoiceAmount);
                            content.put("declaredValueCurrency", currency);

                            JSONObject exportDeclaration = new JSONObject();
                            JSONArray lineItemsArray = new JSONArray();

                            while (rsLines.next()) {

                                JSONObject lineHolder = new JSONObject();
                                JSONObject weight = new JSONObject();
                                JSONObject quantity = new JSONObject();
                                JSONArray commodityCodes = new JSONArray();
                                JSONObject commodityElements = new JSONObject();
                                lineHolder.put("number", rsLines.getInt("itemno"));
                                lineHolder.put("priceCurrency", "USD");
                                lineHolder.put("price", rsLines.getInt("per_unit_value"));
                                lineHolder.put("description", rsLines.getString("item_description"));
                                lineHolder.put("isTaxesPaid", false);
                                lineHolder.put("exportReasonType", "commercial_purpose_or_sale");
                                lineHolder.put("manufacturerCountry", "PK");
                                quantity.put("unitOfMeasurement", "PCS");
                                quantity.put("value", rsLines.getInt("qty"));
                                weight.put("netValue", rsLines.getDouble("weight"));
                                weight.put("grossValue", rsLines.getDouble("weight"));
                                commodityElements.put("value", rsLines.getString("hs_code")); //HS code
                                commodityElements.put("typeCode", "inbound");
                                commodityCodes.put(commodityElements);
                                lineHolder.put("commodityCodes", commodityCodes);
                                lineHolder.put("quantity", quantity);
                                lineHolder.put("weight", weight);
                                lineItemsArray.put(lineHolder);

                            }
                            rsLines.close();
                            psLines.close();

                            exportDeclaration.put("lineItems", lineItemsArray);
                            exportDeclaration.put("exportReason", "Commercial");

                            JSONArray remarksArray = new JSONArray();
                            JSONObject remarksElements = new JSONObject();
                            remarksElements.put("value", remarks);
                            remarksArray.put(remarksElements);
                            exportDeclaration.put("remarks", remarksArray);

                            JSONObject invoiceElements = new JSONObject();
                            invoiceElements.put("date", ShipmentInvoiceDate);
                            invoiceElements.put("number", RefrenceNum);
                            invoiceElements.put("signatureName", "SHAKEEL ZAFAR");
                            exportDeclaration.put("invoice", invoiceElements);

                            JSONArray additionalChargesArray = new JSONArray();

                            if (freightCost > 0.0) {
                                JSONObject additionalChargesElements = new JSONObject();
                                additionalChargesElements.put("value", freightCost);
                                additionalChargesElements.put("caption", "Freight Cost");
                                additionalChargesElements.put("typeCode", "freight");
                                additionalChargesArray.put(additionalChargesElements);
                            }

                            if (vatTax > 0.0) {
                                JSONObject additionalChargesElements = new JSONObject();
                                additionalChargesElements.put("value", vatTax);
                                additionalChargesElements.put("caption", "VAT Fee");
                                additionalChargesElements.put("typeCode", "vat");
                                additionalChargesArray.put(additionalChargesElements);
                            }

                            exportDeclaration.put("additionalCharges", additionalChargesArray);
                            content.put("exportDeclaration", exportDeclaration);

                            JSONArray packagesArray = new JSONArray();
                            JSONObject packagesElements = new JSONObject();
                            JSONArray innerArrayCustomerReferences = new JSONArray();
                            JSONObject customerReferencesElements = new JSONObject();
                            JSONObject dimensions = new JSONObject();
                            customerReferencesElements.put("value", RefrenceNum);
                            innerArrayCustomerReferences.put(customerReferencesElements);
                            packagesElements.put("customerReferences", innerArrayCustomerReferences);
                            packagesElements.put("weight", totalGrossWeight);
                            dimensions.put("length", 18);
                            dimensions.put("width", 13);
                            dimensions.put("height", 13);
                            packagesElements.put("dimensions", dimensions);
                            packagesArray.put(packagesElements);
                            content.put("packages", packagesArray);
                            object.put("content", content);
                            System.out.println(object);

                            this.mutation(object.toString(), orderNumber, OrderId, cnDetailId);

                        } while (rs.next());

                    }
                } finally {

                    try {
                        if (rs != null) {
                            rs.close();
                        }
                    } catch (Exception ignore) {

                    }
                }
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                } catch (Exception ignore) {

                }
            }

        } catch (Exception ex) {
            Console.show("Error In Generate DHL Invoice -> " + ex.getMessage());
            ex.printStackTrace();

        }

    }

    public void mutation(String jsonString, String orderNumber, String orderID, String cnDetailId) {
        try {

            String TrackingNumber = null;
            String TrackingUrl = null;
            ResponseBody responseBodyCopy;
            CallableStatement dCall = db.getDBConnection().prepareCall("begin\n"
                    + " \n"
                    + "  srl_export_order_pkg.postcn(pOrderId => :pOrderId,\n"
                    + "                              pcndetailid => :pcndetailid,\n"
                    + "                              pstatus => :pstatus,\n"
                    + "                              pcnnumber => :pcnnumber,\n"
                    + "                              pmessgae => :pmessgae,\n"
                    + "                              purl => :purl);\n"
                    + "end;");
            try {
                String jString;

                String labelCode;
                String invoiceCode;
                String systemName = System.getProperty("user.name");
                String labelDir = "C:\\Users\\" + systemName + "\\Documents\\label.pdf";
                String InvoiceDir = "C:\\Users\\" + systemName + "\\Documents\\invoice.pdf";
                System.out.println("documents path " + "C:\\Users\\" + systemName + "\\Documents");
                BASE64Decoder decoder = new BASE64Decoder();
                BASE64Decoder decoder1 = new BASE64Decoder();

                PDFMergerUtility obj = new PDFMergerUtility();

                byte[] decodedBytes;
                byte[] decodedBytes1;
                System.out.println(jsonString);
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                        .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                        .readTimeout(5, TimeUnit.MINUTES) // read timeout
                        .retryOnConnectionFailure(true)
                        .build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, jsonString);
                Request request = new Request.Builder()
                    //      .url("https://express.api.dhl.com/mydhlapi/test/shipments")
                        .url("https://express.api.dhl.com/mydhlapi/shipments")
                        .method("POST", body)
                        .addHeader("Authorization", "Basic YXBXOXFu")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Cookie", "")
                        .addHeader("Connection", "close")
                        .build();
                Response response = client.newCall(request).execute();

                if (response.code() == 201) {

                    TrackingUrl = "https://www.logistics.dhl/us-en/home/tracking.html?tracking-id=";
                    jString = response.body().string();
                    Object document = Configuration.defaultConfiguration().jsonProvider().parse(jString);
                    TrackingNumber = JsonPath.read(document, "$.shipmentTrackingNumber");
                    TrackingUrl = JsonPath.read(document, "$.trackingUrl");
                    labelCode = JsonPath.read(document, "$.documents[0].content");
                    invoiceCode = JsonPath.read(document, "$.documents[1].content");
                    decodedBytes = decoder.decodeBuffer(labelCode);
                    decodedBytes1 = decoder1.decodeBuffer(invoiceCode);
                    System.err.println("TrackingNumber" + TrackingNumber);
                    System.err.println("TrackingUrl" + TrackingUrl);
                    System.err.println("TrackingNumber" + TrackingNumber);
                    File label = new File(labelDir);
                    File invoice = new File(InvoiceDir);
                    FileOutputStream fop = new FileOutputStream(label);
                    FileOutputStream fop1 = new FileOutputStream(invoice);
                    fop.write(decodedBytes);
                    fop.flush();
                    fop.close();
                    fop1.write(decodedBytes1);
                    fop1.flush();
                    fop1.close();
                    obj.setDestinationFileName("Z:\\" + orderNumber + ".pdf");
                  //  obj.setDestinationFileName("D:\\#123456\\" + orderNumber + ".pdf");
                    obj.addSource(labelDir);
                    obj.addSource(InvoiceDir);
                    obj.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                    label.delete();
                    invoice.delete();

                    dCall.setString(1, orderID);
                    dCall.setString(2, cnDetailId);
                    dCall.setString(3, "SUCCESS");
                    dCall.setString(4, TrackingNumber);
                    dCall.setString(5, "CN generated Successfully");
                    dCall.setString(6, TrackingUrl + TrackingNumber);
                    dCall.execute();
                    dCall.close(); 
                 
                 
                    //  this.waterMark(orderNumber);
                    Console.show("DHL invoice/CN generated: order# " + orderNumber + " cnNumber#" + TrackingNumber);
                } else {

                    responseBodyCopy = response.peekBody(Long.MAX_VALUE);
                    // responseBodyCopy.string();
                    if (isJSONValid(response.body().string()) == true) {

                        dCall.setString(1, orderID);
                        dCall.setString(2, cnDetailId);
                        dCall.setString(3, "FAIL");
                        dCall.setString(4, TrackingNumber);
                        dCall.setString(5, responseBodyCopy.string());
                        dCall.setString(6, TrackingUrl);
                        dCall.execute();
                        dCall.close();
                        Console.show("Error In Generate DHL Invoice/CN -> Mutation -> order# " + orderNumber + " -> " + "Bad requests 400");
                        //  System.out.println("dhongi baba:  "+responseBodyCopy.string());
                    }
                }
            } catch (Exception ex) {

                Console.show("Error In Generate DHL Invoice/CN -> Mutation -> order# " + orderNumber + " -> " + ex.getMessage());
                ex.printStackTrace();
                try {

                    dCall.setString(1, orderID);
                    dCall.setString(2, cnDetailId);
                    dCall.setString(3, "FAIL");
                    dCall.setString(4, TrackingNumber);
                    dCall.setString(5, ex.getMessage());
                    dCall.setString(6, TrackingUrl);
                    dCall.execute();
                    dCall.close();
                } catch (Exception ignore) {

                }

            }
        } catch (Exception ignore) {

        }
    }

    public int updateRates() {
        try {
            String fcsAPI = "https://fcsapi.com/api-v3/forex/latest?symbol=USD/PKR,GBP/PKR,EUR/PKR&access_key=5E0zOCzF";
            String layerAPI = "http://api.currencylayer.com/live?access_key=299a7fff&source=USD&currencies=PKR&format=1";
            Object document = null;
            String jResponse = null;
            boolean isvalidjson;
            String responseStatus = "Error";
            String dollarRate = null;
            String poundRate = null;
            String euroRate = null;
            CallableStatement statement = null;
            boolean isFcsApiFailed = false;

            jResponse = this.mutation(layerAPI);
            isvalidjson = isJSONValid(jResponse);

            if (isvalidjson == false) {
                isFcsApiFailed = true;

            } else {

                document = Configuration.defaultConfiguration().jsonProvider().parse(jResponse);
                responseStatus = JsonPath.read(document, "$.msg");
                if (!responseStatus.equals("Successfully")) {
                    isFcsApiFailed = true;

                } else {
                    dollarRate = JsonPath.read(document, "$.response[0].c");

                }
            }

            if (isFcsApiFailed == true) {

                jResponse = this.mutation(fcsAPI);
                isvalidjson = isJSONValid(jResponse);

                if (isvalidjson == false) {

                    return 1;
                }

                document = Configuration.defaultConfiguration().jsonProvider().parse(jResponse);
                boolean isValidResponse = JsonPath.read(document, "$.success");
                if (isValidResponse == false) {

                    return 1;
                }
                dollarRate = JsonPath.read(document, "$.quotes.USDPKR").toString();

            }

            if (dollarRate == null) {

                Email email = new Email();
                email.sendAlert("email", "Email", "Exchange Rate Alert", "Exchange rate not update for today due to API bad response, Please run DB job manually named SRL_UPDATE_EXCHANGE_RATE", false, "muhammad.faisal11521@gmail.com");

                return 1;

            }

            String query = "{call SRL_MISC_UTL.UPDATE_DHL_EXCHANGE_RATE(p_usd => ?,p_pound => ?,p_euro => ?,  x_response => ?)}";
            statement = db.getDBConnection().prepareCall(query);

            statement.setString(1, dollarRate);
            statement.setString(2, euroRate);
            statement.setString(3, poundRate);
            statement.registerOutParameter(4, java.sql.Types.VARCHAR);
            statement.executeUpdate();
            String outParameter1 = statement.getString(4);
            statement.close();

            return 0;
        } catch (Exception ex) {
            Console.show("Error In DHL -> update exchange rate " + ex.getMessage());
            return 1;
        }

    }

    public static boolean isJSONValid(String jString) {
        try {
            new JSONObject(jString);
        } catch (JSONException ex) {

            try {
                new JSONArray(jString);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public String mutation(String url) {
        String jResponse = null;
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .addHeader("Cookie", "c_reffer=direct")
                    .build();
            Response response = client.newCall(request).execute();
            jResponse = response.body().string();
            return jResponse;
        } catch (Exception ex) {
            jResponse = ex.getMessage();
            Console.show("Exchange rate mutation error --> " + jResponse);
            return jResponse;
        }

    }

    public void waterMark(String orderNumber) {
        try {
            String fileName = "C:\\Oracle\\dhl_invoices\\" + orderNumber + ".pdf";
            PDDocument realDoc = Loader.loadPDF(new File(fileName));
            HashMap< Integer, String> overlayGuide = new HashMap< Integer, String>();
            for (int i = 0; i < realDoc.getNumberOfPages(); i++) {
                overlayGuide.put(i + 1, "C:\\Oracle\\Shopify\\dhl_invoices\\duplicate\\duplicate.pdf");
            }
            Overlay overlay = new Overlay();
            overlay.setInputPDF(realDoc);
            overlay.overlay(overlayGuide).save("C:\\Oracle\\Shopify\\dhl_invoices\\duplicate\\" + orderNumber + ".pdf");
            overlay.close();
        } catch (Exception ignore) {
        }

    }
}
