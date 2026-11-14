package com.vinhho.onthi10ai.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.vinhho.onthi10ai.data.local.ExamResultEntity
import com.vinhho.onthi10ai.data.local.OnThiDao
import com.vinhho.onthi10ai.data.local.TutorHistoryEntity
import com.vinhho.onthi10ai.data.model.ExamResultItem
import com.vinhho.onthi10ai.data.model.TutorHistoryItem
import com.vinhho.onthi10ai.util.NetworkUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

interface StudyHistoryRepository {
    fun observeHistory(userId: String): Flow<List<TutorHistoryItem>>
    fun observeExamResults(userId: String): Flow<List<ExamResultItem>>
    suspend fun saveTutorHistory(userId: String, subject: String, question: String, answer: String)
    suspend fun saveExamResult(
        userId: String,
        subject: String,
        topic: String,
        score: Int,
        totalQuestions: Int
    )
}

class LocalStudyHistoryRepository(
    private val dao: OnThiDao,
    context: Context? = null
) : StudyHistoryRepository {

    private val appContext = context?.applicationContext

    override fun observeHistory(userId: String): Flow<List<TutorHistoryItem>> {
        return dao.observeHistoryByUser(userId).map { list ->
            list.map { entity ->
                TutorHistoryItem(
                    id = entity.id,
                    userId = entity.userId,
                    subject = entity.subject,
                    question = entity.question,
                    answer = entity.answer,
                    timestampMillis = entity.timestampMillis
                )
            }
        }
    }

    override fun observeExamResults(userId: String): Flow<List<ExamResultItem>> {
        return dao.observeExamResultsByUser(userId).map { list ->
            list.map { entity ->
                ExamResultItem(
                    id = entity.id,
                    userId = entity.userId,
                    subject = entity.subject,
                    topic = entity.topic,
                    score = entity.score,
                    totalQuestions = entity.totalQuestions,
                    timestampMillis = entity.timestampMillis
                )
            }
        }
    }

    override suspend fun saveTutorHistory(
        userId: String,
        subject: String,
        question: String,
        answer: String
    ) = withContext(Dispatchers.IO) {
        val entity = TutorHistoryEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            subject = subject,
            question = question,
            answer = answer,
            timestampMillis = System.currentTimeMillis()
        )
        dao.insertHistory(entity)
        syncHistoryToFirestore(entity)
    }

    override suspend fun saveExamResult(
        userId: String,
        subject: String,
        topic: String,
        score: Int,
        totalQuestions: Int
    ) = withContext(Dispatchers.IO) {
        val entity = ExamResultEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            subject = subject,
            topic = topic,
            score = score,
            totalQuestions = totalQuestions,
            timestampMillis = System.currentTimeMillis()
        )
        dao.insertExamResult(entity)
        syncExamResultToFirestore(entity)
    }

    private suspend fun syncHistoryToFirestore(entity: TutorHistoryEntity) {
        try {
            if (!canSyncWithFirebase()) return
            val cloudUserId = safeFirebaseUid() ?: entity.userId
            val payload = hashMapOf(
                "id" to entity.id,
                "userId" to cloudUserId,
                "subject" to entity.subject,
                "question" to entity.question,
                "answer" to entity.answer,
                "timestampMillis" to entity.timestampMillis
            )
            withTimeoutOrNull(FIRESTORE_SYNC_TIMEOUT_MS) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(cloudUserId)
                    .collection("history")
                    .document(entity.id)
                    .set(payload)
                    .await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Offline or Firestore sync skipped for history ${entity.id}", e)
        }
    }

    private suspend fun syncExamResultToFirestore(entity: ExamResultEntity) {
        try {
            if (!canSyncWithFirebase()) return
            val cloudUserId = safeFirebaseUid() ?: entity.userId
            val payload = hashMapOf(
                "id" to entity.id,
                "userId" to cloudUserId,
                "subject" to entity.subject,
                "topic" to entity.topic,
                "score" to entity.score,
                "totalQuestions" to entity.totalQuestions,
                "timestampMillis" to entity.timestampMillis
            )
            withTimeoutOrNull(FIRESTORE_SYNC_TIMEOUT_MS) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(cloudUserId)
                    .collection("exams")
                    .document(entity.id)
                    .set(payload)
                    .await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Offline or Firestore sync skipped for exam ${entity.id}", e)
        }
    }

    private fun canSyncWithFirebase(): Boolean {
        val ctx = appContext ?: return false
        if (!NetworkUtils.isNetworkAvailable(ctx)) return false
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx) ?: return false
        }
        return true
    }

    private fun safeFirebaseUid(): String? = try {
        Firebase.auth.currentUser?.uid
    } catch (_: Exception) {
        null
    }

    companion object {
        private const val TAG = "StudyHistoryRepo"
        private const val FIRESTORE_SYNC_TIMEOUT_MS = 4_000L
    }
}
