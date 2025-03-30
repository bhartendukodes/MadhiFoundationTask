package com.example.madhifoundationtask.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.madhifoundationtask.presentation.components.AuthenticatedComponent
import com.example.madhifoundationtask.presentation.components.CameraPermission
import com.example.madhifoundationtask.presentation.components.QRScannerViewComponent
import com.example.madhifoundationtask.presentation.components.RollNumberInputComponent
import com.example.madhifoundationtask.presentation.viewmodel.QRScannerViewModel
import kotlinx.coroutines.launch

/**
 * Main screen for QR code scanning, roll number input, and authentication
 */
@Composable
fun QRScannerScreen(
    viewModel: QRScannerViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Show error messages in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Show the appropriate screen based on the current state
            when {
                uiState.isAuthenticated -> {
                    // Show authenticated screen
                    AuthenticatedComponent(
                        student = uiState.rollNumber,
                        onLogout = { viewModel.logout() }
                    )
                }
                
                !uiState.isScanning && uiState.qrCodeValue != null -> {
                    // Show roll number input screen after QR is scanned
                    RollNumberInputComponent(
                        qrCode = uiState.qrCodeValue!!,
                        rollNumber = uiState.rollNumber,
                        isLoading = uiState.isLoading,
                        onRollNumberChanged = { viewModel.onRollNumberChanged(it) },
                        onSubmit = { viewModel.authenticateStudent() },
                        onReScan = { viewModel.resetScanning() }
                    )
                }
                
                else -> {
                    // Show QR scanner
                    CameraPermission {
                        QRScannerViewComponent(
                            onQRCodeScanned = { viewModel.onQRCodeScanned(it) },
                            isProcessingGalleryImage = uiState.isProcessingGalleryImage,
                            onGalleryImageSelected = { uri -> 
                                viewModel.processGalleryImage(context, uri)
                            }
                        )
                    }
                }
            }
        }
    }
} 