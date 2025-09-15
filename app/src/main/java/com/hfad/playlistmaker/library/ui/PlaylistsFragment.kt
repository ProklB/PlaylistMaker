package com.hfad.playlistmaker.library.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.FragmentPlaylistsBinding
import com.hfad.playlistmaker.library.ui.viewmodel.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by viewModel()

    companion object {
        fun newInstance() = PlaylistsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistsBinding.bind(view)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.buttonCreatePlaylist.setOnClickListener {
            // Обработка нажатия на кнопку
        }
    }

    private fun observeViewModel() {
        // дальше думать надо, пока заготовка
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}