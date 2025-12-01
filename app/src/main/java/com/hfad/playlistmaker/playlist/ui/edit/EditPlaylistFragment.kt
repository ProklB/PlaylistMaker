package com.hfad.playlistmaker.playlist.ui.edit

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat

class EditPlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditPlaylistViewModel by viewModel()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val copiedUri = copyImageToPrivateStorage(it)
            copiedUri?.let { copied ->
                viewModel.onCoverSelected(copied.toString())
            }
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
        _binding = FragmentCreatePlaylistBinding.bind(view)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )

        val playlistId = arguments?.getLong("playlist_id", -1L) ?: -1L
        if (playlistId == -1L) {
            findNavController().navigateUp()
            return
        }

        viewModel.loadPlaylist(playlistId)

        setupToolbar()
        setupViews()
        observeViewModel()
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().navigateUp()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.edit_playlist)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
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

        binding.createButton.text = getString(R.string.save)
        binding.createButton.setOnClickListener {
            binding.createButton.isEnabled = false

            viewModel.savePlaylist { success ->
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
        showCustomToast(getString(R.string.save_playlist_error))
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (binding.nameEditText.text?.toString() != state.name) {
                binding.nameEditText.setText(state.name)
            }
            if (binding.descriptionEditText.text?.toString() != state.description) {
                binding.descriptionEditText.setText(state.description)
            }
            binding.createButton.isEnabled = state.isSaveButtonEnabled
            binding.createButton.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (state.isSaveButtonEnabled) R.color.create_playlist_button_activ else R.color.create_playlist_button_inactiv
                )
            )

            state.coverPath?.let { coverPath ->
                loadCoverImage(coverPath)
            } ?: run {
                binding.coverImage.setImageResource(R.drawable.placeholder_newlist)
            }
        }

        viewModel.playlistLoaded.observe(viewLifecycleOwner) { loaded ->
            if (!loaded) {
                findNavController().navigateUp()
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
            "Плейлист \"$playlistName\" сохранен"
        } else {
            "Плейлист сохранен"
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

    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback.remove()  // Не забудьте удалить callback
        _binding = null
    }
}