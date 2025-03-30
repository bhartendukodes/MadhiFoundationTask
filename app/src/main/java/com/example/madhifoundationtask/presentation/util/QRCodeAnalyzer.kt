package com.example.madhifoundationtask.presentation.util

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Analyzer class for scanning QR codes from camera feed
 */
class QRCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit,
    private val onFrameProcessed: (hasQrCode: Boolean, boundingBox: Rect?) -> Unit
) : ImageAnalysis.Analyzer {
    
    private val scanner = BarcodeScanning.getClient()
    
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        
        val inputImage = InputImage.fromMediaImage(
            mediaImage, 
            imageProxy.imageInfo.rotationDegrees
        )
        
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    // Get the first detected QR code
                    val barcode = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                    
                    if (barcode != null) {
                        barcode.rawValue?.let { qrContent ->
                            onQrCodeScanned(qrContent)
                        }
                        
                        // Pass on bounding box for auto-zoom
                        barcode.boundingBox?.let { boundingBox ->
                            onFrameProcessed(true, boundingBox)
                        } ?: onFrameProcessed(true, null)
                    } else {
                        onFrameProcessed(false, null)
                    }
                } else {
                    onFrameProcessed(false, null)
                }
            }
            .addOnFailureListener {
                onFrameProcessed(false, null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
} 