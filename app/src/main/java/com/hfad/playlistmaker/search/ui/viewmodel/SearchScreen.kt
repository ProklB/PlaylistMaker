package com.hfad.playlistmaker.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.search.ui.viewmodel.SearchState
import com.hfad.playlistmaker.search.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onTrackClick: (Track) -> Unit
) {
    val searchState by viewModel.searchState.observeAsState()
    val historyState by viewModel.historyState.observeAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Поиск",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.ys_display_bold)),
                            color = colorResource(id = R.color.text_textview)
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.bg_secondary),
                    titleContentColor = colorResource(id = R.color.text_textview)
                )
            )
        },
        containerColor = colorResource(id = R.color.bg_secondary)
    ) { paddingValues ->
        SearchContent(
            modifier = Modifier.padding(paddingValues),
            searchState = searchState ?: SearchState.Content(emptyList()),
            historyState = historyState ?: emptyList(), // Защита от null
            onSearch = { query -> viewModel.searchTracks(query) },
            onTrackClick = { track ->
                viewModel.addTrackToHistory(track)
                onTrackClick(track)
            },
            onClearHistory = { viewModel.clearSearchHistory() }
        )
    }
}

@Composable
fun SearchContent(
    modifier: Modifier = Modifier,
    searchState: SearchState,
    historyState: List<Track>,
    onSearch: (String) -> Unit,
    onTrackClick: (Track) -> Unit,
    onClearHistory: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(SEARCH_DEBOUNCE_DELAY)
            onSearch(searchQuery)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SearchTextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = newValue
            },
            onClearClick = {
                searchQuery = ""
                keyboardController?.hide()
            },
            onSearch = {
                keyboardController?.hide()
                if (searchQuery.isNotEmpty()) {
                    onSearch(searchQuery)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when {
            searchQuery.isEmpty() -> {
                if (historyState.isNotEmpty()) {
                    SearchHistory(
                        history = historyState,
                        onTrackClick = onTrackClick,
                        onClearHistory = onClearHistory,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                    }
                }
            }

            searchState is SearchState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.progressBar)
                    )
                }
            }

            searchState is SearchState.Error -> {
                ErrorState(
                    errorMessage = (searchState as SearchState.Error).message,
                    onRetry = { onSearch(searchQuery) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            searchState is SearchState.Empty -> {
                EmptyState(
                    message = "Ничего не нашлось",
                    modifier = Modifier.fillMaxSize()
                )
            }

            searchState is SearchState.Content -> {
                val tracks = (searchState as SearchState.Content).tracks
                SearchResults(
                    tracks = tracks,
                    onTrackClick = onTrackClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colorResource(id = R.color.bg_edittext))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.search_icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colorResource(id = R.color.icon_color)
            )

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.text_button),
                    fontFamily = FontFamily(Font(R.font.ys_display_regular))
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { onSearch() }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Поиск",
                                fontSize = 16.sp,
                                color = colorResource(id = R.color.text_hint),
                                fontFamily = FontFamily(Font(R.font.ys_display_regular))
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (value.isNotEmpty()) {
                IconButton(
                    onClick = onClearClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.clear),
                        contentDescription = "Очистить поиск",
                        tint = colorResource(id = R.color.icon_color)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchHistory(
    history: List<Track>,
    onTrackClick: (Track) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Вы искали",
            style = TextStyle(
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.text_textview),
                fontFamily = FontFamily(Font(R.font.ys_display_medium))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    items(history) { track ->
                        TrackItem(
                            track = track,
                            onClick = { onTrackClick(track) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                Button(
                    onClick = onClearHistory,
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.button_background)
                    ),
                    shape = RoundedCornerShape(54.dp)
                ) {
                    Text(
                        text = "Очистить историю",
                        color = colorResource(id = R.color.placeholder_text),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.ys_display_medium))
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResults(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
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

@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("mm:ss", Locale.getDefault()) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colorResource(id = R.color.bg_secondary))
            ) {
                if (track.artworkUrl100.isNotEmpty()) {
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(
                            model = track.artworkUrl100,
                            error = painterResource(id = R.drawable.placeholder)
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.placeholder),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.trackName,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.text_trackname),
                        fontFamily = FontFamily(Font(R.font.ys_display_regular))
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.artistName,
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = colorResource(id = R.color.text_trackinfo),
                            fontFamily = FontFamily(Font(R.font.ys_display_regular))
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = timeFormat.format(track.trackTimeMillis),
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = colorResource(id = R.color.text_trackinfo),
                            fontFamily = FontFamily(Font(R.font.ys_display_regular))
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.arrowforward),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.placeholder_error),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = errorMessage,
            style = TextStyle(
                fontSize = 16.sp,
                color = colorResource(id = R.color.text_textview),
                fontFamily = FontFamily(Font(R.font.ys_display_medium))
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .clip(RoundedCornerShape(54.dp)),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.button_background)
            )
        ) {
            Text(
                text = "Обновить",
                color = colorResource(id = R.color.placeholder_text),
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.ys_display_medium))
            )
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.placeholder_no_results),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = TextStyle(
                fontSize = 16.sp,
                color = colorResource(id = R.color.text_textview),
                fontFamily = FontFamily(Font(R.font.ys_display_medium))
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

private const val SEARCH_DEBOUNCE_DELAY = 2000L