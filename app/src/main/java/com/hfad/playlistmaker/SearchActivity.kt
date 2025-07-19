package com.hfad.playlistmaker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.hfad.playlistmaker.Constanta.Companion.CLICK_DEBOUNCE_DELAY
import com.hfad.playlistmaker.Constanta.Companion.SEARCH_DEBOUNCE_DELAY
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.max

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private var currentText = ""
    private var lastSearchText = ""
    private var tracks: MutableList<Track> = mutableListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholder: View
    private lateinit var placeholderIcon: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var updateButton: TextView
    private lateinit var searchHistory: SearchHistory
    private lateinit var historyViewGroup: ViewGroup
    private lateinit var historyTitle: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var searchAdapter: TrackAdapter
    private lateinit var clearButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private var isClickDebounced = false
    private var searchStartTime = 0L    //удалить после тестирования или ревью

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesApi = retrofit.create(ItunesApi::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { startSearch() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupToolbar()
        setupSearchText()
        setupAdapters()
        setupHistory()
        updateHistoryVisibility()

        updateButton.setOnClickListener {
            if (lastSearchText.isNotEmpty()) {
                startSearch(lastSearchText)
            }
        }
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById<ImageButton>(R.id.clearButton)
        recyclerView = findViewById(R.id.trackList)
        placeholder = findViewById(R.id.placeholder)
        placeholderIcon = findViewById(R.id.placeholder_icon)
        placeholderText = findViewById(R.id.placeholder_text)
        updateButton = findViewById(R.id.update_button)
        historyViewGroup = findViewById(R.id.historyViewGroup)
        historyTitle = findViewById(R.id.historyTitle)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.title)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun setupSearchText() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?,
                                           start: Int,
                                           count: Int,
                                           after: Int) {}
            override fun onTextChanged(s: CharSequence?,
                                       start: Int,
                                       before: Int,
                                       count: Int) {
                clearButton.isVisible = !s.isNullOrEmpty()
                updateHistoryVisibility()
                searchDebounce()
            }
            override fun afterTextChanged(s: Editable?) {
                currentText = s.toString()
            }
        })

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                updateHistoryVisibility()
            }
        }

        clearButton.setOnClickListener {
            searchEditText.text.clear()
            hideKeyboard()
            showPlaceholder(false)
            startSearch()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                startSearch()
                true
            } else {
                false
            }
        }
    }

    private fun setupAdapters() {
        searchAdapter = TrackAdapter(tracks) { track ->
            if (!isClickDebounced) {
                isClickDebounced = true
                addToHistory(track)
                startMediaActivity(track)

                Handler(Looper.getMainLooper()).postDelayed({
                    isClickDebounced = false
                }, CLICK_DEBOUNCE_DELAY)
            }

        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = searchAdapter

        historyAdapter = TrackAdapter(emptyList()) { track ->
            startMediaActivity(track)
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }

    private fun startMediaActivity(track: Track) {
        Intent(this, MediaActivity::class.java).apply {
            putExtra(Constanta.TRACK_KEY, track)
            startActivity(this)
        }
    }

    private fun setupHistory() {
        searchHistory = SearchHistory(getSharedPreferences(Constanta.PLAYLISTMAKER_PREFERENCES, MODE_PRIVATE))
        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            updateHistoryVisibility()
        }
    }

    private fun startSearch(text: String = searchEditText.text.toString()) {
        if (text.isEmpty()) {
            tracks.clear()
            searchAdapter.notifyDataSetChanged()
            return
        }

        lastSearchText = text
        hideKeyboard()

        searchStartTime = System.currentTimeMillis()    //удалить после тестирования или ревью

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        placeholder.visibility = View.GONE

        itunesApi.search(text).enqueue(object : Callback<ItunesResponse> {
            override fun onResponse(call: Call<ItunesResponse>, response: Response<ItunesResponse>) {
                val requestDuration = System.currentTimeMillis() - searchStartTime    //удалить после тестирования или ревью
                val remainingDelay = max(0, Constanta.TEST_PROGRESSBAR_DISPLAY_TIME - requestDuration)    //удалить после тестирования или ревью

                Handler(Looper.getMainLooper()).postDelayed({    //удалить после тестирования или ревью
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val searchResults = response.body()?.results
                        tracks.clear()

                        if (!searchResults.isNullOrEmpty()) {
                            showPlaceholder(false)
                            tracks.addAll(searchResults)
                            searchAdapter.notifyDataSetChanged()
                        } else {
                            showPlaceholder(true, R.drawable.placeholder_no_results,
                                getString(R.string.nothing_found))
                        }
                    } else {
                        showPlaceholder(true, R.drawable.placeholder_error,
                            getString(R.string.server_error))
                    }
                }, remainingDelay)    //удалить после тестирования или ревью
            }

            override fun onFailure(call: Call<ItunesResponse>, t: Throwable) {
                val requestDuration = System.currentTimeMillis() - searchStartTime    //удалить после тестирования или ревью
                val remainingDelay = max(0, Constanta.TEST_PROGRESSBAR_DISPLAY_TIME - requestDuration)    //удалить после тестирования или ревью

                Handler(Looper.getMainLooper()).postDelayed({    //удалить после тестирования или ревью
                    progressBar.visibility = View.GONE
                    showPlaceholder(true, R.drawable.placeholder_error,
                        getString(R.string.server_error))
                }, remainingDelay)    //удалить после тестирования или ревью
            }
        })
    }

    private fun updateHistoryVisibility() {
        val history = searchHistory.getHistory()
        val showHistory = history.isNotEmpty() &&
                searchEditText.text.isEmpty() &&
                searchEditText.hasFocus()

        historyViewGroup.visibility = if (showHistory) View.VISIBLE else View.GONE

        if (showHistory) {
            historyAdapter.updateTracks(history)
        }
    }

    private fun addToHistory(track: Track) {
        searchHistory.addTrack(track)
        updateHistoryVisibility()
    }

    private fun showPlaceholder(show: Boolean, iconRes: Int = 0, text: String = "") {
        if (show) {
            recyclerView.visibility = View.GONE
            placeholder.visibility = View.VISIBLE
            if (iconRes != 0) placeholderIcon.setImageResource(iconRes)
            if (text.isNotEmpty()) placeholderText.text = text
            updateButton.visibility = if (text == getString(R.string.server_error)) View.VISIBLE else View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            placeholder.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Constanta.KEY_SAVED_SEARCH_TEXT, currentText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString(Constanta.KEY_SAVED_SEARCH_TEXT, "")
        searchEditText.setText(savedText)
        currentText = savedText
        if (currentText.isNotEmpty()) startSearch(currentText)
    }

}