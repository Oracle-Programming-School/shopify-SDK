/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shopify.DB.CandelaEconnect;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

/**
 *
 * @author abdul.ahad1
 */
@JsonPropertyOrder({"AppId", "AppKey","OrderId", "shopId", "OrderDate","FirstName","CustomerEmai","Address",
"City","Country","State","Telephone","Status","ShippingCost","CustomerNo","comments","CourierCompany","CourierNumber",
"Weight","Locality"})
public class ProductHeader {
private String AppId;
private String AppKey ;
private String OrderId  ;
 private String shopId ;
private String OrderDate;
 private String FirstName;
 private String CustomerEmai;
 private String Address ;
 private String City ;
private String  Country;
 private String State;
private  String Telephone;
 private String Status;
 private String ShippingCost  ;
 private String CustomerNo;
 private String comments ;
private String CourierCompany ;
 private String CourierNumber;
 private String Weight;
private String Locality;
List<Products> products;

    public String getAppId() {
        return AppId;
    }

    public void setAppId(String AppId) {
        this.AppId = AppId;
    }

    public String getAppKey() {
        return AppKey;
    }

    public void setAppKey(String AppKey) {
        this.AppKey = AppKey;
    }

    public String getOrderId() {
        return OrderId;
    }

    public void setOrderId(String OrderId) {
        this.OrderId = OrderId;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getOrderDate() {
        return OrderDate;
    }

    public void setOrderDate(String OrderDate) {
        this.OrderDate = OrderDate;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String FirstName) {
        this.FirstName = FirstName;
    }

    public String getCustomerEmai() {
        return CustomerEmai;
    }

    public void setCustomerEmai(String CustomerEmai) {
        this.CustomerEmai = CustomerEmai;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String City) {
        this.City = City;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String Country) {
        this.Country = Country;
    }

    public String getState() {
        return State;
    }

    public void setState(String State) {
        this.State = State;
    }

    public String getTelephone() {
        return Telephone;
    }

    public void setTelephone(String Telephone) {
        this.Telephone = Telephone;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

    public String getShippingCost() {
        return ShippingCost;
    }

    public void setShippingCost(String ShippingCost) {
        this.ShippingCost = ShippingCost;
    }

    public String getCustomerNo() {
        return CustomerNo;
    }

    public void setCustomerNo(String CustomerNo) {
        this.CustomerNo = CustomerNo;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCourierCompany() {
        return CourierCompany;
    }

    public void setCourierCompany(String CourierCompany) {
        this.CourierCompany = CourierCompany;
    }

    public String getCourierNumber() {
        return CourierNumber;
    }

    public void setCourierNumber(String CourierNumber) {
        this.CourierNumber = CourierNumber;
    }

    public String getWeight() {
        return Weight;
    }

    public void setWeight(String Weight) {
        this.Weight = Weight;
    }

    public String getLocality() {
        return Locality;
    }

    public void setLocality(String Locality) {
        this.Locality = Locality;
    }

    public List<Products> getProducts() {
        return products;
    }

    public void setProducts(List<Products> products) {
        this.products = products;
    }

   
}
