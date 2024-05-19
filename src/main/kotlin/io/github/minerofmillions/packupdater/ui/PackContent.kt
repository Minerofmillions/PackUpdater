package io.github.minerofmillions.packupdater.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.minerofmillions.packupdater.components.PackComponent

@Composable
fun PackContent(component: PackComponent) = Surface(Modifier.border(2.dp, MaterialTheme.colors.primary)) {
    val hasNewerVersion by component.hasNewerVersion.subscribeAsState()
    Column(Modifier.padding(4.dp)) {
        Text(component.name)
        if (hasNewerVersion) Button(component::update) { Text("Update") }
        else Text("Up to date")
    }
}