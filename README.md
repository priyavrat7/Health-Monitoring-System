# Health Monitoring System

An IoT-based Android application for real-time baby health monitoring that integrates embedded sensors, Bluetooth Low Energy (BLE) communication, and cloud services to provide comprehensive health tracking for infants.

## 📱 Overview

The Health Monitoring System is a complete IoT solution designed to help parents and healthcare professionals monitor infant health in real-time. The system tracks temperature and movement data through embedded sensors, transmits it wirelessly via BLE to an Android application, and stores it securely in the cloud for remote access and analysis.

## ✨ Features

### Core Functionality
- **Real-time Monitoring**: Continuous tracking of baby's temperature and acceleration data
- **Bluetooth Low Energy (BLE) Integration**: Wireless communication with embedded sensor board
- **Cloud Synchronization**: Automatic data upload to Firebase Firestore with configurable intervals
- **Smart Alerts**: Automatic notifications for fever detection (≥38°C) and fall detection
- **Data Visualization**: Interactive line charts for temperature trends over time
- **Automated Data Management**: Configurable upload periods and automatic cleanup of old records

### User Management
- **Multi-role System**: Support for Parents, Doctors, and Admin users
- **Role-based Access Control**: Different permissions and interfaces for each user type
- **Secure Authentication**: Firebase Authentication with email verification
- **Doctor Approval System**: Admin-controlled doctor registration with identity verification

### Medical Records
- **Doctor Notes**: Healthcare professionals can add, edit, and share medical notes
- **Search Functionality**: Doctors can search patients by name, email, or phone number
- **Medical History**: Comprehensive tracking of temperature readings and medical notes
- **Parent-Doctor Communication**: Seamless sharing of health data and medical records

## 🛠️ Technology Stack

### Mobile Application
- **Language**: Java
- **Platform**: Android (minSdk 24, targetSdk 33)
- **Build System**: Gradle (Kotlin DSL)
- **Architecture**: Service-based architecture with background data processing

### Cloud Services
- **Firebase Authentication**: User authentication and email verification
- **Firebase Firestore**: NoSQL database for real-time data storage
- **Firebase UI**: Pre-built UI components for Firestore integration

### Communication
- **Bluetooth Low Energy (BLE)**: Wireless sensor data transmission
- **Embedded Hardware**: EFR32BG22 Thunderboard with:
  - Si7021 relative humidity and temperature sensor
  - ICM-20648 6-axis inertial measurement unit (IMU)

### Libraries & Dependencies
- `com.github.PhilJay:MPAndroidChart` - Data visualization charts
- `com.opencsv:opencsv` - CSV data export
- `com.firebaseui:firebase-ui-firestore` - Firebase UI components
- `com.sun.mail:android-mail` - Email functionality

## 🏗️ Architecture

### System Components

```
┌─────────────────┐         ┌──────────────────┐         ┌──────────────┐
│  Embedded Board │  BLE    │  Android App     │  HTTP    │   Firebase   │
│  (EFR32BG22)    │◄───────►│  (Java/Android) │◄────────►│   Firestore  │
│                 │         │                  │          │              │
│  - Temperature  │         │  - BLE Service   │          │  - Users     │
│  - IMU Sensor   │         │  - Data Process │          │  - Babies    │
│                 │         │  - UI Activities │          │  - Temps     │
└─────────────────┘         └──────────────────┘          │  - Notes     │
                                                           └──────────────┘
```

### Key Services

1. **BLEDataService**: Handles BLE communication with embedded sensors
   - GATT service discovery and characteristic management
   - Real-time data reception via notifications/indications
   - Data parsing and broadcasting to other components

2. **DataProcessService**: Processes and manages sensor data
   - Temperature aggregation and mean calculation
   - Fall detection algorithm (acceleration magnitude analysis)
   - Automated cloud upload with configurable intervals
   - Data retention policy enforcement

3. **Firebase Integration**: Cloud backend services
   - User authentication and authorization
   - Real-time database synchronization
   - Role-based data access control

### Database Schema

```
Firestore
├── users/
│   └── {userId}/
│       ├── email
│       ├── userType (Admin/Doctor/Parent)
│       └── approvalStatus
│
├── babies/
│   └── {babyId}/
│       ├── name
│       ├── dateOfBirth
│       ├── familyInfo
│       ├── temps/ (subcollection)
│       │   └── {timestamp}/
│       │       ├── current_temp
│       │       ├── mean_temp
│       │       └── timestamp
│       └── notes/ (subcollection)
│           └── {noteId}/
│               ├── content
│               ├── doctorId
│               └── timestamp
```

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest version recommended)
- Android SDK (API level 24 or higher)
- Firebase project with Firestore and Authentication enabled
- EFR32BG22 Thunderboard or compatible BLE device
- Physical Android device with BLE support (for testing)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/priyavrat7/Health-Monitoring-System.git
   cd Health-Monitoring-System
   ```

2. **Set up Firebase**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Firestore Database and Authentication (Email/Password)
   - Download `google-services.json` and place it in `app/` directory
   - Configure Firestore security rules for your use case

3. **Configure the Project**
   - Open the project in Android Studio
   - Sync Gradle files
   - Update `applicationId` in `app/build.gradle.kts` if needed

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use Android Studio's Run button to install on a connected device

### Firebase Setup

1. **Authentication Rules**: Configure email/password authentication
2. **Firestore Rules**: Set up appropriate security rules:
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Add your security rules here
     }
   }
   ```

