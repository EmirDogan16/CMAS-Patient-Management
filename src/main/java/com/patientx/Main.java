package com.patientx;

import java.util.List;
import java.util.Scanner;

import com.patientx.dao.CMASResultDAO;
import com.patientx.dao.DatabaseManager;
import com.patientx.dao.DoctorDAO;
import com.patientx.dao.LabResultDAO;
import com.patientx.dao.MeasurementDAO;
import com.patientx.dao.PatientDAO;
import com.patientx.model.CMASResult;
import com.patientx.model.LabResult;
import com.patientx.model.Patient;

public class Main {
    private static Scanner scanner;
    private static final PatientDAO patientDAO = new PatientDAO();
    private static final DoctorDAO doctorDAO = new DoctorDAO();
    private static final LabResultDAO labResultDAO = new LabResultDAO();
    private static final MeasurementDAO measurementDAO = new MeasurementDAO();
    private static final CMASResultDAO cmasResultDAO = CMASResultDAO.getInstance();
    private static String currentUserId = null;
    private static String currentRole = null;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        
        // Perform initial database setup/update
        DatabaseManager.getInstance(); // Constructor now calls performInitialSetup internally via getInstance
        
        // Ensure tables are created by initializing DAOs or calling create methods
        patientDAO.createPatientsTable(); // Make sure Patient table is also handled if needed
        doctorDAO.createDoctorsTable();   // Doctor table
        labResultDAO.createLabResultsTable(); // LabResult and Measurement tables
        cmasResultDAO.createCMASTable(); // CMAS table

