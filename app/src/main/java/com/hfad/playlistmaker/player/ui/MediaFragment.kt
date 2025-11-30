package com.hfad.playlistmaker.player.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.FragmentMediaBinding
import com.hfad.playlistmaker.player.domain.models.PlayerState
import com.hfad.playlistmaker.player.ui.adapter.PlaylistBottomSheetAdapter
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import com.hfad.playlistmaker.search.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.TextView
import android.view.Gravity
import com.hfad.playlistmaker.player.ui.model.AddToPlaylistStatus

class MediaFragment : Fragment(R.layout.fragment_media) {

    private val viewModel: MediaViewModel by viewModel()

    private var _binding: FragmentMediaBinding? = null
    private val binding get() = _binding!!

    private lateinit var track: Track
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var playlistsAdapter: PlaylistBottomSheetAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMediaBinding.bind(view)

        track = arguments?.getParcelable(TRACK_KEY) ?: run {
            findNavController().navigateUp()
            return
        }

        if (track.previewUrl.isBlank()) {
            binding.playButton.isEnabled = false
            return
        }

        viewModel.setTrack(track)

        initBottomSheet()
        observeViewModel()

        viewModel.playerScreenState.observe(viewLifecycleOwner) { state ->
            updatePlayButton(state.playerState)
            updateFavoriteButton(state.isFavorite)
            if (state.playerState == PlayerState.PREPARED) {
                updateProgress(0)
            }
            updateProgress(state.currentPosition)
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistsAdapter.updatePlaylists(playlists)
        }

        viewModel.preparePlayer(track.previewUrl)

        binding.playButton.setOnClickListener {
            viewModel.playPause()
        }

        binding.addToFavoritesButton.setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        binding.addToPlaylistButton.setOnClickListener {
            showPlaylistsBottomSheet()
        }

        binding.newPlaylistButton.setOnClickListener {
            hidePlaylistsBottomSheet()
            findNavController().navigate(R.id.action_mediaFragment_to_createPlaylistFragment)
        }

        displayTrackInfo(track)
        setupToolbar()

        viewModel.addToPlaylistStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is AddToPlaylistStatus.Success -> {
                    showCustomToast("Добавлено в плейлист ${status.playlistName}")
                    hidePlaylistsBottomSheet()
                }
                is AddToPlaylistStatus.TrackAlreadyExists -> {
                    showCustomToast("Трек уже добавлен в плейлист ${status.playlistName}")
                }
                is AddToPlaylistStatus.Error -> {
                    showCustomToast("Ошибка при добавлении в плейлист")
                }
            }
        }
    }

    private fun initBottomSheet() {
        playlistsAdapter = PlaylistBottomSheetAdapter(emptyList()) { playlist ->
            onPlaylistSelected(playlist)
        }
        binding.playlistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistsRecyclerView.adapter = playlistsAdapter

        bottomSheetBehavior = BottomSheetBehavior.from(binding.playlistsBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (!isAdded) return

                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            binding.overlay.visibility = View.GONE
                        }
                        else -> {
                            binding.overlay.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (!isAdded) return

                    binding.overlay.alpha = slideOffset.coerceAtLeast(0f)
                }
            })
        }

        binding.overlay.setOnClickListener {
            hidePlaylistsBottomSheet()
        }
    }

    private fun showPlaylistsBottomSheet() {
        viewModel.loadPlaylists()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hidePlaylistsBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun onPlaylistSelected(playlist: Playlist) {
        viewModel.addTrackToPlaylist(track, playlist)
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

    private fun setupToolbar() {
        binding.title.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updatePlayButton(state: PlayerState) {
        binding.playButton.isEnabled = state != PlayerState.DEFAULT

        val buttonResource = when (state) {
            PlayerState.PLAYING -> {
                R.drawable.button_pause
            }
            PlayerState.PREPARED, PlayerState.PAUSED -> {
                R.drawable.button_play
            }
            PlayerState.DEFAULT -> {
                R.drawable.button_play
            }
        }

        binding.playButton.setImageResource(buttonResource)
    }
    private fun observeViewModel() {
        viewModel.playerScreenState.observe(viewLifecycleOwner) { state ->
            updatePlayButton(state.playerState)
            updateFavoriteButton(state.isFavorite)
            if (state.playerState == PlayerState.PREPARED) {
                updateProgress(0)
            }
            updateProgress(state.currentPosition)
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistsAdapter.updatePlaylists(playlists)
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        binding.addToFavoritesButton.setImageResource(
            if (isFavorite) R.drawable.button_like_rad
            else R.drawable.button_like_gray
        )
    }

    private fun updateProgress(position: Int) {
        val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.progressText.text = timeFormat.format(position)
    }

    private fun displayTrackInfo(track: Track) {
        binding.trackName.text = track.trackName
        binding.artistName.text = track.artistName

        if (track.artworkUrl100.isNotEmpty()) {
            Glide.with(this)
                .load(track.getCoverArtwork())
                .placeholder(R.drawable.playsholder_play_light)
                .centerCrop()
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius_coverImage)))
                .into(binding.coverImage)
        } else {
            binding.coverImage.setImageResource(R.drawable.playsholder_play_light)
        }

        val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.trackTimeMillsValue.text = timeFormat.format(track.trackTimeMillis)
        binding.progressText.text = timeFormat.format(0)

        track.collectionName?.let {
            binding.collectionNameValue.text = it
        } ?: run {
            binding.collectionNameLabel.isVisible = false
            binding.collectionNameValue.isVisible = false
        }

        track.releaseDate?.let {
            binding.releaseDateValue.text = it.substring(0, 4)
        } ?: run {
            binding.releaseDateLabel.isVisible = false
            binding.releaseDateValue.isVisible = false
        }

        track.primaryGenreName?.let {
            binding.genreNameValue.text = it
        } ?: run {
            binding.genreNameLabel.isVisible = false
            binding.genreNameValue.isVisible = false
        }

        track.country?.let {
            binding.countryValue.text = it
        } ?: run {
            binding.countryLabel.isVisible = false
            binding.countryValue.isVisible = false
        }
    }

    override fun onPause() {
        super.onPause()
        val currentState = viewModel.playerScreenState.value
        if (currentState?.playerState == PlayerState.PLAYING) {
            viewModel.playPause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TRACK_KEY = "track"

        fun newInstance(track: Track): MediaFragment {
            return MediaFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(TRACK_KEY, track)
                }
            }
        }
    }
}