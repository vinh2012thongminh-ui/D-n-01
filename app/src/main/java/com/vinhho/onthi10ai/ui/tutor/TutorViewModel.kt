package com.vinhho.onthi10ai.ui.tutor

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vinhho.onthi10ai.data.repository.AiRepository
import com.vinhho.onthi10ai.data.repository.AuthRepository
import com.vinhho.onthi10ai.data.repository.StudyHistoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TutorUiState {
    data object Idle : TutorUiState
    data object Loading : TutorUiState
    data class Success(
        val detectedSubject: String,
        val solution: String,
        val isOfflineSolved: Boolean,
        val savedToHistory: Boolean
    ) : TutorUiState
    data class Error(val message: String) : TutorUiState
}

class TutorViewModel(
    private val aiRepository: AiRepository,
    private val authRepository: AuthRepository,
    private val historyRepository: StudyHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TutorUiState>(TutorUiState.Idle)
    val uiState: StateFlow<TutorUiState> = _uiState.asStateFlow()

    fun solveQuestion(questionText: String, imageBitmap: Bitmap?) {
        viewModelScope.launch {
            _uiState.value = TutorUiState.Loading
            try {
                val result = aiRepository.solveExamQuestion(
                    questionText = questionText,
                    imageBitmap = imageBitmap
                )
                result.fold(
                    onSuccess = { tutorSolution ->
                        val userId = authRepository.getCurrentUser()?.uid
                        val promptForHistory = questionText.trim().ifEmpty {
                            "Bài tập chụp ảnh (${tutorSolution.detectedSubject})"
                        }
                        var saved = false
                        if (userId != null && !tutorSolution.needsOnlineAnalysis) {
                            historyRepository.saveTutorHistory(
                                userId = userId,
                                subject = tutorSolution.detectedSubject,
                                question = promptForHistory,
                                answer = tutorSolution.answer
                            )
                            saved = true
                        }
                        _uiState.value = TutorUiState.Success(
                            detectedSubject = tutorSolution.detectedSubject,
                            solution = tutorSolution.answer,
                            isOfflineSolved = tutorSolution.isOfflineSolved,
                            savedToHistory = saved
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = TutorUiState.Error(
                            error.localizedMessage ?: "Không thể phân tích đề bài lúc này. Vui lòng thử lại."
                        )
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = TutorUiState.Error(
                    e.localizedMessage ?: "Đã xảy ra lỗi khi giải bài. Vui lòng thử lại."
                )
            }
        }
    }

    fun clearError() {
        if (_uiState.value is TutorUiState.Error) {
            _uiState.value = TutorUiState.Idle
        }
    }

    class Factory(
        private val aiRepository: AiRepository,
        private val authRepository: AuthRepository,
        private val historyRepository: StudyHistoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TutorViewModel(aiRepository, authRepository, historyRepository) as T
        }
    }
}
