/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package InventorySync;

import static InventorySync.FetchShopifyInventory.JParser;
import Shopify.DB.DB;
import Shopify.Log.Console;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author abdul.ahad1
 */
public class UpdateInventoryShopify {

        public String INSERT_HEADER = "INSERT INTO [candelastml].[dbo].[srl_shopify_transaction_header]  \n" +
                "(transaction_date, transaction_id, transaction_no, created_by, created_date, entered_by, entered_date) \n" +
                "VALUES(?,?,?,?,?,?,?)";
        public String INSERT_LINES = "INSERT INTO [candelastml].[dbo].[srl_shopify_transaction_lines]  (\n" +
                "transaction_id,\n" +
                "line_id,\n" +
                "sku_name, \n" +
                "onhand_qty,\n" +
                "available_quantity,\n" +
                "price,\n" +
                "error_msg\n" +
                ")\n" +
                "VALUES(\n" +
                "?,?,?,?,?,?,?\n" +
                ")";
        public String UPDATE_DEATAILS = "UPDATE [candelastml].[dbo].[srl_shopify_inventory_details]  \n" +
                "SET available_quantity = ?, transaction_date = ?\n" +
                "WHERE sku_name = ?";

        public String Candela_VS_Shopify_A = "select \n" +
                "q1.variant_code\n" +
                "from (\n" +
                "select  v.product_code,\n" +
                "v.variant_code, \n" +
                "v.Product_Retail_Price, \n" +
                "sum(v.quantity) candela_qty\n" +
                "from candelastml.dbo.vwShopProductInventory v\n" +
                "where 1=1 --v.quantity > 0 -- in case shopify have qty \n" +
                "and v.shop_id = 13\n" +
                "group by \n" +
                "v.product_code,\n" +
                "v.variant_code \n" +
                ",v.Product_Retail_Price \n" +
                ") q1, candelastml.dbo.srl_shopify_inventory_details sid\n" +
                "where sid.sku_name = q1.variant_code\n" +
                "and sid.available_quantity != q1.candela_qty\n" +
                "and sid.inventory_item_id != '-' \n OPTION (MAXDOP 10) ";
        
     /*  public String Candela_VS_Shopify_A = "select \n" +
"                q1.variant_code\n" +
"                from (\n" +
"                select  v.product_code,\n" +
"                v.variant_code, \n" +
"                v.Product_Retail_Price, \n" +
"                sum(v.quantity) candela_qty\n" +
"                from candelastml.dbo.vwShopProductInventory v\n" +
"                where 1=1 --v.quantity > 0 -- in case shopify have qty \n" +
"                and v.shop_id = 13\n" +
"                group by \n" +
"                v.product_code,\n" +
"                v.variant_code \n" +
"                ,v.Product_Retail_Price \n" +
"                ) q1, candelastml.dbo.srl_shopify_inventory_details sid\n" +
"                where sid.sku_name = q1.variant_code\n" +
"                and sid.available_quantity != q1.candela_qty\n" +
"                and sid.inventory_item_id != '-'\n" +
"                and q1.variant_code in (select d.col1 from dbo.erp_dummy d)";*/

        public String Candela_VS_Shopify_M = "select \n" +
                "q1.variant_code\n" +
                "from (\n" +
                "select  v.product_code,\n" +
                "v.variant_code, \n" +
                "v.Product_Retail_Price, \n" +
                "sum(v.quantity) candela_qty\n" +
                "from candelastml.dbo.vwShopProductInventory v\n" +
                "where 1=1 --v.quantity > 0 -- in case shopify have qty \n" +
                "and v.shop_id = 13\n" +
                "group by \n" +
                "v.product_code,\n" +
                "v.variant_code \n" +
                ",v.Product_Retail_Price \n" +
                ") q1, candelastml.dbo.srl_shopify_inventory_details sid\n" +
                "where sid.sku_name = q1.variant_code\n" +
                "and sid.available_quantity != q1.candela_qty\n" +
                "and sid.inventory_item_id != '-'\n" +
                "and q1.variant_code = ? \n"+
                " OPTION (MAXDOP 8)"; //'000SCPDAYV03-MDM-999'

