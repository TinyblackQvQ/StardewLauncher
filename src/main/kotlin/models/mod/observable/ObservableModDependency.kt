package models.mod.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import annotations.PropertyNoDirectRecomposition
import models.common.SemanticVersion
import models.mod.serializable.ModDependency

class ObservableModDependency(
    uniqueID: String,
    minimumVersion:  SemanticVersion? = null,
    isRequired: Boolean = true
) {
    private val innerUniqueID = mutableStateOf(uniqueID)
    @PropertyNoDirectRecomposition
    private val innerMinimumVersion = mutableStateOf(minimumVersion)
    private val innerIsRequired = mutableStateOf(isRequired)

    var uniqueID by innerUniqueID
    @PropertyNoDirectRecomposition
    var minimumVersion by innerMinimumVersion
    var isRequired by innerIsRequired

    fun toSerializable() = ModDependency(
        uniqueID = uniqueID,
        minimumVersion = minimumVersion,
        isRequired = isRequired
    )
}