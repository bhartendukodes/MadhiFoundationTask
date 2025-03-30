package com.example.madhifoundationtask.domain.model

/**
 * Domain model representing a student
 */
data class Student(
    val rollNumber: String,
    val qrCode: String,
    val isAuthenticated: Boolean = false
) 