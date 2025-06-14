/**
 * Copyright (C) 2025 Miluko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package models.mod.serializable

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import models.common.SemanticVersion
import models.mod.exception.ModNoEntryException
import models.mod.observable.ObservableModManifest
import org.koin.core.context.GlobalContext
import services.file.IFileIO

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ModManifest(
    @SerialName("Name")
    val name: String,
    @SerialName("Author")
    val author: String,
    @SerialName("Version")
    val version: SemanticVersion,
    @SerialName("Description")
    val description: String = "",
    @SerialName("UniqueID")
    @JsonNames("UniqueID", "UniqueId")
    val uniqueID: String,
    @SerialName("EntryDll")
    val entryDLL: String? = null,
    @SerialName("MinimumApiVersion")
    val minimumApiVersion: SemanticVersion? = null,
    @SerialName("MinimumGameVersion")
    val minimumGameVersion:  SemanticVersion? = null,
    @SerialName("UpdateKeys")
    val updateKeys: List<ModUpdateKey>? = null,
    @SerialName("ContentPackFor")
    val contentPackFor: ModContentPack? = null,
    @SerialName("Dependencies")
    val dependencies: List<ModDependency>? = null,
    @SerialName("\$schema")
    val schema: String? = null
) {
    init {
        if (entryDLL == null && contentPackFor == null)
            throw ModNoEntryException("The mod $name doesn't have a entry in its manifest.json")
    }

    companion object {
        fun fromFile(filePath: String,
                     file: IFileIO = GlobalContext.get().get(),
                     json: Json = GlobalContext.get().get()): ModManifest {
            val content = file.read(filePath, true)
            return json.decodeFromString(content)
        }
    }

    fun deepCopy(): ModManifest {
        return ModManifest(
            name = name,
            author = author,
            version = version.copy(),
            description = description,
            uniqueID = uniqueID,
            entryDLL = entryDLL,
            minimumApiVersion = minimumApiVersion?.copy(),
            minimumGameVersion = minimumGameVersion?.copy(),
            updateKeys = updateKeys?.map { it }?.toList(),
            contentPackFor = contentPackFor?.deepCopy(),
            dependencies = dependencies?.map { it.deepCopy() }?.toList(),
            schema = schema
        )
    }

    fun toObservable(): ObservableModManifest {
        return ObservableModManifest(
            name = name,
            author = author,
            version = version,
            description = description,
            uniqueID = uniqueID,
            entryDLL = entryDLL,
            minimumApiVersion = minimumApiVersion,
            minimumGameVersion = minimumGameVersion,
            updateKeys = updateKeys,
            contentPackFor = contentPackFor,
            dependencies = dependencies,
        )
    }
}