package com.ordg.blueb.pos.sales;

/**
 *
 * @author Administrator
 */
public class SaleItem {
    private String sku;
    private String name;
    private double price;
    private String uom;
    private int quantity;
    private double subAmount;
    private double discountPct;
    private double discountAmt;
    private double gstPct;
    private double gstAmt;
    private double totalAmt;

    public SaleItem(String sku, String name, double price, String uom, int quantity, double subAmount, double discountPct, double discountAmt, double gstPct, double gstAmt, double totalAmt) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.uom = uom;
        this.quantity = quantity;
        this.subAmount = subAmount;
        this.discountPct = discountPct;
        this.discountAmt = discountAmt;
        this.gstPct = gstPct;
        this.gstAmt = gstAmt;
        this.totalAmt = totalAmt;
    }
    
    
    public SaleItem(int quantity, double subAmount, double discountAmt,  double gstAmt, double totalAmt) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.uom = uom;
        this.quantity = quantity;
        this.subAmount = subAmount;
        this.discountPct = discountPct;
        this.discountAmt = discountAmt;
        this.gstPct = gstPct;
        this.gstAmt = gstAmt;
        this.totalAmt = totalAmt;
    }
}
