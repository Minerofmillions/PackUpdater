package io.github.minerofmillions.packupdater

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class UpdaterProperties @JsonCreator constructor(
    @JsonProperty("updateURL") val updateURL: String,
    @JsonProperty("instanceDirectory") val instanceDirectory: String,
)