        public String Missing_SKU = "select q2.sku variant_code  from (\n" +
                "select row_number() over  (order by q1.sku) seq,q1.sku\n" +
                "from (\n" +
                "select \n" +
                "distinct v.variant_code sku,  v.product_code\n" +
                "from candelastml.dbo.vwShopProductInventory v)q1\n" +
                "where 1=1 \n" +
                "and not exists (select 1 from candelastml.dbo.srl_shopify_inventory_details sid\n" +
                "where sid.sku_name = q1.sku)\n" +
                ")q2\n OPTION (MAXDOP 10) ";
        public String SQL_INSERT = "INSERT INTO candelastml.dbo.srl_shopify_inventory_details" +
                " (sku_id, sku_name, sku_diplay_name, price, available_quantity, inventory_item_id," +
                " transaction_date, last_updated_quantity ) " + "VALUES (?,?,?,?,?,?,?,?)";
        DB db = null;
        public UpdateInventoryShopify(DB CDB) {
                db = CDB;
        }
        String msg = null;
        String sku = null;
        int candela_qty = 0;
        int shopify_qty = 0;
        String qty_to_be_adjust = null;
        String price = null;
        String inventory_item_id = null;
        int header_id = 0;
        int post_line = 0;
        int update_shopify_detail = 0;
        int missing_status = 1;
        int total_sku_to_proceed = 0;

        public void AdjustQuantity(String type, String sku_name) {

                try {
                    
                        Console.show("Start Fetching Missing SKU");
                        while (missing_status == 1) {
                                missing_status = FetchMissingSKU();
                                if (missing_status == -1) {
                                        break;
                                }
                        }
                       
                        Console.show("End of Fetching Missing SKU");
                      
                        PreparedStatement adjust = null;
                        if (type.equals("M")) {
                                adjust = db.getCandelaConnection().prepareStatement(Candela_VS_Shopify_M, ResultSet.TYPE_SCROLL_INSENSITIVE,
                                        ResultSet.CONCUR_READ_ONLY);
                                adjust.setString(1, sku_name);
                        } else if (type.equals("A")) {
                                adjust = db.getCandelaConnection().prepareStatement(Candela_VS_Shopify_A, ResultSet.TYPE_SCROLL_INSENSITIVE,
                                        ResultSet.CONCUR_READ_ONLY);

                        }

                        ResultSet rs = adjust.executeQuery();

                        if (!rs.next()) {
                                 System.out.println("Found No Any SKU For Inventory Adjustment...!");
                                 Console.show("Found No Any SKU For Inventory Adjustment...!");
                                System.exit(0);
                        }
                         Console.show("Start Invnetory Sync");
                        rs.beforeFirst();
                        rs.last();
                        total_sku_to_proceed = rs.getRow();
                        rs.beforeFirst();
                        Console.show("Total numers of SKU available for invnetory Sync are "+total_sku_to_proceed);
                        header_id = AddTransactionHDR();
                       // System.out.println(" after header id " + header_id);
                       Console.show("Please wait invnetory Sync is in process...!");
                        // Loop Start
                        while (rs.next()) {

                                //   System.out.println(" enter in while loop of rs");
                                //0000000EB012-34C-BLK
                                sku = rs.getString("variant_code");

                                Parser gqr = JParser(sku);
                                Thread.sleep(500); //half second
                                inventory_item_id = gqr.invlevelid;
                                shopify_qty = gqr.inventoryQuantity;
                                price = gqr.price;
                                candela_qty = getCandelaQty(sku);
                                qty_to_be_adjust = Integer.toString(candela_qty - shopify_qty);
                                if (candela_qty == 0 && shopify_qty < 0) {
                                        /*
                                        API will not perform any action on shopify if
                                        (Candela Quantity is Zero & Shopify is negative)
                                        */
                                        Post_Transaction("continue");

                                        continue;
                                } else if (candela_qty < 0 || (candela_qty == shopify_qty)) {
                                        /* API will not perform any action on shopify if (Candela is negative)
                                        
                                         Candela Quantity is negative and shopify is zero
                                         Candela Quantity is negative  and shopify is also negative
                                         Candela Quantity is negative and shopify is greater than 0 
                                         Candela Quantity is negative and equal to shopify
                                        
                                        Or 
                                        Candela Quantity is Zero and shopify is also zero
                                        Candela Quantity is available and equal to shopify 
                                        Candela Quantity is negative and equal to shopify
                                        */
                                        Post_Transaction("continue");

                                        continue;
                                } else if (candela_qty > 0 && candela_qty != shopify_qty) {
                                        /* Candela is available and not equal to shopify this algo will cover these
                                           points:
                                        -------------
                                         Candela Quantity is available & Shopify Zero
                                         Candela Quantity is available & Shopify is negative
                                         Candela Quantity is available and  Diff from Shopify
                                        */
                                        //proceed
                                        Post_Transaction("proceed");

                                } else if (candela_qty == 0 && shopify_qty > 0) {
                                        /*Candela Quantity is Zero and diff from shopify */
                                        //procceed
                                        Post_Transaction("proceed");

                                }

                        }

                        rs.close();
                        adjust.close();
                        System.out.println("Inventory Sync Completed...!");
                        Console.show("Inventory Sync Completed Successfully...!");
                } catch (Exception ex) {
                        ex.printStackTrace();

                }
        }
        public int AddTransactionLine(int hdr_id, String sku, int onhandqty, int available_qty, String price, String msg) {
                try {
                        PreparedStatement INSERT_LINE = db.getCandelaConnection().prepareStatement(INSERT_LINES);
                        int line_id = GetNextID("srl_shopify_transaction_lines", "line_id");
                        INSERT_LINE.setInt(1, hdr_id);
                        INSERT_LINE.setInt(2, line_id);
                        INSERT_LINE.setString(3, sku);
                        INSERT_LINE.setInt(4, onhandqty); //candela qty
                        INSERT_LINE.setInt(5, available_qty); //shopify qty
                        INSERT_LINE.setString(6, price);
                        INSERT_LINE.setString(7, msg);
                        INSERT_LINE.addBatch();
                        int[] insert = INSERT_LINE.executeBatch();
                        INSERT_LINE.close();
                        return 1;
                } catch (Exception ex) {
                        ex.printStackTrace();
                        return 0;
                }
        }

