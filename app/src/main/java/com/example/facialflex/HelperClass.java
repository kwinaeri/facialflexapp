package com.example.facialflex;

public class HelperClass {

    String fname, em, pword, uname, age, address, birthday, description, role, account_created, unread_notification, is_active, accountstatus;

    public HelperClass(String fname, String em, String pword, String uname, String account_created) {
        this.fname = fname;
        this.em = em;
        this.pword = pword;
        this.uname = uname;
        this.age = "NA";
        this.address = "NA";
        this.birthday = "NA";
        this.description = "NA";
        this.role = "patient";
        this.account_created = account_created;
        this.unread_notification = "1";
        this.is_active = "1";
        this.accountstatus = "activated";

    }

    // Getters and setters for all fields
    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getEm() {
        return em;
    }

    public void setEm(String em) {
        this.em = em;
    }

    public String getPword() {
        return pword;
    }

    public void setPword(String pword) {
        this.pword = pword;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccount_created() {
        return account_created;
    }

    public void setAccount_created(String account_created) {
        this.account_created = account_created;
    }

    public String getUnread_notification() {
        return unread_notification;
    }

    public void setUnread_notification(String unread_notification) {
        this.unread_notification = unread_notification;
    }

    public String getIs_active() {
        return is_active;
    }

    public void setIs_active(String is_active) {
        this.is_active = is_active;
    }

    public String getAccountstatus() {
        return accountstatus;
    }

    public void setAccountstatus(String role) {
        this.accountstatus = accountstatus;
    }
}
