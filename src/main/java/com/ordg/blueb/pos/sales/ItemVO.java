
package com.ordg.blueb.pos.sales;

public class ItemVO {

    private String styleCode;
    private String styleName;
    private String designCode;
    private String designName;
    private String colorCode;
    private String colorName;
    private String lapDip;
    private String pantoneNo;
    private String sizeCode;
    private String itemSize;
    private Integer sizeSort;
    private String itemType;
    private String fabricCode;
    private String skuName;
    private Double consumption;
    private Double cost;
    private Double retailPrice;
    private Double onlinePrice;
    private Double retPrice;
    private Double whPrice;
    private String fabricName;
    private String fabricCategory;
    private String ItemBarcode;
    
    
    
    public ItemVO(String styleCode, String styleName, String designCode, String designName, String colorCode, String colorName, String lapDip, String pantoneNo, String sizeCode, String itemSize, Integer sizeSort, String itemType, String fabricCode, String skuName, Double consumption, Double cost, Double retailPrice, Double onlinePrice, Double retPrice, Double whPrice, String fabricName, String fabricCategory,String ItemBarcode) {
        this.styleCode = styleCode;
        this.styleName = styleName;
        this.designCode = designCode;
        this.designName = designName;
        this.colorCode = colorCode;
        this.colorName = colorName;
        this.lapDip = lapDip;
        this.pantoneNo = pantoneNo;
        this.sizeCode = sizeCode;
        this.itemSize = itemSize;
        this.sizeSort = sizeSort;
        this.itemType = itemType;
        this.fabricCode = fabricCode;
        this.skuName = skuName;
        this.consumption = consumption;
        this.cost = cost;
        this.retailPrice = retailPrice;
        this.onlinePrice = onlinePrice;
        this.retPrice = retPrice;
        this.whPrice = whPrice;
        this.fabricName = fabricName;
        this.fabricCategory = fabricCategory;
        this.ItemBarcode = ItemBarcode;
    }

    public String getItemBarcode() {
        return ItemBarcode;
    }

    public void setItemBarcode(String ItemBarcode) {
        this.ItemBarcode = ItemBarcode;
    }
    
    public String getStyleCode() {
        return styleCode;
    }

    public void setStyleCode(String styleCode) {
        this.styleCode = styleCode;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public String getDesignCode() {
        return designCode;
    }

    public void setDesignCode(String designCode) {
        this.designCode = designCode;
    }

    public String getDesignName() {
        return designName;
    }

    public void setDesignName(String designName) {
        this.designName = designName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getLapDip() {
        return lapDip;
    }

    public void setLapDip(String lapDip) {
        this.lapDip = lapDip;
    }

    public String getPantoneNo() {
        return pantoneNo;
    }

    public void setPantoneNo(String pantoneNo) {
        this.pantoneNo = pantoneNo;
    }

    public String getSizeCode() {
        return sizeCode;
    }

    public void setSizeCode(String sizeCode) {
        this.sizeCode = sizeCode;
    }

    public String getItemSize() {
        return itemSize;
    }

    public void setItemSize(String itemSize) {
        this.itemSize = itemSize;
    }

    public Integer getSizeSort() {
        return sizeSort;
    }

    public void setSizeSort(Integer sizeSort) {
        this.sizeSort = sizeSort;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getFabricCode() {
        return fabricCode;
    }

    public void setFabricCode(String fabricCode) {
        this.fabricCode = fabricCode;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public Double getConsumption() {
        return consumption;
    }

    public void setConsumption(Double consumption) {
        this.consumption = consumption;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(Double retailPrice) {
        this.retailPrice = retailPrice;
    }

    public Double getOnlinePrice() {
        return onlinePrice;
    }

    public void setOnlinePrice(Double onlinePrice) {
        this.onlinePrice = onlinePrice;
    }

    public Double getRetPrice() {
        return retPrice;
    }

    public void setRetPrice(Double retPrice) {
        this.retPrice = retPrice;
    }

    public Double getWhPrice() {
        return whPrice;
    }

    public void setWhPrice(Double whPrice) {
        this.whPrice = whPrice;
    }

    public String getFabricName() {
        return fabricName;
    }

    public void setFabricName(String fabricName) {
        this.fabricName = fabricName;
    }

    public String getFabricCategory() {
        return fabricCategory;
    }

    public void setFabricCategory(String fabricCategory) {
        this.fabricCategory = fabricCategory;
    }

    @Override
    public String toString() {
        return "ItemVO{" + "styleCode=" + styleCode + ", styleName=" + styleName + ", designCode=" + designCode + ", designName=" + designName + ", colorCode=" + colorCode + ", colorName=" + colorName + ", lapDip=" + lapDip + ", pantoneNo=" + pantoneNo + ", sizeCode=" + sizeCode + ", itemSize=" + itemSize + ", sizeSort=" + sizeSort + ", itemType=" + itemType + ", fabricCode=" + fabricCode + ", skuName=" + skuName + ", consumption=" + consumption + ", cost=" + cost + ", retailPrice=" + retailPrice + ", onlinePrice=" + onlinePrice + ", retPrice=" + retPrice + ", whPrice=" + whPrice + ", fabricName=" + fabricName + ", fabricCategory=" + fabricCategory + '}';
    }

    
    
}
