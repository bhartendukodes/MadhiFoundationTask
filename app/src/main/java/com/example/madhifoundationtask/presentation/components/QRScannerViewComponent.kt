package com.example.madhifoundationtask.presentation.components

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.madhifoundationtask.presentation.util.CameraUtil
import com.example.madhifoundationtask.presentation.util.QRCodeAnalyzer
import java.util.concurrent.Executors

/**
 * Component for QR code scanning with camera preview and auto-zoom
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun QRScannerViewComponent(
    onQRCodeScanned: (String) -> Unit,
    isProcessingGalleryImage: Boolean = false,
    onGalleryImageSelected: (android.net.Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var hasDetectedCode by remember { mutableStateOf(false) }
    
    // Image picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onGalleryImageSelected(it) }
    }
    
    val cameraUtil = remember {
        CameraUtil(lifecycleOwner, cameraExecutor)
    }
    
    // Preview view for camera
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // Scanner UI
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        // Scanner overlay components
        ScannerOverlay(
            isProcessing = isProcessingGalleryImage,
            onGalleryClick = { galleryLauncher.launch("image/*") }
        )
    }
    
    // Camera setup
    LaunchedEffect(key1 = previewView) {
        setupCamera(
            context = context,
            previewView = previewView,
            lifecycleOwner = lifecycleOwner,
            cameraExecutor = cameraExecutor,
            cameraUtil = cameraUtil,
            onQRCodeScanned = { qrCode ->
                if (!hasDetectedCode) {
                    hasDetectedCode = true
                    onQRCodeScanned(qrCode)
                }
            },
            onFrameProcessed = { hasQrCode, boundingBox ->
                cameraUtil.handleAutoZoom(hasQrCode, boundingBox)
            }
        )
    }
}

/**
 * Enhanced scanner overlay with frame, instructions and gallery button
 */
@Composable
private fun ScannerOverlay(
    isProcessing: Boolean = false,
    onGalleryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Animated scanner frame
        AnimatedScannerFrame(isProcessing)
        
        // Instructions card at the top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Position the QR code inside the frame",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        // Add gallery button at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scanning indicator
                Text(
                    text = if (isProcessing) "Processing..." else "Scanning...",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Gallery button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { onGalleryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Pick from Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Gallery",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Animated scanner frame with pulsing effect
 */
@Composable
private fun AnimatedScannerFrame(isProcessing: Boolean) {
    // Animated scale for pulse effect
    val animatedScale by animateFloatAsState(
        targetValue = if (isProcessing) 1f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ),
        label = "scanner-animation"
    )
    
    Box(
        modifier = Modifier
            .size(250.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .border(
                width = 3.dp,
                color = if (isProcessing) MaterialTheme.colorScheme.primary else Color.White,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Show loading indicator when processing
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
        }
    }
}

/**
 * Sets up the camera with preview and QR code analysis
 */
private fun setupCamera(
    context: android.content.Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraExecutor: java.util.concurrent.Executor,
    cameraUtil: CameraUtil,
    onQRCodeScanned: (String) -> Unit,
    onFrameProcessed: (Boolean, Rect?) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    
    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            
            // Setup image analysis for QR code scanning
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            
            // Set up QR code analyzer
            val qrCodeAnalyzer = QRCodeAnalyzer(
                onQrCodeScanned = onQRCodeScanned,
                onFrameProcessed = onFrameProcessed
            )
            
            imageAnalysis.setAnalyzer(cameraExecutor, qrCodeAnalyzer)
            
            // Setup camera preview
            val preview = Preview.Builder().build()
            
            // Setup camera with preview and analysis
            cameraUtil.setupCamera(
                cameraProvider = cameraProvider,
                preview = preview,
                imageAnalysis = imageAnalysis,
                surfaceProvider = previewView.surfaceProvider
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
} 