package com.hfad.playlistmaker.presentation.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.hfad.playlistmaker.Creator
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.ActivitySearchBinding
import com.hfad.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.hfad.playlistmaker.domain.interactor.SearchInteractor
import com.hfad.playlistmaker.domain.models.Track
import com.hfad.playlistmaker.presentation.media.MediaActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchInteractor: SearchInteractor
    private lateinit var searchHistoryInteractor: SearchHistoryInteractor
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private var currentText = ""
    private var lastSearchText = ""
    private var isClickDebounced = false

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { startSearch() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchInteractor = Creator.provideSearchInteractor()
        searchHistoryInteractor = Creator.provideSearchHistoryInteractor(this)

        adapter = TrackAdapter(emptyList()) { track ->
            onTrackClick(track)
            searchHistoryInteractor.addTrackToHistory(track)
        }

        historyAdapter = TrackAdapter(emptyList()) { track ->
            onTrackClick(track)
        }

        setupViews()
        setupToolbar()
        setupListeners()
        restoreState(savedInstanceState)
        showHistoryIfEmpty()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.title)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupViews() {
        binding.trackList.layoutManager = LinearLayoutManager(this)
        binding.trackList.adapter = adapter

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        binding.trackList.visibility = View.GONE
        binding.historyViewGroup.visibility = View.GONE

        binding.clearButton.setOnClickListener {
            binding.searchEditText.text.clear()
            hideKeyboard()
            showPlaceholder(false)
            showHistoryIfEmpty()
        }

        binding.clearHistoryButton.setOnClickListener {
            searchHistoryInteractor.clearSearchHistory()
            binding.historyViewGroup.visibility = View.GONE
        }
    }

    private fun showHistoryIfEmpty() {
        if (binding.searchEditText.text.isNullOrEmpty()) {
            val history = searchHistoryInteractor.getSearchHistory()
            if (history.isNotEmpty()) {
                binding.historyViewGroup.visibility = View.VISIBLE
                binding.trackList.visibility = View.GONE
                binding.placeholder.visibility = View.GONE
                historyAdapter.updateTracks(history)
            } else {
                binding.historyViewGroup.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                if (s.isNullOrEmpty()) {
                    showHistoryIfEmpty()
                } else {
                    binding.historyViewGroup.visibility = View.GONE
                    searchDebounce()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                currentText = s.toString()
            }
        })

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                startSearch()
                true
            } else {
                false
            }
        }

        binding.searchEditText.setOnClickListener {
            if (binding.searchEditText.text.isNullOrEmpty()) {
                showHistoryIfEmpty()
            }
        }

        binding.updateButton.setOnClickListener {
            if (lastSearchText.isNotEmpty()) {
                startSearch(lastSearchText)
            }
        }
    }

    private fun showHistory() {
        val history = searchHistoryInteractor.getSearchHistory()
        if (history.isEmpty()) {
            binding.historyViewGroup.visibility = View.GONE
            binding.clearHistoryButton.visibility = View.VISIBLE
        } else {
            binding.historyViewGroup.visibility = View.VISIBLE
            binding.clearHistoryButton.visibility = View.GONE
            historyAdapter.updateTracks(history)
            binding.trackList.adapter = historyAdapter
        }
    }

    private fun showSearchResults() {
        binding.historyViewGroup.visibility = View.GONE
        binding.trackList.visibility = View.VISIBLE
        binding.trackList.adapter = adapter
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun startSearch(query: String = binding.searchEditText.text.toString()) {
        if (query.isEmpty()) {
            showHistory()
            return
        }

        lastSearchText = query
        hideKeyboard()
        showSearchResults()

        binding.progressBar.visibility = View.VISIBLE
        binding.trackList.visibility = View.GONE
        binding.placeholder.visibility = View.GONE

        searchInteractor.searchTracks(query, object : SearchInteractor.SearchConsumer {
            override fun consume(foundTracks: List<Track>, error: String?) {
                handler.post {
                    binding.progressBar.visibility = View.GONE

                    if (error != null) {
                        showPlaceholder(true, R.drawable.placeholder_error, getString(R.string.server_error))
                        binding.updateButton.visibility = View.VISIBLE
                    } else if (foundTracks.isEmpty()) {
                        showPlaceholder(true, R.drawable.placeholder_no_results, getString(R.string.nothing_found))
                        binding.updateButton.visibility = View.GONE
                    } else {
                        showPlaceholder(false)
                        adapter.updateTracks(foundTracks)
                        binding.trackList.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun onTrackClick(track: Track) {
        if (!isClickDebounced) {
            isClickDebounced = true
            searchHistoryInteractor.addTrackToHistory(track)
            startMediaActivity(track)

            Handler(Looper.getMainLooper()).postDelayed(
                { isClickDebounced = false },
                CLICK_DEBOUNCE_DELAY
            )
        }
    }

    private fun startMediaActivity(track: Track) {
        val intent = Intent(this, MediaActivity::class.java).apply {
            putExtra(TRACK_KEY, track)
        }
        startActivity(intent)
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
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.getString(KEY_SAVED_SEARCH_TEXT)?.let { savedText ->
            binding.searchEditText.setText(savedText)
            currentText = savedText
            if (currentText.isNotEmpty()) startSearch(currentText)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SAVED_SEARCH_TEXT, currentText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString(KEY_SAVED_SEARCH_TEXT, "")
        binding.searchEditText.setText(savedText)
        currentText = savedText
        if (currentText.isNotEmpty()) startSearch(currentText)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 500L
        const val TRACK_KEY = "TRACK"
        private const val KEY_SAVED_SEARCH_TEXT = "SAVED_SEARCH_TEXT"
    }
}