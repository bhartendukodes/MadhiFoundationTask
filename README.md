# QR Scanner App - Documentation

## Application Overview

The QR Scanner App is an Android application built with Jetpack Compose that enables students to authenticate by scanning a QR code and entering their roll number. The application follows a clean architecture pattern with a clear separation of concerns between data, domain, and presentation layers. It uses Dagger Hilt for dependency injection and follows MVVM architectural pattern.

## Package Structure

```
com.example.madhifoundationtask/
├── QRScannerApp.kt           # Application class with Hilt initialization
├── MainActivity.kt           # Main activity of the application
├── data/                     # Data layer
│   └── repository/           # Repository implementations
├── di/                       # Dependency Injection modules
├── domain/                   # Domain layer
│   ├── model/                # Domain models
│   ├── repository/           # Repository interfaces
│   └── usecase/              # Use cases for business logic
├── presentation/             # Presentation layer
│   ├── components/           # Reusable UI components
│   ├── screens/              # Full screens of the app
│   ├── util/                 # Utility classes for the presentation layer
│   └── viewmodel/            # ViewModels for screens
└── ui/                       # UI theme and styling
    └── theme/                # Application theme definitions
```

## Core Components

### 1. Application Layer

- **QRScannerApp.kt**: Application class with Hilt annotation for dependency injection setup.
- **MainActivity.kt**: Main activity that sets up the Compose content and hosts the QR scanner screen.

### 2. Domain Layer

- **Student.kt**: Domain model representing a student with roll number, QR code, and authentication status.
- **StudentRepository.kt**: Interface defining operations for student authentication.
- **AuthenticateStudentUseCase.kt**: Use case for authenticating a student with roll number and QR code.
- **GetCurrentStudentUseCase.kt**: Use case for retrieving the current authenticated student.

### 3. Data Layer

- **StudentRepositoryImpl.kt**: Implementation of the StudentRepository interface that stores student data in memory and simulates authentication with predefined valid QR codes and roll numbers.

### 4. Presentation Layer

#### ViewModels

- **QRScannerViewModel.kt**: Manages the state and business logic for the QR scanning screen, including:
  - QR code scanning
  - Roll number input validation
  - Student authentication
  - Image processing from gallery

#### Screens and Components

- **QRScannerScreen.kt**: Main screen that orchestrates the flow between scanning, input, and authenticated states.
- **QRScannerViewComponent.kt**: Component for capturing and processing QR codes via the camera.
- **RollNumberInputComponent.kt**: Form for entering roll number after QR code is scanned.
- **AuthenticatedComponent.kt**: Screen shown after successful authentication.
- **CameraPermission.kt**: Component handling camera permission requests.

#### Utilities

- **GalleryImageScanner.kt**: Utility for scanning QR codes from gallery images.
- **CameraUtil.kt**: Utility functions for camera setup and operations.
- **QRCodeAnalyzer.kt**: Image analyzer for detecting and decoding QR codes from camera frames.

### 5. Dependency Injection

- **RepositoryModule.kt**: Hilt module for providing repository implementations.

## Application Flow

1. **QR Code Scanning**: 
   - The app starts with a camera view to scan QR codes.
   - Users can scan QR codes directly or select an image from gallery.
   - Once a QR code is detected, the app transitions to the roll number input screen.

2. **Roll Number Input**:
   - User enters their roll number.
   - The application validates the roll number against the scanned QR code.

3. **Authentication**:
   - If the roll number is valid for the scanned QR code, the student is authenticated.
   - Upon successful authentication, the authenticated screen is shown.
   - Users can log out to return to the scanning screen.

## Technical Implementation Details

1. **Clean Architecture**:
   - Clear separation between data, domain, and presentation layers.
   - Domain layer contains business logic independent of Android framework.
   - Repository pattern for data operations.

2. **MVVM Architecture**:
   - ViewModels manage UI state and business logic.
   - UI components observe state changes through StateFlow.
   - Unidirectional data flow from ViewModel to UI.

3. **Dependency Injection**:
   - Dagger Hilt for dependency management.
   - Singleton scope for repositories.

4. **Coroutines and Flow**:
   - Kotlin coroutines for asynchronous operations.
   - Flow for reactive data streams.

