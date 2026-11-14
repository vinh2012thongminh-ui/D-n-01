package com.vinhho.onthi10ai.ui.history

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vinhho.onthi10ai.OnThiApplication
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.data.model.TutorHistoryItem
import com.vinhho.onthi10ai.databinding.DialogHistoryDetailBinding
import com.vinhho.onthi10ai.databinding.FragmentHistoryBinding
import com.vinhho.onthi10ai.ui.login.LoginActivity
import com.vinhho.onthi10ai.ui.main.MainActivity
import com.vinhho.onthi10ai.util.ThemeManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: HistoryAdapter

    private val viewModel: HistoryViewModel by viewModels {
        val app = requireActivity().application as OnThiApplication
        HistoryViewModel.Factory(app.authRepository, app.studyHistoryRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeState()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter { item ->
            showHistoryDetailDialog(item)
        }
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun setupListeners() {
        binding.btnHistoryPro.setOnClickListener {
            (activity as? MainActivity)?.openProSheet()
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        val themeManager = (requireActivity().application as OnThiApplication).themeManager
        binding.toggleThemeGroup.check(
            if (themeManager.isDarkMode()) R.id.btnThemeDark else R.id.btnThemeLight
        )
        binding.toggleThemeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                themeManager.setDarkMode(checkedId == R.id.btnThemeDark)
            }
        }
    }

    private fun observeState() {
        val app = requireActivity().application as OnThiApplication
        val proManager = app.proManager
        val themeManager = app.themeManager
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        val user = state.user
                        binding.tvUserAccount.text = when {
                            user == null || user.isGuest -> getString(R.string.account_guest_label)
                            user.authProvider == com.vinhho.onthi10ai.data.repository.LocalAuthRepository.PROVIDER_GOOGLE &&
                                !user.email.isNullOrBlank() ->
                                getString(R.string.account_google_prefix, user.displayName, user.email)
                            !user.email.isNullOrBlank() -> user.email
                            else -> user.displayName
                        }
                        binding.tvTotalQuestions.text = state.totalQuestions.toString()
                        binding.tvAvgScore.text = String.format(Locale.US, "%.1f", state.averageScore10)
                        binding.tvAvgScoreLabel.text = if (state.totalExams > 0) {
                            "${getString(R.string.history_avg_score)} (${state.totalExams} đề)"
                        } else {
                            getString(R.string.history_avg_score)
                        }

                        val isEmpty = state.historyItems.isEmpty()
                        binding.cardEmptyHistory.visibility = if (isEmpty) View.VISIBLE else View.GONE
                        binding.rvHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
                        historyAdapter.submitList(state.historyItems)
                    }
                }
                launch {
                    combine(
                        proManager.isPro,
                        proManager.currentPlan,
                        proManager.expiresAtMillis
                    ) { isPro, plan, _ ->
                        isPro to plan
                    }.collect { (isPro, plan) ->
                        renderProCard(isPro, plan)
                    }
                }
                launch {
                    themeManager.themeMode.collect { mode ->
                        val targetId = if (mode == ThemeManager.MODE_DARK) {
                            R.id.btnThemeDark
                        } else {
                            R.id.btnThemeLight
                        }
                        if (binding.toggleThemeGroup.checkedButtonId != targetId) {
                            binding.toggleThemeGroup.check(targetId)
                        }
                    }
                }
                launch {
                    viewModel.loggedOut.collect { isLoggedOut ->
                        if (isLoggedOut) {
                            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                }
            }
        }
    }

    private fun renderProCard(isPro: Boolean, planName: String) {
        val ctx = requireContext()
        val proManager = (requireActivity().application as OnThiApplication).proManager
        if (isPro) {
            binding.cardProStatus.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.quiz_correct_bg))
            binding.cardProStatus.strokeColor = ContextCompat.getColor(ctx, R.color.quiz_correct)
            binding.tvProCardTitle.text = getString(R.string.pro_welcome_member_title)
            val expiryLabel = proManager.formatExpiryLabel(ctx)
            binding.tvProCardDesc.text = buildString {
                append(getString(R.string.pro_card_desc_active, planName))
                if (expiryLabel.isNotBlank()) {
                    append("\n")
                    append(expiryLabel)
                }
            }
            binding.btnHistoryPro.visibility = View.GONE
        } else {
            binding.cardProStatus.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.pro_gold_bg))
            binding.cardProStatus.strokeColor = ContextCompat.getColor(ctx, R.color.pro_gold_border)
            binding.tvProCardTitle.text = getString(R.string.pro_card_title_free)
            binding.tvProCardDesc.text = getString(R.string.pro_card_desc_free)
            binding.btnHistoryPro.text = getString(R.string.btn_upgrade_pro)
            binding.btnHistoryPro.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.pro_button_bg))
            binding.btnHistoryPro.visibility = View.VISIBLE
        }
    }

    private fun showHistoryDetailDialog(item: TutorHistoryItem) {
        val dialogBinding = DialogHistoryDetailBinding.inflate(layoutInflater)
        dialogBinding.tvDialogQuestion.text = item.question
        dialogBinding.tvDialogAnswer.text = item.answer

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${item.subject} - ${getString(R.string.tutor_solution_title)}")
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.dialog_close, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
