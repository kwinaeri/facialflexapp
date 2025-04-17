package com.example.facialflex;

public class Appointment {
    private String date;
    private String doctor;
    private String fullname;
    private String status;
    private String time;
    private String userkey;
    private String username;

    public Appointment() { }

    public Appointment(String date, String doctor, String fullname, String status, String time, String userkey, String username) {
        this.date = date;
        this.doctor = doctor;
        this.fullname = fullname;
        this.status = status;
        this.time = time;
        this.userkey = userkey;
        this.username = username;
    }

    // Getters and setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getUserkey() { return userkey; }
    public void setUserkey(String userkey) { this.userkey = userkey; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
