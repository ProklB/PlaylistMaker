package com.hfad.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.presentation.media.MediaActivity
import com.hfad.playlistmaker.presentation.search.SearchActivity
import com.hfad.playlistmaker.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button1 = findViewById<Button>(R.id.button1)
        button1.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener {
            val intent = Intent(this, MediaActivity::class.java)
            startActivity(intent)
        }

        val button3 = findViewById<Button>(R.id.button3)
        button3.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }

}