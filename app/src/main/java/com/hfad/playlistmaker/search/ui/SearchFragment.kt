package com.hfad.playlistmaker.search.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.FragmentSearchBinding
import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.search.ui.adapter.TrackAdapter
import com.hfad.playlistmaker.search.ui.viewmodel.SearchState
import com.hfad.playlistmaker.search.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.content.IntentFilter
import com.hfad.playlistmaker.utils.NetworkChangeReceiver
import android.content.BroadcastReceiver

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private var currentText = ""
    private var lastSearchText = ""
    private var isSearchPerformed = false

    private val scope = MainScope()
    private var searchJob: Job? = null
    private val clickDebounceMap = mutableMapOf<Int, Job>()

    private var networkChangeReceiver: BroadcastReceiver? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        adapter = TrackAdapter(emptyList()) { track -> onTrackClick(track) }
        historyAdapter = TrackAdapter(emptyList()) { track -> onTrackClick(track) }

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = historyAdapter

        restoreState(savedInstanceState)

        setupViews()
        setupObservers()
        setupListeners()

        if (currentText.isEmpty()) {
            resetSearchState()
            showHistoryIfAvailable()
        } else if (isSearchPerformed && lastSearchText.isNotEmpty()) {
            startSearch(lastSearchText)
        } else {
            resetSearchState()
            showHistoryIfAvailable()
        }
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            currentText = it.getString(KEY_CURRENT_TEXT, "")
            lastSearchText = it.getString(KEY_LAST_SEARCH_TEXT, "")
            isSearchPerformed = it.getBoolean(KEY_IS_SEARCH_PERFORMED, false)
            binding.searchEditText.setText(currentText)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_TEXT, currentText)
        outState.putString(KEY_LAST_SEARCH_TEXT, lastSearchText)
        outState.putBoolean(KEY_IS_SEARCH_PERFORMED, isSearchPerformed)
        if (currentText.isEmpty()) {
            outState.putBoolean(KEY_IS_SEARCH_PERFORMED, false)
        }
    }

    private fun setupObservers() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Loading -> showLoading()
                is SearchState.Empty -> {
                    showEmptyState()
                    isSearchPerformed = true
                }
                is SearchState.Error -> {
                    showErrorState(state.message)
                    isSearchPerformed = true
                }
                is SearchState.Content -> {
                    showSearchResults(state.tracks)
                    isSearchPerformed = true
                }
            }
        }

        viewModel.historyState.observe(viewLifecycleOwner) { history ->
            if (!isSearchPerformed && currentText.isEmpty()) {
                if (history.isNotEmpty()) {
                    showHistory(history)
                } else {
                    hideAllSearchViews()
                }
            }
        }
    }

