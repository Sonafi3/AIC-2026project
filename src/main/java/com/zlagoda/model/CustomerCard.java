package com.zlagoda.model;

public class CustomerCard {
    private String cardNumber;
    private String custSurname;
    private String custName;
    private String custPatronymic;
    private String phoneNumber;
    private String city;
    private String street;
    private String zipCode;
    private int percent;

    public CustomerCard() {
    }

    public CustomerCard(String cardNumber, String custSurname, String custName, String custPatronymic,
            String phoneNumber, String city, String street, String zipCode, int percent) {
        this.cardNumber = cardNumber;
        this.custSurname = custSurname;
        this.custName = custName;
        this.custPatronymic = custPatronymic;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.street = street;
        this.zipCode = zipCode;
        this.percent = percent;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCustSurname() {
        return custSurname;
    }

    public void setCustSurname(String custSurname) {
        this.custSurname = custSurname;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getCustPatronymic() {
        return custPatronymic;
    }

    public void setCustPatronymic(String custPatronymic) {
        this.custPatronymic = custPatronymic;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }
}