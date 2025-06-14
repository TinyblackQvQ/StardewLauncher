package models.mod.serializable

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.common.SemanticVersion
import models.mod.observable.ObservableModContentPack

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ModContentPack(
    @SerialName("UniqueID")
    val uniqueID: String,
    @SerialName("MinimumVersion")
    val minimumVersion:  SemanticVersion? = null
) {
    fun toObservable(): ObservableModContentPack {
        return ObservableModContentPack(
            uniqueID = uniqueID,
            minimumVersion = minimumVersion
        )
    }

    fun deepCopy(): ModContentPack {
        return ModContentPack(
            uniqueID = uniqueID,
            minimumVersion = minimumVersion?.copy()
        )
    }
}