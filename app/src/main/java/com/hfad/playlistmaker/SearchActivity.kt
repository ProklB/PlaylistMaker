package com.hfad.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.ImageButton
import com.google.android.material.appbar.MaterialToolbar
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

    val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private var itunesApi = retrofit.create(ItunesApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val toolbar = findViewById<MaterialToolbar>(R.id.title)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        searchEditText = findViewById<EditText>(R.id.searchEditText)
        val clearButton = findViewById< ImageButton>(R.id.clearButton)
        recyclerView = findViewById<RecyclerView>(R.id.trackList)
        placeholder = findViewById(R.id.placeholder)
        placeholderIcon = findViewById(R.id.placeholder_icon)
        placeholderText = findViewById(R.id.placeholder_text)
        updateButton = findViewById(R.id.update_button)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.isVisible= !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                currentText = s.toString()
            }
        })

        clearButton.setOnClickListener {
            searchEditText.text.clear()
            clearButton.visibility = android.view.View.GONE
            hideKeybord()
            showPlaceholder(false)
            startSearch()
        }

        updateButton.setOnClickListener {
            if (lastSearchText.isNotEmpty()) {
                startSearch(lastSearchText)
            }
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // ВЫПОЛНЯЙТЕ ПОИСКОВЫЙ ЗАПРОС ЗДЕСЬ
                startSearch()
                true
            }
            false
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TrackAdapter(tracks)

    }

    private fun startSearch(text: String = searchEditText.text.toString()) {
        if (text.isEmpty()) {
            tracks.clear()
            recyclerView.adapter = TrackAdapter(tracks)
            return
        }

        lastSearchText = text
        hideKeybord()

        itunesApi.search(text)
            .enqueue(object : Callback<ItunesResponse> {
                override fun onResponse(call: Call<ItunesResponse>, response: Response<ItunesResponse>) {
                    if (response.isSuccessful) {
                        val searchResults = response.body()?.results
                        tracks.clear()

                        if (!searchResults.isNullOrEmpty()) {
                            showPlaceholder(false)
                            tracks.addAll(searchResults)
                            recyclerView.adapter = TrackAdapter(tracks)
                        } else {
                            showPlaceholder(true, R.drawable.placeholder_no_results,
                                getString(R.string.nothing_found))
                        }
                    } else {
                        showPlaceholder(true, R.drawable.placeholder_error,
                            getString(R.string.server_error))
                    }
                }

                override fun onFailure(call: Call<ItunesResponse>, t: Throwable) {
                    showPlaceholder(true, R.drawable.placeholder_error, getString(R.string.server_error))
                }
            })
    }

    private fun showPlaceholder(show: Boolean, iconRes: Int = 0, text: String = "") {
        if (show) {
            recyclerView.visibility = View.GONE
            placeholder.visibility = View.VISIBLE
            if (iconRes != 0) {
                placeholderIcon.setImageResource(iconRes)
            }
            if (text.isNotEmpty()) {
                placeholderText.text = text
            }
            updateButton.visibility = if (text == getString(R.string.server_error)) View.VISIBLE else View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            placeholder.visibility = View.GONE
        }
    }

    private fun hideKeybord () {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    companion object {
        private const val KEY_SAVED_SEARCH_TEXT = "SAVED_SEARCH_TEXT"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SAVED_SEARCH_TEXT, currentText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString(KEY_SAVED_SEARCH_TEXT, "")
        searchEditText.setText(savedText)
        currentText = savedText
        if (currentText.isNotEmpty()) {
            startSearch(currentText)
        }
    }

}