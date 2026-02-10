package com.hfad.playlistmaker.search.ui

import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.search.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val scope = MainScope()
    private val clickDebounceMap = mutableMapOf<Int, Job>()
    private val viewModel: SearchViewModel by viewModel()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SearchScreen(
                    viewModel = viewModel,
                    onTrackClick = { track -> onTrackClick(track) }
                )
            }
        }
    }

    private fun onTrackClick(track: Track) {
        val trackId = track.trackId

        if (clickDebounceMap[trackId]?.isActive == true) {
            return
        }

        startMediaFragment(track)

        val debounceJob = scope.launch {
            delay(CLICK_DEBOUNCE_DELAY)
            clickDebounceMap.remove(trackId)
        }

        clickDebounceMap[trackId] = debounceJob
    }

    private fun startMediaFragment(track: Track) {
        val bundle = Bundle().apply {
            putParcelable(TRACK_KEY, track)
        }
        findNavController().navigate(
            R.id.action_searchFragment_to_mediaFragment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanupCoroutines()
    }

    override fun onPause() {
        super.onPause()
        cleanupCoroutines()
    }

    private fun cleanupCoroutines() {
        clickDebounceMap.values.forEach { it.cancel() }
        clickDebounceMap.clear()
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 500L
        const val TRACK_KEY = "track"
    }
}