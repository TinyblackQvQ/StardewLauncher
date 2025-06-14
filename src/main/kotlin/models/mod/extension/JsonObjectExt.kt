package models.mod.extension

import kotlinx.serialization.json.JsonObject

fun JsonObject.deepCopy(): JsonObject {
    return JsonObject(this.map {
        it.key to when (val value = it.value) {
            is JsonObject -> value.deepCopy()
            else -> value
        }
    }.toMap())
}
