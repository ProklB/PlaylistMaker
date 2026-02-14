package com.hfad.playlistmaker.library.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.compose.runtime.livedata.observeAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.library.ui.viewmodel.PlaylistsState
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import androidx.compose.ui.text.font.Font

@Composable
fun PlaylistsScreen(
    playlistsState: LiveData<PlaylistsState>,
    onCreatePlaylistClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit
) {
    val state by playlistsState.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onCreatePlaylistClick,
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterHorizontally)
                .padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.placeholder_background)
            ),
            shape = RoundedCornerShape(54.dp)
        ) {
            Text(
                text = stringResource(R.string.create_playlist),
                color = colorResource(id = R.color.placeholder_text),
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.ys_display_medium))
            )
        }

        when (state) {
            is PlaylistsState.Empty -> {
                EmptyPlaylistsState(Modifier.weight(1f))
            }
            is PlaylistsState.Content -> {
                val playlists = (state as PlaylistsState.Content).playlists
                PlaylistsGrid(
                    playlists = playlists,
                    onPlaylistClick = onPlaylistClick,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
            else -> {
                EmptyPlaylistsState(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun EmptyPlaylistsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 46.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Image(
            painter = painterResource(id = R.drawable.placeholder_no_playlists),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = stringResource(R.string.no_playlists_title),
            modifier = Modifier.padding(top = 16.dp),
            color = colorResource(id = R.color.text_trackname),
            fontFamily = FontFamily(Font(R.font.ys_display_medium)),
            fontSize = 19.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlaylistsGrid(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(playlists) { playlist ->
            PlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (playlist.coverPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(playlist.coverPath)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.playsholder_play_light),
                        error = painterResource(id = R.drawable.playsholder_play_light)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.playsholder_play_light),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = playlist.name,
                    maxLines = 2,
                    color = colorResource(id = R.color.text_textview),
                    fontFamily = FontFamily(Font(R.font.ys_display_regular)),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(4.dp))

                val trackCountText =
                    LocalContext.current.resources.getQuantityString(
                        R.plurals.tracks_count,
                        playlist.trackCount,
                        playlist.trackCount
                    )

                Text(
                    text = trackCountText,
                    color = colorResource(id = R.color.text_trackinfo),
                    fontFamily = FontFamily(Font(R.font.ys_display_regular)),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}