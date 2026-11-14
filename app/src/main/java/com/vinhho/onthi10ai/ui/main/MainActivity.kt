package com.vinhho.onthi10ai.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vinhho.onthi10ai.OnThiApplication
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.databinding.ActivityMainBinding
import com.vinhho.onthi10ai.ui.chat.ChatFragment
import com.vinhho.onthi10ai.ui.history.HistoryFragment
import com.vinhho.onthi10ai.ui.login.LoginActivity
import com.vinhho.onthi10ai.ui.pro.ProBottomSheetDialogFragment
import com.vinhho.onthi10ai.ui.quiz.QuizFragment
import com.vinhho.onthi10ai.ui.tutor.TutorFragment
import com.vinhho.onthi10ai.util.ProRenewalNotice
import com.vinhho.onthi10ai.util.ThemeManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isMiniAdDismissedForSession = false
    private var defaultToolbarSubtitle: String = ""
    private var renewalDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        syncSystemBarsAppearance()

        val app = application as OnThiApplication
        val user = app.authRepository.getCurrentUser()
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsets()
        setupToolbar(user.displayName, user.email, user.isGuest)
        setupProAndAdControls()
        setupThemeToggle()
        observeProStatus()
        setupBottomNavigation(savedInstanceState == null)
        setupBackNavigation()
    }

    override fun onResume() {
        super.onResume()
        (application as OnThiApplication).proManager.refreshSubscriptionStatus()
    }

    override fun onDestroy() {
        renewalDialog?.dismiss()
        renewalDialog = null
        super.onDestroy()
    }

    private fun syncSystemBarsAppearance() {
        val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isNight
            isAppearanceLightNavigationBars = !isNight
        }
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = statusBars.top)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = navBars.bottom)
            insets
        }
    }

    private fun setupToolbar(displayName: String, email: String?, isGuest: Boolean) {
        binding.toolbar.title = getString(R.string.app_name)
        defaultToolbarSubtitle = if (isGuest) {
            getString(R.string.account_guest_label)
        } else {
            getString(R.string.account_local_prefix, email ?: displayName)
        }
        binding.toolbar.subtitle = defaultToolbarSubtitle
    }

    private fun setupProAndAdControls() {
        binding.btnToolbarPro.setOnClickListener {
            openProSheet()
        }
        binding.cardMiniAd.setOnClickListener {
            openProSheet()
        }
        binding.btnCloseMiniAd.setOnClickListener {
            isMiniAdDismissedForSession = true
            binding.cardMiniAd.visibility = View.GONE
        }
    }

    private fun setupThemeToggle() {
        val themeManager = (application as OnThiApplication).themeManager
        updateThemeToggleIcon(themeManager.isDarkMode())
        binding.btnToolbarTheme.setOnClickListener {
            themeManager.toggleTheme()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeManager.themeMode.collect { mode ->
                    updateThemeToggleIcon(mode == ThemeManager.MODE_DARK)
                }
            }
        }
    }

    private fun updateThemeToggleIcon(isDarkMode: Boolean) {
        val iconRes = if (isDarkMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
        binding.btnToolbarTheme.setIconResource(iconRes)
    }

    private fun observeProStatus() {
        val proManager = (application as OnThiApplication).proManager
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    proManager.isPro.collect { isPro ->
                        updateProUi(isPro)
                    }
                }
                launch {
                    proManager.pendingRenewalNotice.collect { notice ->
                        if (notice != null) {
                            showRenewalDialog(notice)
                        }
                    }
                }
            }
        }
    }

    private fun showRenewalDialog(notice: ProRenewalNotice) {
        if (isFinishing || isDestroyed || renewalDialog?.isShowing == true) return
        val proManager = (application as OnThiApplication).proManager
        val expiredDate = proManager.formatDate(notice.expiredAtMillis)
        renewalDialog = MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_crown)
            .setTitle(R.string.pro_renewal_dialog_title)
            .setMessage(getString(R.string.pro_renewal_dialog_message, notice.expiredPlanName, expiredDate))
            .setPositiveButton(R.string.pro_renewal_action_now) { _, _ ->
                proManager.consumeRenewalNotice()
                openProSheet()
            }
            .setNegativeButton(R.string.pro_renewal_action_later) { _, _ ->
                proManager.consumeRenewalNotice()
            }
            .setOnCancelListener {
                proManager.consumeRenewalNotice()
            }
            .show()
    }

    private fun updateProUi(isPro: Boolean) {
        if (isPro) {
            binding.cardMiniAd.visibility = View.GONE
            binding.btnToolbarPro.visibility = View.GONE
            binding.toolbar.subtitle = getString(R.string.pro_toolbar_welcome)
        } else {
            binding.cardMiniAd.visibility = if (isMiniAdDismissedForSession) View.GONE else View.VISIBLE
            binding.btnToolbarPro.visibility = View.VISIBLE
            binding.btnToolbarPro.text = getString(R.string.btn_upgrade_pro)
            binding.btnToolbarPro.setTextColor(ContextCompat.getColor(this, R.color.pro_gold))
            binding.btnToolbarPro.setBackgroundColor(ContextCompat.getColor(this, R.color.pro_gold_bg))
            binding.btnToolbarPro.iconTint =
                android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.pro_gold))
            if (defaultToolbarSubtitle.isNotEmpty()) {
                binding.toolbar.subtitle = defaultToolbarSubtitle
            }
        }
    }

    fun openProSheet() {
        if (supportFragmentManager.findFragmentByTag(ProBottomSheetDialogFragment.TAG) == null) {
            ProBottomSheetDialogFragment().show(supportFragmentManager, ProBottomSheetDialogFragment.TAG)
        }
    }

    private fun setupBottomNavigation(isFirstLaunch: Boolean) {
        if (isFirstLaunch) {
            switchFragment(R.id.nav_tutor)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            switchFragment(item.itemId)
            true
        }
    }

    private fun switchFragment(itemId: Int) {
        val tag = when (itemId) {
            R.id.nav_tutor -> TAG_TUTOR
            R.id.nav_chat -> TAG_CHAT
            R.id.nav_quiz -> TAG_QUIZ
            R.id.nav_history -> TAG_HISTORY
            else -> TAG_TUTOR
        }

        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()
        fm.fragments.forEach { fragment ->
            if (fragment.isAdded && !fragment.isHidden) {
                transaction.hide(fragment)
            }
        }

        var target: Fragment? = fm.findFragmentByTag(tag)
        if (target == null) {
            target = when (tag) {
                TAG_TUTOR -> TutorFragment()
                TAG_CHAT -> ChatFragment()
                TAG_QUIZ -> QuizFragment()
                TAG_HISTORY -> HistoryFragment()
                else -> TutorFragment()
            }
            transaction.add(R.id.fragmentContainer, target, tag)
        } else {
            transaction.show(target)
        }
        transaction.commit()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bottomNavigation.selectedItemId != R.id.nav_tutor) {
                    binding.bottomNavigation.selectedItemId = R.id.nav_tutor
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private companion object {
        const val TAG_TUTOR = "tab_tutor"
        const val TAG_CHAT = "tab_chat"
        const val TAG_QUIZ = "tab_quiz"
        const val TAG_HISTORY = "tab_history"
    }
}
