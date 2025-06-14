package models.mod.serializable

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.common.SemanticVersion
import models.mod.observable.ObservableModDependency

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ModDependency(
    @SerialName("UniqueID")
    var uniqueID: String,
    @SerialName("MinimumVersion")
    val minimumVersion: SemanticVersion? = null,
    @SerialName("IsRequired")
    val isRequired: Boolean = true
) {
    fun deepCopy(): ModDependency {
        return ModDependency(
            uniqueID = uniqueID,
            minimumVersion = minimumVersion?.copy(),
            isRequired = isRequired
        )
    }

    fun toObservable(): ObservableModDependency {
        return ObservableModDependency(
            uniqueID = uniqueID,
            minimumVersion = minimumVersion,
            isRequired = isRequired
        )
    }
}