5. **Jetpack Compose**:
   - Declarative UI built with Jetpack Compose.
   - Material 3 design components.
   - Edge-to-edge UI.

6. **Camera and QR Scanning**:
   - CameraX for camera implementation.
   - ML Kit or ZXing for QR code detection and decoding.
   - Permission handling for camera access.

## Class Documentation

### Domain Layer

#### Student (Domain Model)
```kotlin
data class Student(
    val rollNumber: String,
    val qrCode: String,
    val isAuthenticated: Boolean = false
)
```
- **Purpose**: Represents a student entity with authentication state
- **Properties**:
  - `rollNumber`: Unique identifier for the student
  - `qrCode`: The QR code associated with the student
  - `isAuthenticated`: Flag indicating if the student is authenticated

#### StudentRepository (Interface)
```kotlin
interface StudentRepository {
    suspend fun authenticateStudent(rollNumber: String, qrCode: String): Flow<Boolean>
    fun getCurrentStudent(): Flow<Student?>
}
```
- **Purpose**: Defines operations for student authentication
- **Methods**:
  - `authenticateStudent`: Validates a student's roll number against a QR code
  - `getCurrentStudent`: Retrieves the currently authenticated student

#### AuthenticateStudentUseCase
```kotlin
class AuthenticateStudentUseCase @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(rollNumber: String, qrCode: String): Flow<Boolean>
}
```
- **Purpose**: Encapsulates the authentication logic
- **Usage**: Called from ViewModel to authenticate a student

#### GetCurrentStudentUseCase
```kotlin
class GetCurrentStudentUseCase @Inject constructor(
    private val repository: StudentRepository
) {
    operator fun invoke(): Flow<Student?>
}
```
- **Purpose**: Provides access to the current authenticated student
- **Usage**: Used by ViewModel to observe authentication state

### Data Layer

#### StudentRepositoryImpl
```kotlin
@Singleton
class StudentRepositoryImpl @Inject constructor() : StudentRepository {
    // In-memory storage
    private val currentStudent = MutableStateFlow<Student?>(null)
    
    // Simulated database
    private val validStudents = mapOf(
        "QR123" to listOf("ROLL001", "ROLL002"),
        "QR456" to listOf("ROLL003", "ROLL004")
    )
    
    override suspend fun authenticateStudent(rollNumber: String, qrCode: String): Flow<Boolean>
    override fun getCurrentStudent(): Flow<Student?>
}
```
- **Purpose**: Implements the StudentRepository interface
- **Features**:
  - Simulated authentication with predefined QR codes and roll numbers
  - In-memory storage of the current authenticated student

### Presentation Layer

#### QRScannerViewModel
```kotlin
@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val authenticateStudentUseCase: AuthenticateStudentUseCase,
    private val getCurrentStudentUseCase: GetCurrentStudentUseCase
) : ViewModel() {
    // UI state for QR scanning screen
    data class QRScannerState(...)
    
    private val _uiState = MutableStateFlow(QRScannerState())
    val uiState: StateFlow<QRScannerState> = _uiState.asStateFlow()
    
    // Methods for UI interactions
    fun onQRCodeScanned(qrCodeValue: String)
    fun onRollNumberChanged(rollNumber: String)
    fun processGalleryImage(context: Context, imageUri: Uri)
    fun authenticateStudent()
    fun resetScanning()
    fun logout()
}
```
- **Purpose**: Manages UI state and business logic for QR scanning
- **Key Features**:
  - Processes QR codes from camera or gallery
  - Handles student authentication
  - Manages UI state transitions

#### QRScannerScreen
```kotlin
@Composable
fun QRScannerScreen(
    viewModel: QRScannerViewModel
) {
    // UI implementation
}
```
- **Purpose**: Main screen of the application
- **Features**:
  - Displays different UI based on authentication state
  - Handles user interactions

## Implementation Notes

The current implementation uses an in-memory repository with hardcoded student data. In a production application, this would be replaced with a real database or API connection.

## Future Enhancements

1. **Backend Integration**: 
   - Replace the simulated authentication with a real API
   - Add secure token-based authentication

2. **Data Persistence**:
   - Add Room database for local storage
   - Implement offline capability

3. **Additional Features**:
   - Student profile management
   - Attendance tracking
   - QR code generation for administrators 