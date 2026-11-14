package com.vinhho.onthi10ai.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vinhho.onthi10ai.data.model.AppUser
import com.vinhho.onthi10ai.data.model.TutorHistoryItem
import com.vinhho.onthi10ai.data.repository.AuthRepository
import com.vinhho.onthi10ai.data.repository.StudyHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val user: AppUser? = null,
    val totalQuestions: Int = 0,
    val totalExams: Int = 0,
    val averageScore10: Double = 0.0,
    val historyItems: List<TutorHistoryItem> = emptyList()
)

class HistoryViewModel(
    private val authRepository: AuthRepository,
    historyRepository: StudyHistoryRepository
) : ViewModel() {

    private val currentUser = authRepository.getCurrentUser()
    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut

    val uiState: StateFlow<HistoryUiState> = if (currentUser == null) {
        MutableStateFlow(HistoryUiState())
    } else {
        combine(
            historyRepository.observeHistory(currentUser.uid),
            historyRepository.observeExamResults(currentUser.uid)
        ) { history, exams ->
            val avg = if (exams.isEmpty()) {
                0.0
            } else {
                exams.map { exam ->
                    if (exam.totalQuestions > 0) exam.score * 10.0 / exam.totalQuestions else 0.0
                }.average()
            }
            HistoryUiState(
                user = currentUser,
                totalQuestions = history.size,
                totalExams = exams.size,
                averageScore10 = avg,
                historyItems = history
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = HistoryUiState(user = currentUser)
        )
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _loggedOut.value = true
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val historyRepository: StudyHistoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(authRepository, historyRepository) as T
        }
    }
}
