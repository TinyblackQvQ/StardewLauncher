package models.mod.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import annotations.PropertyNoDirectRecomposition
import models.common.SemanticVersion
import models.mod.serializable.ModContentPack

class ObservableModContentPack(
    uniqueID: String,
    minimumVersion:  SemanticVersion? = null
) {
    val innerUniqueID = mutableStateOf(uniqueID)
    @PropertyNoDirectRecomposition
    val innerMinimumVersion = mutableStateOf(minimumVersion)

    var uniqueID by innerUniqueID
    @PropertyNoDirectRecomposition
    var minimumVersion by innerMinimumVersion

    fun toSerializable(): ModContentPack {
        return ModContentPack(
            uniqueID = uniqueID,
            minimumVersion = minimumVersion
        )
    }
}