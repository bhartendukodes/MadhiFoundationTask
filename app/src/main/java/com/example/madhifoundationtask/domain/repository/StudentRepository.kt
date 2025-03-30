package com.example.madhifoundationtask.domain.repository

import com.example.madhifoundationtask.domain.model.Student
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for student authentication operations
 */
interface StudentRepository {
    /**
     * Authenticates a student with the provided roll number and QR code
     * @param rollNumber The student's roll number
     * @param qrCode The scanned QR code
     * @return Flow of Boolean indicating authentication status
     */
    suspend fun authenticateStudent(rollNumber: String, qrCode: String): Flow<Boolean>
    
    /**
     * Gets the current authenticated student, if any
     * @return Flow of Student or null if not authenticated
     */
    fun getCurrentStudent(): Flow<Student?>
} 