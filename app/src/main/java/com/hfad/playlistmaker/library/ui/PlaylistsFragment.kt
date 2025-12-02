package com.hfad.playlistmaker.library.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.FragmentPlaylistsBinding
import com.hfad.playlistmaker.library.ui.adapter.PlaylistAdapter
import com.hfad.playlistmaker.library.ui.viewmodel.PlaylistsState
import com.hfad.playlistmaker.library.ui.viewmodel.PlaylistsViewModel
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var adapter: PlaylistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistsBinding.bind(view)

        setupRecyclerView()
        setupViews()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = PlaylistAdapter(emptyList()) { playlist ->
            onPlaylistClick(playlist)
        }

        binding.playlistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRecyclerView.adapter = adapter
    }

    private fun setupViews() {
        binding.buttonCreatePlaylist.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_createPlaylistFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.playlistsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistsState.Empty -> showEmptyState()
                is PlaylistsState.Content -> showPlaylists(state.playlists)
            }
        }
    }

    private fun showEmptyState() {
        binding.emptyStateView.visibility = View.VISIBLE
        binding.playlistsRecyclerView.visibility = View.GONE
    }

    private fun showPlaylists(playlists: List<com.hfad.playlistmaker.playlist.domain.models.Playlist>) {
        binding.emptyStateView.visibility = View.GONE
        binding.playlistsRecyclerView.visibility = View.VISIBLE
        adapter.updatePlaylists(playlists)
    }

    private fun onPlaylistClick(playlist: Playlist) {
        val bundle = Bundle().apply {
            putLong("playlist_id", playlist.id)
        }
        findNavController().navigate(R.id.action_libraryFragment_to_playlistDetailsFragment, bundle)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}