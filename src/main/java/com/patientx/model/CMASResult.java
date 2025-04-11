package com.patientx.model;

public class CMASResult {
    private long id;
    private String patientId;
    private String testDate;
    private int score;
    private int scoreType;

    public CMASResult(long id, String patientId, String testDate, int score, int scoreType) {
        this.id = id;
        this.patientId = patientId;
        this.testDate = testDate;
        this.score = score;
        this.scoreType = scoreType;
    }

    public long getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getTestDate() {
        return testDate;
    }

    public int getScore() {
        return score;
    }

    public int getScoreType() {
        return scoreType;
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Test Date: %s, Score: %d, Type: %d", id, testDate, score, scoreType);
    }
} 