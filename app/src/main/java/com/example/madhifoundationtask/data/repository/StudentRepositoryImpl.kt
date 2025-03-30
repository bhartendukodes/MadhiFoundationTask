package com.example.madhifoundationtask.data.repository

import com.example.madhifoundationtask.domain.model.Student
import com.example.madhifoundationtask.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of StudentRepository
 * In a real app, this would connect to a database or API
 */
@Singleton
class StudentRepositoryImpl @Inject constructor() : StudentRepository {

    private val currentStudent = MutableStateFlow<Student?>(null)

    private val validStudents = mapOf(
        "QR123" to listOf("ROLL001", "ROLL002"),
        "QR456" to listOf("ROLL003", "ROLL004")
    )
    
    override suspend fun authenticateStudent(rollNumber: String, qrCode: String): Flow<Boolean> = flow {
        val isAuthenticated = validStudents[qrCode]?.contains(rollNumber) ?: false
        
        if (isAuthenticated) {
            val student = Student(rollNumber = rollNumber, qrCode = qrCode, isAuthenticated = true)
            currentStudent.value = student
            emit(true)
        } else {
            emit(false)
        }
    }
    
    override fun getCurrentStudent(): Flow<Student?> = currentStudent
} 