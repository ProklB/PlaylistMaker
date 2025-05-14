package com.hfad.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.ImageButton
import com.google.android.material.appbar.MaterialToolbar
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private var currentText = ""

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

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //clearButton.visibility = if (s.isNullOrEmpty()) android.view.View.GONE else android.view.View.VISIBLE
                clearButton.isVisible= !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                currentText = s.toString()
            }
        })

        clearButton.setOnClickListener {
            searchEditText.text.clear()
            clearButton.visibility = android.view.View.GONE
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }

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
    }

}