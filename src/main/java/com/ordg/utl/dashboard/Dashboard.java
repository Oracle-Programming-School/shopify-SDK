package com.ordg.utl.dashboard;

import com.ordg.utl.display.WindowManager;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import com.ordg.utl.collections.Constants;
import com.ordg.blueb.pos.dashboard.MainDashboard;
import com.ordg.blueb.pos.returns.PosReturnForm;
import com.ordg.blueb.pos.sales.Sales;
import com.ordg.utl.display.Display;

public class Dashboard extends JFrame {

    WindowManager windowManager;
    JMenu TransactionMenu;
    public static Display display;
    public Dashboard() {
        init();
        display = new Display(this);
        try {
        UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (UnsupportedLookAndFeelException ex) 
            {
            // Handle the exception if needed
            }
    }

    private void init() {
        // create menu bar
        JMenuBar menuBar = new JMenuBar();

        windowManager = new WindowManager(this);

        // create file menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        fileMenu.add(exitMenuItem);

        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        
        TransactionMenu = new JMenu("Transaction");
        ViewStoryInventory();
        ViewSaleForm();
        ViewReturnForm();
        

        // add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(TransactionMenu);

        // set menu bar for the frame
        setJMenuBar(menuBar);

        // set frame properties
        setTitle("BlueB - Point of Sale | "+Constants.get("APP_STORE_NAME"));
        setSize(500, 500);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // maximize the window
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        windowManager.addScene("Main Dashboard", new MainDashboard());
        
        setVisible(true);
    }

    private void ViewStoryInventory() {

        JMenuItem ViewStoryInventory_MI = new JMenuItem("View Store Inventory");
        TransactionMenu.add(ViewStoryInventory_MI);

        ViewStoryInventory_MI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JMenuItem source = (JMenuItem) event.getSource();
                String menuName = source.getText();
                ViewStoryInventory_C(menuName);
            }
        });
    }
    private void ViewStoryInventory_C(String menuName) {
        DynamicJTablePanel j = new DynamicJTablePanel("select * from POSC_Item_onhand_v where store_id ="+Constants.get("APP_STORE_ID"));
        windowManager.addScene(menuName, j);
    }
    
     private void ViewSaleForm() {

        JMenuItem ViewSaleForm_MI = new JMenuItem("Sale Form");
        TransactionMenu.add(ViewSaleForm_MI);

        ViewSaleForm_MI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                JMenuItem source = (JMenuItem) event.getSource();
                String menuName = source.getText();
                ViewSaleForm_C(menuName);
            }
        });
    }
    private void ViewSaleForm_C(String menuName) {
        Sales j = new Sales();
        windowManager.addScene(menuName, j);
    }
    
    
    private void ViewReturnForm() {

        JMenuItem ViewReturnForm_MI = new JMenuItem("Return Form");
        TransactionMenu.add(ViewReturnForm_MI);

        ViewReturnForm_MI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                JMenuItem source = (JMenuItem) event.getSource();
                String menuName = source.getText();
                ViewReturnForm_C(menuName);
            }
        });
    }
    private void ViewReturnForm_C(String menuName) {
        PosReturnForm j = new PosReturnForm();
        windowManager.addScene(menuName, j);
    }
    
    
}
