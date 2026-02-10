package com.hfad.playlistmaker.library.ui

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.library.ui.compose.PlaylistsScreen
import com.hfad.playlistmaker.library.ui.viewmodel.PlaylistsViewModel
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private val viewModel: PlaylistsViewModel by viewModel()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PlaylistsScreen(
                    playlistsState = viewModel.playlistsState,
                    onCreatePlaylistClick = {
                        findNavController().navigate(R.id.action_libraryFragment_to_createPlaylistFragment)
                    },
                    onPlaylistClick = { playlist -> onPlaylistClick(playlist) }
                )
            }
        }
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

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}