## 📖 Usage

### For Parents

1. **Registration**: Create an account with email and password
2. **Baby Profile**: Add your baby's information (name, DOB, contact info)
3. **Connect Device**: Scan and connect to the BLE sensor board
4. **Configure Settings**: Set upload interval and data retention period
5. **Monitor**: View real-time temperature and acceleration data
6. **View Charts**: Check temperature trends over time
7. **Receive Alerts**: Get notified for fever or fall detection

### For Doctors

1. **Registration**: Sign up with professional credentials
2. **Admin Approval**: Wait for admin to approve your account
3. **Search Patients**: Find babies by name, email, or phone
4. **View Data**: Access temperature charts and historical data
5. **Add Notes**: Create, edit, and share medical notes with parents

### For Admins

1. **Approve Doctors**: Review and approve doctor registration requests
2. **Manage Users**: Oversee user accounts and access

## 🔧 Configuration

### Data Upload Settings

- **Upload Period**: Configure how often temperature data is uploaded to cloud (default: 1 second)
- **Data Retention**: Set how long to keep temperature records (default: 30 seconds)
- These can be adjusted in the app's settings interface

### Alert Thresholds

- **Fever Threshold**: 38°C (configurable in `DataProcessService.java`)
- **Fall Detection**: Acceleration magnitude threshold (default: 70, configurable)

## 📁 Project Structure

```
ChildHealthCareProject/
├── app/
│   ├── build.gradle.kts          # App-level build configuration
│   ├── google-services.json      # Firebase configuration
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/childhealthcareproject/
│       │   ├── BLEDataService.java          # BLE communication service
│       │   ├── DataProcessService.java      # Data processing & cloud sync
│       │   ├── ParentActivity.java          # Parent user interface
│       │   ├── DoctorActivity.java          # Doctor user interface
│       │   ├── LoginActivity.java           # Authentication
│       │   ├── RegisterActivity.java        # User registration
│       │   └── [Other activity classes]
│       └── res/                             # Resources (layouts, drawables, etc.)
├── build.gradle.kts              # Project-level build configuration
├── settings.gradle.kts           # Project settings
└── gradle/                        # Gradle wrapper files
```

## 🔐 Security Considerations

- All user authentication is handled through Firebase Authentication
- Firestore security rules should be configured to enforce role-based access
- BLE communication uses standard GATT security
- Sensitive data is stored securely in Firebase Firestore

## 🧪 Testing

The project includes basic test structure:
- Unit tests: `app/src/test/java/`
- Instrumented tests: `app/src/androidTest/java/`

To run tests:
```bash
./gradlew test              # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests
```

## 📝 Key Algorithms

### Fall Detection
```java
float totalMag = Math.sqrt(acc[0]² + acc[1]² + acc[2]²);
if (totalMag >= threshold) {
    // Trigger fall notification
}
```

### Temperature Aggregation
- Collects temperature readings over a configurable period
- Calculates mean temperature for the period
- Uploads aggregated data to reduce cloud storage usage

### Data Retention
- Automatically queries and deletes records older than retention period
- Uses Firestore batch operations for efficient deletion

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## 📄 License

This project is part of academic coursework. Please refer to the license file for details.

## 👥 Authors

- **Priyavrat Dev Sharma** - *Initial work* - [GitHub](https://github.com/priyavrat7)
- **Han Cat Nguyen** - *Co-developer*

## 🙏 Acknowledgments

- McGill University, Department of Electrical and Computer Engineering
- Firebase team for excellent documentation and services
- Silicon Labs for EFR32BG22 development board support

## 📚 Related Documentation

- [Project Report](./IoT_HealthCare_Monitoring_Application.md) - Detailed technical documentation
- [Firebase Documentation](https://firebase.google.com/docs)
- [Android BLE Guide](https://developer.android.com/guide/topics/connectivity/bluetooth/ble-overview)

## 🔗 Links

- **Repository**: [https://github.com/priyavrat7/Health-Monitoring-System](https://github.com/priyavrat7/Health-Monitoring-System)
- **Issues**: [Report a bug or request a feature](https://github.com/priyavrat7/Health-Monitoring-System/issues)

---

**Note**: This application is designed for educational and research purposes. For medical use, ensure compliance with healthcare regulations and consult with healthcare professionals.

