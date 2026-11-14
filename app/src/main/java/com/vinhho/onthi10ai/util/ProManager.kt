package com.vinhho.onthi10ai.util

import android.content.Context
import com.vinhho.onthi10ai.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

data class ProRenewalNotice(
    val expiredPlanName: String,
    val expiredAtMillis: Long
)

class ProManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _currentPlan = MutableStateFlow(PLAN_FREE)
    val currentPlan: StateFlow<String> = _currentPlan.asStateFlow()

    private val _isLifetime = MutableStateFlow(false)
    val isLifetime: StateFlow<Boolean> = _isLifetime.asStateFlow()

    private val _expiresAtMillis = MutableStateFlow(EXPIRY_NONE)
    val expiresAtMillis: StateFlow<Long> = _expiresAtMillis.asStateFlow()

    private val _pendingRenewalNotice = MutableStateFlow<ProRenewalNotice?>(null)
    val pendingRenewalNotice: StateFlow<ProRenewalNotice?> = _pendingRenewalNotice.asStateFlow()

    init {
        refreshSubscriptionStatus(System.currentTimeMillis())
    }

    fun isProActive(): Boolean {
        refreshSubscriptionStatus(System.currentTimeMillis())
        return _isPro.value
    }

    fun refreshSubscriptionStatus(nowMillis: Long = System.currentTimeMillis()) {
        val storedActive = prefs.getBoolean(KEY_IS_PRO, false)
        val storedPlan = prefs.getString(KEY_PLAN, PLAN_FREE) ?: PLAN_FREE
        val storedLifetime = prefs.getBoolean(KEY_IS_LIFETIME, false)
        val storedExpiry = prefs.getLong(KEY_EXPIRES_AT, EXPIRY_NONE)

        if (storedActive && !storedLifetime && storedExpiry > 0L && nowMillis >= storedExpiry) {
            // Fixed-duration plan has expired -> downgrade to Free and queue renewal notice
            prefs.edit()
                .putBoolean(KEY_IS_PRO, false)
                .putString(KEY_PLAN, PLAN_FREE)
                .putBoolean(KEY_IS_LIFETIME, false)
                .putLong(KEY_EXPIRES_AT, EXPIRY_NONE)
                .putString(KEY_PENDING_RENEWAL_PLAN, storedPlan)
                .putLong(KEY_PENDING_RENEWAL_DATE, storedExpiry)
                .apply()
            _isPro.value = false
            _currentPlan.value = PLAN_FREE
            _isLifetime.value = false
            _expiresAtMillis.value = EXPIRY_NONE
            _pendingRenewalNotice.value = ProRenewalNotice(storedPlan, storedExpiry)
            return
        }

        _isPro.value = storedActive
        _currentPlan.value = if (storedActive) storedPlan else PLAN_FREE
        _isLifetime.value = storedActive && storedLifetime
        _expiresAtMillis.value = if (storedActive) storedExpiry else EXPIRY_NONE

        val pendingPlan = prefs.getString(KEY_PENDING_RENEWAL_PLAN, null)
        val pendingDate = prefs.getLong(KEY_PENDING_RENEWAL_DATE, 0L)
        if (!storedActive && !pendingPlan.isNullOrBlank() && pendingDate > 0L) {
            _pendingRenewalNotice.value = ProRenewalNotice(pendingPlan, pendingDate)
        } else {
            _pendingRenewalNotice.value = null
        }
    }

    fun activatePro(planName: String, nowMillis: Long = System.currentTimeMillis()) {
        val cleanPlan = planName.ifBlank { PLAN_YEARLY }
        val isLifetimePlan = cleanPlan == PLAN_LIFETIME
        val durationMillis = when (cleanPlan) {
            PLAN_MONTHLY -> DAYS_MONTHLY * MILLIS_PER_DAY
            PLAN_YEARLY -> DAYS_YEARLY * MILLIS_PER_DAY
            else -> 0L
        }
        val expiryMillis = if (isLifetimePlan) EXPIRY_PERMANENT else nowMillis + durationMillis

        prefs.edit()
            .putBoolean(KEY_IS_PRO, true)
            .putString(KEY_PLAN, cleanPlan)
            .putBoolean(KEY_IS_LIFETIME, isLifetimePlan)
            .putLong(KEY_ACTIVATED_AT, nowMillis)
            .putLong(KEY_EXPIRES_AT, expiryMillis)
            .remove(KEY_PENDING_RENEWAL_PLAN)
            .remove(KEY_PENDING_RENEWAL_DATE)
            .apply()

        _isPro.value = true
        _currentPlan.value = cleanPlan
        _isLifetime.value = isLifetimePlan
        _expiresAtMillis.value = expiryMillis
        _pendingRenewalNotice.value = null
    }

    fun deactivatePro() {
        prefs.edit()
            .putBoolean(KEY_IS_PRO, false)
            .putString(KEY_PLAN, PLAN_FREE)
            .putBoolean(KEY_IS_LIFETIME, false)
            .putLong(KEY_EXPIRES_AT, EXPIRY_NONE)
            .remove(KEY_ACTIVATED_AT)
            .remove(KEY_PENDING_RENEWAL_PLAN)
            .remove(KEY_PENDING_RENEWAL_DATE)
            .apply()

        _isPro.value = false
        _currentPlan.value = PLAN_FREE
        _isLifetime.value = false
        _expiresAtMillis.value = EXPIRY_NONE
        _pendingRenewalNotice.value = null
    }

    fun consumeRenewalNotice() {
        prefs.edit()
            .remove(KEY_PENDING_RENEWAL_PLAN)
            .remove(KEY_PENDING_RENEWAL_DATE)
            .apply()
        _pendingRenewalNotice.value = null
    }

    fun formatExpiryLabel(context: Context, nowMillis: Long = System.currentTimeMillis()): String {
        if (!_isPro.value) return ""
        if (_isLifetime.value || _expiresAtMillis.value == EXPIRY_PERMANENT) {
            return context.getString(R.string.pro_expiry_permanent)
        }
        val expiry = _expiresAtMillis.value
        if (expiry <= 0L) return context.getString(R.string.pro_expiry_permanent)
        val formattedDate = formatDate(expiry)
        val remainingDays = ceil(((expiry - nowMillis).coerceAtLeast(0L)).toDouble() / MILLIS_PER_DAY)
            .toInt()
            .coerceAtLeast(1)
        return context.getString(R.string.pro_expiry_date_format, formattedDate, remainingDays)
    }

    fun formatDate(millis: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date(millis))
    }

    companion object {
        private const val PREFS_NAME = "giasuai_pro_prefs"
        private const val KEY_IS_PRO = "is_pro_active"
        private const val KEY_PLAN = "pro_plan_name"
        private const val KEY_IS_LIFETIME = "pro_is_lifetime"
        private const val KEY_ACTIVATED_AT = "pro_activated_at"
        private const val KEY_EXPIRES_AT = "pro_expires_at"
        private const val KEY_PENDING_RENEWAL_PLAN = "pro_pending_renewal_plan"
        private const val KEY_PENDING_RENEWAL_DATE = "pro_pending_renewal_date"

        const val EXPIRY_NONE = 0L
        const val EXPIRY_PERMANENT = -1L
        const val DAYS_MONTHLY = 30L
        const val DAYS_YEARLY = 365L
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

        const val PLAN_FREE = "Miễn phí"
        const val PLAN_MONTHLY = "Gói Tháng (24.500đ • Giảm 50%)"
        const val PLAN_YEARLY = "Gói Năm (149.500đ • Giảm 50%)"
        const val PLAN_LIFETIME = "Gói Trọn Đời (299.500đ • Giảm 50%)"
    }
}
