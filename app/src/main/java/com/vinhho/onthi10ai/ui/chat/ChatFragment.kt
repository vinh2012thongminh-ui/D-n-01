package com.vinhho.onthi10ai.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.vinhho.onthi10ai.OnThiApplication
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.databinding.FragmentChatBinding
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels {
        val app = requireActivity().application as OnThiApplication
        ChatViewModel.Factory(app.aiRepository, app)
    }

    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupControls()
        observeState()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { text ->
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("GiaSuAiChat", text))
            Snackbar.make(binding.root, R.string.chat_copied, Snackbar.LENGTH_SHORT).show()
        }
        binding.rvChatMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupControls() {
        binding.btnSendChat.setOnClickListener {
            submitCurrentInput()
        }

        binding.etChatMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitCurrentInput()
                true
            } else {
                false
            }
        }

        binding.btnClearChat.setOnClickListener {
            viewModel.clearChat()
            Snackbar.make(binding.root, R.string.chat_cleared_notice, Snackbar.LENGTH_SHORT).show()
        }

        binding.chipQuickSimpler.setOnClickListener {
            sendQuickPrompt(getString(R.string.chat_quick_simpler))
        }
        binding.chipQuickExample.setOnClickListener {
            sendQuickPrompt(getString(R.string.chat_quick_example))
        }
        binding.chipQuickPlan.setOnClickListener {
            sendQuickPrompt(getString(R.string.chat_quick_plan))
        }
        binding.chipQuickMemory.setOnClickListener {
            sendQuickPrompt(getString(R.string.chat_quick_memory))
        }
    }

    private fun submitCurrentInput() {
        val text = binding.etChatMessage.text?.toString().orEmpty()
        if (text.isBlank() || viewModel.isSending.value) return
        binding.etChatMessage.setText("")
        viewModel.sendMessage(text)
    }

    private fun sendQuickPrompt(prompt: String) {
        if (viewModel.isSending.value) return
        viewModel.sendMessage(prompt)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.messages.collect { list ->
                        chatAdapter.submitList(list) {
                            if (list.isNotEmpty()) {
                                binding.rvChatMessages.smoothScrollToPosition(list.lastIndex)
                            }
                        }
                    }
                }
                launch {
                    viewModel.isSending.collect { sending ->
                        binding.layoutChatTyping.visibility = if (sending) View.VISIBLE else View.GONE
                        binding.btnSendChat.isEnabled = !sending
                        binding.chipQuickSimpler.isEnabled = !sending
                        binding.chipQuickExample.isEnabled = !sending
                        binding.chipQuickPlan.isEnabled = !sending
                        binding.chipQuickMemory.isEnabled = !sending
                        if (sending && chatAdapter.itemCount > 0) {
                            binding.rvChatMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
                        }
                    }
                }
                launch {
                    viewModel.statusNotice.collect { notice ->
                        if (!notice.isNullOrBlank()) {
                            Snackbar.make(binding.root, notice, Snackbar.LENGTH_SHORT).show()
                            viewModel.clearNotice()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
