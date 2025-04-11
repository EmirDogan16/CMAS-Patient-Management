# CMAS Patient Management System

A Java-based patient management system that handles CMAS (Childhood Myositis Assessment Scale) results and laboratory data.

## Features

- User Authentication (Doctors and Patients)
- CMAS Results Management (Add/View)
- Laboratory Results Management (Add/View)
- Patient Records Management (Add/View)
- Separate Dashboards for Doctors and Patients
- SQLite Database Backend
- Automatic Database Backup and Recovery

## Prerequisites

- Java JDK 11 or higher
- Apache Maven (for building and dependency management)
- An IDE like IntelliJ IDEA or Eclipse (optional, but recommended)

## Setup and Running

1. Clone the Repository:
```bash
git clone https://github.com/EmirDogan16/CMAS-Patient-Management.git
cd CMAS-Patient-Management
```

2. Build with Maven:
```bash
mvn clean package
```

3. Run the Application:
```bash
java -cp "target/cmas-management-1.0-SNAPSHOT.jar;target/dependency/*" com.patientx.Main
```

## Database Protection

The system automatically creates a backup of the database (`PatientXdatabase.backup.db`) when first run. If the main database file is deleted or corrupted, the system will automatically restore it from the backup. This ensures your data is protected even if you extract the project to a new location.

**Important:** Do not delete both the main database file (`PatientXdatabase.db`) and the backup file (`PatientXdatabase.backup.db`) at the same time, as this will result in data loss.

## License

This project is licensed under the MIT License.
