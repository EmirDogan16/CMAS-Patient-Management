# CMAS Patient Management System

A Java-based patient management system that handles CMAS (Childhood Myositis Assessment Scale) results and laboratory data.

## Features

- User Authentication (Doctors and Patients)
- CMAS Results Management (Add/View)
- Laboratory Results Management (Add/View)
- Patient Records Management (Add/View)
- Separate Dashboards for Doctors and Patients
- SQLite Database Backend with Auto-Recovery

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

## Database Management

The system includes a pre-configured SQLite database with initial data. When you run the application:

1. If no database exists in the current directory, the system automatically creates one from the embedded template
2. All your data modifications are saved to this local database
3. If you move or delete the database file, a fresh copy will be created from the template
4. Each installation starts with the same initial data, ensuring consistency

This approach ensures that:
- You always start with a working database
- Your data is preserved between runs
- Moving or extracting the project to a new location works seamlessly

## License

This project is licensed under the MIT License.
