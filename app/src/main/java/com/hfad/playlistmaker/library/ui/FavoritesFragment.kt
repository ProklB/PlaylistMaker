package com.hfad.playlistmaker.library.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.FragmentFavoritesBinding
import com.hfad.playlistmaker.library.ui.viewmodel.FavoritesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesViewModel by viewModel()

    companion object {
        fun newInstance() = FavoritesFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoritesBinding.bind(view)

        observeViewModel()
    }

    private fun observeViewModel() {
        // Здесь будет наблюдение за состоянием ViewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}