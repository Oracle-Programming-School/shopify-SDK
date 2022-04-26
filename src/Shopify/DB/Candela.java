
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.DB;

import Shopify.Log.Console;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 *
 * @author Faisal
 */
public class Candela {

    DB db = null;

    // Get DB Connection  
    public Candela(DB pdb) {
        db = pdb;
    }

    //nvl
    private String nvl(Object v) {
        if (v == null) {
            return "null";
        } else {
            return v.toString();
        }
    }

    public void TransferStoreDataToOracle() {
        try {
            Console.show("Start TransferStoreDataToOracle");
            ///CandelaDB COde
            Statement st = db.getCandelaConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet Drs = st.executeQuery("SELECT StoreCode,StoreName,StoreCity,StoreRegion,shop_id FROM [CANDELAstml].[dbo].[DimStore] where 1=2");

            System.err.println("before for loop : " + getRowCount(Drs) );
            /*while(Drs.next())
            {  
                System.err.println("in loop");
            }*/
            
        /*   for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 

                //Oracle Code
                CallableStatement dCall = db.getDBConnection().prepareCall("begin str_utl.post_shop(p_store_id => :p_store_id,p_store_code => :p_store_code,p_store_name => :p_store_name,p_store_city => :p_store_city,p_store_region => :p_store_region); end;");
                dCall.setString(1, Drs.getString("shop_id"));
                dCall.setString(2, Drs.getString("StoreCode"));
                dCall.setString(3, Drs.getString("StoreName"));
                dCall.setString(4, Drs.getString("StoreCity"));
                dCall.setString(5, Drs.getString("StoreRegion"));

                dCall.execute();
                dCall.close();
                //ENd Oracle Code

            }*/
            System.err.println("after for loop");
            Drs.close();
            st.close();
        } catch (Exception ex) {
            Console.write("Error in Transaction /  TransferStoreDataToOracle ");
        }
    }

