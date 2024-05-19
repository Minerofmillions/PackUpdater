package io.github.minerofmillions.packupdater

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Pack(
    val name: String,
    val versions: Map<String, VersionHistory>,
)

data class VersionHistory @JsonCreator constructor(
    @JsonProperty("additions") val additions: Map<String, String>?,
    @JsonProperty("removals") val removals: List<String>?,
)