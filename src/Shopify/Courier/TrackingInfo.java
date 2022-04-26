/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.Courier;

/**
 *
 * @author M.Faisal1521
 */
public class TrackingInfo {
    private String TrackingOrderId;
    private String RequestStatus;
    private String RequestMessage;
    private String TrackingNumber;
    private String shipper_Name;
    private String PickupOrigin;
    private String consignee;
    private String order_information;
    private String TrackingCompany;
    private Integer TransactionID;
    /**
     * @return the TrackingOrderId
     */
    public String getTrackingOrderId() {
        return TrackingOrderId;
    }

    /**
     * @param TrackingOrderId the TrackingOrderId to set
     */
    public void setTrackingOrderId(String TrackingOrderId) {
        this.TrackingOrderId = TrackingOrderId;
    }

    /**
     * @return the RequestStatus
     */
    public String getRequestStatus() {
        return RequestStatus;
    }

    /**
     * @param RequestStatus the RequestStatus to set
     */
    public void setRequestStatus(String RequestStatus) {
        this.RequestStatus = RequestStatus;
    }

    /**
     * @return the RequestMessage
     */
    public String getRequestMessage() {
        return RequestMessage;
    }

    /**
     * @param RequestMessage the RequestMessage to set
     */
    public void setRequestMessage(String RequestMessage) {
        this.RequestMessage = RequestMessage;
    }

    /**
     * @return the TrackingNumber
     */
    public String getTrackingNumber() {
        return TrackingNumber;
    }

    /**
     * @param TrackingNumber the TrackingNumber to set
     */
    public void setTrackingNumber(String TrackingNumber) {
        this.TrackingNumber = TrackingNumber;
    }

    /**
     * @return the shipper_Name
     */
    public String getShipper_Name() {
        return shipper_Name;
    }

    /**
     * @param shipper_Name the shipper_Name to set
     */
    public void setShipper_Name(String shipper_Name) {
        this.shipper_Name = shipper_Name;
    }

    /**
     * @return the PickupOrigin
     */
    public String getPickupOrigin() {
        return PickupOrigin;
    }

    /**
     * @param PickupOrigin the PickupOrigin to set
     */
    public void setPickupOrigin(String PickupOrigin) {
        this.PickupOrigin = PickupOrigin;
    }

    /**
     * @return the consignee
     */
    public String getConsignee() {
        return consignee;
    }

    /**
     * @param consignee the consignee to set
     */
    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    /**
     * @return the order_information
     */
    public String getOrder_information() {
        return order_information;
    }

    /**
     * @param order_information the order_information to set
     */
    public void setOrder_information(String order_information) {
        this.order_information = order_information;
    }

    /**
     * @return the TrackingCompany
     */
    public String getTrackingCompany() {
        return TrackingCompany;
    }

    /**
     * @param TrackingCompany the TrackingCompany to set
     */
    public void setTrackingCompany(String TrackingCompany) {
        this.TrackingCompany = TrackingCompany;
    }

    /**
     * @return the TransactionID
     */
    public Integer getTransactionID() {
        return TransactionID;
    }

    /**
     * @param TransactionID the TransactionID to set
     */
    public void setTransactionID(Integer TransactionID) {
        this.TransactionID = TransactionID;
    }
}
