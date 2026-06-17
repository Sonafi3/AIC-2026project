package com.zlagoda.model;

import java.math.BigDecimal;
import java.sql.Date;

public class Employee {
    private String idEmployee;
    private String emplSurname;
    private String emplName;
    private String emplPatronymic;
    private String emplRole;
    private BigDecimal salary;
    private Date dateOfBirth;
    private Date dateOfStart;
    private String phoneNumber;
    private String city;
    private String street;
    private String zipCode;
    private String passwordHash;

    public Employee() {
    }

    public Employee(String idEmployee, String emplSurname, String emplName, String emplPatronymic,
            String emplRole, BigDecimal salary, Date dateOfBirth, Date dateOfStart,
            String phoneNumber, String city, String street, String zipCode, String passwordHash) {
        this.idEmployee = idEmployee;
        this.emplSurname = emplSurname;
        this.emplName = emplName;
        this.emplPatronymic = emplPatronymic;
        this.emplRole = emplRole;
        this.salary = salary;
        this.dateOfBirth = dateOfBirth;
        this.dateOfStart = dateOfStart;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.street = street;
        this.zipCode = zipCode;
        this.passwordHash = passwordHash;
    }

    public String getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(String idEmployee) {
        this.idEmployee = idEmployee;
    }

    public String getEmplSurname() {
        return emplSurname;
    }

    public void setEmplSurname(String emplSurname) {
        this.emplSurname = emplSurname;
    }

    public String getEmplName() {
        return emplName;
    }

    public void setEmplName(String emplName) {
        this.emplName = emplName;
    }

    public String getEmplPatronymic() {
        return emplPatronymic;
    }

    public void setEmplPatronymic(String emplPatronymic) {
        this.emplPatronymic = emplPatronymic;
    }

    public String getEmplRole() {
        return emplRole;
    }

    public void setEmplRole(String emplRole) {
        this.emplRole = emplRole;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Date getDateOfStart() {
        return dateOfStart;
    }

    public void setDateOfStart(Date dateOfStart) {
        this.dateOfStart = dateOfStart;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}