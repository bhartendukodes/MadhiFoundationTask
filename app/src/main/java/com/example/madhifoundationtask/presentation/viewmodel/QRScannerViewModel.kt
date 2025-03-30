package com.example.madhifoundationtask.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madhifoundationtask.domain.model.Student
import com.example.madhifoundationtask.domain.usecase.AuthenticateStudentUseCase
import com.example.madhifoundationtask.domain.usecase.GetCurrentStudentUseCase
import com.example.madhifoundationtask.presentation.util.GalleryImageScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val authenticateStudentUseCase: AuthenticateStudentUseCase,
    private val getCurrentStudentUseCase: GetCurrentStudentUseCase
) : ViewModel() {

    // UI state for QR scanning screen
    data class QRScannerState(
        val isScanning: Boolean = true,
        val qrCodeValue: String? = null,
        val rollNumber: String = "",
        val isAuthenticated: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isProcessingGalleryImage: Boolean = false,
        val selectedImageUri: Uri? = null
    )
    
    private val _uiState = MutableStateFlow(QRScannerState())
    val uiState: StateFlow<QRScannerState> = _uiState.asStateFlow()
    
    init {
        // Observe current student
        viewModelScope.launch {
            getCurrentStudentUseCase().collectLatest { student ->
                student?.let {
                    _uiState.update { state ->
                        state.copy(
                            isAuthenticated = it.isAuthenticated,
                            rollNumber = it.rollNumber,
                            qrCodeValue = it.qrCode
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Called when a QR code is successfully scanned
     */
    fun onQRCodeScanned(qrCodeValue: String) {
        _uiState.update { state ->
            // Only update if we're in scanning mode and don't already have this QR code
            if (state.isScanning && state.qrCodeValue != qrCodeValue) {
                state.copy(
                    qrCodeValue = qrCodeValue,
                    isScanning = false,
                    errorMessage = null
                )
            } else {
                state
            }
        }
    }
    
    /**
     * Updates the roll number field
     */
    fun onRollNumberChanged(rollNumber: String) {
        _uiState.update { it.copy(rollNumber = rollNumber, errorMessage = null) }
    }
    
    /**
     * Processes a QR code from a gallery image
     */
    fun processGalleryImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isProcessingGalleryImage = true,
                selectedImageUri = imageUri,
                errorMessage = null
            ) }
            
            try {
                val qrContent = GalleryImageScanner.scanQRFromImage(context, imageUri)
                
                if (qrContent != null) {
                    _uiState.update { it.copy(
                        qrCodeValue = qrContent,
                        isScanning = false,
                        isProcessingGalleryImage = false
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isProcessingGalleryImage = false,
                        errorMessage = "No QR code found in the selected image"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isProcessingGalleryImage = false,
                    errorMessage = "Error processing image: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Authenticates a student with provided QR code and roll number
     */
    fun authenticateStudent() {
        val currentState = _uiState.value
        val qrCode = currentState.qrCodeValue
        val rollNumber = currentState.rollNumber
        
        if (qrCode.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Please scan a QR code first") }
            return
        }
        
        if (rollNumber.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your roll number") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                authenticateStudentUseCase(rollNumber, qrCode).collectLatest { success ->
                    _uiState.update { 
                        it.copy(
                            isAuthenticated = success,
                            isLoading = false,
                            errorMessage = if (!success) "Authentication failed. Please try again." else null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "An error occurred: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Resets the scanning process
     */
    fun resetScanning() {
        _uiState.update { 
            it.copy(
                isScanning = true,
                qrCodeValue = null,
                selectedImageUri = null,
                errorMessage = null
            )
        }
    }
    
    /**
     * Logs out the current user
     */
    fun logout() {
        _uiState.update {
            QRScannerState()
        }
    }
} 