package io.github.minerofmillions.packupdater

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.minerofmillions.packupdater.components.DefaultRootComponent
import io.github.minerofmillions.packupdater.ui.RootContent
import io.github.minerofmillions.runOnUiThread

fun main() {
    val lifecycle = LifecycleRegistry()

    val root = runOnUiThread {
        DefaultRootComponent(DefaultComponentContext(lifecycle))
    }

    application {
        val darkMode by root.isDarkMode.subscribeAsState()
        val windowState = rememberWindowState()

        LifecycleController(lifecycle, windowState)

        Window(onCloseRequest = ::exitApplication, state = windowState, title = "Pack Updater") {
            MaterialTheme(colors = if (darkMode) darkColors() else lightColors()) {
                RootContent(component = root, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
