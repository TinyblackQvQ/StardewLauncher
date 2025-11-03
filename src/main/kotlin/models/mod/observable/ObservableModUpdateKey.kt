package models.mod.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import annotations.PropertyNoDirectRecomposition
import models.mod.enums.ModUpdateKeyType
import models.mod.serializable.ModUpdateKey

class ObservableModUpdateKey(
    type: ModUpdateKeyType,
    value: String
) {
    @PropertyNoDirectRecomposition
    val innerType = mutableStateOf(type)
    val innerValue = mutableStateOf(value)
    @PropertyNoDirectRecomposition
    val type by innerType
    val value by innerValue

    fun toSerializable() = ModUpdateKey(type, value)
}
