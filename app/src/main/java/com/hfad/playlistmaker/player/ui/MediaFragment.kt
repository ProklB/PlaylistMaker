package com.hfad.playlistmaker.player.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.FragmentMediaBinding
import com.hfad.playlistmaker.player.domain.models.PlayerState
import com.hfad.playlistmaker.search.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class MediaFragment : Fragment(R.layout.fragment_media) {

    private val viewModel: MediaViewModel by viewModel()

    private var _binding: FragmentMediaBinding? = null
    private val binding get() = _binding!!

    private lateinit var track: Track

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMediaBinding.bind(view)

        track = arguments?.getParcelable(TRACK_KEY) ?: run {
            findNavController().navigateUp()
            return
        }

        viewModel.playerState.observe(viewLifecycleOwner) { state ->
            updatePlayButton(state)
        }

        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            updateProgress(position)
        }

        viewModel.preparePlayer(track.previewUrl)

        binding.playButton.setOnClickListener {
            viewModel.playPause()
        }

        displayTrackInfo(track)

        setupToolbar()
    }

    private fun setupToolbar() {
        binding.title.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updatePlayButton(state: PlayerState) {
        binding.playButton.isEnabled = state != PlayerState.DEFAULT
        binding.playButton.setImageResource(
            if (state == PlayerState.PLAYING) R.drawable.button_pause
            else R.drawable.button_play
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
            binding.collectionNameLabel.visibility = View.GONE
            binding.collectionNameValue.visibility = View.GONE
        }

        track.releaseDate?.let {
            binding.releaseDateValue.text = it.substring(0, 4)
        } ?: run {
            binding.releaseDateLabel.visibility = View.GONE
            binding.releaseDateValue.visibility = View.GONE
        }

        track.primaryGenreName?.let {
            binding.genreNameValue.text = it
        } ?: run {
            binding.genreNameLabel.visibility = View.GONE
            binding.genreNameValue.visibility = View.GONE
        }

        track.country?.let {
            binding.countryValue.text = it
        } ?: run {
            binding.countryLabel.visibility = View.GONE
            binding.countryValue.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.playerState.value == PlayerState.PLAYING) {
            viewModel.playPause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TRACK_KEY = "track"  // Должен совпадать с ключом в SearchFragment

        fun newInstance(track: Track): MediaFragment {
            return MediaFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(TRACK_KEY, track)
                }
            }
        }
    }
}