        while (true) {
            if (currentUserId == null) {
                displayLoginMenu();
            } else if ("doctor".equals(currentRole)) {
                displayDoctorDashboard();
            } else if ("patient".equals(currentRole)) {
                displayPatientDashboard();
            }
        }
    }

    private static void displayLoginMenu() {
        clearScreen();
        System.out.println("=== CMAS Dashboard Login ===");
        System.out.println("1. Doctor Login");
        System.out.println("2. Patient Login");
        System.out.println("3. Register New Doctor");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");

        int choice = getUserChoice();
        switch (choice) {
            case 1:
                handleDoctorLogin();
                break;
            case 2:
                handlePatientLogin();
                break;
            case 3:
                handleDoctorRegistration();
                break;
            case 0:
                System.out.println("Goodbye!");
                System.exit(0);
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void handleDoctorLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (doctorDAO.login(username, password)) {
            currentUserId = doctorDAO.getDoctorId(username);
            currentRole = "doctor";
            System.out.println("Doctor login successful!");
        } else {
            System.out.println("Invalid credentials. Please try again.");
            waitForEnter();
        }
    }

    private static void handlePatientLogin() {
        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine();

        if (patientDAO.validatePatient(patientId)) {
            currentUserId = patientId;
            currentRole = "patient";
            System.out.println("Patient login successful!");
        } else {
            System.out.println("Patient not found. Please try again.");
        }
        waitForEnter();
    }

    private static void handleDoctorRegistration() {
        System.out.println("\n=== Register New Doctor ===");
        String username;
        String password;
        String doctorId = java.util.UUID.randomUUID().toString(); // Generate ID upfront

        // Get and validate username
        while (true) {
            System.out.print("Enter desired username: ");
            username = scanner.nextLine().trim(); // Trim whitespace
            if (!username.isEmpty()) {
                break; // Exit loop if username is not empty
            } else {
                System.out.println("Username cannot be empty. Please try again.");
            }
        }

        // Get and validate password
        while (true) {
            System.out.print("Enter password: ");
            password = scanner.nextLine().trim(); // Trim whitespace
            if (!password.isEmpty()) {
                break; // Exit loop if password is not empty
            } else {
                System.out.println("Password cannot be empty. Please try again.");
            }
        }
        
        // Attempt registration
        if (doctorDAO.registerDoctor(doctorId, username, password)) {
            System.out.println("Doctor registered successfully with ID: " + doctorId);
        } else {
            // Error message (like username exists) is printed by DAO
            // System.out.println("Failed to register doctor."); // Redundant message
        }
        
        waitForEnter();
    }

    private static void displayDoctorDashboard() {
        clearScreen();
        System.out.println("=== Doctor Dashboard ===");
        System.out.println("1. View All Patients");
        System.out.println("2. Add New Patient");
        System.out.println("3. View CMAS Results");
        System.out.println("4. Add CMAS Result");
        System.out.println("5. View Lab Results");
        System.out.println("6. Add Lab Result");
        System.out.println("0. Logout");
        System.out.print("Enter your choice: ");

        int choice = getUserChoice();
        switch (choice) {
            case 1:
                handleViewAllPatients();
                break;
            case 2:
                handleAddPatient();
                break;
            case 3:
                handleViewCMASResults();
                break;
            case 4:
                handleAddCMASResult();
                break;
            case 5:
                handleViewLabResults();
                break;
            case 6:
                handleAddLabResult();
                break;
            case 0:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                waitForEnter();
        }
    }

    private static void displayPatientDashboard() {
        clearScreen();
        System.out.println("=== Patient Dashboard ===");
        System.out.println("1. View My CMAS Results");
        System.out.println("2. View My Lab Results");
        System.out.println("0. Logout");
        System.out.print("Enter your choice: ");

        int choice = getUserChoice();
        switch (choice) {
            case 1:
                handleViewMyCMASResults();
                break;
            case 2:
                handleViewMyLabResults();
                break;
            case 0:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void logout() {
        currentUserId = null;
        currentRole = null;
        System.out.println("Logged out successfully.");
        waitForEnter();
    }

    private static void handleAddPatient() {
        System.out.println("\n=== Add New Patient ===");
        String name;

        // Get, validate, and check uniqueness of patient name
        while (true) {
            System.out.print("Enter patient name: ");
            name = scanner.nextLine().trim(); // Trim whitespace
            
            // Check for empty name
            if (name.isEmpty()) {
                System.out.println("Patient name cannot be empty. Please try again.");
                continue; // Ask for input again
            }
            
            // Check if name already exists (case-insensitive)
            if (patientDAO.checkPatientNameExists(name)) {
                System.out.println("A patient with this name already exists. Please enter a different name.");
                continue; // Ask for input again - NOW we prevent adding duplicate name
            } else {
                 break; // Name is not empty and does not exist, exit loop
            }
        }
        
        // Add patient (DAO will print success/error)
        patientDAO.addPatient(name);
        
        waitForEnter();
    }

    private static void handleViewAllPatients() {
        clearScreen();
        System.out.println("=== All Patients ===");
        
        List<Patient> patients = patientDAO.getAllPatients();
        if (patients.isEmpty()) {
            System.out.println("No patients found.");
        } else {
            for (Patient patient : patients) {
                System.out.println(patient);
            }
        }
        waitForEnter();
    }

    private static void handleViewCMASResults() {
        System.out.println("\n=== View CMAS Results ===");
        System.out.println("1. View Latest Results");
        System.out.println("2. Search by Patient ID");
        System.out.print("Enter your choice: ");
        
        String choice = scanner.nextLine();
        
        if ("1".equals(choice)) {
            List<CMASResult> results = cmasResultDAO.getLatestCMASResults(10);
            displayCMASResults(results, "Latest CMAS Results");
        } else if ("2".equals(choice)) {
            System.out.print("Enter patient ID: ");
            String patientId = scanner.nextLine();
            List<CMASResult> results = cmasResultDAO.getCMASResults(patientId);
            displayCMASResults(results, "CMAS Results for Patient ID: " + patientId);
        } else {
            System.out.println("Invalid choice.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private static void displayCMASResults(List<CMASResult> results, String title) {
        if (results.isEmpty()) {
            System.out.println("No CMAS results found.");
            return;
        }

        System.out.println("\n" + title);
        for (CMASResult result : results) {
            System.out.printf("Patient ID: %s, Date: %s, Score: %d%n",
                result.getPatientId(),
                result.getTestDate(),
                result.getScore());
        }
    }

    private static void handleAddCMASResult() {
        System.out.println("\n=== Add New CMAS Result ===");
        
        System.out.print("Enter patient ID: ");
        String patientId = scanner.nextLine();
        
        System.out.print("Enter assessment date (YYYY-MM-DD): ");
        String testDate = scanner.nextLine();
        
        System.out.print("Enter total score (0-52): ");
        int score = Integer.parseInt(scanner.nextLine());
        
        System.out.println("\nSelect Score Type:");
        System.out.println("1. High (>10)");
        System.out.println("2. Medium (4-9)");
        System.out.print("Enter your choice: ");
        int scoreType = Integer.parseInt(scanner.nextLine());
        
        if (scoreType != 1 && scoreType != 2) {
            System.out.println("Invalid score type. Please enter 1 or 2.");
            return;
        }
        
        if (score < 0 || score > 52) {
            System.out.println("Invalid score. Please enter a value between 0 and 52.");
            return;
        }
        
        cmasResultDAO.addCMASResult(patientId, testDate, score, scoreType);
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void handleViewLabResults() {
        System.out.print("Enter patient ID: ");
        String patientId = scanner.nextLine();
        
        List<LabResult> results = labResultDAO.getLabResults(patientId);
        
        if (results.isEmpty()) {
            System.out.println("No lab results found for this patient.");
        } else {
            System.out.println("\nLab Results for Patient ID: " + patientId);
            System.out.println("----------------------------------------");
            for (LabResult result : results) {
                System.out.println("Test Date: " + result.getTestDate());
                System.out.println("Test Name: " + result.getTestName());
                System.out.println("Value: " + result.getValue() + " " + result.getUnit());
                System.out.println("Reference Range: " + result.getReferenceRange());
                System.out.println("----------------------------------------");
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void handleAddLabResult() {
        System.out.println("\n=== Add New Lab Result ===");
        
        // Get patient ID
        System.out.print("Enter patient ID: ");
        String patientId = scanner.nextLine();
        
        // Validate patient exists
        if (!patientDAO.validatePatient(patientId)) {
            System.out.println("Patient not found. Please check the ID and try again.");
            waitForEnter();
            return;
        }
        
        try {
            // Get test date
            System.out.print("Enter test date (YYYY-MM-DD): ");
            String testDate = scanner.nextLine();
            
            // Get test name
            System.out.print("Enter test name: ");
            String testName = scanner.nextLine();
            
            // Get test value
            System.out.print("Enter test value: ");
            String value = scanner.nextLine();
            
            // Get unit
            System.out.print("Enter unit (or press Enter to skip): ");
            String unit = scanner.nextLine();
            
            // Get reference range
            System.out.print("Enter reference range (or press Enter to skip): ");
            String referenceRange = scanner.nextLine();
            
            // Add the lab result
            if (labResultDAO.addLabResult(patientId, testDate, testName, value, unit, referenceRange)) {
                System.out.println("Lab result added successfully!");
            } else {
                System.out.println("Failed to add lab result. Please try again.");
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        waitForEnter();
    }

    private static void handleViewMyCMASResults() {
        List<CMASResult> results = cmasResultDAO.getCMASResults(currentUserId);
        
        if (results.isEmpty()) {
            System.out.println("\nNo CMAS results found.");
        } else {
            System.out.println("\nYour CMAS Results:");
            for (CMASResult result : results) {
                System.out.printf("Date: %s, Score: %d%n",
                    result.getTestDate(),
                    result.getScore());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void handleViewMyLabResults() {
        System.out.println("=== My Lab Results and Measurements ===\n");
        
        List<LabResult> results = labResultDAO.getLabResults(currentUserId);
        
        if (results.isEmpty()) {
            System.out.println("No laboratory results or measurements found for this patient.");
        } else {
            for (LabResult result : results) {
                System.out.println("\n" + result.toString());
                System.out.println("------------------------");
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void waitForEnter() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
} 