package com.example.madhifoundationtask.presentation.util

import android.graphics.Rect
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

/**
 * Utility class for camera operations including auto-zoom functionality
 */
class CameraUtil(
    private val lifecycleOwner: LifecycleOwner,
    private val cameraExecutor: Executor
) {
    
    private var camera: Camera? = null
    private var lastZoomRatio = 1.0f
    private var isZooming = false
    
    /**
     * Sets up the camera with preview and image analysis
     */
    fun setupCamera(
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
        imageAnalysis: androidx.camera.core.ImageAnalysis,
        surfaceProvider: Preview.SurfaceProvider
    ): Camera? {
        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()
            
            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            // Set up the preview use case
            preview.setSurfaceProvider(surfaceProvider)
            
            // Bind use cases to camera
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            
            return camera
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Handles auto-zoom based on QR code detection
     */
    fun handleAutoZoom(hasQrCode: Boolean, boundingBox: Rect?) {
        if (isZooming) return
        
        val cam = camera ?: return
        
        if (hasQrCode && boundingBox != null) {
            // Calculate desired zoom ratio based on QR code position and size
            val centerX = boundingBox.centerX().toFloat()
            val centerY = boundingBox.centerY().toFloat()
            
            // Auto-focus on the QR code
            autoFocus(centerX, centerY)
            
            // Calculate zoom ratio based on QR code size
            val qrCodeSize = min(boundingBox.width(), boundingBox.height())
            val screenSize = max(cam.cameraInfo.sensorRotationDegrees, 1080) // Approximation
            
            // If QR code is too small on screen, zoom in
            val ratio = screenSize / max(qrCodeSize, 1)
            val desiredZoom = min(max(ratio * 0.2f, 1.0f), cam.cameraInfo.zoomState.value?.maxZoomRatio ?: 2.0f)
            
            // Only zoom if there's a significant change to avoid flickering
            if (Math.abs(desiredZoom - lastZoomRatio) > 0.1f) {
                isZooming = true
                cam.cameraControl.setZoomRatio(desiredZoom)
                    .addListener({
                        lastZoomRatio = desiredZoom
                        isZooming = false
                    }, ContextCompat.getMainExecutor(lifecycleOwner as android.content.Context))
            }
        } else if (!hasQrCode) {
            // If no QR code is detected and we're zoomed in, gradually zoom out
            if (lastZoomRatio > 1.1f) {
                isZooming = true
                val newZoom = max(lastZoomRatio * 0.95f, 1.0f)
                cam.cameraControl.setZoomRatio(newZoom)
                    .addListener({
                        lastZoomRatio = newZoom
                        isZooming = false
                    }, ContextCompat.getMainExecutor(lifecycleOwner as android.content.Context))
            }
        }
    }
    
    /**
     * Auto-focus on a specific point in the camera view
     */
    private fun autoFocus(x: Float, y: Float) {
        val cam = camera ?: return
        
        // Create a metering point factory
        val factory = SurfaceOrientedMeteringPointFactory(1.0f, 1.0f)
        val point = factory.createPoint(x, y)
        
        // Create a focus action with the metering point
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()
        
        // Start focusing
        cam.cameraControl.startFocusAndMetering(action)
    }
} 