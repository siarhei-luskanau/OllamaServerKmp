package io.ollama.server.feature.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.ollama.server.ui.common.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ServerScreen(viewModel: ServerViewModel) {
    ServerContent(
        viewStateFlow = viewModel.viewState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun ServerContent(
    viewStateFlow: StateFlow<ServerViewState>,
    onEvent: (ServerViewEvent) -> Unit,
) {
    val viewState = viewStateFlow.collectAsState()
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Ollama Server") },
                actions = {
                    TextButton(onClick = { onEvent(ServerViewEvent.NavigateToModels) }) {
                        Text("Models")
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
                ServerViewState.Loading -> CircularProgressIndicator()
                is ServerViewState.Error -> Text("Error: ${state.error.message}")
                is ServerViewState.Success -> ServerSuccessContent(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun ServerSuccessContent(
    state: ServerViewState.Success,
    onEvent: (ServerViewEvent) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Status: ${if (state.isRunning) "Running" else "Stopped"}")
        Text("Port: ${state.port}")
        if (state.pid != null) {
            Text("PID: ${state.pid}")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.isRunning) {
                Button(onClick = { onEvent(ServerViewEvent.StopServer) }) {
                    Text("Stop")
                }
                Button(onClick = { onEvent(ServerViewEvent.RestartServer) }) {
                    Text("Restart")
                }
            } else {
                Button(onClick = { onEvent(ServerViewEvent.StartServer) }) {
                    Text("Start")
                }
            }
        }
    }
}

@Preview
@Composable
internal fun ServerScreenPreview() =
    AppTheme {
        ServerContent(
            viewStateFlow = MutableStateFlow(ServerViewState.Success(isRunning = true, port = 11434, pid = null)),
            onEvent = {},
        )
    }
