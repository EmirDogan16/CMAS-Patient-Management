# CMAS Patient Management System

A Java-based patient management system that handles CMAS (Childhood Myositis Assessment Scale) results and laboratory data.

## Version
Current Version: 17.1
- All system messages and comments converted to English
- Improved database path handling
- Enhanced error messages and logging
- Added backup and recovery features

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

### Method 1: Using Git Clone

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

### Method 2: Using ZIP Download

1. Download the ZIP file from GitHub
2. Extract the ZIP file
3. **IMPORTANT:** When launching the application, you must drag and drop the `Main.java` file from INSIDE the extracted folder. Do not drag the folder itself.
   - ✅ Correct: Drag `extracted-folder/src/main/java/.../Main.java`
   - ❌ Incorrect: Drag the entire extracted folder
   
   If you don't follow this step, the application will create a new empty database because it cannot locate the correct path to the existing database.

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
