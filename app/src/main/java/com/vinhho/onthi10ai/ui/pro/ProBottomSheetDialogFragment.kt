package com.vinhho.onthi10ai.ui.pro

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vinhho.onthi10ai.OnThiApplication
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.databinding.BottomSheetProBinding
import com.vinhho.onthi10ai.util.ProManager

class ProBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetProBinding? = null
    private val binding get() = _binding!!

    private var selectedPlan: String = ProManager.PLAN_YEARLY

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetProBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val proManager = (requireActivity().application as OnThiApplication).proManager

        binding.tvOldPriceMonthly.paintFlags =
            binding.tvOldPriceMonthly.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.tvOldPriceYearly.paintFlags =
            binding.tvOldPriceYearly.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.tvOldPriceLifetime.paintFlags =
            binding.tvOldPriceLifetime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        val isPro = proManager.isProActive()
        if (isPro) {
            binding.layoutProRegisterSection.visibility = View.GONE
            binding.layoutProMemberWelcome.visibility = View.VISIBLE
            val expiryText = proManager.formatExpiryLabel(requireContext())
            binding.tvProWelcomeDesc.text = buildString {
                append(getString(R.string.pro_card_desc_active, proManager.currentPlan.value))
                if (expiryText.isNotBlank()) {
                    append("\n")
                    append(expiryText)
                }
            }
            return
        }

        binding.layoutProRegisterSection.visibility = View.VISIBLE
        binding.layoutProMemberWelcome.visibility = View.GONE

        selectedPlan = when (proManager.currentPlan.value) {
            ProManager.PLAN_MONTHLY -> ProManager.PLAN_MONTHLY
            ProManager.PLAN_LIFETIME -> ProManager.PLAN_LIFETIME
            else -> ProManager.PLAN_YEARLY
        }
        renderPlanSelection()

        binding.cardPlanMonthly.setOnClickListener {
            selectedPlan = ProManager.PLAN_MONTHLY
            renderPlanSelection()
        }
        binding.cardPlanYearly.setOnClickListener {
            selectedPlan = ProManager.PLAN_YEARLY
            renderPlanSelection()
        }
        binding.cardPlanLifetime.setOnClickListener {
            selectedPlan = ProManager.PLAN_LIFETIME
            renderPlanSelection()
        }

        binding.btnActivatePro.setOnClickListener {
            showContestProConfirmationDialog(proManager)
        }
    }

    private fun showContestProConfirmationDialog(proManager: ProManager) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_crown)
            .setTitle(R.string.pro_contest_dialog_title)
            .setMessage(getString(R.string.pro_contest_dialog_message, selectedPlan))
            .setPositiveButton(R.string.pro_contest_dialog_confirm) { _, _ ->
                proManager.activatePro(selectedPlan)
                Toast.makeText(requireContext(), R.string.pro_activated_toast, Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .setNegativeButton(R.string.dialog_close, null)
            .show()
    }

    private fun renderPlanSelection() {
        updatePlanCard(binding.cardPlanMonthly, selectedPlan == ProManager.PLAN_MONTHLY)
        updatePlanCard(binding.cardPlanYearly, selectedPlan == ProManager.PLAN_YEARLY)
        updatePlanCard(binding.cardPlanLifetime, selectedPlan == ProManager.PLAN_LIFETIME)
    }

    private fun updatePlanCard(card: MaterialCardView, isSelected: Boolean) {
        val ctx = requireContext()
        if (isSelected) {
            card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.pro_gold_bg))
            card.strokeColor = ContextCompat.getColor(ctx, R.color.pro_gold)
            card.strokeWidth = resources.displayMetrics.density.toInt().coerceAtLeast(1) * 2
        } else {
            card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.surface_bg))
            card.strokeColor = ContextCompat.getColor(ctx, R.color.outline_subtle)
            card.strokeWidth = resources.displayMetrics.density.toInt().coerceAtLeast(1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ProBottomSheetDialogFragment"
    }
}
