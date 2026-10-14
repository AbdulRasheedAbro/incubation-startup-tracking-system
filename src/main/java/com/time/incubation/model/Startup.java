package com.time.incubation.model;

import java.time.LocalDate;

public class Startup {

    private String startupId;
    private String startupName;
    private String founderName;
    private String coFounderName;
    private String email;
    private String phoneNumber;
    private String industry;
    private StartupStage stage;
    private LocalDate incubationStartDate;
    private LocalDate expectedGraduationDate;
    private StartupStatus currentStatus;
    private String programId;

    public Startup(String startupId, String startupName, String founderName,
                   String coFounderName, String email, String phoneNumber,
                   String industry, StartupStage stage,
                   LocalDate incubationStartDate, LocalDate expectedGraduationDate,
                   StartupStatus currentStatus, String programId) {
        this.startupId = startupId;
        this.startupName = startupName;
        this.founderName = founderName;
        this.coFounderName = coFounderName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.industry = industry;
        this.stage = stage;
        this.incubationStartDate = incubationStartDate;
        this.expectedGraduationDate = expectedGraduationDate;
        this.currentStatus = currentStatus;
        this.programId = programId;
    }

    public String getStartupId() { return startupId; }
    public void setStartupId(String startupId) { this.startupId = startupId; }

    public String getStartupName() { return startupName; }
    public void setStartupName(String startupName) { this.startupName = startupName; }

    public String getFounderName() { return founderName; }
    public void setFounderName(String founderName) { this.founderName = founderName; }

    public String getCoFounderName() { return coFounderName; }
    public void setCoFounderName(String coFounderName) { this.coFounderName = coFounderName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public StartupStage getStage() { return stage; }
    public void setStage(StartupStage stage) { this.stage = stage; }

    public LocalDate getIncubationStartDate() { return incubationStartDate; }
    public void setIncubationStartDate(LocalDate incubationStartDate) { this.incubationStartDate = incubationStartDate; }

    public LocalDate getExpectedGraduationDate() { return expectedGraduationDate; }
    public void setExpectedGraduationDate(LocalDate expectedGraduationDate) { this.expectedGraduationDate = expectedGraduationDate; }

    public StartupStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(StartupStatus currentStatus) { this.currentStatus = currentStatus; }

    public String getProgramId() { return programId; }
    public void setProgramId(String programId) { this.programId = programId; }

    @Override
    public String toString() {
        return startupId + " | " + startupName + " | " + industry + " | " + currentStatus;
    }
}