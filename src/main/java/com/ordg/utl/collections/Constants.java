package com.ordg.utl.collections;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import com.ordg.utl.log.Console;
import com.ordg.utl.database.OracleConnection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class Constants {

    // Store the resulting HashMap in a constant class
    private static final HashMap<String, String> CONFIG_VALUES = new HashMap<>();
    
    public static void storeXMLValue(String key, String value) {
        CONFIG_VALUES.put(key, value);
    }
    
    public static String get(String Key)
    {
        return CONFIG_VALUES.get(Key);
    }
    
    public static void set(String Key,String value)
    {
        CONFIG_VALUES.put(Key, value);
    }
    
    /*private static HashMap<String, String> getConfigValues() {
        return CONFIG_VALUES;
    }*/

    public static void initializeConfigValues(String filePath) throws Exception {
        // Load the XML content from the file
        File file = new File(filePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(file);

        // Traverse the XML tree and extract the values
        NodeList nodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String key = node.getNodeName();
                String value = node.getTextContent();
                storeXMLValue(key, value);
            }
        }
    }
    
  /* We will use this in new Release  
    public static void WriteConfigHashMapIntoLog()
    {   
        //Print of HashMap
        HashMap<String, String> configValues = Constants.getConfigValues();
        for (String key : configValues.keySet()) {
          String value = configValues.get(key);
          Console.write(key + ": " + value);
        }
    }
*/
     public static void loadStoreConfiguration() 
    {
        try {
            HashMap<String, Object> load = getFirstRow("select STORE_ID,STORE_NAME,PARTY_ID,GST_PERCENTAGE, FBR_HOST , FBR_PORT,PCT_CODE,POS_ID "
                    + "  from pos_stores where party_id ="+Constants.get("APP_PARTY_ID"));
            Constants.set("APP_STORE_ID", (String) load.get("STORE_ID").toString());
            Constants.set("APP_STORE_NAME", (String) load.get("STORE_NAME"));
            Constants.set("APP_GST_PERCENTAGE", load.get("GST_PERCENTAGE").toString());
            Constants.set("APP_FBR_HOST", load.get("FBR_HOST").toString());
            Constants.set("APP_FBR_PORT", load.get("FBR_PORT").toString());
            Constants.set("APP_PCT_CODE", load.get("PCT_CODE").toString());
            Constants.set("APP_POS_ID", load.get("POS_ID").toString());
        }
        catch(SQLException e)
        {
            Console.showError("Store configuration issue", true, Constants.class);
        }
    }
        
     public static HashMap<String, Object> getFirstRow(String query) throws SQLException {
        Connection conn = OracleConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        HashMap<String, Object> result = new HashMap<>();

        if (rs.next()) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = rsmd.getColumnName(i).toUpperCase();
                Object value = rs.getObject(i);
                result.put(columnName, value);
            }
        }

        rs.close();
        stmt.close();
        return result;
    }
        
    
}

