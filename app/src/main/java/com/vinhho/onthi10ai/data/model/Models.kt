package com.vinhho.onthi10ai.data.model

data class AppUser(
    val uid: String,
    val displayName: String,
    val email: String?,
    val isGuest: Boolean,
    val authProvider: String = "anonymous"
)

data class TutorSolution(
    val detectedSubject: String,
    val answer: String,
    val isOfflineSolved: Boolean = true,
    val needsOnlineAnalysis: Boolean = false
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class QuizGenerationResult(
    val gradeLevel: String,
    val detectedSubject: String,
    val questions: List<QuizQuestion>
)

data class TutorHistoryItem(
    val id: String,
    val userId: String,
    val subject: String,
    val question: String,
    val answer: String,
    val timestampMillis: Long
)

data class ExamResultItem(
    val id: String,
    val userId: String,
    val subject: String,
    val topic: String,
    val score: Int,
    val totalQuestions: Int,
    val timestampMillis: Long
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val isOfflineReply: Boolean = false,
    val isWelcome: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class ChatReply(
    val text: String,
    val isOfflineReply: Boolean
)
