package io.github.minerofmillions.packupdater.components

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import io.github.minerofmillions.forEachParallel
import io.github.minerofmillions.packupdater.Pack
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.URL

class PackComponent(pack: Pack, instanceFile: Value<File>, private val refresh: () -> Unit) {
    val name by pack::name
    private val versions by pack::versions

    private val packDir = instanceFile.map { it.resolve(name) }

    private val versionFile = packDir.map { it.resolve(".version") }
    private val installedVersion = versionFile.map { it.takeIf(File::isFile)?.readText()?.toIntOrNull() ?: 0 }
    val hasNewerVersion = installedVersion.map { (versions.keys.maxOfOrNull(String::toInt) ?: 0) > it }

    fun update() {
        val packDir = packDir.value
        val installedVersion = installedVersion.value
        val latestVersion = versions.keys.maxOfOrNull(String::toInt) ?: return
        if (installedVersion >= latestVersion) return
        versions.entries.map { it.key.toInt() to it.value }.sortedBy { it.first }
            .forEach { (_, history) ->
                runBlocking {
                    history.removals?.forEachParallel { packDir.resolve(it).delete() }
                    history.additions?.forEachParallel { (location, url) ->
                        val fullLocation = packDir.resolve(location)
                        fullLocation.parentFile.mkdirs()
                        fullLocation.writeBytes(URL(url).readBytes())
                    }
                }
            }
        versionFile.value.writeText(latestVersion.toString())
        refresh()
    }
}