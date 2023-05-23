package com.ordg.blueb.pos.sales;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import com.ordg.utl.collections.Constants;
import com.ordg.utl.collections.DataLoader;

/**
 *
 * @author Muhammad Usman
 */
public class ProductSearch extends javax.swing.JPanel {

    /**
     * Creates new form ProductSearch
     */
    TableRowSorter<DefaultTableModel> rt = null;
    Sales sale =null; 

    public ProductSearch(Sales pSale) {
        
        this.sale=pSale;
        initComponents();

        try {
            DefaultTableModel tm = (DefaultTableModel) jTable1.getModel();

            for (int ldx = 0; ldx < DataLoader.getItemVOList().size(); ldx++) {
                ItemVO product = DataLoader.getItemVOList().get(ldx);
                tm.addRow(new Object[]{ldx, product.getStyleName(), product.getSkuName(), product.getRetPrice(), product.getColorName(),product.getItemBarcode()}
                );
            }
            rt = new TableRowSorter<DefaultTableModel>(tm);
            jTable1.setRowSorter(rt);

            if (!"".equals(sale.barcode.getText()) && sale.barcode.getText().length() > 0) {
                rt.setRowFilter(RowFilter.regexFilter(sale.barcode.getText()));
            }

            jTable1.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        int row = jTable1.getSelectedRow();
                        String itemBarcode = jTable1.getValueAt(row, 5).toString();
                        ItemVO item = DataLoader.getItemByBarcode(itemBarcode);
                        sale.addRowToTable(item.getItemBarcode(), item.getSkuName(), item.getStyleName(), item.getRetPrice(), "PCS", 1);
                    }
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setBackground(new java.awt.Color(204, 204, 204));
        setMinimumSize(new java.awt.Dimension(700, 455));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel1.setMinimumSize(new java.awt.Dimension(0, 100));
        jPanel1.setName(""); // NOI18N

        jLabel1.setText("SKU:");

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 670, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel1))
            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        add(jPanel1);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Row #", "Product Name", "SKU", "Price", "Color", "ItemBarcode"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setColumnSelectionAllowed(true);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(0).setPreferredWidth(5);
            jTable1.getColumnModel().getColumn(5).setResizable(false);
        }

        add(jScrollPane1);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        // TODO add your handling code here:
        System.out.println(jTextField1.getText());
        rt.setRowFilter(RowFilter.regexFilter(jTextField1.getText()));

    }//GEN-LAST:event_jTextField1KeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
