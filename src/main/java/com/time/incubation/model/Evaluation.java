package com.time.incubation.model;

import java.time.LocalDate;

public class Evaluation {

    private String evaluationId;
    private String startupId;

    private int teamScore;
    private int problemValidationScore;
    private int productDevelopmentScore;
    private int marketValidationScore;
    private int businessModelScore;
    private int tractionScore;
    private int revenueScore;
    private int pitchReadinessScore;

    private double overallScore;
    private LocalDate evaluatedDate;

    public Evaluation(String evaluationId, String startupId,
                      int teamScore, int problemValidationScore,
                      int productDevelopmentScore, int marketValidationScore,
                      int businessModelScore, int tractionScore,
                      int revenueScore, int pitchReadinessScore,
                      double overallScore, LocalDate evaluatedDate) {
        this.evaluationId = evaluationId;
        this.startupId = startupId;
        this.teamScore = teamScore;
        this.problemValidationScore = problemValidationScore;
        this.productDevelopmentScore = productDevelopmentScore;
        this.marketValidationScore = marketValidationScore;
        this.businessModelScore = businessModelScore;
        this.tractionScore = tractionScore;
        this.revenueScore = revenueScore;
        this.pitchReadinessScore = pitchReadinessScore;
        this.overallScore = overallScore;
        this.evaluatedDate = evaluatedDate;
    }

    public String getEvaluationId() { return evaluationId; }
    public void setEvaluationId(String evaluationId) { this.evaluationId = evaluationId; }

    public String getStartupId() { return startupId; }
    public void setStartupId(String startupId) { this.startupId = startupId; }

    public int getTeamScore() { return teamScore; }
    public void setTeamScore(int teamScore) { this.teamScore = teamScore; }

    public int getProblemValidationScore() { return problemValidationScore; }
    public void setProblemValidationScore(int problemValidationScore) { this.problemValidationScore = problemValidationScore; }

    public int getProductDevelopmentScore() { return productDevelopmentScore; }
    public void setProductDevelopmentScore(int productDevelopmentScore) { this.productDevelopmentScore = productDevelopmentScore; }

    public int getMarketValidationScore() { return marketValidationScore; }
    public void setMarketValidationScore(int marketValidationScore) { this.marketValidationScore = marketValidationScore; }

    public int getBusinessModelScore() { return businessModelScore; }
    public void setBusinessModelScore(int businessModelScore) { this.businessModelScore = businessModelScore; }

    public int getTractionScore() { return tractionScore; }
    public void setTractionScore(int tractionScore) { this.tractionScore = tractionScore; }

    public int getRevenueScore() { return revenueScore; }
    public void setRevenueScore(int revenueScore) { this.revenueScore = revenueScore; }

    public int getPitchReadinessScore() { return pitchReadinessScore; }
    public void setPitchReadinessScore(int pitchReadinessScore) { this.pitchReadinessScore = pitchReadinessScore; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public LocalDate getEvaluatedDate() { return evaluatedDate; }
    public void setEvaluatedDate(LocalDate evaluatedDate) { this.evaluatedDate = evaluatedDate; }

    /**
     * Average of the 8 individual scores, rounded to 2 decimals.
     * This is the value that should be stored in overall_score.
     */
    public static double computeOverall(int team, int problemValidation,
                                        int productDevelopment, int marketValidation,
                                        int businessModel, int traction,
                                        int revenue, int pitchReadiness) {
        int sum = team + problemValidation + productDevelopment + marketValidation
                + businessModel + traction + revenue + pitchReadiness;
        double avg = sum / 8.0;
        return Math.round(avg * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return startupId + " | overall " + overallScore + "/10";
    }
}