private fun showHistoryIfAvailable() {
    viewModel.loadSearchHistory()
}

    private fun hideAllSearchViews() {
        binding.historyViewGroup.isVisible = false
        binding.trackList.isVisible = false
        binding.placeholder.isVisible = false
        binding.progressBar.isVisible = false
    }

    private fun showHistory(history: List<Track>) {
        hideAllSearchViews()
        binding.historyViewGroup.isVisible = true
        historyAdapter.updateTracks(history)
    }

    private fun resetSearchState() {
        isSearchPerformed = false
        lastSearchText = ""
        binding.trackList.isVisible = false
        binding.placeholder.isVisible = false
        binding.progressBar.isVisible = false
        viewModel.loadSearchHistory()
    }

    private fun onTrackClick(track: Track) {
        val trackId = track.trackId

        if (clickDebounceMap[trackId]?.isActive == true) {
            return
        }

        viewModel.addTrackToHistory(track)
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
        findNavController().navigate(R.id.action_searchFragment_to_mediaFragment, bundle)
    }

    private fun setupViews() {
        adapter = TrackAdapter(emptyList()) { track -> onTrackClick(track) }
        historyAdapter = TrackAdapter(emptyList()) { track -> onTrackClick(track) }

        binding.trackList.layoutManager = LinearLayoutManager(requireContext())
        binding.trackList.adapter = adapter

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = historyAdapter

        binding.trackList.isVisible = false
        binding.historyViewGroup.isVisible = false

        binding.clearButton.setOnClickListener {
            binding.searchEditText.text.clear()
            hideKeyboard()
            resetSearchState()
            showHistoryIfAvailable()
            searchJob?.cancel()
            searchJob = null
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearSearchHistory()
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentText.isEmpty()) {
            resetSearchState()
            showHistoryIfAvailable()
        }
        registerNetworkReceiver()
    }

    private fun setupListeners() {
        binding.searchEditText.addTextChangedListener { editable ->
            val text = editable?.toString() ?: ""
            binding.clearButton.isVisible = if (text.isEmpty()) false else true

            if (text.isEmpty()) {
                resetSearchState()
                showHistoryIfAvailable()
                searchJob?.cancel()
                searchJob = null
            } else {
                binding.historyViewGroup.isVisible = false
                searchDebounce()
            }
            currentText = text
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchJob?.cancel()
                searchJob = null
                startSearch()
                true
            } else {
                false
            }
        }

        binding.updateButton.setOnClickListener {
            if (lastSearchText.isNotEmpty()) {
                startSearch(lastSearchText)
            }
        }
    }

    private fun searchDebounce() {
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            startSearch()
        }
    }

    private fun startSearch(query: String = binding.searchEditText.text.toString()) {
        if (query.isEmpty()) {
            isSearchPerformed = false
            viewModel.loadSearchHistory()
            return
        }

        lastSearchText = query
        isSearchPerformed = true
        hideKeyboard()

        binding.progressBar.isVisible = true
        binding.trackList.isVisible = false
        binding.placeholder.isVisible = false
        binding.historyViewGroup.isVisible = false

        viewModel.searchTracks(query)
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.trackList.isVisible = false
        binding.placeholder.isVisible = false
    }

    private fun showEmptyState() {
        binding.progressBar.isVisible = false
        showPlaceholder(true, R.drawable.placeholder_no_results, getString(R.string.nothing_found))
        binding.updateButton.isVisible = false
    }

    private fun showErrorState(message: String) {
        binding.progressBar.isVisible = false
        showPlaceholder(true, R.drawable.placeholder_error, getString(R.string.server_error))
        binding.updateButton.isVisible = true
    }

    private fun showSearchResults(tracks: List<Track>) {
        binding.progressBar.isVisible = false
        hideAllSearchViews()
        adapter.updateTracks(tracks)
        binding.trackList.isVisible = true
    }

    private fun showPlaceholder(show: Boolean, iconRes: Int = 0, text: String = "") {
        if (show) {
            binding.trackList.isVisible = false
            binding.placeholder.isVisible = true
            if (iconRes != 0) binding.placeholderIcon.setImageResource(iconRes)
            if (text.isNotEmpty()) binding.placeholderText.text = text
        } else {
            binding.trackList.isVisible = true
            binding.placeholder.isVisible = false
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun registerNetworkReceiver() {
        if (networkChangeReceiver == null) {
            networkChangeReceiver = NetworkChangeReceiver()

            val filter = IntentFilter().apply {
                addAction(NetworkChangeReceiver.CONNECTIVITY_ACTION)
            }

            requireContext().registerReceiver(networkChangeReceiver, filter)
        }
    }

    private fun unregisterNetworkReceiver() {
        networkChangeReceiver?.let {
            requireContext().unregisterReceiver(it)
            networkChangeReceiver = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanupCoroutines()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        cleanupCoroutines()
        unregisterNetworkReceiver()
    }

    private fun cleanupCoroutines() {
        searchJob?.cancel()
        searchJob = null
        clickDebounceMap.values.forEach { it.cancel() }
        clickDebounceMap.clear()
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 500L
        const val TRACK_KEY = "track"

        private const val KEY_CURRENT_TEXT = "current_text"
        private const val KEY_LAST_SEARCH_TEXT = "last_search_text"
        private const val KEY_IS_SEARCH_PERFORMED = "is_search_performed"

        fun newInstance() = SearchFragment()
    }
}