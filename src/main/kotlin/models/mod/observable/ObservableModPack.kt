package models.mod.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import models.common.SemanticVersion
import models.mod.common.ModUUID
import models.mod.serializable.Mod
import models.mod.serializable.ModPack
import java.util.UUID

class ObservableModPack(
    name: String,
    desc: String,
    gameVersion: SemanticVersion,
    apiVersion: SemanticVersion,
    mods: Set<Mod>,
    uniqueID: String = UUID.randomUUID().toString(),
    version: SemanticVersion = SemanticVersion(0, 0, 0),
) {
    private val innerUniqueID = mutableStateOf(uniqueID)
    private val innerVersion = mutableStateOf(version)
    private val innerName = mutableStateOf(name)
    private val innerDesc = mutableStateOf(desc)
    private val innerGameVersion = mutableStateOf(gameVersion)
    private val innerApiVersion = mutableStateOf(apiVersion)
    val mods = mutableStateSetOf<ObservableMod>().apply { mods.forEach { add(it.toObservable()) } }

    val uniqueID by innerUniqueID
    val version by innerVersion
    val name by innerName
    val desc by innerDesc
    val gameVersion by innerGameVersion
    val apiVersion by innerApiVersion

    fun getModInstanceByUUID(modUUID: ModUUID) =
        mods.firstOrNull { it.manifest.uniqueID == modUUID.uniqueID && it.manifest.version == modUUID.version }

    fun toSerializable() = ModPack(
        uniqueID,
        version,
        name,
        desc,
        gameVersion,
        apiVersion,
        mods.map { it.toSerializable() }.toSet()
    )
}