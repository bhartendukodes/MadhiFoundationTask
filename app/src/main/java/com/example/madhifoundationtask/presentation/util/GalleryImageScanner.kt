package com.example.madhifoundationtask.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Utility class to handle scanning QR codes from gallery images
 */
object GalleryImageScanner {
    
    /**
     * Scans a QR code from the given image URI
     * @param context Application context
     * @param imageUri URI of the image to scan
     * @return QR code content or null if not found
     */
    suspend fun scanQRFromImage(context: Context, imageUri: Uri): String? {
        // Convert URI to bitmap
        val bitmap = getBitmapFromUri(context, imageUri) ?: return null
        
        // Create an input image for ML Kit
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        
        // Use ML Kit to scan the image
        return withTimeoutOrNull(5000) { // 5 second timeout
            suspendCancellableCoroutine { continuation ->
                BarcodeScanning.getClient().process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        // Find the first QR code
                        val qrCode = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                        val result = qrCode?.rawValue
                        
                        if (continuation.isActive) {
                            continuation.resume(result)
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
            }
        }
    }
    
    /**
     * Converts a URI to a bitmap
     */
    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
} 