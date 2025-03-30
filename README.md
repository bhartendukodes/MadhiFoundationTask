# QR Code Authentication System

## Overview

This mobile application provides a secure authentication system using QR code scanning and roll number verification. Developed for Madhi Foundation, this solution helps manage student attendance and authentication through a two-factor approach.

## Features

- **QR Code Scanning**: Scan QR codes using device camera
- **Gallery Import**: Upload QR code images from gallery
- **Two-Factor Authentication**: Requires both QR code and roll number
- **Secure Validation**: Validates QR code and roll number combinations against pre-defined data
- **User-Friendly Interface**: Modern Material Design UI with animations and clear feedback

## Authentication Flow

### Step 1: Generate QR Codes (Admin Process)
QR codes are generated with specific identifiers that are mapped to valid roll numbers in the system:
- QR123 → ROLL001, ROLL002
- QR456 → ROLL003, ROLL004

### Step 2: Scan QR Code
Users can scan a QR code through:
- Live camera scanning with auto-zoom capabilities
- Uploading images from the gallery

### Step 3: Enter Roll Number
After scanning, users are prompted to enter their roll number for verification.

### Step 4: Verification
The system checks if the scanned QR code and entered roll number form a valid combination:
- Example: QR123 + ROLL001 = Valid Authentication
- Example: QR123 + ROLL003 = Invalid Authentication

### Step 5: Authentication Result
- Success: User is logged in and welcomed
- Failure: Error message displayed, access denied

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **QR Scanning**: Google ML Kit for Barcode Scanning
- **Camera**: CameraX API
- **Dependency Injection**: Hilt
- **Asynchronous Operations**: Kotlin Coroutines and Flow

## Security Features

- Two-factor verification process
- No sensitive information stored in QR codes
- Input validation to prevent injection attacks
- Session management with secure logout

## Demonstration

Here's a demonstration of the authentication process:

1. **QR Code Generation**
   - Generate QR codes with values "QR123" and "QR456"
   - These QR codes represent unique identifiers in our system

2. **Valid Authentication Demo**
   - Scan "QR123" QR code
   - Enter "ROLL001" (valid combination)
   - System authenticates successfully
   - Welcome screen appears with user information

3. **Invalid Authentication Demo**
   - Scan "QR123" QR code
   - Enter "ROLL003" (invalid combination for this QR code)
   - System denies authentication
   - Error message displayed: "Authentication failed. Please try again."

4. **Alternative QR Code Demo**
   - Scan "QR456" QR code
   - Enter "ROLL003" (valid combination)
   - System authenticates successfully

5. **Gallery Import Feature**
   - Select QR code image from gallery
   - Enter corresponding roll number
   - Authentication proceeds as with camera scanning

## Implementation Benefits

- **Enhanced Security**: Two-factor authentication provides stronger security than single-factor methods
- **User Experience**: Simple and intuitive interface with clear feedback
- **Flexibility**: Multiple methods for QR code scanning accommodate different user needs
- **Reliability**: Robust error handling and validation ensures system integrity

## Future Enhancements

- Cloud synchronization for real-time validation
- Analytics for tracking authentication patterns
- Offline authentication capabilities
- Biometric authentication as an additional factor

---

Developed by [Your Company Name] for Madhi Foundation 