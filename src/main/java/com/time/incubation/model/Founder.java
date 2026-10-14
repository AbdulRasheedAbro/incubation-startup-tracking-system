package com.time.incubation.model;

public class Founder {

    private String founderId;
    private String name;
    private String startupId;
    private String email;
    private String phone;
    private String qualification;
    private String skills;
    private String linkedinUrl;

    public Founder(String founderId, String name, String startupId,
                   String email, String phone,
                   String qualification, String skills, String linkedinUrl) {
        this.founderId = founderId;
        this.name = name;
        this.startupId = startupId;
        this.email = email;
        this.phone = phone;
        this.qualification = qualification;
        this.skills = skills;
        this.linkedinUrl = linkedinUrl;
    }

    public String getFounderId() { return founderId; }
    public void setFounderId(String founderId) { this.founderId = founderId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartupId() { return startupId; }
    public void setStartupId(String startupId) { this.startupId = startupId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    @Override
    public String toString() {
        return name + " (" + qualification + ")";
    }
}