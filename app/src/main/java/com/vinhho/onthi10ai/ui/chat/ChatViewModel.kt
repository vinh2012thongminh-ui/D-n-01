package com.vinhho.onthi10ai.ui.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.data.model.ChatMessage
import com.vinhho.onthi10ai.data.repository.AiRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val aiRepository: AiRepository,
    private val welcomeText: String
) : ViewModel() {

    private val _messages = MutableStateFlow(listOf(createWelcomeMessage()))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _statusNotice = MutableStateFlow<String?>(null)
    val statusNotice: StateFlow<String?> = _statusNotice.asStateFlow()

    fun sendMessage(rawText: String) {
        val clean = rawText.trim()
        if (clean.isEmpty() || _isSending.value) return

        val historyBeforeSend = _messages.value
        val userMessage = ChatMessage(
            text = clean,
            isFromUser = true
        )
        _messages.value = historyBeforeSend + userMessage
        _isSending.value = true

        viewModelScope.launch {
            try {
                val result = aiRepository.sendChatMessage(
                    history = historyBeforeSend,
                    userMessage = clean
                )
                result.fold(
                    onSuccess = { reply ->
                        val aiMessage = ChatMessage(
                            text = reply.text,
                            isFromUser = false,
                            isOfflineReply = reply.isOfflineReply
                        )
                        _messages.value = _messages.value + aiMessage
                    },
                    onFailure = { error ->
                        _statusNotice.value = error.localizedMessage
                            ?: "Mình chưa nhận được phản hồi, bạn thử gửi lại nhé!"
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _statusNotice.value = "Kết nối gián đoạn nhẹ, bạn thử nhắn lại giúp mình nhé!"
            } finally {
                _isSending.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(createWelcomeMessage())
    }

    fun clearNotice() {
        _statusNotice.value = null
    }

    private fun createWelcomeMessage(): ChatMessage {
        return ChatMessage(
            text = welcomeText,
            isFromUser = false,
            isOfflineReply = true,
            isWelcome = true
        )
    }

    class Factory(
        private val aiRepository: AiRepository,
        context: Context
    ) : ViewModelProvider.Factory {
        private val welcome = context.getString(R.string.chat_welcome_message)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(aiRepository, welcome) as T
        }
    }
}
