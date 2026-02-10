package com.hfad.playlistmaker.library.ui

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.hfad.playlistmaker.library.ui.compose.LibraryScreen
import com.hfad.playlistmaker.library.ui.compose.FavoritesScreen
import com.hfad.playlistmaker.library.ui.compose.PlaylistsScreen
import com.hfad.playlistmaker.library.ui.viewmodel.FavoritesViewModel
import com.hfad.playlistmaker.library.ui.viewmodel.PlaylistsViewModel
import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import androidx.navigation.fragment.findNavController
import com.hfad.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModel()
    private val playlistsViewModel: PlaylistsViewModel by viewModel()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LibraryScreen(
                    favoritesContent = {
                        FavoritesScreen(
                            favoritesState = favoritesViewModel.favoritesState,
                            onTrackClick = { track -> onTrackClick(track) }
                        )
                    },
                    playlistsContent = {
                        PlaylistsScreen(
                            playlistsState = playlistsViewModel.playlistsState,
                            onCreatePlaylistClick = {
                                findNavController().navigate(R.id.action_libraryFragment_to_createPlaylistFragment)
                            },
                            onPlaylistClick = { playlist -> onPlaylistClick(playlist) }
                        )
                    },
                    showFavorites = true
                )
            }
        }
    }

    private fun onTrackClick(track: Track) {
        val bundle = Bundle().apply {
            putParcelable("track", track)
        }
        findNavController().navigate(R.id.action_libraryFragment_to_mediaFragment, bundle)
    }

    private fun onPlaylistClick(playlist: Playlist) {
        val bundle = Bundle().apply {
            putLong("playlist_id", playlist.id)
        }
        findNavController().navigate(R.id.action_libraryFragment_to_playlistDetailsFragment, bundle)
    }

    override fun onResume() {
        super.onResume()
        favoritesViewModel.loadFavoriteTracks()
        playlistsViewModel.loadPlaylists()
    }

    companion object {
        fun newInstance() = LibraryFragment()
    }
}