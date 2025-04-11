package com.patientx.model;

public class LabResult {
    private String labResultId;
    private String patientId;
    private String testDate;
    private String testName;
    private String value;
    private String unit;
    private String referenceRange;
    private String measurementDate;
    private String measurementValue;

    public LabResult(String labResultId, String patientId, String testDate, String testName, String value, String unit, String referenceRange) {
        this.labResultId = labResultId;
        this.patientId = patientId;
        this.testDate = testDate;
        this.testName = testName;
        this.value = value;
        this.unit = unit;
        this.referenceRange = referenceRange;
    }

    public void addMeasurement(String date, String value) {
        this.measurementDate = date;
        this.measurementValue = value;
    }

    // Getters
    public String getLabResultId() { return labResultId; }
    public String getPatientId() { return patientId; }
    public String getTestDate() { return testDate; }
    public String getTestName() { return testName; }
    public String getValue() { return value; }
    public String getUnit() { return unit; }
    public String getReferenceRange() { return referenceRange; }
    public String getMeasurementDate() { return measurementDate; }
    public String getMeasurementValue() { return measurementValue; }
    public boolean hasMeasurement() { return measurementDate != null && measurementValue != null; }

    @Override
    public String toString() {
        return "LabResult [" +
               "labResultId=" + labResultId +
               ", patientId=" + patientId +
               ", testDate=" + testDate +
               ", testName=" + testName +
               ", value=" + value + (unit != null && !unit.isEmpty() ? " " + unit : "") +
               ", referenceRange=" + (referenceRange != null ? referenceRange : "N/A") +
               ']';
    }
} 