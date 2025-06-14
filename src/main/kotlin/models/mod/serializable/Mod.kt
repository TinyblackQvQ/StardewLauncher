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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import models.mod.common.ModUUID
import models.mod.extension.deepCopy
import models.mod.observable.ObservableMod
import org.koin.core.context.GlobalContext
import services.file.IFileIO
import services.logger.AppLogger
import services.logger.LogModule
import java.io.File
import java.io.FileNotFoundException

/**
 * ### IMPORTANT
 * - TODO: XNB Mods are not supported for now, migration it to a Content Pack mod automatically maybe one of the solution, it will be supported, i guess...
 *
 *    See [Using_XNB_mods](https://stardewvalleywiki.com/Modding:Using_XNB_mods) to read more.
 *
 * - TODO: config features for Content Patcher mods' custom config files (e.g. content.json for Mod [Content Patcher](https://www.nexusmods.com/stardewvalley/mods/1915)) is not supported
 *
 * - __When update mods, JSON storages will not be transferred__
 *
 *    If you are a mod creator, consider use Save data or Global app data
 *
 *    See [APIs/Data](https://stardewvalleywiki.com/Modding:Modder_Guide/APIs/Data) to read more.
 *
 * - TODO: i18n features only support single i18n file mode, folder mode is not supported
 *
 *    e.g.
 *    ```text
 *    YourMod/
 *       i18n/
 *          default/
 *             dialogue.json
 *             events.json
 *          fr/
 *             dialogue.json
 *             events.json
 *       manifest.json
 *       YourMod.dll
 *    ```
 *
 *    See [APIs/Translation](https://stardewvalleywiki.com/Modding:Modder_Guide/APIs/Translation) to read more.
 * */

@OptIn(ExperimentalSerializationApi::class)
@Serializable
class Mod {
    var manifest: ModManifest
    var config: JsonObject = JsonObject(mapOf())
    var enabled: Boolean = true

    constructor(
        rootPath: String,
        fileIO: IFileIO = GlobalContext.get().get(),
        json: Json = GlobalContext.get().get()
    ) {
        try {
            manifest = ModManifest.Companion.fromFile("$rootPath/manifest.json")
            if (!fileIO.exists("$rootPath/config.json")) {
                AppLogger.warn(LogModule.ModLoader, "When loading mod folder $rootPath, config.json is missing")
            } else
                config = json.decodeFromString(fileIO.read("$rootPath/config.json", true))
        } catch (e: FileNotFoundException) {
            AppLogger.error(
                LogModule.ModLoader,
                "When opening mod folder $rootPath, manifest.json or config.json is missing."
            )
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    constructor(
        manifest: ModManifest,
        config: JsonObject = JsonObject(mapOf()),
        enabled: Boolean = true
    ) {
        this.manifest = manifest
        this.config = config
        this.enabled = enabled
    }

    fun getUUID() = ModUUID(
        uniqueID = manifest.uniqueID,
        version = manifest.version
    )

    fun deepCopy(): Mod {
        return Mod(manifest.deepCopy(), config.deepCopy(), enabled)
    }

    fun toObservable(): ObservableMod {
        return ObservableMod(
            manifest,
            config,
            enabled
        )
    }

    companion object {
        fun isDirectoryAValidMod(filePath: String): Boolean {
            return try {
                ModManifest.Companion.fromFile("$filePath/manifest.json")
                true
            } catch (e: Exception) {
                false
            }
        }

        fun isDirectoryAValidMod(filePath: File): Boolean {
            return isDirectoryAValidMod(filePath.absolutePath)
        }

        fun isConfigFileExist(filePath: String, fileIO: IFileIO = GlobalContext.get().get()): Boolean {
            return fileIO.exists("$filePath/config.json")
        }
    }
}

