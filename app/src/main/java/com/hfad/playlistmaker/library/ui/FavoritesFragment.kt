package com.hfad.playlistmaker.library.ui

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.library.ui.compose.FavoritesScreen
import com.hfad.playlistmaker.library.ui.viewmodel.FavoritesViewModel
import com.hfad.playlistmaker.search.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private val viewModel: FavoritesViewModel by viewModel()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FavoritesScreen(
                    favoritesState = viewModel.favoritesState,
                    onTrackClick = { track -> onTrackClick(track) }
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

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}