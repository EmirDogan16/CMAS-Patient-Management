# CMAS Patient Management System

A Java-based patient management system that handles CMAS (Childhood Myositis Assessment Scale) results and laboratory data.

## Features

- User Authentication (Doctors and Patients)
- CMAS Results Management (Add/View)
- Laboratory Results Management (Add/View)
- Patient Records Management (Add/View)
- Separate Dashboards for Doctors and Patients
- SQLite Database Backend

## Prerequisites

- Java JDK 11 or higher
- Apache Maven (for building and dependency management)
- An IDE like IntelliJ IDEA or Eclipse (optional, but recommended)

## Setup and Running

1.  **Clone the Repository:**
    ```bash
    git clone [repository-url]
    cd [project-directory]
    ```
2.  **Build with Maven:**
    ```bash
    mvn clean package
    ```
    This will compile the code and download the necessary dependencies (like the SQLite driver).
3.  **Run the Application:**
    You can run the application in two ways:
    *   **From IDE (Recommended):**
        - Import the project as a Maven project into your IDE (IntelliJ, Eclipse, etc.).
        - Locate the `src/main/java/com/patientx/Main.java` file.
        - Right-click and select "Run 'Main.main()'".
    *   **From Command Line:**
        After building with Maven, run the following command from the project's root directory:
        ```bash
        # Replace target/cmas-management-1.0-SNAPSHOT.jar with the actual JAR file name
        java -cp "target/cmas-management-1.0-SNAPSHOT.jar;target/dependency/*" com.patientx.Main 
        # Note: On Linux/macOS use : instead of ; in the classpath
        # java -cp "target/cmas-management-1.0-SNAPSHOT.jar:target/dependency/*" com.patientx.Main
        ```
        (Make sure Maven copied dependencies to `target/dependency`. If not, adjust the classpath or build configuration).

## Usage Guide

Upon running the application, you will see the main login menu.

### Initial Login / Registration

1.  **Register a Doctor:**
    *   Select option `3. Register New Doctor`.
    *   Enter a desired username (cannot be empty or already taken).
    *   Enter a password (cannot be empty).
    *   The system will confirm successful registration and provide a Doctor ID (UUID).
2.  **First Doctor Login:**
    *   Select option `1. Doctor Login`.
    *   Enter the username and password you just registered.

### Doctor Dashboard Actions

Once logged in as a doctor, you will see the Doctor Dashboard:

1.  **View All Patients (`1`):** Lists all patients currently registered in the system (from both `Patient` and `Patients` tables), showing their name and primary ID.
2.  **Add New Patient (`2`):**
    *   Prompts for the patient's name.
    *   The name cannot be empty.
    *   If a patient with the same name (case-insensitive) already exists, a warning is shown, but the patient is still added (a new unique ID will be generated).
    *   The new patient is added to the `Patients` table.
3.  **View CMAS Results (`3`):**
    *   Choose `1. View Latest Results` to see the most recent CMAS results across all patients (up to 10).
    *   Choose `2. Search by Patient ID` and enter a patient's UUID to see all their CMAS results.
4.  **Add CMAS Result (`4`):**
    *   Enter the Patient ID (UUID) for whom you are adding the result.
        *   **Important:** The patient *must* exist in the `Patient` table (due to database constraints). If the patient only exists in the `Patients` table, the system will automatically attempt to copy their record to the `Patient` table first.
    *   Enter the assessment date (YYYY-MM-DD format).
    *   Enter the total score (0-52).
    *   Select the score type (`1` for High >10, `2` for Medium 4-9).
    *   The system will save the result with an auto-generated integer ID and the descriptive score type ("Score > 10" or "Score 4-9").
5.  **View Lab Results (`5`):**
    *   Enter the Patient ID (UUID) to view their laboratory results.
6.  **Add Lab Result (`6`):**
    *   Enter the Patient ID (UUID).
    *   Enter test date, test name, value, unit (optional), and reference range (optional).
7.  **Logout (`0`):** Returns to the main login menu.

### Patient Dashboard Actions

1.  **Patient Login:**
    *   Select option `2. Patient Login` from the main menu.
    *   Enter your assigned Patient ID (UUID).
2.  **Patient Dashboard:**
    *   **View My CMAS Results (`1`):** Shows your own CMAS assessment results.
    *   **View My Lab Results (`2`):** Shows your own laboratory test results.
    *   **Logout (`0`):** Returns to the main login menu.

## Database Structure

The system uses an SQLite database (`PatientXdatabase.db`) with the following main tables:

-   `Patient`: Stores core patient information required for foreign keys (PatientID TEXT PRIMARY KEY, Name TEXT NOT NULL).
-   `Patients`: Stores additional or initially added patient details (PatientID TEXT PRIMARY KEY, Name TEXT, Age INTEGER, Gender TEXT).
-   `Doctors`: Stores doctor login credentials (DoctorID TEXT PRIMARY KEY, Username TEXT UNIQUE NOT NULL, Password TEXT NOT NULL).
-   `CMAS`: Stores CMAS results (ID INTEGER PRIMARY KEY AUTOINCREMENT, PatientID TEXT, TestDate TEXT, Score INTEGER, ScoreType TEXT, FOREIGN KEY (PatientID) REFERENCES Patient(PatientID)).
-   `LabResult`: Stores lab test header information (LabResultID TEXT PRIMARY KEY, PatientID TEXT, ResultName_English TEXT, Unit TEXT).
-   `Measurement`: Stores individual lab measurements linked to `LabResult` (ID TEXT PRIMARY KEY, LabResultID TEXT, DateTime TEXT, Value TEXT, FOREIGN KEY (LabResultID) REFERENCES LabResult(LabResultID)).

## Troubleshooting

-   **`[SQLITE_BUSY] database is locked`:** This error might occur if multiple parts of the application try to access the database simultaneously or if another program is holding the file open. The application now includes a `busy_timeout` setting and retries for some operations. If it persists, ensure no other SQLite tools are accessing `PatientXdatabase.db` while the application is running.
-   **`FOREIGN KEY constraint failed` when adding CMAS:** Ensure the Patient ID you entered exists in the `Patient` table. The system tries to copy from `Patients` automatically, but if the ID doesn't exist in either, the constraint will fail.
-   **JDBC Driver Issues:** Ensure Maven downloaded the `sqlite-jdbc` dependency correctly. Check your project's build path or dependencies.

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 