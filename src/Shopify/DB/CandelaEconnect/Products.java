/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.DB.CandelaEconnect;

/**
 *
 * @author abdul.ahad1
 */
public class Products {
private String ProductCode;
private String Qty;
private String DiscountPerc; 
private String ItemAmount; 
private String ItemTotal;

    public String getProductCode() {
        return ProductCode;
    }

    public void setProductCode(String ProductCode) {
        this.ProductCode = ProductCode;
    }

    public String getQty() {
        return Qty;
    }

    public void setQty(String Qty) {
        this.Qty = Qty;
    }

    public String getDiscountPerc() {
        return DiscountPerc;
    }

    public void setDiscountPerc(String DiscountPerc) {
        this.DiscountPerc = DiscountPerc;
    }

    public String getItemAmount() {
        return ItemAmount;
    }

    public void setItemAmount(String ItemAmount) {
        this.ItemAmount = ItemAmount;
    }

    public String getItemTotal() {
        return ItemTotal;
    }

    public void setItemTotal(String ItemTotal) {
        this.ItemTotal = ItemTotal;
    }

    
}
