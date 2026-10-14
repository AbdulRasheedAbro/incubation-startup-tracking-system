package com.time.incubation.model;

import java.time.LocalDate;

public class ProgressReport {

    private String reportId;
    private String startupId;
    private LocalDate reportDate;
    private String summary;
    private StartupStage currentStage;
    private double overallScore;
    private int completedMilestones;
    private int pendingMilestones;

    public ProgressReport(String reportId, String startupId, LocalDate reportDate,
                          String summary, StartupStage currentStage, double overallScore,
                          int completedMilestones, int pendingMilestones) {
        this.reportId = reportId;
        this.startupId = startupId;
        this.reportDate = reportDate;
        this.summary = summary;
        this.currentStage = currentStage;
        this.overallScore = overallScore;
        this.completedMilestones = completedMilestones;
        this.pendingMilestones = pendingMilestones;
    }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getStartupId() { return startupId; }
    public void setStartupId(String startupId) { this.startupId = startupId; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public StartupStage getCurrentStage() { return currentStage; }
    public void setCurrentStage(StartupStage currentStage) { this.currentStage = currentStage; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public int getCompletedMilestones() { return completedMilestones; }
    public void setCompletedMilestones(int completedMilestones) { this.completedMilestones = completedMilestones; }

    public int getPendingMilestones() { return pendingMilestones; }
    public void setPendingMilestones(int pendingMilestones) { this.pendingMilestones = pendingMilestones; }

    @Override
    public String toString() {
        return reportId + " | " + startupId + " | " + reportDate;
    }
}