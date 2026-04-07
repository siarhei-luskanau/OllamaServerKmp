package io.ollama.server.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.ollama.server.ui.common.resources.Res
import io.ollama.server.ui.common.resources.back_button
import io.ollama.server.ui.common.resources.ic_arrow_back
import io.ollama.server.ui.common.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    SettingsContent(
        viewStateFlow = viewModel.viewState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun SettingsContent(
    viewStateFlow: StateFlow<SettingsViewState>,
    onEvent: (SettingsViewEvent) -> Unit,
) {
    val viewState = viewStateFlow.collectAsState()
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsViewEvent.NavigateBack) }) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back_button),
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = viewState.value) {
                SettingsViewState.Loading -> CircularProgressIndicator()
                is SettingsViewState.Error -> Text("Error: ${state.error.message}")
                is SettingsViewState.Success -> Text("Server URL: ${state.serverUrl}")
            }
        }
    }
}

@Preview
@Composable
internal fun SettingsScreenPreview() =
    AppTheme {
        SettingsContent(
            viewStateFlow = MutableStateFlow(SettingsViewState.Success(serverUrl = "http://127.0.0.1:11434")),
            onEvent = {},
        )
    }
