package com.example.facialflex;

public class Doctor {
    private String name;
    private String certificate;
    private String email;
    private String number;
    private String schedule;
    private String profile_image;// Ensure this matches your database structure

    // Default constructor required for calls to DataSnapshot.getValue(Doctor.class)
    public Doctor() {}

    public Doctor(String name, String certificate, String email, String number, String profile_image) {
        this.name = name;
        this.certificate = certificate;
        this.email = email;
        this.number = number;
        this.profile_image = profile_image;
        this.schedule = "";
    }

    // Getters
    public String getName() { return name; }
    public String getCertificate() { return certificate; }
    public String getEmail() { return email; }
    public String getNumber() { return number; }
    public String getProfileImage() { return profile_image; } // Getter for the image URL
    // Getter for schedule
    public String getSchedule() {
        return schedule;
    }

    // Setter for schedule
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
}
