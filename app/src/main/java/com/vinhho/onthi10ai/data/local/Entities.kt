package com.vinhho.onthi10ai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_users")
data class LocalUserEntity(
    @PrimaryKey val email: String,
    val uid: String,
    val displayName: String,
    val passwordHash: String
)

@Entity(tableName = "tutor_history")
data class TutorHistoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val subject: String,
    val question: String,
    val answer: String,
    val timestampMillis: Long
)

@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val subject: String,
    val topic: String,
    val score: Int,
    val totalQuestions: Int,
    val timestampMillis: Long
)
