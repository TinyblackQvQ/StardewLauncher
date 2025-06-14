package models.mod.common

import models.common.SemanticVersion

data class ModUUID(
    val uniqueID: String,
    val version: SemanticVersion
)