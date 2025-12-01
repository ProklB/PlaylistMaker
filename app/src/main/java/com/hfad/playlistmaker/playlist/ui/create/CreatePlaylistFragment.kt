package com.hfad.playlistmaker.playlist.ui.create

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.FragmentCreatePlaylistBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreatePlaylistViewModel by viewModel()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val copiedUri = copyImageToPrivateStorage(it)
            copiedUri?.let { copied ->
                viewModel.onCoverSelected(copied.toString())
            }
        }
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBackPress()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        setupToolbar()
        setupViews()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            handleBackPress()
        }
    }

    private fun setupViews() {
        binding.coverImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onNameChanged(s.toString())
            }
        })

        binding.descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onDescriptionChanged(s.toString())
            }
        })

        binding.createButton.setOnClickListener {
            binding.createButton.isEnabled = false

            viewModel.createPlaylist { success ->
                activity?.runOnUiThread {
                    if (success) {
                        showSuccessMessage()
                        findNavController().navigateUp()
                    } else {
                        showErrorMessage()
                        binding.createButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun showErrorMessage() {
        showCustomToast(getString(R.string.create_playlist_error))
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (binding.nameEditText.text?.toString() != state.name) {
                binding.nameEditText.setText(state.name)
            }
            if (binding.descriptionEditText.text?.toString() != state.description) {
                binding.descriptionEditText.setText(state.description)
            }
            binding.createButton.isEnabled = state.isCreateButtonEnabled
            binding.createButton.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (state.isCreateButtonEnabled) R.color.create_playlist_button_activ else R.color.create_playlist_button_inactiv
                )
            )

            state.coverPath?.let { coverPath ->
                loadCoverImage(coverPath)
            } ?: run {
                binding.coverImage.setImageResource(R.drawable.placeholder_newlist)
            }
        }
    }

    private fun loadCoverImage(coverPath: String) {
        Glide.with(this)
            .load(coverPath.toUri())
            .placeholder(R.drawable.placeholder_newlist)
            .centerCrop()
            .into(binding.coverImage)
    }

    private fun copyImageToPrivateStorage(uri: Uri): Uri? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "PLAYLIST_COVER_$timeStamp.jpg"

            val file = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showSuccessMessage() {
        val playlistName = viewModel.uiState.value?.name ?: ""
        val message = if (playlistName.isNotEmpty()) {
            getString(R.string.playlist_created_with_name, playlistName)
        } else {
            getString(R.string.playlist_created)
        }

        showCustomToast(message)
    }

    private fun showCustomToast(message: String) {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(R.layout.toast, null)

        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = message

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                resources.getDimensionPixelSize(R.dimen.toast_margin_start),
                resources.getDimensionPixelSize(R.dimen.toast_margin_top),
                resources.getDimensionPixelSize(R.dimen.toast_margin_end),
                resources.getDimensionPixelSize(R.dimen.toast_margin_bottom)
            )
        }
        layout.layoutParams = layoutParams

        with(Toast(requireContext())) {
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    private fun handleBackPress() {
        if (viewModel.hasUnsavedChanges()) {
            showUnsavedChangesDialog()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Завершить создание плейлиста?")
            .setMessage("Все несохраненные данные будут потеряны")
            .setPositiveButton("Завершить") { _, _ ->
                backPressedCallback.isEnabled = false
                findNavController().navigateUp()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback.remove()
        _binding = null
    }
}