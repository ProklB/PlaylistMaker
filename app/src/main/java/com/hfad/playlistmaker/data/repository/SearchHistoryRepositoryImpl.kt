package com.hfad.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hfad.playlistmaker.domain.models.Track
import com.hfad.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SearchHistoryRepository {

    private val gson = Gson()
    private val historyKey = "search_history"
    private val maxHistorySize = 10

    override fun addTrackToHistory(track: Track) {
        val history = getSearchHistory().toMutableList()

        history.removeAll { it.trackId == track.trackId }

        history.add(0, track)

        if (history.size > maxHistorySize) {
            history.removeAt(history.size - 1)
        }

        saveHistory(history)
    }

    override fun getSearchHistory(): List<Track> {
        val json = sharedPreferences.getString(historyKey, null)
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    override fun clearSearchHistory() {
        sharedPreferences.edit().remove(historyKey).apply()
    }

    private fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString(historyKey, json).apply()
    }
}