package com.patientx.model;

public class Measurement {
    private int id;
    private String patientId;
    private String date;
    private int labResultId;
    private String testName;
    private String value;
    private String unit;

    public Measurement(int id, String patientId, String date, int labResultId, String testName, String value, String unit) {
        this.id = id;
        this.patientId = patientId;
        this.date = date;
        this.labResultId = labResultId;
        this.testName = testName;
        this.value = value;
        this.unit = unit;
    }

    public int getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDate() {
        return date;
    }

    public int getLabResultId() {
        return labResultId;
    }

    public String getTestName() {
        return testName;
    }

    public String getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return String.format("Date: %s, Test: %s, Value: %s %s", 
            date, testName, value, unit);
    }
} 