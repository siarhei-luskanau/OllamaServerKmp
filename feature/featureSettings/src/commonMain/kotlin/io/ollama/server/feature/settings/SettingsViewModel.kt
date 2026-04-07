package io.ollama.server.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ollama.server.core.common.DispatcherSet
import io.ollama.server.core.pref.PrefService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class SettingsViewModel(
    @Provided private val navigationCallback: SettingsNavigationCallback,
    @Provided private val dispatcherSet: DispatcherSet,
    @Provided private val prefService: PrefService,
) : ViewModel() {
    val viewState: StateFlow<SettingsViewState>
        field = MutableStateFlow<SettingsViewState>(SettingsViewState.Loading)

    init {
        viewModelScope.launch(dispatcherSet.defaultDispatcher()) {
            prefService.getKey().collect { key ->
                viewState.value =
                    SettingsViewState.Success(
                        serverUrl = key ?: "http://127.0.0.1:11434",
                    )
            }
        }
    }

    fun onEvent(event: SettingsViewEvent) {
        viewModelScope.launch(dispatcherSet.defaultDispatcher()) {
            when (event) {
                SettingsViewEvent.NavigateBack -> navigationCallback.goBack()
            }
        }
    }
}
