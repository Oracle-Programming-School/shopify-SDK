# shopify-SDK
Java SDK for Shopify REST APIs

### List of Technical Operations
After Checkout fetch & download an order with the speed of 960 orders per minute.
Order hold for 5 minutes in fulfilment Box, So Database jobs can update its additional information like, Vendor Tag, Store ID, Fulfilment Type. 
Auto fulfillment:  (This schedule after every 5 minutes) 
Courier wise auto fulfilment including DHL for international orders
System generates the CN for big cities as per ratio assigned 
System fulfilled an order on Shopify
If order is cancelled than system will reverse all above steps:
  Cancel fulfillment on Shopify
  Update an order in Candela via E-connect mark as cancelled
  Update Tag on Shopify
  Cancel order on Shopify 

Estore team print the invoice either in bulk or a single order
This application provides single page invoice with minimum foot-step algorithm.
System identified the city user after that User can correct it.
Scanning of an invoice for duplication & data validations. 
Dispatch Scanning of an order
Extract Data of Sale & Import in Oracle ERP
Mark dispatch / return tag on Shopify.
SMS Utility configure for alerts & send Discount code to customers.
Email Utility is configured for alerts.
Send invoice detail to customer via SMS.
Fetch Day-to– day tracking of order from courier partners.
Fetch & update Exchange rate on daily basis. 
Inventory Sync:
Fetching inventory of each SKU from Shopify
Fetching SKU’s which are included in cart orders
Validation each SKU net quantity on given rules provided by ECOMM
Adjust quantity on Shopify.


## Flow chart of application
![Alt text](https://github.com/Oracle-Programming-School/shopify-SDK/blob/main/images/flow1.png?raw=true "Optional Title")
![Alt text](https://github.com/Oracle-Programming-School/shopify-SDK/blob/main/images/flow2.png?raw=true "Optional Title")