        public int AddTransactionHDR() {
                try {

                        PreparedStatement INSERT_HDR = db.getCandelaConnection().prepareStatement(INSERT_HEADER);
                        int hdr_id = GetNextID("srl_shopify_transaction_header", "transaction_id");
                        INSERT_HDR.setDate(1, getCurrentDate());
                        INSERT_HDR.setInt(2, hdr_id);
                        INSERT_HDR.setString(3, "SIA-" + String.format("%05d", hdr_id));
                        INSERT_HDR.setString(4, "SYSTEM");
                        INSERT_HDR.setDate(5, getCurrentDate());
                        INSERT_HDR.setString(6, "SYSTEM");
                        INSERT_HDR.setDate(7, getCurrentDate());
                        INSERT_HDR.addBatch();
                        int[] insert = INSERT_HDR.executeBatch();
                        INSERT_HDR.close();
                        return hdr_id;
                } catch (Exception ex) {
                        ex.printStackTrace();
                        return 0;
                }
        }
        public int missingskucount() throws SQLException{
            int row;
         PreparedStatement select_st = db.getCandelaConnection().prepareStatement(Missing_SKU,ResultSet.TYPE_SCROLL_INSENSITIVE,
                                        ResultSet.CONCUR_READ_ONLY);
                        ResultSet rs = select_st.executeQuery();
                        rs.last();
                        row = rs.getRow();
                        rs.close();
                        select_st.close();
                        return row;
        }
        
