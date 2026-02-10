package com.hfad.playlistmaker.library.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.library.ui.viewmodel.FavoritesState
import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.search.ui.TrackItem

@Composable
fun FavoritesScreen(
    favoritesState: LiveData<FavoritesState>,
    onTrackClick: (Track) -> Unit
) {
    val state by favoritesState.observeAsState()

    when (state) {
        is FavoritesState.Empty -> {
            EmptyFavoritesState()
        }
        is FavoritesState.Content -> {
            val tracks = (state as FavoritesState.Content).tracks
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(tracks) { track ->
                    TrackItem(
                        track = track,
                        onClick = { onTrackClick(track) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
        null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.progressBar)
                )
            }
        }
    }
}

@Composable
fun EmptyFavoritesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.placeholder_no_favorites),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = stringResource(R.string.no_favorites_title),
            modifier = Modifier.padding(top = 16.dp),
            color = colorResource(id = R.color.text_trackname),
            fontFamily = FontFamily(Font(R.font.ys_display_medium)),
            fontSize = 19.sp,
            textAlign = TextAlign.Center
        )
    }
}