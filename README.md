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

## License

This project is licensed under the MIT License.