        //Review after 30-aug-2021
            public int FetchMissingSKU() {
                int batchSize = 100;
                int count = 0;
                
               
                try {    
                        PreparedStatement ps = db.getCandelaConnection().prepareStatement(SQL_INSERT);
                        PreparedStatement select_st = db.getCandelaConnection().prepareStatement(Missing_SKU,ResultSet.TYPE_SCROLL_INSENSITIVE,
                                        ResultSet.CONCUR_READ_ONLY);
                        ResultSet rs = select_st.executeQuery();
                        
                        if (!rs.next()) {
                                return 0;
                                
                                //   missing_status = 0;
                        } else {
                                do {
                                        Parser gqr = JParser(rs.getString("variant_code"));
                                        // Thread.sleep(500);

                                      //  System.out.println(count + " " + gqr.displayName + "  " + gqr.sku);
                                        ps.setString(1, gqr.skuid);
                                        ps.setString(2, rs.getString("variant_code"));
                                        ps.setString(3, gqr.displayName);
                                        ps.setString(4, gqr.price);
                                        ps.setInt(5, gqr.inventoryQuantity);
                                        ps.setString(6, gqr.invlevelid);
                                        ps.setDate(7, getCurrentDate());
                                        ps.setInt(8, 0);
                                        ps.addBatch();
                                        count++;
                                        if (count % batchSize == 0) {
                                                int[] ins = ps.executeBatch();
                                                System.out.println("batch done");
                                        }

                                } while (rs.next());
                                int[] ins = ps.executeBatch();
                                rs.close();
                               
                               
                                //  missing_status = 1;
                                return 1;
                        }

                } catch (Exception ex) {
                        ex.printStackTrace();
                        return -1;
                        //  missing_status = -1;
                }

        }
        public int UpdateShopifyDetail(String sku, int qty) {
                try {   
                        PreparedStatement DETAILS_UPDATE = db.getCandelaConnection().prepareStatement(UPDATE_DEATAILS);
                        DETAILS_UPDATE.setInt(1, qty);
                        DETAILS_UPDATE.setDate(2, getCurrentDate());
                        DETAILS_UPDATE.setString(3, sku);
                        int update_det = DETAILS_UPDATE.executeUpdate();
                        DETAILS_UPDATE.close();
                        return 1;
                } catch (Exception ex) {
                        ex.printStackTrace();
                        return 0;
                }

        }

        public void Post_Transaction(String Type) throws InterruptedException {
                msg = "No any action performed";

                if (Type.equals("proceed")) {
                        msg = Mutation.adjustquantity(inventory_item_id, qty_to_be_adjust);
                        Thread.sleep(500); //half second
                        if (msg.contains("successfully")) {
                                update_shopify_detail = UpdateShopifyDetail(sku, candela_qty);
                        }
                       
                } else if (Type.equals("continue")) {
                        update_shopify_detail = UpdateShopifyDetail(sku, candela_qty);
                }

                post_line = AddTransactionLine(header_id, sku, candela_qty, shopify_qty, price, msg);

        }

        public int getCandelaQty(String sku) {
                int qty = 0;
                try {
                        PreparedStatement select_st = db.getCandelaConnection().
                        prepareStatement("select  \n" +
                                "sum(quantity) candela_qty\n" +
                                "from [candelastml].[dbo].[vwShopProductInventory]\n" +
                                "where 1=1  \n" +
                                "and shop_id = 13 \n" +
                                "and variant_code = ? \n OPTION (MAXDOP 8)");
                        select_st.setString(1, sku);
                        ResultSet rs = select_st.executeQuery();
                        while (rs.next()) {
                                qty = rs.getInt("candela_qty");
                        }
                        rs.close();
                        select_st.close();
                        return qty;
                } catch (Exception ex) {
                        ex.printStackTrace();
                        return -777;
                }

        }
        public int GetNextID(String tbl, String col) throws SQLException {
                int id = -1;
                PreparedStatement select_st = db.getCandelaConnection().
                prepareStatement("select  ISNULL(max(t." + col + "),0) + 1 nxt_id\n" +
                        "from [candelastml].[dbo].[" + tbl + "] t \n OPTION (MAXDOP 8) ");
                ResultSet rs = select_st.executeQuery();
                while (rs.next()) {
                        id = rs.getInt("nxt_id");
                }
                rs.close();
                select_st.close();
                return id;
        }
       
        private static java.sql.Date getCurrentDate() {
                java.util.Date today = new java.util.Date();
                return new java.sql.Date(today.getTime());
        }
}