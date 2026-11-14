package com.vinhho.onthi10ai.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vinhho.onthi10ai.data.model.TutorHistoryItem
import com.vinhho.onthi10ai.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (TutorHistoryItem) -> Unit
) : ListAdapter<TutorHistoryItem, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    private val dateFormatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TutorHistoryItem) {
            binding.chipHistorySubject.text = item.subject
            binding.tvHistoryTimestamp.text = dateFormatter.format(Date(item.timestampMillis))
            binding.tvHistoryQuestion.text = item.question
            binding.tvHistoryAnswerPreview.text = item.answer
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<TutorHistoryItem>() {
        override fun areItemsTheSame(oldItem: TutorHistoryItem, newItem: TutorHistoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TutorHistoryItem, newItem: TutorHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
