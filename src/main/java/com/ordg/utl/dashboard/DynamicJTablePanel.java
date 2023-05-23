package com.ordg.utl.dashboard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import com.ordg.utl.database.OracleConnection;
import com.ordg.utl.log.Console;


public class DynamicJTablePanel extends JPanel {

   private final String query;
   private  JTable table;
    private BoxLayout boxLayout;
    JLabel totalRowCountLabel = new JLabel();
    Vector<String> columnNames = new Vector<String>();
    OracleConnection DB = new OracleConnection();
   
    
    DefaultTableModel tableModel;
   int totalRowCount; 
   int currentOffset = 0;
   int rowsPerLoad = 10000;
    
    
   public DynamicJTablePanel(String query) {

    this.query = query;
    this.boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
    setLayout(boxLayout);
    InitColumnNames(query);
    
   // add(createButtonsTab());
    
    add(createTablePanel());
    
    loadMoreData();
    
    add(createStatusTab());   
}  
    
private JPanel createButtonsTab() {
    JPanel buttonsTab = new JPanel();
    buttonsTab.setLayout(new FlowLayout(FlowLayout.LEADING));
    buttonsTab.setPreferredSize(new Dimension(0, 40));
    JButton refreshButton = new JButton("Refresh");
    refreshButton.addActionListener(e -> {
         tableModel = (DefaultTableModel) table.getModel();
        tableModel.setRowCount(0);
    });
    buttonsTab.add(refreshButton);
    return buttonsTab;
}

 private JPanel createStatusTab() 
 {   
    totalRowCount = getTotalRowCount();
    totalRowCountLabel.setText("Total rows: " + totalRowCount);
    
    JPanel statusTab = new JPanel();
    statusTab.setLayout(new FlowLayout(FlowLayout.LEADING));
    statusTab.setPreferredSize(new Dimension(0, 20));
    statusTab.add(totalRowCountLabel);
    return statusTab;
}
 
 private JPanel createTablePanel() {
   JPanel tablePanel = new JPanel(new BorderLayout());
   tablePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

   tableModel = new DefaultTableModel(columnNames, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
         return false;
      }
   };

   table = new JTable(tableModel);
   table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

   // Add a JScrollPane to the JTable
   JScrollPane scrollPane = new JScrollPane(table);
   scrollPane.setPreferredSize(new Dimension(800, 600));
   tablePanel.add(scrollPane, BorderLayout.CENTER);

   // Add a JScrollBar to the JScrollPane
   JScrollBar scrollBar = new JScrollBar(JScrollBar.VERTICAL);
   scrollPane.setVerticalScrollBar(scrollBar);

   // Add a JViewport to the JScrollPane
   JViewport viewport = new JViewport();
   scrollPane.setViewport(viewport);

   // Add a JPanel to the JViewport
   JPanel view = new JPanel();
   view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
   viewport.setView(view);

   // Add the JTable to the JPanel
   view.add(table.getTableHeader());
   view.add(table);

   // Add a ChangeListener to the JScrollBar to detect when the user scrolls
   scrollBar.addAdjustmentListener(new AdjustmentListener() {
      public void adjustmentValueChanged(AdjustmentEvent e) {
         if (!e.getValueIsAdjusting()) {
            JScrollBar scrollBar = (JScrollBar) e.getAdjustable();
            int value = scrollBar.getValue();
            int extent = scrollBar.getModel().getExtent();
            int maximum = scrollBar.getModel().getMaximum();
            if (value + extent == maximum) {
               loadMoreData();
            }
         }
      }
   });

   return tablePanel;
}


    private int getTotalRowCount() {
        int count = 0;
        String countQuery = "SELECT COUNT(*) FROM (" + query + ")";
        try {   
              Statement stmt = DB.getConnection().createStatement(); 
              ResultSet rs = stmt.executeQuery(countQuery);
              
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    
public void loadMoreData() {
    
    if (totalRowCount ==0) return;
    
   try (Connection con = OracleConnection.getConnection();
        PreparedStatement pstmt = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

      // Set the fetch size and current row offset
      pstmt.setFetchSize(rowsPerLoad);
      //pstmt.setMaxRows(currentOffset + rowsPerLoad);

      // Execute the query and get the result set
      ResultSet rs = pstmt.executeQuery();
      
      // Loop through the result set and add the rows to the table model
      int LoadedRecord = 0;
      
      if (currentOffset > 0 ) 
         {
                rs.absolute(currentOffset);
         }
      
      while (rs.next() && LoadedRecord < rowsPerLoad) {
         Vector<String> rowData = new Vector<String>();
         for (int i = 1; i <= tableModel.getColumnCount() - 1; i++) { // Change the loop range to exclude the "Row #" column
            rowData.add(rs.getString(i));
         }
         tableModel.addRow(rowData);
         LoadedRecord++;
      }
      System.out.println(currentOffset);
      
      currentOffset += rowsPerLoad;
      
      System.out.println(currentOffset);
      
   } catch (Exception e) {
      System.out.println("Error occurred in loadMoreData:");
      e.printStackTrace(); // Print the exception stack trace to understand the error
   }
}

private void InitColumnNames(String query) {
    try (
         Statement stmt = DB.getConnection().createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(rsmd.getColumnName(i));
        }
        rs.close();
        stmt.close();
    } catch (Exception e) {
        Console.showError(query, this.getClass());
        e.printStackTrace();
    }
}

}
    