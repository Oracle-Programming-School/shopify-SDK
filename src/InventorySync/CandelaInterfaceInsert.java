/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package InventorySync;

import static InventorySync.FetchShopifyInventory.JParser;
import Shopify.DB.DB;
import Shopify.Log.Console;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 *
 * @author abdul.ahad1
 */
public class CandelaInterfaceInsert {

        public CandelaInterfaceInsert(DB CDB) {
                db = CDB;
        }
         
          DB db = null;
        
          
        public String SQL_INSERT = "INSERT INTO candelastml.dbo.srl_shopify_inventory_details" +
                " (sku_id, sku_name, sku_diplay_name, price, available_quantity, inventory_item_id," +
                " transaction_date, last_updated_quantity ) " + "VALUES (?,?,?,?,?,?,?,?)";
        
      
      
    
        public void batchcall(String query) throws IOException, InterruptedException {
                /* call_type should be either 0 or 1
                 1 = sku will be proceed having quantity in shopify
                 0 = sku will be proceed having no any quantity in shopify */
                try {
                        int count = 0;
                         int batchSize = 100;
                        int total_count = 0;
                        int final_count = 0;
                        boolean openbatch = false;
                        //  Parser gqr = null;
                        // final long startTime = System.currentTimeMillis();
                        PreparedStatement ps = db.getCandelaConnection().prepareStatement(SQL_INSERT);
                        PreparedStatement select_st;
                        select_st = db.getCandelaConnection().prepareStatement(query);

                        ResultSet rs = select_st.executeQuery();
                        if (!rs.next()) {
                                Console.show("Nothing to proceed");
                        } else {
                                do {
                                        Parser gqr = JParser(rs.getString("variant_code"));
                                        Thread.sleep(500);
                                       
                                        ps.setString(1, gqr.skuid);
                                        ps.setString(2, gqr.sku);
                                        ps.setString(3, gqr.displayName);
                                        ps.setString(4, gqr.price);
                                        ps.setInt(5, gqr.inventoryQuantity);
                                        ps.setString(6, gqr.invlevelid);
                                        ps.setDate(7, getCurrentDate());
                                        ps.setInt(8, 0);
                                        ps.addBatch();
                                        count++;
                                        if (count % batchSize == 0) {
                                                int[] inserted = ps.executeBatch();
                                               
                                        }

                                        
                                       

                                } while (rs.next());
                                 int[] inserted = ps.executeBatch();
                        } //end master while  
                        rs.close();
                       

                        Console.show("thread close");
                } catch (SQLException ex) {
                        System.err.println("SQLException information");

                        System.err.println("Error msg: " + ex.getMessage());
                        ex = ex.getNextException();
                }

        }
     
        
     
      private static java.sql.Date getCurrentDate() {
                java.util.Date today = new java.util.Date();
                return new java.sql.Date(today.getTime());
        }
  }
   
        
