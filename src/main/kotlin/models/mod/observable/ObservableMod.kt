package models.mod.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import annotations.PropertyNoDirectRecomposition
import kotlinx.serialization.json.JsonObject
import models.mod.common.ModUUID
import models.mod.serializable.Mod
import models.mod.serializable.ModManifest

class ObservableMod(
    manifest: ModManifest,
    config: JsonObject = JsonObject(mapOf()),
    enabled: Boolean = true
) {
    @PropertyNoDirectRecomposition
    val innerConfig = mutableStateOf(config)
    val innerEnabled = mutableStateOf(enabled)

    val manifest = manifest.toObservable()
    val config by innerConfig
    var enabled by innerEnabled

    fun getUUID() = ModUUID(
        uniqueID = manifest.uniqueID,
        version = manifest.version
    )

    fun toSerializable(): Mod {
        return Mod(
            manifest = manifest.toSerializable(),
            config = config,
            enabled = enabled
        )
    }
}