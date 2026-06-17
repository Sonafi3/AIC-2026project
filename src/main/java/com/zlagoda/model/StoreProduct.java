package com.zlagoda.model;

import java.math.BigDecimal;

public class StoreProduct {
    private String upc;
    private String upcProm;
    private int idProduct;
    private BigDecimal sellingPrice;
    private int productsNumber;
    private boolean promotionalProduct;

    public StoreProduct() {
    }

    public StoreProduct(String upc, String upcProm, int idProduct, BigDecimal sellingPrice, int productsNumber,
            boolean promotionalProduct) {
        this.upc = upc;
        this.upcProm = upcProm;
        this.idProduct = idProduct;
        this.sellingPrice = sellingPrice;
        this.productsNumber = productsNumber;
        this.promotionalProduct = promotionalProduct;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getUpcProm() {
        return upcProm;
    }

    public void setUpcProm(String upcProm) {
        this.upcProm = upcProm;
    }

    public int getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(int idProduct) {
        this.idProduct = idProduct;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getProductsNumber() {
        return productsNumber;
    }

    public void setProductsNumber(int productsNumber) {
        this.productsNumber = productsNumber;
    }

    public boolean isPromotionalProduct() {
        return promotionalProduct;
    }

    public void setPromotionalProduct(boolean promotionalProduct) {
        this.promotionalProduct = promotionalProduct;
    }
}