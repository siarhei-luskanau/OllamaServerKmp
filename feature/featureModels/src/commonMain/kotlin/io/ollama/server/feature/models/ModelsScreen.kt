package io.ollama.server.feature.models

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.ollama.server.core.ollama.OllamaModel
import io.ollama.server.ui.common.resources.Res
import io.ollama.server.ui.common.resources.back_button
import io.ollama.server.ui.common.resources.ic_arrow_back
import io.ollama.server.ui.common.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ModelsScreen(viewModel: ModelsViewModel) {
    ModelsContent(
        viewStateFlow = viewModel.viewState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun ModelsContent(
    viewStateFlow: StateFlow<ModelsViewState>,
    onEvent: (ModelsViewEvent) -> Unit,
) {
    val viewState = viewStateFlow.collectAsState()
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Models") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ModelsViewEvent.NavigateBack) }) {
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
                ModelsViewState.Loading -> {
                    CircularProgressIndicator()
                }

                is ModelsViewState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Error: ${state.error.message}")
                        Button(onClick = { onEvent(ModelsViewEvent.LoadModels) }) {
                            Text("Retry")
                        }
                    }
                }

                is ModelsViewState.Success -> {
                    ModelsSuccessContent(state = state, onEvent = onEvent)
                }
            }
        }
    }
}

@Composable
private fun ModelsSuccessContent(
    state: ModelsViewState.Success,
    onEvent: (ModelsViewEvent) -> Unit,
) {
    var modelName by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.models) { model ->
                ModelRow(model = model)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = modelName,
                onValueChange = { modelName = it },
                label = { Text("Model name") },
            )
            Button(
                onClick = {
                    onEvent(ModelsViewEvent.PullModel(modelName))
                    modelName = ""
                },
            ) {
                Text("Pull")
            }
        }
    }
}

@Composable
private fun ModelRow(model: OllamaModel) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(model.name)
        Text(model.getSizeString())
        Text(model.family)
    }
}

@Preview
@Composable
internal fun ModelsScreenPreview() =
    AppTheme {
        ModelsContent(
            viewStateFlow =
                MutableStateFlow(
                    ModelsViewState.Success(
                        models =
                            listOf(
                                OllamaModel("llama3.2", "llama", listOf("llama"), 2_000_000_000L, listOf("completion")),
                            ),
                    ),
                ),
            onEvent = {},
        )
    }
