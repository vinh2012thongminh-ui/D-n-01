package com.vinhho.onthi10ai.ui.login

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.vinhho.onthi10ai.OnThiApplication
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.data.repository.LocalAuthRepository
import com.vinhho.onthi10ai.databinding.ActivityLoginBinding
import com.vinhho.onthi10ai.databinding.DialogGoogleSigninBinding
import com.vinhho.onthi10ai.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        val app = application as OnThiApplication
        LoginViewModel.Factory(app.authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isNight
            isAppearanceLightNavigationBars = !isNight
        }

        // Only auto-enter MainActivity if the user signed in with a real Google/Email account.
        // Guest (anonymous) sessions never auto-bypass the Login screen.
        if (viewModel.isRealAccountLoggedIn()) {
            navigateToMain()
            return
        }
        viewModel.clearGuestSessionOnStartup()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsets()
        setupListeners()
        observeUiState()
    }

    private fun applyInsets() {
        val basePadding = resources.getDimensionPixelSize(R.dimen.login_padding_default)
        ViewCompat.setOnApplyWindowInsetsListener(binding.loginScrollView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top + basePadding,
                bottom = systemBars.bottom + basePadding
            )
            insets
        }
    }

    private fun setupListeners() {
        binding.etEmail.doAfterTextChanged {
            binding.tilEmail.error = null
        }
        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
        }

        binding.btnGoogleSignIn.setOnClickListener {
            launchGoogleSignInFlow()
        }

        binding.btnLogin.setOnClickListener {
            val (email, password) = validateEmailPasswordInputs() ?: return@setOnClickListener
            viewModel.loginWithEmail(email, password)
        }

        binding.btnRegister.setOnClickListener {
            val (email, password) = validateEmailPasswordInputs() ?: return@setOnClickListener
            viewModel.registerWithEmail(email, password)
        }

        // Anonymous / guest login ONLY happens when the user explicitly taps this button.
        binding.btnQuickGuest.setOnClickListener {
            viewModel.signInAnonymously()
        }
    }

    private fun validateEmailPasswordInputs(): Pair<String, String>? {
        val email = binding.etEmail.text?.toString().orEmpty().trim()
        val password = binding.etPassword.text?.toString().orEmpty()

        var isValid = true
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_short_password)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return if (isValid) email to password else null
    }

    private fun launchGoogleSignInFlow() {
        val webClientId = try {
            getString(R.string.default_web_client_id).trim()
        } catch (_: Exception) {
            ""
        }
        val hasPlayServices = try {
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) ==
                ConnectionResult.SUCCESS
        } catch (_: Exception) {
            false
        }

        if (webClientId.isEmpty() || !hasPlayServices) {
            showGoogleAccountPickerDialog()
            return
        }

        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@LoginActivity)
            try {
                // First attempt GetGoogleIdOption (separate single-option request per API 36 guidance)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(this@LoginActivity, request)
                handleGoogleCredentialResult(result.credential)
            } catch (e: NoCredentialException) {
                Log.w(TAG, "No authorized Google credentials found, trying interactive Sign-In with Google", e)
                tryInteractiveSignInWithGoogle(credentialManager, webClientId)
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled or dismissed: ${e.message}", e)
                setLoading(false)
            } catch (e: GetCredentialException) {
                Log.w(TAG, "CredentialManager unavailable on this device/preview, opening fallback picker", e)
                showGoogleAccountPickerDialog()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during Google Sign-In", e)
                showGoogleAccountPickerDialog()
            }
        }
    }

    private suspend fun tryInteractiveSignInWithGoogle(
        credentialManager: CredentialManager,
        webClientId: String
    ) {
        try {
            val signInOption = GetSignInWithGoogleOption.Builder(webClientId).build()
            val interactiveRequest = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()
            val result = credentialManager.getCredential(this@LoginActivity, interactiveRequest)
            handleGoogleCredentialResult(result.credential)
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Interactive Google Sign-In cancelled or dismissed: ${e.message}", e)
            setLoading(false)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Interactive CredentialManager unavailable, opening fallback picker", e)
            showGoogleAccountPickerDialog()
        } catch (e: Exception) {
            Log.e(TAG, "Interactive Google Sign-In error", e)
            showGoogleAccountPickerDialog()
        }
    }

    private fun handleGoogleCredentialResult(credential: androidx.credentials.Credential) {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
            val email = googleCred.id
            val name = googleCred.displayName?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@")
            viewModel.signInWithGoogleIdToken(googleCred.idToken, name, email)
        } else {
            showGoogleAccountPickerDialog()
        }
    }

    private fun showGoogleAccountPickerDialog() {
        val dialogBinding = DialogGoogleSigninBinding.inflate(layoutInflater)
        val lastAccount = viewModel.getLastGoogleAccount()
        if (lastAccount != null) {
            dialogBinding.etGoogleName.setText(lastAccount.first)
            dialogBinding.etGoogleEmail.setText(lastAccount.second)
        }
        dialogBinding.etGoogleName.doAfterTextChanged {
            dialogBinding.tilGoogleName.error = null
        }
        dialogBinding.etGoogleEmail.doAfterTextChanged {
            dialogBinding.tilGoogleEmail.error = null
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.google_dialog_confirm, null)
            .setNegativeButton(R.string.google_dialog_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.etGoogleName.text?.toString().orEmpty().trim()
                val email = dialogBinding.etGoogleEmail.text?.toString().orEmpty().trim()

                var valid = true
                if (name.isEmpty()) {
                    dialogBinding.tilGoogleName.error = getString(R.string.error_empty_google_name)
                    valid = false
                }
                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    dialogBinding.tilGoogleEmail.error = getString(R.string.error_invalid_google_email)
                    valid = false
                }
                if (valid) {
                    dialog.dismiss()
                    viewModel.signInWithGoogle(name, email)
                }
            }
        }
        dialog.show()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LoginUiState.Idle -> setLoading(false)
                        is LoginUiState.Loading -> setLoading(true)
                        is LoginUiState.Authenticated -> {
                            setLoading(false)
                            val welcomeMsg = when {
                                state.user.isGuest -> getString(R.string.welcome_guest_smooth)
                                state.user.authProvider == LocalAuthRepository.PROVIDER_GOOGLE ->
                                    getString(R.string.welcome_google_success, state.user.email ?: state.user.displayName)
                                else -> null
                            }
                            if (welcomeMsg != null) {
                                Toast.makeText(this@LoginActivity, welcomeMsg, Toast.LENGTH_SHORT).show()
                            }
                            navigateToMain()
                        }
                        is LoginUiState.Error -> {
                            setLoading(false)
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnGoogleSignIn.isEnabled = !isLoading
        binding.btnLogin.isEnabled = !isLoading
        binding.btnRegister.isEnabled = !isLoading
        binding.btnQuickGuest.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
