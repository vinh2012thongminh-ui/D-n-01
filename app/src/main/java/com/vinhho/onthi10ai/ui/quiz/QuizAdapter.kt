package com.vinhho.onthi10ai.ui.quiz

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.radiobutton.MaterialRadioButton
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.data.model.QuizQuestion
import com.vinhho.onthi10ai.databinding.ItemQuizQuestionBinding

class QuizAdapter(
    private val onOptionSelected: (questionIndex: Int, optionIndex: Int) -> Unit
) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    private var questions: List<QuizQuestion> = emptyList()
    private var selectedAnswers: Map<Int, Int> = emptyMap()
    private var isGraded: Boolean = false

    fun submitData(
        questions: List<QuizQuestion>,
        selectedAnswers: Map<Int, Int>,
        isGraded: Boolean
    ) {
        this.questions = questions
        this.selectedAnswers = selectedAnswers
        this.isGraded = isGraded
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemQuizQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(position, questions[position], selectedAnswers[position], isGraded)
    }

    override fun getItemCount(): Int = questions.size

    inner class QuizViewHolder(
        private val binding: ItemQuizQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            index: Int,
            item: QuizQuestion,
            selectedIndex: Int?,
            graded: Boolean
        ) {
            val ctx = binding.root.context
            binding.tvQuestionTitle.text = "Câu ${index + 1}: ${item.question}"

            val radioButtons: List<MaterialRadioButton> = listOf(
                binding.rbOption0,
                binding.rbOption1,
                binding.rbOption2,
                binding.rbOption3
            )

            binding.rgOptions.setOnCheckedChangeListener(null)
            binding.rgOptions.clearCheck()

            val defaultTextColor = ContextCompat.getColor(ctx, R.color.text_primary)
            val correctColor = ContextCompat.getColor(ctx, R.color.quiz_correct)
            val wrongColor = ContextCompat.getColor(ctx, R.color.quiz_wrong)
            val primaryColor = ContextCompat.getColor(ctx, R.color.primary_blue)

            radioButtons.forEachIndexed { optIdx, rb ->
                val text = item.options.getOrNull(optIdx).orEmpty()
                rb.text = text
                rb.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
                rb.isEnabled = !graded
                rb.isChecked = selectedIndex == optIdx
                rb.setTextColor(defaultTextColor)
                rb.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                rb.buttonTintList = ColorStateList.valueOf(primaryColor)

                if (graded) {
                    if (optIdx == item.correctIndex) {
                        rb.setTextColor(correctColor)
                        rb.setBackgroundColor(ContextCompat.getColor(ctx, R.color.quiz_correct_bg))
                        rb.buttonTintList = ColorStateList.valueOf(correctColor)
                    } else if (selectedIndex == optIdx) {
                        rb.setTextColor(wrongColor)
                        rb.setBackgroundColor(ContextCompat.getColor(ctx, R.color.quiz_wrong_bg))
                        rb.buttonTintList = ColorStateList.valueOf(wrongColor)
                    }
                }
            }

            binding.rgOptions.setOnCheckedChangeListener { _, checkedId ->
                if (graded) return@setOnCheckedChangeListener
                val chosen = when (checkedId) {
                    R.id.rbOption0 -> 0
                    R.id.rbOption1 -> 1
                    R.id.rbOption2 -> 2
                    R.id.rbOption3 -> 3
                    else -> -1
                }
                if (chosen >= 0) {
                    onOptionSelected(index, chosen)
                }
            }

            if (graded) {
                binding.cardExplanation.visibility = View.VISIBLE
                binding.tvExplanation.text =
                    "${ctx.getString(R.string.quiz_explanation_prefix)} ${item.explanation}"
            } else {
                binding.cardExplanation.visibility = View.GONE
            }
        }
    }
}
