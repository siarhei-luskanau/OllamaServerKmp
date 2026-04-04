package template.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinConfiguration
import org.koin.plugin.module.dsl.koinConfiguration
import template.navigation.NavApp
import template.navigation.navigationModule

@Preview
@Composable
fun KoinApp() =
    KoinApplication(
        configuration =
            KoinConfiguration {
                koinConfiguration<DiKoinApplication>().config.invoke(this)
                modules(navigationModule)
            },
    ) {
        NavApp()
    }
