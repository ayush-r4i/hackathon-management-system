package com.collegefest.management.dto;

/**
 * Read-only projection used to render the leaderboard.
 * Not a JPA entity - it is built in the service layer from Score data.
 */
public class LeaderboardEntry {

    private int rank;
    private Long teamId;
    private String teamName;
    private String eventName;
    private double averageScore;
    private long numberOfJudgesScored;
    private double avgTechnicalImplementation;
    private double avgInnovation;

    public LeaderboardEntry() {
    }

    public LeaderboardEntry(Long teamId, String teamName, String eventName,
                             double averageScore, long numberOfJudgesScored) {
        this(teamId, teamName, eventName, averageScore, numberOfJudgesScored, 0.0, 0.0);
    }

    public LeaderboardEntry(Long teamId, String teamName, String eventName,
                             double averageScore, long numberOfJudgesScored,
                             double avgTechnicalImplementation, double avgInnovation) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.eventName = eventName;
        this.averageScore = averageScore;
        this.numberOfJudgesScored = numberOfJudgesScored;
        this.avgTechnicalImplementation = avgTechnicalImplementation;
        this.avgInnovation = avgInnovation;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public long getNumberOfJudgesScored() {
        return numberOfJudgesScored;
    }

    public void setNumberOfJudgesScored(long numberOfJudgesScored) {
        this.numberOfJudgesScored = numberOfJudgesScored;
    }

    public double getAvgTechnicalImplementation() {
        return avgTechnicalImplementation;
    }

    public void setAvgTechnicalImplementation(double avgTechnicalImplementation) {
        this.avgTechnicalImplementation = avgTechnicalImplementation;
    }

    public double getAvgInnovation() {
        return avgInnovation;
    }

    public void setAvgInnovation(double avgInnovation) {
        this.avgInnovation = avgInnovation;
    }
}
