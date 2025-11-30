package com.hfad.playlistmaker.library.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.hfad.playlistmaker.R
import com.google.android.material.tabs.TabLayoutMediator
import com.hfad.playlistmaker.databinding.FragmentLibraryBinding
import com.hfad.playlistmaker.library.ui.adapter.LibraryPagerAdapter

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var tabMediator: TabLayoutMediator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupViewPager() {
        val adapter = LibraryPagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.favorites_tab)
                else -> getString(R.string.playlists_tab)
            }
        }
        tabMediator.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabMediator.detach()
        _binding = null
    }

    companion object {
        fun newInstance() = LibraryFragment()
    }
}