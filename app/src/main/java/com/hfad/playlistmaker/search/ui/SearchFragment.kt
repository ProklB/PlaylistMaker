package com.hfad.playlistmaker.search.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private var currentText = ""
    private var lastSearchText = ""
    private var isClickDebounced = false
    private var isSearchPerformed = false

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { startSearch() }

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

        if (isSearchPerformed && lastSearchText.isNotEmpty()) {
            startSearch(lastSearchText)
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
            if (history.isNotEmpty() && !isSearchPerformed && currentText.isEmpty()) {
                showHistory(history)
            } else {
                hideHistory()
            }
        }
    }

    private fun showHistory(history: List<Track>) {
        binding.historyViewGroup.visibility = View.VISIBLE
        binding.trackList.visibility = View.GONE
        binding.placeholder.visibility = View.GONE
        historyAdapter.updateTracks(history)
    }

    private fun hideHistory() {
        binding.historyViewGroup.visibility = View.GONE
    }

    private fun onTrackClick(track: Track) {
        if (!isClickDebounced) {
            isClickDebounced = true
            viewModel.addTrackToHistory(track)
            startMediaFragment(track)

            Handler(Looper.getMainLooper()).postDelayed(
                { isClickDebounced = false },
                CLICK_DEBOUNCE_DELAY
            )
        }
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

        binding.trackList.visibility = View.GONE
        binding.historyViewGroup.visibility = View.GONE

        binding.clearButton.setOnClickListener {
            binding.searchEditText.text.clear()
            hideKeyboard()
            showPlaceholder(false)
            isSearchPerformed = false
            viewModel.loadSearchHistory()
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearSearchHistory()
        }
    }

    private fun setupListeners() {
        binding.searchEditText.addTextChangedListener { editable ->
            val text = editable?.toString() ?: ""
            binding.clearButton.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE

            if (text.isEmpty()) {
                isSearchPerformed = false
                viewModel.loadSearchHistory()
            } else {
                binding.historyViewGroup.visibility = View.GONE
                searchDebounce()
            }
            currentText = text
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
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
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
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

        binding.progressBar.visibility = View.VISIBLE
        binding.trackList.visibility = View.GONE
        binding.placeholder.visibility = View.GONE
        binding.historyViewGroup.visibility = View.GONE

        viewModel.searchTracks(query)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.trackList.visibility = View.GONE
        binding.placeholder.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        showPlaceholder(true, R.drawable.placeholder_no_results, getString(R.string.nothing_found))
        binding.updateButton.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        binding.progressBar.visibility = View.GONE
        showPlaceholder(true, R.drawable.placeholder_error, getString(R.string.server_error))
        binding.updateButton.visibility = View.VISIBLE
    }

    private fun showSearchResults(tracks: List<Track>) {
        binding.progressBar.visibility = View.GONE
        showPlaceholder(false)
        adapter.updateTracks(tracks)
        binding.trackList.visibility = View.VISIBLE
    }

    private fun showPlaceholder(show: Boolean, iconRes: Int = 0, text: String = "") {
        if (show) {
            binding.trackList.visibility = View.GONE
            binding.placeholder.visibility = View.VISIBLE
            if (iconRes != 0) binding.placeholderIcon.setImageResource(iconRes)
            if (text.isNotEmpty()) binding.placeholderText.text = text
        } else {
            binding.trackList.visibility = View.VISIBLE
            binding.placeholder.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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