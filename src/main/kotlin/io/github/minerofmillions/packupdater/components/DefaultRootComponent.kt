package io.github.minerofmillions.packupdater.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.getValue
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import io.github.minerofmillions.packupdater.Pack
import io.github.minerofmillions.packupdater.UpdaterProperties
import io.github.minerofmillions.packupdater.VersionHistory
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.net.MalformedURLException
import java.net.URL

class DefaultRootComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    private val scope = coroutineScope(SupervisorJob())

    private val _isDarkMode = MutableValue(true)
    val isDarkMode: Value<Boolean> = _isDarkMode

    private val _packs = MutableValue(emptyList<PackComponent>())
    val packs: Value<List<PackComponent>> = _packs

    private val _updateURL = MutableValue("https://minerofmillions.github.io/packs/packs.json")
    val updateURL: Value<String> = _updateURL

    val isValidUpdateURL = _updateURL.map {
        try {
            URL(it)
            true
        } catch (_: MalformedURLException) {
            false
        }
    }

    private val _instanceDirectory = MutableValue("")
    val instanceDirectory: Value<String> = _instanceDirectory

    private val instanceFile = _instanceDirectory.map(::File)
    val isValidInstanceDirectory = instanceFile.map(File::isDirectory)

    init {
        try {
            val properties = propertiesMapper.readValue(propertiesFile, UpdaterProperties::class.java)
            _updateURL.value = properties.updateURL
            _instanceDirectory.value = properties.instanceDirectory
        } catch (_: Exception) {
        }

        refreshPacks()
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun updateInstanceDirectory(newValue: String) {
        _instanceDirectory.value = newValue
        if (isValidInstanceDirectory.value) writeProperties()
    }

    fun updateUpdateURL(newValue: String) {
        _updateURL.value = newValue
        if (isValidUpdateURL.value) writeProperties()
    }

    fun refreshPacks() {
        scope.launch {
            if (!isValidUpdateURL.value) return@launch

            val updateURL by _updateURL
            val tree = mapper.readTree(URL(updateURL))
            val packs = mutableListOf<Pack>()
            tree.fields().forEachRemaining { (packName, packData) ->
                val versions = packData.fields().asSequence().associate { (version, data) ->
                    val history = mapper.treeToValue(data, VersionHistory::class.java)
                    version to history
                }
                packs += Pack(packName, versions)
            }
            _packs.value = packs.map {
                PackComponent(it, instanceFile, ::refreshPacks)
            }
        }
    }

    private fun writeProperties() {
        propertiesMapper.writeValue(
            propertiesFile, UpdaterProperties(
                _updateURL.value, _instanceDirectory.value
            )
        )
    }

    companion object {
        private val mapper = ObjectMapper()
        private val propertiesMapper = TomlMapper()
        private val propertiesFile = File("updater_properties.toml")
    }
}