package com.vinhho.onthi10ai.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.vinhho.onthi10ai.OnThiApplication
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.databinding.FragmentQuizBinding
import kotlinx.coroutines.launch

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private lateinit var quizAdapter: QuizAdapter

    private val viewModel: QuizViewModel by viewModels {
        val app = requireActivity().application as OnThiApplication
        QuizViewModel.Factory(
            app.aiRepository,
            app.authRepository,
            app.studyHistoryRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSlider()
        setupListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {
        quizAdapter = QuizAdapter { questionIndex, optionIndex ->
            viewModel.selectAnswer(questionIndex, optionIndex)
        }
        binding.rvQuizQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quizAdapter
        }
    }

    private fun setupSlider() {
        updateCountLabel(binding.sliderQuestionCount.value.toInt())
        binding.sliderQuestionCount.addOnChangeListener { _, value, _ ->
            updateCountLabel(value.toInt())
        }
    }

    private fun updateCountLabel(count: Int) {
        binding.tvQuestionCountLabel.text = getString(R.string.quiz_count_format, count)
    }

    private fun setupListeners() {
        binding.btnGenerateQuiz.setOnClickListener {
            val topic = binding.etTopic.text?.toString().orEmpty()
            if (topic.isBlank()) {
                Snackbar.make(binding.root, R.string.error_empty_topic, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedGrade = getSelectedGrade()
            val questionCount = binding.sliderQuestionCount.value.toInt().coerceIn(30, 40)
            viewModel.generateQuiz(selectedGrade, topic, questionCount)
        }

        binding.btnSubmitQuiz.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.questions.isEmpty() || state.isGraded) return@setOnClickListener

            val answeredCount = state.selectedAnswers.size
            val totalCount = state.questions.size
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.quiz_confirm_title)
                .setMessage(getString(R.string.quiz_confirm_message, answeredCount, totalCount))
                .setPositiveButton(R.string.quiz_confirm_positive) { _, _ ->
                    viewModel.submitAndGradeQuiz()
                }
                .setNegativeButton(R.string.quiz_confirm_negative, null)
                .show()
        }
    }

    private fun getSelectedGrade(): String {
        val checkedId = binding.chipGroupGrade.checkedChipId
        val chip = binding.chipGroupGrade.findViewById<Chip>(checkedId)
        return chip?.text?.toString() ?: getString(R.string.grade_9)
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.layoutQuizLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.btnGenerateQuiz.isEnabled = !state.isLoading

                    val hasQuestions = state.questions.isNotEmpty()
                    binding.cardQuizBanner.visibility = if (hasQuestions) View.VISIBLE else View.GONE
                    binding.rvQuizQuestions.visibility = if (hasQuestions) View.VISIBLE else View.GONE
                    binding.btnSubmitQuiz.visibility =
                        if (hasQuestions && !state.isGraded) View.VISIBLE else View.GONE

                    if (hasQuestions) {
                        val gradeTitle = state.selectedGrade.ifEmpty { "Lớp 9" }
                        val subjectTitle = state.detectedSubject.ifEmpty { "Tổng hợp" }
                        binding.tvQuizBannerTitle.text = getString(
                            R.string.quiz_banner_format,
                            gradeTitle,
                            subjectTitle
                        )
                        binding.tvQuizBannerSubtitle.text = getString(
                            R.string.quiz_banner_sub_format,
                            state.questions.size,
                            gradeTitle
                        )
                    }

                    if (state.isGraded && hasQuestions) {
                        val score10 = state.correctCount * 10.0 / state.questions.size
                        binding.cardQuizScore.visibility = View.VISIBLE
                        binding.tvQuizScore.text = getString(
                            R.string.quiz_score_format,
                            state.selectedGrade.ifEmpty { "Lớp 9" },
                            state.detectedSubject.ifEmpty { "Tổng hợp" },
                            state.correctCount,
                            state.questions.size,
                            score10
                        )
                    } else {
                        binding.cardQuizScore.visibility = View.GONE
                    }

                    quizAdapter.submitData(
                        questions = state.questions,
                        selectedAnswers = state.selectedAnswers,
                        isGraded = state.isGraded
                    )

                    if (state.errorMessage != null) {
                        Snackbar.make(binding.root, state.errorMessage, Snackbar.LENGTH_LONG).show()
                        viewModel.clearError()
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
