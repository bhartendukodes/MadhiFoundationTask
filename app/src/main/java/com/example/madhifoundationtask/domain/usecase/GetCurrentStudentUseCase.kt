package com.example.madhifoundationtask.domain.usecase

import com.example.madhifoundationtask.domain.model.Student
import com.example.madhifoundationtask.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting the current authenticated student
 */
class GetCurrentStudentUseCase @Inject constructor(
    private val repository: StudentRepository
) {
    operator fun invoke(): Flow<Student?> {
        return repository.getCurrentStudent()
    }
} 