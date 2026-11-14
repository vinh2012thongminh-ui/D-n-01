package com.vinhho.onthi10ai.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.data.local.LocalUserEntity
import com.vinhho.onthi10ai.data.local.OnThiDao
import com.vinhho.onthi10ai.data.model.AppUser
import com.vinhho.onthi10ai.util.NetworkUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.security.MessageDigest
import java.util.UUID

interface AuthRepository {
    fun getCurrentUser(): AppUser?
    fun isRealUserLoggedIn(): Boolean
    fun clearGuestSessionIfPresent()
    fun getLastGoogleAccount(): Pair<String, String>?
    suspend fun signInWithGoogleIdToken(idToken: String, displayName: String, email: String): Result<AppUser>
    suspend fun signInWithGoogle(displayName: String, email: String): Result<AppUser>
    suspend fun loginWithEmail(email: String, password: String): Result<AppUser>
    suspend fun registerWithEmail(email: String, password: String): Result<AppUser>
    suspend fun signInAnonymously(): Result<AppUser>
    suspend fun signOut()
}

class LocalAuthRepository(
    context: Context,
    private val dao: OnThiDao
) : AuthRepository {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getCurrentUser(): AppUser? {
        val uid = prefs.getString(KEY_UID, null) ?: return null
        val displayName = prefs.getString(KEY_DISPLAY_NAME, null)
            ?: appContext.getString(R.string.account_guest_label)
        val email = prefs.getString(KEY_EMAIL, null)
        val isGuest = prefs.getBoolean(KEY_IS_GUEST, false)
        val authProvider = prefs.getString(
            KEY_AUTH_PROVIDER,
            if (isGuest) PROVIDER_ANONYMOUS else PROVIDER_EMAIL
        ) ?: PROVIDER_ANONYMOUS
        return AppUser(
            uid = uid,
            displayName = displayName,
            email = email,
            isGuest = isGuest,
            authProvider = authProvider
        )
    }

    override fun isRealUserLoggedIn(): Boolean {
        val localUser = getCurrentUser()
        if (localUser != null) {
            return !localUser.isGuest
        }
        val fbUser = safeFirebaseUser()
        return fbUser != null && !fbUser.isAnonymous
    }

    override fun clearGuestSessionIfPresent() {
        val user = getCurrentUser()
        if (user != null && user.isGuest) {
            clearSessionPrefs()
        }
    }

    override fun getLastGoogleAccount(): Pair<String, String>? {
        val name = prefs.getString(KEY_LAST_GOOGLE_NAME, null)?.takeIf { it.isNotBlank() } ?: return null
        val email = prefs.getString(KEY_LAST_GOOGLE_EMAIL, null)?.takeIf { it.isNotBlank() } ?: return null
        return name to email
    }

    override suspend fun signInWithGoogleIdToken(
        idToken: String,
        displayName: String,
        email: String
    ): Result<AppUser> = withContext(Dispatchers.IO) {
        try {
            ensureFirebaseInitialized()
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = Firebase.auth.signInWithCredential(authCredential).await()
            val fbUser = authResult.user
            val resolvedEmail = (fbUser?.email ?: email).trim().lowercase()
            val resolvedName = (fbUser?.displayName ?: displayName).trim()
                .ifEmpty { resolvedEmail.substringBefore("@") }
            val resolvedUid = fbUser?.uid ?: "google_${hashText(resolvedEmail).take(16)}"

            val user = AppUser(
                uid = resolvedUid,
                displayName = resolvedName,
                email = resolvedEmail,
                isGuest = false,
                authProvider = PROVIDER_GOOGLE
            )
            prefs.edit()
                .putString(KEY_LAST_GOOGLE_NAME, resolvedName)
                .putString(KEY_LAST_GOOGLE_EMAIL, resolvedEmail)
                .commit()
            saveSession(user)
            Result.success(user)
        } catch (_: Exception) {
            signInWithGoogle(displayName, email)
        }
    }

    override suspend fun signInWithGoogle(displayName: String, email: String): Result<AppUser> =
        withContext(Dispatchers.IO) {
            val cleanName = displayName.trim()
            val normalizedEmail = email.trim().lowercase()
            if (cleanName.isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException(appContext.getString(R.string.error_empty_google_name))
                )
            }
            if (normalizedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                return@withContext Result.failure(
                    IllegalArgumentException(appContext.getString(R.string.error_invalid_google_email))
                )
            }

            val deterministicUid = "google_${hashText(normalizedEmail).take(16)}"
            val user = AppUser(
                uid = deterministicUid,
                displayName = cleanName,
                email = normalizedEmail,
                isGuest = false,
                authProvider = PROVIDER_GOOGLE
            )
            prefs.edit()
                .putString(KEY_LAST_GOOGLE_NAME, cleanName)
                .putString(KEY_LAST_GOOGLE_EMAIL, normalizedEmail)
                .commit()
            saveSession(user)
            Result.success(user)
        }

    override suspend fun loginWithEmail(email: String, password: String): Result<AppUser> =
        withContext(Dispatchers.IO) {
            val normalizedEmail = email.trim().lowercase()
            if (normalizedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                return@withContext Result.failure(
                    IllegalArgumentException(appContext.getString(R.string.error_invalid_email))
                )
            }
            if (password.length < 6) {
                return@withContext Result.failure(
                    IllegalArgumentException(appContext.getString(R.string.error_short_password))
                )
            }

            val existing = dao.getUserByEmail(normalizedEmail)
            val inputHash = hashText(password)

            if (NetworkUtils.isNetworkAvailable(appContext)) {
                try {
                    ensureFirebaseInitialized()
                    val authResult = withTimeoutOrNull(FIREBASE_AUTH_TIMEOUT_MS) {
                        Firebase.auth.signInWithEmailAndPassword(normalizedEmail, password).await()
                    }
                    val fbUser = authResult?.user
                    if (fbUser != null) {
                        val displayName = fbUser.displayName?.takeIf { it.isNotBlank() }
                            ?: existing?.displayName
                            ?: normalizedEmail.substringBefore("@").ifBlank { "Học sinh" }
                        dao.upsertUser(
                            LocalUserEntity(
                                email = normalizedEmail,
                                uid = fbUser.uid,
                                displayName = displayName,
                                passwordHash = inputHash
                            )
                        )
                        val user = AppUser(
                            uid = fbUser.uid,
                            displayName = displayName,
                            email = normalizedEmail,
                            isGuest = false,
                            authProvider = PROVIDER_EMAIL
                        )
                        saveSession(user)
                        return@withContext Result.success(user)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: FirebaseAuthInvalidCredentialsException) {
                    return@withContext Result.failure(
                        IllegalStateException("Mật khẩu chưa chính xác. Vui lòng thử lại.")
                    )
                } catch (_: FirebaseAuthInvalidUserException) {
                    if (existing == null) {
                        return@withContext Result.failure(
                            IllegalStateException("Không tìm thấy tài khoản với email này. Bạn hãy bấm \"Tạo tài khoản mới\" nhé!")
                        )
                    }
                    if (existing.passwordHash == inputHash) {
                        val syncedUid = tryCreateFirebaseUser(normalizedEmail, password) ?: existing.uid
                        if (syncedUid != existing.uid) {
                            dao.upsertUser(existing.copy(uid = syncedUid))
                        }
                        val user = AppUser(
                            uid = syncedUid,
                            displayName = existing.displayName,
                            email = existing.email,
                            isGuest = false,
                            authProvider = PROVIDER_EMAIL
                        )
                        saveSession(user)
                        return@withContext Result.success(user)
                    }
                    return@withContext Result.failure(
                        IllegalStateException("Mật khẩu chưa chính xác. Vui lòng thử lại.")
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase signInWithEmailAndPassword fallback to local Room", e)
                }
            }

            if (existing == null) {
                return@withContext Result.failure(
                    IllegalStateException("Không tìm thấy tài khoản với email này. Bạn hãy bấm \"Tạo tài khoản mới\" nhé!")
                )
            }
            if (existing.passwordHash != inputHash) {
                return@withContext Result.failure(
                    IllegalStateException("Mật khẩu chưa chính xác. Vui lòng thử lại.")
                )
            }
            val user = AppUser(
                uid = existing.uid,
                displayName = existing.displayName,
                email = existing.email,
                isGuest = false,
                authProvider = PROVIDER_EMAIL
            )
            saveSession(user)
            Result.success(user)
        }

    override suspend fun registerWithEmail(email: String, password: String): Result<AppUser> =
        withContext(Dispatchers.IO) {
            val normalizedEmail = email.trim().lowercase()
            if (normalizedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                return@withContext Result.failure(
                    IllegalArgumentException(appContext.getString(R.string.error_invalid_email))
                )
            }
            if (password.length < 6) {
                return@withContext Result.failure(
                    IllegalArgumentException(appContext.getString(R.string.error_short_password))
                )
            }
            val existing = dao.getUserByEmail(normalizedEmail)
            if (existing != null) {
                return@withContext Result.failure(
                    IllegalStateException("Email này đã được đăng ký rồi. Bạn hãy bấm \"Đăng nhập\" nhé!")
                )
            }

            var resolvedUid = "local_${UUID.randomUUID()}"
            if (NetworkUtils.isNetworkAvailable(appContext)) {
                try {
                    ensureFirebaseInitialized()
                    val authResult = withTimeoutOrNull(FIREBASE_AUTH_TIMEOUT_MS) {
                        Firebase.auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
                    }
                    authResult?.user?.uid?.let { fbUid ->
                        resolvedUid = fbUid
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: FirebaseAuthUserCollisionException) {
                    return@withContext Result.failure(
                        IllegalStateException("Email này đã được đăng ký rồi. Bạn hãy bấm \"Đăng nhập\" nhé!")
                    )
                } catch (_: FirebaseAuthWeakPasswordException) {
                    return@withContext Result.failure(
                        IllegalArgumentException(appContext.getString(R.string.error_short_password))
                    )
                } catch (_: FirebaseAuthInvalidCredentialsException) {
                    return@withContext Result.failure(
                        IllegalArgumentException(appContext.getString(R.string.error_invalid_email))
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase createUserWithEmailAndPassword fallback to local Room", e)
                }
            }

            val displayName = normalizedEmail.substringBefore("@").ifBlank { "Học sinh" }
            val entity = LocalUserEntity(
                email = normalizedEmail,
                uid = resolvedUid,
                displayName = displayName,
                passwordHash = hashText(password)
            )
            dao.upsertUser(entity)
            val user = AppUser(
                uid = entity.uid,
                displayName = entity.displayName,
                email = entity.email,
                isGuest = false,
                authProvider = PROVIDER_EMAIL
            )
            saveSession(user)
            Result.success(user)
        }

    private suspend fun tryCreateFirebaseUser(email: String, password: String): String? {
        return try {
            withTimeoutOrNull(FIREBASE_AUTH_TIMEOUT_MS) {
                Firebase.auth.createUserWithEmailAndPassword(email, password).await()
            }?.user?.uid
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun signInAnonymously(): Result<AppUser> = withContext(Dispatchers.IO) {
        val guestUid = prefs.getString(KEY_GUEST_UID, null) ?: "guest_${UUID.randomUUID()}".also {
            prefs.edit().putString(KEY_GUEST_UID, it).commit()
        }
        val user = AppUser(
            uid = guestUid,
            displayName = appContext.getString(R.string.account_guest_label),
            email = null,
            isGuest = true,
            authProvider = PROVIDER_ANONYMOUS
        )
        saveSession(user)
        Result.success(user)
    }

    override suspend fun signOut() {
        withContext(Dispatchers.IO) {
            clearSessionPrefs()
        }
        try {
            if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                Firebase.auth.signOut()
            }
        } catch (_: Exception) {
            // Safe ignore if Firebase isn't initialized in unit tests
        }
        val hasPlayServices = try {
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(appContext) ==
                ConnectionResult.SUCCESS
        } catch (_: Exception) {
            false
        }
        if (hasPlayServices) {
            try {
                withTimeoutOrNull(800L) {
                    CredentialManager.create(appContext)
                        .clearCredentialState(ClearCredentialStateRequest())
                }
            } catch (_: Exception) {
                // Safe ignore on environments without active credential providers
            }
        }
    }

    private fun ensureFirebaseInitialized() {
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext)
        }
    }

    private fun safeFirebaseUser() = try {
        if (FirebaseApp.getApps(appContext).isNotEmpty()) Firebase.auth.currentUser else null
    } catch (_: Exception) {
        null
    }

    private fun saveSession(user: AppUser) {
        prefs.edit()
            .putString(KEY_UID, user.uid)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_EMAIL, user.email)
            .putBoolean(KEY_IS_GUEST, user.isGuest)
            .putString(KEY_AUTH_PROVIDER, user.authProvider)
            .commit()
    }

    private fun clearSessionPrefs() {
        prefs.edit()
            .remove(KEY_UID)
            .remove(KEY_DISPLAY_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_IS_GUEST)
            .remove(KEY_AUTH_PROVIDER)
            .commit()
    }

    private fun hashText(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(raw.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val PROVIDER_GOOGLE = "google"
        const val PROVIDER_EMAIL = "email"
        const val PROVIDER_ANONYMOUS = "anonymous"

        private const val TAG = "LocalAuthRepository"
        private const val FIREBASE_AUTH_TIMEOUT_MS = 8_000L
        private const val PREFS_NAME = "onthi10_auth_session"
        private const val KEY_UID = "session_uid"
        private const val KEY_DISPLAY_NAME = "session_display_name"
        private const val KEY_EMAIL = "session_email"
        private const val KEY_IS_GUEST = "session_is_guest"
        private const val KEY_AUTH_PROVIDER = "session_auth_provider"
        private const val KEY_GUEST_UID = "persistent_guest_uid"
        private const val KEY_LAST_GOOGLE_NAME = "last_google_name"
        private const val KEY_LAST_GOOGLE_EMAIL = "last_google_email"
    }
}
