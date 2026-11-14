package com.vinhho.onthi10ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OnThiDao {

    @Query("SELECT * FROM local_users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): LocalUserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: LocalUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: LocalUserEntity)

    @Query("SELECT * FROM tutor_history WHERE userId = :userId ORDER BY timestampMillis DESC")
    fun observeHistoryByUser(userId: String): Flow<List<TutorHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: TutorHistoryEntity)

    @Query("SELECT * FROM exam_results WHERE userId = :userId ORDER BY timestampMillis DESC")
    fun observeExamResultsByUser(userId: String): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(result: ExamResultEntity)
}
