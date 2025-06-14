package models.mod.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.SerialName
import models.common.SemanticVersion
import models.mod.common.ModUUID
import models.mod.serializable.ModContentPack

class ObservableModContentPack(
    uniqueID: String,
    minimumVersion:  SemanticVersion? = null
) {
    val innerUniqueID = mutableStateOf(uniqueID)
    val innerMinimumVersion = mutableStateOf(minimumVersion)

    var uniqueID by innerUniqueID
    var minimumVersion by innerMinimumVersion

    fun toSerializable(): ModContentPack {
        return ModContentPack(
            uniqueID = uniqueID,
            minimumVersion = minimumVersion
        )
    }
}