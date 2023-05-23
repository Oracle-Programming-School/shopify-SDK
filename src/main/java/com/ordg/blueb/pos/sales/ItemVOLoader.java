
package com.ordg.blueb.pos.sales;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.ordg.utl.collections.Constants;
import com.ordg.utl.database.OracleConnection;

public class ItemVOLoader {

    public static List<ItemVO> loadItemsFromQuery() {
        String sql = "SELECT STYLECODE, STYLE_NAME, DESIGNCODE, DESIGN_NAME, COLORCODE, COLOR_NAME, LAPDIP, PANTONENO, SIZECODE, ITEM_SIZE, SIZE_SORT, ITEMTYPE, FABRICCODE, SKU_NAME, CONSUMPTION, COST, RETAIL_PRICE, ONLINEPRICE, RET_PRICE, WHPRICE, FABRIC_NAME, FABRIC_CATEGORY, BARCODE FROM POSC_INV_SYSTEM_ITEMS_ALL where onhand > 0 and store_id ="+Constants.get("APP_STORE_ID");

        List<ItemVO> items = new ArrayList<>();

        try (Connection connection = OracleConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
                ) 
        {
            preparedStatement.setFetchSize(10000);
            
            ResultSet resultSet = preparedStatement.executeQuery(sql);

            while (resultSet.next()) {
                ItemVO item = new ItemVO(
                        resultSet.getString("STYLECODE"),
                        resultSet.getString("STYLE_NAME"),
                        resultSet.getString("DESIGNCODE"),
                        resultSet.getString("DESIGN_NAME"),
                        resultSet.getString("COLORCODE"),
                        resultSet.getString("COLOR_NAME"),
                        resultSet.getString("LAPDIP"),
                        resultSet.getString("PANTONENO"),
                        resultSet.getString("SIZECODE"),
                        resultSet.getString("ITEM_SIZE"),
                        resultSet.getInt("SIZE_SORT"),
                        resultSet.getString("ITEMTYPE"),
                        resultSet.getString("FABRICCODE"),
                        resultSet.getString("SKU_NAME"),
                        resultSet.getDouble("CONSUMPTION"),
                        resultSet.getDouble("COST"),
                        resultSet.getDouble("RETAIL_PRICE"),
                        resultSet.getDouble("ONLINEPRICE"),
                        resultSet.getDouble("RET_PRICE"),
                        resultSet.getDouble("WHPRICE"),
                        resultSet.getString("FABRIC_NAME"),
                        resultSet.getString("FABRIC_CATEGORY"),
                        resultSet.getString("BARCODE")
                );
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

   
}

