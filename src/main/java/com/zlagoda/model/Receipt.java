package com.zlagoda.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Receipt {
    private String checkNumber;
    private String idEmployee;
    private String cardNumber;
    private Timestamp printDate;
    private BigDecimal sumTotal;
    private BigDecimal vat;

    public Receipt() {
    }

    public Receipt(String checkNumber, String idEmployee, String cardNumber,
            Timestamp printDate, BigDecimal sumTotal, BigDecimal vat) {
        this.checkNumber = checkNumber;
        this.idEmployee = idEmployee;
        this.cardNumber = cardNumber;
        this.printDate = printDate;
        this.sumTotal = sumTotal;
        this.vat = vat;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(String idEmployee) {
        this.idEmployee = idEmployee;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Timestamp getPrintDate() {
        return printDate;
    }

    public void setPrintDate(Timestamp printDate) {
        this.printDate = printDate;
    }

    public BigDecimal getSumTotal() {
        return sumTotal;
    }

    public void setSumTotal(BigDecimal sumTotal) {
        this.sumTotal = sumTotal;
    }

    public BigDecimal getVat() {
        return vat;
    }

    public void setVat(BigDecimal vat) {
        this.vat = vat;
    }
}