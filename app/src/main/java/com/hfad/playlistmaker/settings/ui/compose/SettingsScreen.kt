package com.hfad.playlistmaker.settings.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.settings.ui.SettingsViewModel
import com.hfad.playlistmaker.ui.theme.MaterialTextViewStyle
import com.hfad.playlistmaker.ui.theme.MyTitleTextStyle
import com.hfad.playlistmaker.ui.theme.PlaylistMakerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onShareClick: () -> Unit,
    onSupportClick: () -> Unit,
    onAgreementClick: () -> Unit
) {
    val themeSwitchState by viewModel.themeSwitchState.observeAsState(false)

    PlaylistMakerTheme(darkTheme = themeSwitchState) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.button_settings),
                            style = MyTitleTextStyle()
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.switch_themes),
                        style = MaterialTextViewStyle(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = themeSwitchState,
                        onCheckedChange = { isChecked ->
                            viewModel.onThemeSwitchChanged(isChecked)
                        },
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedBorderColor = Color.Transparent,
                            checkedThumbColor = MaterialTheme.colorScheme.inversePrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.inverseSurface,
                            checkedBorderColor = Color.Transparent
                        )
                    )
                }

                SettingsItem(
                    text = stringResource(id = R.string.textview_share),
                    iconRes = R.drawable.share,
                    onClick = onShareClick
                )

                SettingsItem(
                    text = stringResource(id = R.string.textview_support),
                    iconRes = R.drawable.support,
                    onClick = onSupportClick
                )

                SettingsItem(
                    text = stringResource(id = R.string.textview_arrowForward),
                    iconRes = R.drawable.arrowforward,
                    onClick = onAgreementClick
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    text: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTextViewStyle(),
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

class PreviewSettingsViewModel {
    private val _themeSwitchState = MutableLiveData<Boolean>(false)
    val themeSwitchState: LiveData<Boolean> = _themeSwitchState

    fun onThemeSwitchChanged(isChecked: Boolean) {
        _themeSwitchState.value = isChecked
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    val previewViewModel = remember { PreviewSettingsViewModel() }
    val themeSwitchState by previewViewModel.themeSwitchState.observeAsState(false)

    PlaylistMakerTheme(darkTheme = themeSwitchState) {
        PreviewSettingsScreenContent(
            themeSwitchState = themeSwitchState,
            onThemeSwitchChanged = { previewViewModel.onThemeSwitchChanged(it) },
            onShareClick = {},
            onSupportClick = {},
            onAgreementClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewSettingsScreenContent(
    themeSwitchState: Boolean,
    onThemeSwitchChanged: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onSupportClick: () -> Unit,
    onAgreementClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.button_settings),
                        style = MyTitleTextStyle()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.switch_themes),
                    style = MaterialTextViewStyle(),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = themeSwitchState,
                    onCheckedChange = { isChecked ->
                        onThemeSwitchChanged(isChecked)
                    },
                    colors = SwitchDefaults.colors(
                        uncheckedThumbColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedBorderColor = Color.Transparent,
                        checkedThumbColor = MaterialTheme.colorScheme.inversePrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.inverseSurface,
                        checkedBorderColor = Color.Transparent
                    )
                )
            }

            PreviewSettingsItem(
                text = stringResource(id = R.string.textview_share),
                iconRes = R.drawable.share,
                onClick = onShareClick
            )

            PreviewSettingsItem(
                text = stringResource(id = R.string.textview_support),
                iconRes = R.drawable.support,
                onClick = onSupportClick
            )

            PreviewSettingsItem(
                text = stringResource(id = R.string.textview_arrowForward),
                iconRes = R.drawable.arrowforward,
                onClick = onAgreementClick
            )
        }
    }
}

@Composable
fun PreviewSettingsItem(
    text: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTextViewStyle(),
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}