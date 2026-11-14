package com.vinhho.onthi10ai.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vinhho.onthi10ai.data.model.QuizQuestion
import com.vinhho.onthi10ai.data.repository.AiRepository
import com.vinhho.onthi10ai.data.repository.AuthRepository
import com.vinhho.onthi10ai.data.repository.StudyHistoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuizUiState(
    val isLoading: Boolean = false,
    val selectedGrade: String = "",
    val detectedSubject: String = "",
    val topic: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val selectedAnswers: Map<Int, Int> = emptyMap(),
    val isGraded: Boolean = false,
    val correctCount: Int = 0,
    val errorMessage: String? = null
)

class QuizViewModel(
    private val aiRepository: AiRepository,
    private val authRepository: AuthRepository,
    private val historyRepository: StudyHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun generateQuiz(selectedGrade: String, topicPrompt: String, questionCount: Int) {
        viewModelScope.launch {
            _uiState.value = QuizUiState(
                isLoading = true,
                selectedGrade = selectedGrade,
                topic = topicPrompt.trim()
            )
            try {
                val result = aiRepository.generatePracticeQuiz(selectedGrade, topicPrompt, questionCount)
                result.fold(
                    onSuccess = { generated ->
                        _uiState.value = QuizUiState(
                            isLoading = false,
                            selectedGrade = generated.gradeLevel,
                            detectedSubject = generated.detectedSubject,
                            topic = topicPrompt.trim(),
                            questions = generated.questions
                        )
                    },
                    onFailure = { err ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "Không thể tạo đề luyện tập. Vui lòng thử lại."
                        )
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Đã xảy ra lỗi khi tạo đề."
                )
            }
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        val current = _uiState.value
        if (current.isGraded) return
        val updated = current.selectedAnswers.toMutableMap().apply {
            put(questionIndex, optionIndex)
        }
        _uiState.value = current.copy(selectedAnswers = updated)
    }

    fun submitAndGradeQuiz() {
        val current = _uiState.value
        if (current.questions.isEmpty() || current.isGraded) return

        var correct = 0
        current.questions.forEachIndexed { index, question ->
            if (current.selectedAnswers[index] == question.correctIndex) {
                correct++
            }
        }

        _uiState.value = current.copy(
            isGraded = true,
            correctCount = correct
        )

        val userId = authRepository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            historyRepository.saveExamResult(
                userId = userId,
                subject = "${current.selectedGrade} - ${current.detectedSubject.ifEmpty { "Tổng hợp" }}",
                topic = current.topic,
                score = correct,
                totalQuestions = current.questions.size
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(
        private val aiRepository: AiRepository,
        private val authRepository: AuthRepository,
        private val historyRepository: StudyHistoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuizViewModel(aiRepository, authRepository, historyRepository) as T
        }
    }
}
