package com.zlagoda.model;

public class Product {
    private int idProduct;
    private int categoryNumber;
    private String productName;
    private String producer;
    private String characteristics;

    public Product(int idProduct, int categoryNumber, String productName, String producer, String characteristics) {
        this.idProduct = idProduct;
        this.categoryNumber = categoryNumber;
        this.productName = productName;
        this.producer = producer;
        this.characteristics = characteristics;
    }

    public int getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(int idProduct) {
        this.idProduct = idProduct;
    }

    public int getCategoryNumber() {
        return categoryNumber;
    }

    public void setCategoryNumber(int categoryNumber) {
        this.categoryNumber = categoryNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }
}