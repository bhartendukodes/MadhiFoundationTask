package com.example.madhifoundationtask.domain.usecase

import com.example.madhifoundationtask.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for authenticating a student
 */
class AuthenticateStudentUseCase @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke(rollNumber: String, qrCode: String): Flow<Boolean> {
        return repository.authenticateStudent(rollNumber, qrCode)
    }
} 