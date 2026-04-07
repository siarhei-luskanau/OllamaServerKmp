package io.ollama.server.feature.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ollama.server.core.common.DispatcherSet
import io.ollama.server.core.ollama.OllamaApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class ModelsViewModel(
    @Provided private val navigationCallback: ModelsNavigationCallback,
    @Provided private val dispatcherSet: DispatcherSet,
    @Provided private val apiClient: OllamaApiClient,
) : ViewModel() {
    val viewState: StateFlow<ModelsViewState>
        field = MutableStateFlow<ModelsViewState>(ModelsViewState.Loading)

    init {
        loadModels()
    }

    fun onEvent(event: ModelsViewEvent) {
        viewModelScope.launch(dispatcherSet.defaultDispatcher()) {
            when (event) {
                ModelsViewEvent.NavigateBack -> {
                    navigationCallback.goBack()
                }

                ModelsViewEvent.LoadModels -> {
                    loadModels()
                }

                is ModelsViewEvent.PullModel -> {
                    apiClient.pullModel(event.name)
                    loadModels()
                }
            }
        }
    }

    private fun loadModels() {
        viewModelScope.launch(dispatcherSet.defaultDispatcher()) {
            viewState.value = ModelsViewState.Loading
            viewState.value =
                runCatching { apiClient.listModels() }
                    .fold(
                        onSuccess = { ModelsViewState.Success(it) },
                        onFailure = { ModelsViewState.Error(it) },
                    )
        }
    }
}
