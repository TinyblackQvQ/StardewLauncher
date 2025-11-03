package models.mod.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import annotations.PropertyNoDirectRecomposition
import models.common.SemanticVersion
import models.mod.common.ModUUID
import models.mod.serializable.Mod
import models.mod.serializable.ModPack
import views.ModResourceView
import java.util.*

class ObservableModPack(
    name: String,
    desc: String,
    gameVersion: SemanticVersion,
    apiVersion: SemanticVersion,
    mods: Set<Mod>,
    uniqueID: String = UUID.randomUUID().toString(),
    version: SemanticVersion = SemanticVersion(0, 0, 0),
) {
    private var innerUniqueID = mutableStateOf(uniqueID)

    @PropertyNoDirectRecomposition
    private var innerVersion = mutableStateOf(version)
    private var innerName = mutableStateOf(name)
    private var innerDesc = mutableStateOf(desc)

    @PropertyNoDirectRecomposition
    private var innerGameVersion = mutableStateOf(gameVersion)

    @PropertyNoDirectRecomposition
    private var innerApiVersion = mutableStateOf(apiVersion)
    var mods = mutableStateListOf<ObservableMod>().apply { mods.forEach { add(it.toObservable()) } }

    var uniqueID by innerUniqueID

    @PropertyNoDirectRecomposition
    var version by innerVersion
    var name by innerName
    var desc by innerDesc

    @PropertyNoDirectRecomposition
    var gameVersion by innerGameVersion

    @PropertyNoDirectRecomposition
    var apiVersion by innerApiVersion

    fun getModInstanceByUniqueID(uniqueID: String) = mods.firstOrNull { it.manifest.uniqueID == uniqueID }
    fun getModInstanceByUUID(modUUID: ModUUID) =
        mods.firstOrNull { it.manifest.uniqueID == modUUID.uniqueID && it.manifest.version == modUUID.version }

    fun updateModInstance(mod: Mod): Boolean {
        val index = mods.indexOfFirst { it.manifest.uniqueID == mod.manifest.uniqueID }
        if (index != -1) {
            mods[index] = mod.toObservable()
            return true
        }
        return false
    }

    fun getEnabledMods() = mods.filter { it.enabled }
    fun getDisabledMods() = mods.filter { !it.enabled }
    fun getDependedMods(): List<ObservableMod> {
        val dependedMods = mutableListOf<ObservableMod>()
        for (mod in mods) {
            for (dependency in mod.manifest.dependencies) {
                if (!dependency.isRequired) continue
                val dependedMod = getModInstanceByUniqueID(dependency.uniqueID)
                dependedMod?.let { dependedMods.add(it) }
            }
        }
        return dependedMods
    }

    fun enableMod(modUUID: ModUUID) {
        val mod = getModInstanceByUUID(modUUID)
        if (mod != null) {
            mod.enabled = true
        } else {
            ModResourceView.getModInstanceByUUID(modUUID)?.let {
                val copiedInstance = it.toSerializable().deepCopy().toObservable()
                copiedInstance.enabled = true
                mods.add(copiedInstance)
            }
        }
        reapplyDependedMods()
    }

    fun disableMod(modUUID: ModUUID) {
        val mod = getModInstanceByUUID(modUUID)
        mod?.let {
            mod.enabled = false
            for (dependency in mod.manifest.dependencies) {
                if (!dependency.isRequired) continue
                val dependedMod = getModInstanceByUniqueID(dependency.uniqueID)
                dependedMod?.enabled = false
            }
            reapplyDependedMods()
        }
    }

    fun reapplyDependedMods() {
        for (mod in mods) {
            if (mod.enabled) {
                for (dependency in mod.manifest.dependencies) {
                    if (!dependency.isRequired) continue
                    val dependedMod = getModInstanceByUniqueID(dependency.uniqueID)
                    dependedMod?.enabled = true
                }
            }
        }
    }

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