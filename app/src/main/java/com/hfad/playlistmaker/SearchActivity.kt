package com.hfad.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.ImageButton
import com.google.android.material.appbar.MaterialToolbar

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val toolbar = findViewById<MaterialToolbar>(R.id.title)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val clearButton = findViewById< ImageButton>(R.id.clearButton)

        // Фокус на поле ввода
        searchEditText.requestFocus()

        // TextWatcher для отслеживания изменений текста
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Не используется
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Показать, скрыть кнопку очистки в зависимости от наличия текста
                clearButton.visibility = if (s.isNullOrEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                // Эаглушка для будущей логики поиска
            }
        })

        clearButton.setOnClickListener {
            // Очищаем текст
            searchEditText.text.clear()
            clearButton.visibility = android.view.View.GONE
        }

    }

}