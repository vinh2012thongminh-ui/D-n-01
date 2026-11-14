package com.vinhho.onthi10ai.ui.tutor

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.vinhho.onthi10ai.OnThiApplication
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.databinding.FragmentTutorBinding
import com.vinhho.onthi10ai.util.ImageUtils
import kotlinx.coroutines.launch

class TutorFragment : Fragment() {

    private var _binding: FragmentTutorBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var pendingCameraUri: Uri? = null

    private val viewModel: TutorViewModel by viewModels {
        val app = requireActivity().application as OnThiApplication
        TutorViewModel.Factory(
            app.aiRepository,
            app.authRepository,
            app.studyHistoryRepository
        )
    }

    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            showSelectedImage(uri)
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            showSelectedImage(uri)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Snackbar.make(binding.root, R.string.error_camera_permission, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeUiState()
    }

    private fun setupClickListeners() {
        binding.btnPickGallery.setOnClickListener {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnTakePhoto.setOnClickListener {
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (hasCameraPermission) {
                launchCamera()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnRemoveImage.setOnClickListener {
            clearSelectedImage()
        }

        binding.btnSolveAi.setOnClickListener {
            val questionText = binding.etQuestion.text?.toString().orEmpty()
            val imageUri = selectedImageUri

            if (questionText.isBlank() && imageUri == null) {
                Snackbar.make(binding.root, R.string.error_empty_question, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val bitmap = imageUri?.let { ImageUtils.decodeScaledBitmap(requireContext(), it) }
                viewModel.solveQuestion(questionText, bitmap)
            }
        }

        binding.btnCopySolution.setOnClickListener {
            val text = binding.tvSolutionBody.text?.toString().orEmpty()
            if (text.isNotBlank()) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("GiaSuAiSolution", text))
                Snackbar.make(binding.root, R.string.solution_copied, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchCamera() {
        val uri = ImageUtils.createCameraImageUri(requireContext())
        pendingCameraUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun showSelectedImage(uri: Uri) {
        selectedImageUri = uri
        binding.layoutImagePreview.visibility = View.VISIBLE
        binding.ivQuestionPreview.load(uri) {
            crossfade(true)
        }
    }

    private fun clearSelectedImage() {
        selectedImageUri = null
        binding.ivQuestionPreview.setImageDrawable(null)
        binding.layoutImagePreview.visibility = View.GONE
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is TutorUiState.Idle -> {
                            binding.layoutTutorLoading.visibility = View.GONE
                            binding.btnSolveAi.isEnabled = true
                        }
                        is TutorUiState.Loading -> {
                            binding.layoutTutorLoading.visibility = View.VISIBLE
                            binding.btnSolveAi.isEnabled = false
                        }
                        is TutorUiState.Success -> {
                            binding.layoutTutorLoading.visibility = View.GONE
                            binding.btnSolveAi.isEnabled = true
                            binding.cardSolution.visibility = View.VISIBLE
                            binding.chipDetectedSubject.text = getString(
                                R.string.tutor_detected_subject_format,
                                state.detectedSubject
                            )
                            binding.chipSolveMode.text = getString(
                                if (state.isOfflineSolved) R.string.tutor_mode_offline
                                else R.string.tutor_mode_online_hint
                            )
                            binding.tvSolutionBody.text = state.solution
                            binding.tvSavedNotice.visibility = if (state.savedToHistory) View.VISIBLE else View.GONE
                        }
                        is TutorUiState.Error -> {
                            binding.layoutTutorLoading.visibility = View.GONE
                            binding.btnSolveAi.isEnabled = true
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
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
