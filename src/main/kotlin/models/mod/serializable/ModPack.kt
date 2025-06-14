package models.mod.serializable

import kotlinx.serialization.Serializable
import models.common.SemanticVersion
import models.mod.observable.ObservableModPack

@Serializable
data class ModPack(
    val uniqueID: String,
    val version: SemanticVersion,
    val name: String,
    val desc: String,
    val gameVersion: SemanticVersion,
    val apiVersion: SemanticVersion,
    val mods: Set<Mod>
) {
    fun toObservable(): ObservableModPack {
        return ObservableModPack(
            uniqueID = uniqueID,
            version = version,
            name = name,
            desc = desc,
            gameVersion = gameVersion,
            apiVersion = apiVersion,
            mods = mods
        )
    }

    fun deepCopy(
        uniqueID: String = this.uniqueID,
        version: SemanticVersion = this.version.copy(),
        name: String = this.name,
        desc: String = this.desc,
        gameVersion: SemanticVersion = this.gameVersion.copy(),
        apiVersion: SemanticVersion = this.apiVersion.copy(),
        mods: Set<Mod> = this.mods.map { it.deepCopy() }.toSet()
    ): ModPack {
        return ModPack(
            uniqueID = uniqueID,
            version = version,
            name = name,
            desc = desc,
            gameVersion = gameVersion,
            apiVersion = apiVersion,
            mods = mods
        )
    }
}