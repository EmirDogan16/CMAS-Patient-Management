package com.patientx.model;

public class Patient {
    private String patientId;
    private String name;

    public Patient(String patientId, String name) {
        this.patientId = patientId;
        this.name = name;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        // Ensure output is in English
        return String.format("Patient ID: %s, Name: %s", patientId, (name != null ? name : "N/A"));
    }
} 