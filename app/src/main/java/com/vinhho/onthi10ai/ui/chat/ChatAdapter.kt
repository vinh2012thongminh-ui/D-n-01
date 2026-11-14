package com.vinhho.onthi10ai.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.data.model.ChatMessage
import com.vinhho.onthi10ai.databinding.ItemChatMessageBinding

class ChatAdapter(
    private val onCopyClick: (String) -> Unit
) : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding, onCopyClick)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatViewHolder(
        private val binding: ItemChatMessageBinding,
        private val onCopyClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessage) {
            if (item.isFromUser) {
                binding.layoutUserMessage.visibility = View.VISIBLE
                binding.layoutAiMessage.visibility = View.GONE
                binding.tvUserMessage.text = item.text
            } else {
                binding.layoutUserMessage.visibility = View.GONE
                binding.layoutAiMessage.visibility = View.VISIBLE
                binding.tvAiMessage.text = item.text
                binding.tvAiModeBadge.text = if (item.isOfflineReply) {
                    binding.root.context.getString(R.string.tutor_mode_offline)
                } else {
                    "Trực tuyến"
                }
                binding.btnCopyAiMessage.setOnClickListener {
                    onCopyClick(item.text)
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
