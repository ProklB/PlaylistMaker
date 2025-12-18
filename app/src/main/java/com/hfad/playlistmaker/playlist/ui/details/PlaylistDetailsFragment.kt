package com.hfad.playlistmaker.playlist.ui.details

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.FragmentPlaylistDetailsBinding
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import com.hfad.playlistmaker.playlist.ui.details.viewmodel.PlaylistDetailsState
import com.hfad.playlistmaker.playlist.ui.details.viewmodel.PlaylistDetailsViewModel
import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.search.ui.adapter.TrackAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.TextView
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class PlaylistDetailsFragment : Fragment(R.layout.fragment_playlist_details) {

    private var _binding: FragmentPlaylistDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistDetailsViewModel by viewModel()

    private lateinit var tracksAdapter: TrackAdapter
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var playlistId: Long = -1L

    private val menuBottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (_binding == null) return

            when (newState) {
                BottomSheetBehavior.STATE_HIDDEN -> {
                    binding.overlay.visibility = View.GONE
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    binding.overlay.visibility = View.VISIBLE
                }
                else -> {
                    binding.overlay.visibility = View.VISIBLE
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (_binding == null) return

            binding.overlay.alpha = slideOffset.coerceAtLeast(0f)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistDetailsBinding.bind(view)

        playlistId = arguments?.getLong("playlist_id", -1L) ?: -1L
        if (playlistId == -1L) {
            findNavController().navigateUp()
            return
        }

        viewModel.navigateToLibrary.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigateUp()
                viewModel.onNavigationComplete()
            }
        }

        setupToolbar()
        setupBottomSheets()
        setupObservers()
        viewModel.loadPlaylist(playlistId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupBottomSheets() {
        tracksAdapter = TrackAdapter(emptyList()) { track ->
            onTrackClick(track)
        }

        tracksAdapter.setOnLongClickListener { track ->
            showDeleteTrackDialog(track)
            true
        }

        binding.playlistTracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistTracksRecyclerView.adapter = tracksAdapter

        setupMenuBottomSheet()
    }

    private fun setupMenuBottomSheet() {
        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetMenu).apply {
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN
            addBottomSheetCallback(menuBottomSheetCallback)
        }

        binding.overlay.setOnClickListener {
            hideMenuBottomSheet()
        }

        binding.menuShare.setOnClickListener {
            onShareClicked()
        }

        binding.menuEdit.setOnClickListener {
            hideMenuBottomSheet()
            val currentState = viewModel.playlistDetailsState.value
            if (currentState is PlaylistDetailsState.Content) {
                val bundle = Bundle().apply {
                    putLong("playlist_id", currentState.playlist.id)
                }
                findNavController().navigate(
                    R.id.action_playlistDetailsFragment_to_editPlaylistFragment,
                    bundle
                )
            }
        }

        binding.menuDelete.setOnClickListener {
            showDeletePlaylistDialog()
        }
    }

    private fun showMenuBottomSheet() {
        val currentState = viewModel.playlistDetailsState.value
        if (currentState is PlaylistDetailsState.Content) {
            binding.menuPlaylistTitle.text = currentState.playlist.name

            val trackCount = currentState.playlist.trackCount
            val trackCountText = resources.getQuantityString(
                R.plurals.tracks_count,
                trackCount,
                trackCount
            )
            binding.menuPlaylistInfo.text = trackCountText

            currentState.playlist.coverPath?.let { coverPath ->
                Glide.with(this)
                    .load(coverPath)
                    .placeholder(R.drawable.playsholder_play_light)
                    .centerCrop()
                    .into(binding.menuPlaylistCover)
            } ?: run {
                binding.menuPlaylistCover.setImageResource(R.drawable.playsholder_play_light)
            }
        }
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideMenuBottomSheet() {
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun onShareClicked() {
        hideMenuBottomSheet()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val currentState = viewModel.playlistDetailsState.value
                if (currentState is PlaylistDetailsState.Content) {
                    if (currentState.tracks.isEmpty()) {
                        showToast(getString(R.string.share_playlist_empty))
                    } else {
                        val shareText = getShareText(currentState.playlist, currentState.tracks)
                        sharePlaylist(shareText)
                    }
                }
            } catch (e: Exception) {
                showToast(getString(R.string.share_playlist_error))
            }
        }
    }

    private fun getShareText(playlist: Playlist, tracks: List<Track>): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append(playlist.name)
        stringBuilder.append("\n")

        playlist.description?.let { description ->
            stringBuilder.append(description)
            stringBuilder.append("\n")
        }

        val trackCountText = when (playlist.trackCount) {
            1 -> "[01] трек"
            in 2..4 -> "[${String.format("%02d", playlist.trackCount)}] трека"
            else -> "[${String.format("%02d", playlist.trackCount)}] треков"
        }
        stringBuilder.append(trackCountText)
        stringBuilder.append("\n\n")

        tracks.forEachIndexed { index, track ->
            val trackNumber = index + 1
            val duration = formatTrackDuration(track.trackTimeMillis)

            stringBuilder.append("${String.format("%02d", trackNumber)}. ${track.artistName} - ${track.trackName} ($duration)")
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private fun formatTrackDuration(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun sharePlaylist(shareText: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareChooser = Intent.createChooser(shareIntent, getString(R.string.share_playlist_chooser))
        startActivity(shareChooser)
    }

    private fun showDeletePlaylistDialog() {
        hideMenuBottomSheet()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.delete_playlist_confirm))
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                viewModel.deletePlaylist()
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(requireContext(), R.color.dialog_background_white))
            )

            val messageView = dialog.findViewById<TextView>(android.R.id.message)
            messageView?.let {
                it.setTextColor(ContextCompat.getColor(requireContext(), R.color.dialog_text_dark))
                it.textSize = 14f
            }

            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.dialog_button_blue))
            negativeButton.textSize = 14f

            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.dialog_button_blue))
            positiveButton.textSize = 14f
        }

        dialog.show()
    }

    private fun showDeleteTrackDialog(track: Track) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.delete_track_confirm))
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                viewModel.removeTrack(track.trackId)
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(requireContext(), R.color.dialog_background_white))
            )

            val messageView = dialog.findViewById<TextView>(android.R.id.message)
            messageView?.let {
                it.setTextColor(ContextCompat.getColor(requireContext(), R.color.dialog_text_dark))
                it.textSize = 14f
            }

            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.dialog_button_blue))
            negativeButton.textSize = 14f

            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.dialog_button_blue))
            positiveButton.textSize = 14f
        }

        dialog.show()
    }

    private fun onTrackClick(track: Track) {
        val bundle = Bundle().apply {
            putParcelable("track", track)
        }
        findNavController().navigate(R.id.action_playlistDetailsFragment_to_mediaFragment, bundle)
    }

    private fun setupObservers() {
        viewModel.playlistDetailsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistDetailsState.Loading -> showLoading()
                is PlaylistDetailsState.Content -> {
                    showPlaylistDetails(state)
                    showTracksBottomSheet(state.tracks)
                }
                is PlaylistDetailsState.Error -> showError()
            }
        }
    }

    private fun showTracksBottomSheet(tracks: List<Track>) {
        if (tracks.isEmpty()) {
            binding.emptyTracksView.visibility = View.VISIBLE
            binding.playlistTracksRecyclerView.visibility = View.GONE
        } else {
            binding.emptyTracksView.visibility = View.GONE
            binding.playlistTracksRecyclerView.visibility = View.VISIBLE
            tracksAdapter.updateTracks(tracks)
        }
        binding.playlistTracksBottomSheet.visibility = View.VISIBLE
    }

    private fun showPlaylistDetails(state: PlaylistDetailsState.Content) {
        binding.playlistName.text = state.playlist.name
        binding.playlistDescription.text = state.playlist.description ?: ""
        binding.trackCount.text = resources.getQuantityString(
            R.plurals.tracks_count,
            state.playlist.trackCount,
            state.playlist.trackCount
        )

        val totalDuration = state.totalDuration
        val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(totalDuration)
        binding.playlistDuration.text = getString(R.string.playlist_duration_format, minutes)

        state.playlist.coverPath?.let { coverPath ->
            Glide.with(this)
                .load(coverPath)
                .placeholder(R.drawable.playsholder_play_light)
                .centerCrop()
                .into(binding.playlistCover)
        } ?: run {
            binding.playlistCover.setImageResource(R.drawable.playsholder_play_light)
        }

        binding.playlistDescription.visibility =
            if (state.playlist.description.isNullOrEmpty()) View.GONE else View.VISIBLE

        binding.shareButton.setOnClickListener {
            if (state.tracks.isEmpty()) {
                showToast(getString(R.string.share_playlist_empty))
            } else {
                val shareText = getShareText(state.playlist, state.tracks)
                sharePlaylist(shareText)
            }
        }

        binding.menuButton.setOnClickListener {
            showMenuBottomSheet()
        }
    }

    private fun showLoading() {
    }

    private fun showError() {
        findNavController().navigateUp()
    }

    private fun showToast(message: String) {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(R.layout.toast_playlist_details, null)

        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = message

        with(Toast(requireContext())) {
            setGravity((Gravity.BOTTOM), 0, 100)
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        menuBottomSheetBehavior.removeBottomSheetCallback(menuBottomSheetCallback)
        _binding = null
    }

    companion object {
        const val PLAYLIST_ID_KEY = "playlist_id"

        fun newInstance(playlistId: Long): PlaylistDetailsFragment {
            return PlaylistDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(PLAYLIST_ID_KEY, playlistId)
                }
            }
        }
    }
}