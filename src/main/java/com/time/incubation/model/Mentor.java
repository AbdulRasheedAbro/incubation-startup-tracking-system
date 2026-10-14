package com.time.incubation.model;

public class Mentor {

    private String mentorId;
    private String fullName;
    private String expertise;
    private String email;
    private String phone;
    private String organization;

    public Mentor(String mentorId, String fullName, String expertise,
                  String email, String phone, String organization) {
        this.mentorId = mentorId;
        this.fullName = fullName;
        this.expertise = expertise;
        this.email = email;
        this.phone = phone;
        this.organization = organization;
    }

    public String getMentorId() { return mentorId; }
    public void setMentorId(String mentorId) { this.mentorId = mentorId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    @Override
    public String toString() {
        return fullName + " (" + expertise + ")";
    }
}