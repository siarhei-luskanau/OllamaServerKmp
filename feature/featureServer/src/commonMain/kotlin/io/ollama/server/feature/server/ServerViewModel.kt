package io.ollama.server.feature.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ollama.server.core.common.DispatcherSet
import io.ollama.server.core.ollama.OllamaServerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class ServerViewModel(
    @Provided private val navigationCallback: ServerNavigationCallback,
    @Provided private val dispatcherSet: DispatcherSet,
    @Provided private val serverController: OllamaServerController,
) : ViewModel() {
    val viewState: StateFlow<ServerViewState>
        field = MutableStateFlow<ServerViewState>(ServerViewState.Loading)

    init {
        viewModelScope.launch(dispatcherSet.defaultDispatcher()) {
            serverController.status.collect { status ->
                viewState.value =
                    ServerViewState.Success(
                        isRunning = status.isRunning,
                        port = status.port,
                        pid = status.pid,
                    )
            }
        }
    }

    fun onEvent(event: ServerViewEvent) {
        viewModelScope.launch(dispatcherSet.defaultDispatcher()) {
            when (event) {
                ServerViewEvent.StartServer -> serverController.start()
                ServerViewEvent.StopServer -> serverController.stop()
                ServerViewEvent.RestartServer -> serverController.restart()
                ServerViewEvent.NavigateToModels -> navigationCallback.goModelsScreen()
                ServerViewEvent.NavigateToSettings -> navigationCallback.goSettingsScreen()
            }
        }
    }
}
