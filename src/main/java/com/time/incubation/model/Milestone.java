package com.time.incubation.model;

import java.time.LocalDate;

public class Milestone {

    private String milestoneId;
    private String startupId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate deadline;
    private LocalDate completionDate;
    private MilestoneStatus status;
    private int completionPercentage;

    public Milestone(String milestoneId, String startupId, String title,
                     String description, LocalDate startDate, LocalDate deadline,
                     LocalDate completionDate, MilestoneStatus status,
                     int completionPercentage) {
        this.milestoneId = milestoneId;
        this.startupId = startupId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.deadline = deadline;
        this.completionDate = completionDate;
        this.status = status;
        this.completionPercentage = completionPercentage;
    }

    public String getMilestoneId() { return milestoneId; }
    public void setMilestoneId(String milestoneId) { this.milestoneId = milestoneId; }

    public String getStartupId() { return startupId; }
    public void setStartupId(String startupId) { this.startupId = startupId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public MilestoneStatus getStatus() { return status; }
    public void setStatus(MilestoneStatus status) { this.status = status; }

    public int getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }

    @Override
    public String toString() {
        return title + " (" + status + " " + completionPercentage + "%)";
    }
}