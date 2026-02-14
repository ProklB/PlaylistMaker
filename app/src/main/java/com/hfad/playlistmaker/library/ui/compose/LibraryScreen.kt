package com.hfad.playlistmaker.library.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.ui.theme.MyTitleTextStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun LibraryScreen(
    favoritesContent: @Composable () -> Unit,
    playlistsContent: @Composable () -> Unit,
    showFavorites: Boolean = true
) {
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(initialPage = if (showFavorites) 0 else 1)

    val initialPage = if (showFavorites) 0 else 1

    LaunchedEffect(key1 = initialPage) {
        if (pagerState.currentPage != initialPage) {
            pagerState.animateScrollToPage(initialPage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.library_title),
                        style = MyTitleTextStyle(),
                        color = colorResource(id = R.color.text_textview)
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
        val selectedTabIndex = remember { derivedStateOf { pagerState.currentPage } }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex.value,
                modifier = Modifier.fillMaxWidth(),
                divider = {},
                containerColor = colorResource(id = R.color.tab_background),
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTabIndex.value),
                        color = colorResource(id = R.color.tab_indicator),
                        height = 2.dp
                    )
                }
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.favorites_tab),
                            color = if (pagerState.currentPage == 0) {
                                colorResource(id = R.color.tab_selected_text)
                            } else {
                                colorResource(id = R.color.tab_text)
                            },
                            fontFamily = FontFamily(Font(R.font.ys_display_regular)),
                            fontSize = 14.sp,
                            fontWeight = if (pagerState.currentPage == 0) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                )

                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.playlists_tab),
                            color = if (pagerState.currentPage == 1) {
                                colorResource(id = R.color.tab_selected_text)
                            } else {
                                colorResource(id = R.color.tab_text)
                            },
                            fontFamily = FontFamily(Font(R.font.ys_display_regular)),
                            fontSize = 14.sp,
                            fontWeight = if (pagerState.currentPage == 1) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                )
            }

            HorizontalPager(
                count = 2,
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> favoritesContent()
                    1 -> playlistsContent()
                }
            }
        }
    }
}
