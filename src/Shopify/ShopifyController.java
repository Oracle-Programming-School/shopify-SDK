/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author M.Faisal1521
 */
public class ShopifyController {

    // Controller 
    public ShopifyController() {
    }

    //get Row Count 
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

    /*    public void getOrderEconnectStatus() {
        try {
            Statement stmt = null;
            DB dbConnection = new DB(this.console);
            this.console.show("Start 01");

            stmt = dbConnection.getDBConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("select order_name  , order_id  \n"
                    + "from mf_shopify_order_header soh    \n"
                    + "where organization_id = 299 and econnect_status is  null ");
            this.console.show("Start");
            //getRowCount
            //for (int i = 1; i <= getRowCount(rs); i++) {
            while (rs.next()) {
                //rs.absolute(i); //MOVE TO ROW
                //int Sequence = getSQLDBSeq();
            this.console.show("Start" + rs.getString("order_name"));
                Statement SQLstatement = dbConnection.getSQLConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet Qrs = SQLstatement.executeQuery("select distinct orderid,status from [CANDELAstml].[dbo].[tblWebstoreSale] where OrderId ='" + rs.getString("order_name") + "'");

                String order_status = new String();

                if (getRowCount(Qrs) == 0) {
                    order_status = "Record Not Availble";
                } else {
                    Qrs.absolute(1);
                    order_status = Qrs.getString("status");
                }

                SQLstatement.close();

                //Complete Log Status
                Statement stmtUpdate = dbConnection.getDBConnection().createStatement();
                stmtUpdate.executeUpdate("update  mf_shopify_order_header soh set econnect_status ='" + order_status + "', econnect_status_date = sysdate where organization_id = 299 and order_id =" + rs.getString("order_id"));
                stmtUpdate.close();
            }
            stmt.close();
        } catch (SQLException ex) {
            this.console.write("Econnect Sale data error" + ex.getMessage());
        }
    }
    */
}