    public void TransferProductDataToOracle() {
        try {
            Console.show("Start TransferProductDataToOracle");
            String l_last_max_date = null;
            String l_product_list = null;
            //Oracle Code
            CallableStatement productDateCAll = db.getDBConnection().prepareCall("begin  :result := str_utl.get_product_max_date(:2); end;");
            productDateCAll.registerOutParameter(1, Types.VARCHAR);
            productDateCAll.registerOutParameter(2, Types.VARCHAR);
            productDateCAll.execute();
            l_last_max_date = productDateCAll.getString(1);
            l_product_list = productDateCAll.getString(2);
            productDateCAll.close();
                //ENd Oracle Code

            ///CandelaDB COde
            Statement st = db.getCandelaConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            Console.show("l_last_max_date =>>> " + l_last_max_date);

            ResultSet Drs = st.executeQuery("SELECT Line_Item_Id\n"
                    + "      ,Line_Item_Code\n"
                    + "      ,Line_Item_Name\n"
                    + "      ,Categroy_Code\n"
                    + "      ,Category_Name\n"
                    + "      ,Sub_Category_Code\n"
                    + "      ,Sub_Category_Name\n"
                    + "      ,Product_Type_Code\n"
                    + "      ,Product_Type_Name\n"
                    + "      ,Product_Group_Code\n"
                    + "      ,Product_Group_Name\n"
                    + "      ,Product_Season_Code\n"
                    + "      ,Product_Season_Name\n"
                    + "      ,Status_Comments_Code\n"
                    + "      ,Status_Comments_Name\n"
                    + "      ,Product_Gender_Code\n"
                    + "      ,Product_Gender_Name\n"
                    + "      ,Print_Type_Code\n"
                    + "      ,Print_Type_Name\n"
                    + "      ,Product_Class_Code\n"
                    + "      ,Product_Class_Name\n"
                    + "      ,Volumne_Code\n"
                    + "      ,Volumne_Name\n"
                    + "      ,Product_Code\n"
                    + "      ,Product_Name\n"
                    + "      ,Variant_Id\n"
                    + "      ,Variant_Code\n"
                    + "      ,Size_Code\n"
                    + "      ,Size_Name\n"
                    + "      ,Color_Code\n"
                    + "      ,Color_Name\n"
                    + "      ,Product_Retail_Price\n"
                    + "      ,Product_Cost_Price\n"
                    + "      ,vat_type\n"
                    + "      ,vat\n"
                    + "      ,HSCode\n"
                    + "      ,ProductStatus\n"
                    + "      ,Creation_Date\n"
                    + "      ,Update_Date\n"
                    + "      ,Launch_Date\n"
                    + "  FROM CANDELAstml.dbo.vw_ProductWithAttributes"
                    + "  where Variant_Id in (" + l_product_list + ") or   ISNULL(update_date,Creation_Date) > " + l_last_max_date
            );

            Console.show("l_last_max_date =>>> " + l_last_max_date);

            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 

                Console.show("Variant Code =>>> " + Drs.getString("variant_code"));
                //Oracle Code
                CallableStatement dCall = db.getDBConnection().prepareCall("begin\n"
                        + "  -- Call the procedure\n"
                        + "  str_utl.post_product(p_line_item_id => :p_line_item_id,\n"
                        + "                       p_line_item_code => :p_line_item_code,\n"
                        + "                       p_line_item_name => :p_line_item_name,\n"
                        + "                       p_categroy_code => :p_categroy_code,\n"
                        + "                       p_category_name => :p_category_name,\n"
                        + "                       p_sub_category_code => :p_sub_category_code,\n"
                        + "                       p_sub_category_name => :p_sub_category_name,\n"
                        + "                       p_product_type_code => :p_product_type_code,\n"
                        + "                       p_product_type_name => :p_product_type_name,\n"
                        + "                       p_product_group_code => :p_product_group_code,\n"
                        + "                       p_product_group_name => :p_product_group_name,\n"
                        + "                       p_product_season_code => :p_product_season_code,\n"
                        + "                       p_product_season_name => :p_product_season_name,\n"
                        + "                       p_status_comments_code => :p_status_comments_code,\n"
                        + "                       p_status_comments_name => :p_status_comments_name,\n"
                        + "                       p_product_gender_code => :p_product_gender_code,\n"
                        + "                       p_product_gender_name => :p_product_gender_name,\n"
                        + "                       p_print_type_code => :p_print_type_code,\n"
                        + "                       p_print_type_name => :p_print_type_name,\n"
                        + "                       p_product_class_code => :p_product_class_code,\n"
                        + "                       p_product_class_name => :p_product_class_name,\n"
                        + "                       p_volumne_code => :p_volumne_code,\n"
                        + "                       p_volumne_name => :p_volumne_name,\n"
                        + "                       p_product_code => :p_product_code,\n"
                        + "                       p_product_name => :p_product_name,\n"
                        + "                       p_variant_id => :p_variant_id,\n"
                        + "                       p_variant_code => :p_variant_code,\n"
                        + "                       p_size_code => :p_size_code,\n"
                        + "                       p_size_name => :p_size_name,\n"
                        + "                       p_color_code => :p_color_code,\n"
                        + "                       p_color_name => :p_color_name,\n"
                        + "                       p_product_retail_price => :p_product_retail_price,\n"
                        + "                       p_product_cost_price => :p_product_cost_price,\n"
                        + "                       p_vat_type => :p_vat_type,\n"
                        + "                       p_vat => :p_vat,\n"
                        + "                       p_hscode => :p_hscode,\n"
                        + "                       p_productstatus => :p_productstatus,\n"
                        + "                       p_creation_date => :p_creation_date,\n"
                        + "                       p_update_date => :p_update_date,\n"
                        + "                       p_launch_date => :p_launch_date);\n"
                        + "end;");

                dCall.setString(1, Drs.getString("line_item_id"));
                dCall.setString(2, Drs.getString("line_item_code"));
                dCall.setString(3, Drs.getString("line_item_name"));
                dCall.setString(4, Drs.getString("categroy_code"));
                dCall.setString(5, Drs.getString("category_name"));
                dCall.setString(6, Drs.getString("sub_category_code"));
                dCall.setString(7, Drs.getString("sub_category_name"));
                dCall.setString(8, Drs.getString("product_type_code"));
                dCall.setString(9, Drs.getString("product_type_name"));
                dCall.setString(10, Drs.getString("product_group_code"));
                dCall.setString(11, Drs.getString("product_group_name"));
                dCall.setString(12, Drs.getString("product_season_code"));
                dCall.setString(13, Drs.getString("product_season_name"));
                dCall.setString(14, Drs.getString("status_comments_code"));
                dCall.setString(15, Drs.getString("status_comments_name"));
                dCall.setString(16, Drs.getString("product_gender_code"));
                dCall.setString(17, Drs.getString("product_gender_name"));
                dCall.setString(18, Drs.getString("print_type_code"));
                dCall.setString(19, Drs.getString("print_type_name"));
                dCall.setString(20, Drs.getString("product_class_code"));
                dCall.setString(21, Drs.getString("product_class_name"));
                dCall.setString(22, Drs.getString("volumne_code"));
                dCall.setString(23, Drs.getString("volumne_name"));
                dCall.setString(24, Drs.getString("product_code"));
                dCall.setString(25, Drs.getString("product_name"));
                dCall.setString(26, Drs.getString("variant_id"));
                dCall.setString(27, Drs.getString("variant_code"));
                dCall.setString(28, Drs.getString("size_code"));
                dCall.setString(29, Drs.getString("size_name"));
                dCall.setString(30, Drs.getString("color_code"));
                dCall.setString(31, Drs.getString("color_name"));
                dCall.setString(32, Drs.getString("product_retail_price"));
                dCall.setString(33, Drs.getString("product_cost_price"));
                dCall.setString(34, Drs.getString("vat_type"));
                dCall.setString(35, Drs.getString("vat"));
                dCall.setString(36, Drs.getString("hscode"));
                dCall.setString(37, Drs.getString("productstatus"));
                dCall.setString(38, Drs.getString("creation_date"));
                dCall.setString(39, Drs.getString("update_date"));
                dCall.setString(40, Drs.getString("launch_date"));

                dCall.execute();
                dCall.close();
                //ENd Oracle Code

            }
            Drs.close();
            st.close();
        } catch (SQLException ex) {
            Console.write("Error in Transaction /  TransferProductDataToOracle " + ex.getMessage());
        }
    }

    public void TransferStrDataToOracle() {
        try {
            Console.show("Start TransferStrDataToOracle");

            String l_last_max_date = null;

            //Oracle Code
            CallableStatement productDateCAll = db.getDBConnection().prepareCall("begin  :result := str_utl.get_str_max_number; end;");
            productDateCAll.registerOutParameter(1, Types.VARCHAR);
            productDateCAll.execute();
            l_last_max_date = productDateCAll.getString(1);
            productDateCAll.close();

            Console.show("STR Date = " + l_last_max_date);
                //ENd Oracle Code

            ///CandelaDB COde
            Statement st = db.getCandelaConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet Drs = st.executeQuery("SELECT master_str_number\n"
                    + "      ,str_number\n"
                    + "      ,dispatched_shop_ID\n"
                    + "      ,received_shop_ID\n"
                    + "      ,issued_date\n"
                    + "      ,str_qty\n"
                    + "      ,pending_dispatch_age\n"
                    + "      ,comments\n"
                    + "      ,location_type\n"
                    + "      ,dispatch_from\n"
                    + "      ,dispatch_group\n"
                    + "  FROM CANDELAstml.dbo.vw_PendingDispatch"
                    + " where   str_number >" + l_last_max_date);

            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 

                //Oracle Code  Insert STR header 
                CallableStatement dCall = db.getDBConnection().prepareCall("begin\n"
                        + "  -- Call the procedure\n"
                        + "  str_utl.post_str_header(p_master_str_number => :p_master_str_number,\n"
                        + "                          p_str_number => :p_str_number,\n"
                        + "                          p_dispatched_shop_id => :p_dispatched_shop_id,\n"
                        + "                          p_received_shop_id => :p_received_shop_id,\n"
                        + "                          p_issued_date => :p_issued_date,\n"
                        + "                          p_str_qty => :p_str_qty,\n"
                        + "                          p_comments => :p_comments,\n"
                        + "                          p_location_type => :p_location_type,\n"
                        + "                          p_dispatch_from => :p_dispatch_from,\n"
                        + "                          p_dispatch_group => :p_dispatch_group);\n"
                        + "end;");
                dCall.setString(1, Drs.getString("master_str_number"));
                dCall.setString(2, Drs.getString("str_number"));
                dCall.setString(3, Drs.getString("dispatched_shop_ID"));
                dCall.setString(4, Drs.getString("received_shop_ID"));
                dCall.setString(5, Drs.getString("issued_date"));
                dCall.setString(6, Drs.getString("str_qty"));
                dCall.setString(7, Drs.getString("comments"));
                dCall.setString(8, Drs.getString("location_type"));
                dCall.setString(9, Drs.getString("dispatch_from"));
                dCall.setString(10, Drs.getString("dispatch_group"));

                dCall.execute();
                dCall.close();
                            //ENd Oracle Code

                //Insert STR Lines 
                ///CandelaDB COde
                Statement str_lines = db.getCandelaConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet DrstrLines = str_lines.executeQuery("SELECT str_master_id,str_number,product_item_id,qty FROM CANDELAstml.dbo.vw_idb_pending_str where  qty < 0 and  str_master_id=" + Drs.getString("master_str_number") + " and str_number = " + Drs.getString("str_number"));
                for (int strline = 1; strline <= getRowCount(DrstrLines); strline++) {
                    DrstrLines.absolute(strline); //MOVE TO ROW 
                    //Oracle Code  Insert STR header 
                    CallableStatement StrLinesCall = db.getDBConnection().prepareCall("begin\n"
                            + "  -- Call the procedure\n"
                            + "  str_utl.post_str_line(P_Master_Str_Number => :P_Master_Str_Number,\n"
                            + "                        p_str_number => :p_str_number,\n"
                            + "                        p_product_item_id => :p_product_item_id,\n"
                            + "                        p_qty => :p_qty);\n"
                            + "end;");
                    StrLinesCall.setString(1, DrstrLines.getString("str_master_id"));
                    StrLinesCall.setString(2, DrstrLines.getString("str_number"));
                    StrLinesCall.setString(3, DrstrLines.getString("product_item_id"));
                    StrLinesCall.setString(4, DrstrLines.getString("qty"));
                    StrLinesCall.execute();
                    StrLinesCall.close();
                }
                DrstrLines.close();
                str_lines.close();
                //end Insert STR Lines 
            }
            Drs.close();
            st.close();
        } catch (Exception ex) {
            Console.write("Error in Transaction /  TransferStrDataToOracle " + ex.getMessage());
        }
    }

    public void TransferStrDataToOracle(String p_STR_number) {
        try {
            Console.show("Start TransferStrDataToOracle");

            //ENd Oracle Code
            ///CandelaDB COde
            Statement st = db.getCandelaConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet Drs = st.executeQuery("SELECT master_str_number\n"
                    + "      ,str_number\n"
                    + "      ,dispatched_shop_ID\n"
                    + "      ,received_shop_ID\n"
                    + "      ,issued_date\n"
                    + "      ,str_qty\n"
                    + "      ,pending_dispatch_age\n"
                    + "      ,comments\n"
                    + "      ,location_type\n"
                    + "      ,dispatch_from\n"
                    + "      ,dispatch_group\n"
                    + "  FROM CANDELAstml.dbo.vw_PendingDispatch"
                    + " where   str_number = " + p_STR_number);

            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 

                //Oracle Code  Insert STR header 
                CallableStatement dCall = db.getDBConnection().prepareCall("begin\n"
                        + "  -- Call the procedure\n"
                        + "  str_utl.post_str_header(p_master_str_number => :p_master_str_number,\n"
                        + "                          p_str_number => :p_str_number,\n"
                        + "                          p_dispatched_shop_id => :p_dispatched_shop_id,\n"
                        + "                          p_received_shop_id => :p_received_shop_id,\n"
                        + "                          p_issued_date => :p_issued_date,\n"
                        + "                          p_str_qty => :p_str_qty,\n"
                        + "                          p_comments => :p_comments,\n"
                        + "                          p_location_type => :p_location_type,\n"
                        + "                          p_dispatch_from => :p_dispatch_from,\n"
                        + "                          p_dispatch_group => :p_dispatch_group);\n"
                        + "end;");
                dCall.setString(1, Drs.getString("master_str_number"));
                dCall.setString(2, Drs.getString("str_number"));
                dCall.setString(3, Drs.getString("dispatched_shop_ID"));
                dCall.setString(4, Drs.getString("received_shop_ID"));
                dCall.setString(5, Drs.getString("issued_date"));
                dCall.setString(6, Drs.getString("str_qty"));
                dCall.setString(7, Drs.getString("comments"));
                dCall.setString(8, Drs.getString("location_type"));
                dCall.setString(9, Drs.getString("dispatch_from"));
                dCall.setString(10, Drs.getString("dispatch_group"));

                dCall.execute();
                dCall.close();
                            //ENd Oracle Code

                //Insert STR Lines 
                ///CandelaDB COde
                Statement str_lines = db.getCandelaConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet DrstrLines = str_lines.executeQuery("SELECT str_master_id,str_number,product_item_id,qty FROM CANDELAstml.dbo.vw_idb_pending_str where  qty < 0 and  str_master_id=" + Drs.getString("master_str_number") + " and str_number = " + Drs.getString("str_number"));
                for (int strline = 1; strline <= getRowCount(DrstrLines); strline++) {
                    DrstrLines.absolute(strline); //MOVE TO ROW 
                    //Oracle Code  Insert STR header 
                    CallableStatement StrLinesCall = db.getDBConnection().prepareCall("begin\n"
                            + "  -- Call the procedure\n"
                            + "  str_utl.post_str_line(P_Master_Str_Number => :P_Master_Str_Number,\n"
                            + "                        p_str_number => :p_str_number,\n"
                            + "                        p_product_item_id => :p_product_item_id,\n"
                            + "                        p_qty => :p_qty);\n"
                            + "end;");
                    StrLinesCall.setString(1, DrstrLines.getString("str_master_id"));
                    StrLinesCall.setString(2, DrstrLines.getString("str_number"));
                    StrLinesCall.setString(3, DrstrLines.getString("product_item_id"));
                    StrLinesCall.setString(4, DrstrLines.getString("qty"));
                    StrLinesCall.execute();
                    StrLinesCall.close();
                }
                DrstrLines.close();
                str_lines.close();
                //end Insert STR Lines 
            }
            Drs.close();
            st.close();
        } catch (Exception ex) {
            Console.write("Error in Transaction /  TransferStrDataToOracle " + ex.getMessage());
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

    ///Candela Sale Fetch 
    public void FetchSaleDataToOracle() {

        try {

            ///CandelaDB COde
            Statement st = db.getCandelaConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            ResultSet Drs = st.executeQuery("select ProductCode,StoreCode,SyncTimeStamp,TimeStamp,ReceiptCode,Unitsold,GrossValueSold,Tax,COGS,NetValueSold,Discount,ModeofPayment,TransactionType,MobileNumber,Email,ReturnReason ,cast(T.TimeStamp as date ) SaleDate \n" +
"from Cnfz_SalesFactTable t \n" 
              + "where cast(T.TimeStamp as date ) = cast(getdate() - 1as date ) ");
            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 

                //Oracle Code
                CallableStatement dCall = db.getDBConnection().prepareCall("begin \n"
                        + "app_db_utl.post_cnfz_salesfacttable(p_productcode => :p_productcode,\n"
                        + "                                      p_storecode => :p_storecode,\n"
                        + "                                      p_transaction_date => :p_transaction_date,\n"
                        + "                                      p_receiptcode => :p_receiptcode,\n"
                        + "                                      p_unitsold => :p_unitsold,\n"
                        + "                                      p_transactiontype => :p_transactiontype,\n"
                        + "                                      p_synctimestamp => :p_synctimestamp,\n"
                        + "                                      p_vtimestamp => :p_vtimestamp,\n"
                        + "                                      p_grossvaluesold => :p_grossvaluesold,\n"
                        + "                                      p_tax => :p_tax,\n"
                        + "                                      p_cogs => :p_cogs,\n"
                        + "                                      p_netvaluesold => :p_netvaluesold,\n"
                        + "                                      p_discount => :p_discount,\n"
                        + "                                      p_modeofpayment => :p_modeofpayment,\n"
                        + "                                      p_mobilenumber => :p_mobilenumber,\n"
                        + "                                      p_email => :p_email,\n"
                        + "                                      p_returnreason => :p_returnreason); "
                        + "end;");

                dCall.setString(1, Drs.getString("productCode"));
                dCall.setString(2, Drs.getString("storeCode"));
                dCall.setString(3, Drs.getString("SaleDate"));
                dCall.setString(4, Drs.getString("ReceiptCode"));
                dCall.setString(5, Drs.getString("Unitsold"));
                dCall.setString(6, Drs.getString("TransactionType"));
                dCall.setString(7, Drs.getString("synctimestamp"));
                dCall.setString(8, Drs.getString("timestamp"));
                dCall.setString(9, Drs.getString("grossvaluesold"));
                dCall.setString(10, Drs.getString("tax"));
                dCall.setString(11, Drs.getString("cogs"));
                dCall.setString(12, Drs.getString("netvaluesold"));
                dCall.setString(13, Drs.getString("discount"));
                dCall.setString(14, Drs.getString("modeofpayment"));
                dCall.setString(15, nvl(Drs.getString("mobilenumber")));
                dCall.setString(16, nvl(Drs.getString("email")));
                dCall.setString(17, nvl(Drs.getString("returnreason")));

                dCall.execute();
                dCall.close();
                //ENd Oracle Code

            }
            Drs.close();
            st.close();
        } catch (SQLException ex) {
            Console.write("Error in Transaction /  FetchSaleDataToOracle " + ex.getMessage());
        }

    }
 
    
    public void transferSaleCandelaToOra() throws SQLException
   {    
       
       ///CandelaDB COde
            Statement st = db.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet Drs = st.executeQuery("select shop_id , saleid from Srl_tblsales_ids");
            for (int D = 1; D <= getRowCount(Drs); D++) {
                Drs.absolute(D); //MOVE TO ROW 


                String insertSaleData = "insert into srl_tblsales( order_name, sale_id, shop_id, sale_date," +
                " invoice_no, gt_amount, mark_discount,\n" +
                "nt_amount, vat, salereturningno, transactiontime, syncdate, updatedsyncdate, status) \n" +
                "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
       
       
                int batchSize = 50;
                int count = 0;
                try{
                PreparedStatement ps = db.getCandelaConnection().prepareStatement(" SELECT s.adjustment_comments  Order_name,\n" +
          "       s.sale_id, s.shop_id, s.sale_date, s.invoice_no, s.GT_amount, \n" +
          "       s.Mark_Discount, s.NT_amount, s.vat, s.SaleReturningNo, s.TransactionTime, s.SyncDate, s.UpdatedSyncDate, \n" +
          "       case when s.NT_amount > 0 then\n" +
          "        'Processed'\n" +
          "        when s.NT_amount < 0 then \n" +
          "        'Returned'\n" +
          "        end Status \n" +
          "       FROM dbo.tblSales s\n" +
          "       WHERE 1 = 1 \n" +
          "       AND s.shop_id =" +Drs.getInt("shop_id")+" and s.sale_id >"+Drs.getString("saleid"), ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                PreparedStatement psOra = db.getDBConnection().prepareStatement(insertSaleData);

                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                   Console.show("Nothing to proceed");
                   System.out.println("Nothing to proceed");
                   continue;
                } else {
                   do {
                      psOra.setString(1, rs.getString("Order_name"));
                      psOra.setInt(2, rs.getInt("sale_id"));
                      psOra.setInt(3, rs.getInt("shop_id"));
                      psOra.setDate(4, rs.getDate("sale_date"));
                      psOra.setString(5, rs.getString("invoice_no"));
                      psOra.setString(6, rs.getString("mark_discount"));
                      psOra.setString(7, rs.getString("gt_amount"));
                      psOra.setString(8, rs.getString("nt_amount"));
                      psOra.setString(9, rs.getString("vat"));
                      psOra.setString(10, rs.getString("salereturningno"));
                      psOra.setDate(11, rs.getDate("transactiontime"));
                      psOra.setDate(12, rs.getDate("syncdate"));
                      psOra.setDate(13, rs.getDate("updatedsyncdate"));
                      psOra.setString(14, rs.getString("status"));
                      psOra.addBatch();
                      if (++count % batchSize == 0) {
                         int[] inserted = psOra.executeBatch();
                      }

                   } while (rs.next());
                   int[] inserted = psOra.executeBatch();
                   psOra.close();
                }

               rs.close();
               ps.close();
                }
                catch(Exception ex){
                    Console.show("transferSaleCandelaToOra : Error "+ex.getMessage());
      }
   }
            /// Close
            st.close();
  }
    

   
}
