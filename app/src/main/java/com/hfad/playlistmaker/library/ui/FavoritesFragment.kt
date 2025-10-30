package com.hfad.playlistmaker.library.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.FragmentFavoritesBinding
import com.hfad.playlistmaker.library.ui.viewmodel.FavoritesState
import com.hfad.playlistmaker.library.ui.viewmodel.FavoritesViewModel
import com.hfad.playlistmaker.search.ui.adapter.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesViewModel by viewModel()

    private lateinit var adapter: TrackAdapter

    companion object {
        fun newInstance() = FavoritesFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoritesBinding.bind(view)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(emptyList()) { track ->
            onTrackClick(track)
        }

        val recyclerView = binding.root.findViewById<androidx.recyclerview.widget.RecyclerView>(
            R.id.favorites_recycler_view
        )

        if (recyclerView != null) {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
        }
    }

    private fun observeViewModel() {
        viewModel.favoritesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoritesState.Empty -> showEmptyState()
                is FavoritesState.Content -> showFavorites(state.tracks)
            }
        }
    }

    private fun showEmptyState() {
        binding.root.findViewById<View>(R.id.empty_state_view)?.visibility = View.VISIBLE
        binding.root.findViewById<androidx.recyclerview.widget.RecyclerView>(
            R.id.favorites_recycler_view
        )?.visibility = View.GONE
    }

    private fun showFavorites(tracks: List<com.hfad.playlistmaker.search.domain.models.Track>) {
        binding.root.findViewById<View>(R.id.empty_state_view)?.visibility = View.GONE
        val recyclerView = binding.root.findViewById<androidx.recyclerview.widget.RecyclerView>(
            R.id.favorites_recycler_view
        )
        recyclerView?.visibility = View.VISIBLE
        adapter.updateTracks(tracks)
    }

    private fun onTrackClick(track: com.hfad.playlistmaker.search.domain.models.Track) {
        val bundle = Bundle().apply {
            putParcelable("track", track)
        }
        findNavController().navigate(R.id.action_libraryFragment_to_mediaFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}