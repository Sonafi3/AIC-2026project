package com.zlagoda.model;

import java.math.BigDecimal;

public class Sale {
    private String upc;
    private String checkNumber;
    private int productNumber;
    private BigDecimal sellingPrice;

    public Sale() {
    }

    public Sale(String upc, String checkNumber, int productNumber, BigDecimal sellingPrice) {
        this.upc = upc;
        this.checkNumber = checkNumber;
        this.productNumber = productNumber;
        this.sellingPrice = sellingPrice;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public int getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(int productNumber) {
        this.productNumber = productNumber;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
}