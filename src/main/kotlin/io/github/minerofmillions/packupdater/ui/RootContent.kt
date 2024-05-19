package io.github.minerofmillions.packupdater.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.minerofmillions.packupdater.components.DefaultRootComponent

@Composable
fun RootContent(component: DefaultRootComponent, modifier: Modifier = Modifier) {
    val isDarkMode by component.isDarkMode.subscribeAsState()
    val packs by component.packs.subscribeAsState()

    val updateURL by component.updateURL.subscribeAsState()
    val isValidUpdateURL by component.isValidUpdateURL.subscribeAsState()

    val instanceDirectory by component.instanceDirectory.subscribeAsState()
    val isValidInstanceDirectory by component.isValidInstanceDirectory.subscribeAsState()

    Scaffold(modifier, topBar = {
        TopAppBar({ Text("Pack Updater") }, actions = {
            IconButton(component::refreshPacks, enabled = isValidUpdateURL) {
                Icon(Icons.Default.Refresh, "Refresh Packs")
            }
            IconButton(component::toggleDarkMode) {
                if (isDarkMode) Icon(Icons.Default.LightMode, "Enable Light Mode")
                else Icon(Icons.Default.DarkMode, "Enable Dark Mode")
            }
        })
    }, content = {
        Column(Modifier.padding(it)) {
            Row {
                TextField(
                    updateURL,
                    component::updateUpdateURL,
                    modifier = Modifier.weight(1f),
                    label = { Text("Update URL") },
                    isError = !isValidUpdateURL,
                    singleLine = true
                )
                TextField(
                    instanceDirectory,
                    component::updateInstanceDirectory,
                    modifier = Modifier.weight(1f),
                    label = { Text("Instance Directory") },
                    isError = !isValidInstanceDirectory,
                    singleLine = true
                )
            }
            LazyVerticalGrid(GridCells.Adaptive(300.dp)) {
                items(packs) { pack ->
                    PackContent(pack)
                }
            }
        }
    })
}