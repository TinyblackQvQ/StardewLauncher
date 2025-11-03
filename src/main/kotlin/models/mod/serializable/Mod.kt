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

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
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
 * - TODO: XNB Mods are not supported for now, migration it to a Content Pack mod automatically maybe one of the solution
 *         , it will be supported, i guess...
 *
 *    See [Using_XNB_mods](https://stardewvalleywiki.com/Modding:Using_XNB_mods) to read more.
 *
 * - TODO: config features for Content Patcher mods' custom config files
 *         (e.g. content.json for Mod [Content Patcher](https://www.nexusmods.com/stardewvalley/mods/1915)) is not supported
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
    /**
     * The manifest of the mod which contains metadata like name, version, author, etc.
     */
    var manifest: ModManifest
    
    /**
     * Configuration data for the mod stored as a JSON object
     */
    var config: JsonObject = JsonObject(mapOf())
    
    /**
     * Whether the mod is enabled or disabled
     */
    @EncodeDefault
    var enabled: Boolean = false

    /**
     * Constructor that loads a mod from a directory path
     *
     * @param rootPath The root directory path of the mod
     * @param fileIO File I/O service for reading files (default: retrieved from Koin)
     * @param json JSON serialization service (default: retrieved from Koin)
     * @throws FileNotFoundException if manifest.json or config.json is missing
     */
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
        }
    }

    /**
     * Constructor that creates a mod from provided parameters
     *
     * @param manifest The mod manifest
     * @param config The mod configuration as a JSON object (default: empty JSON object)
     * @param enabled Whether the mod is enabled (default: true)
     */
    constructor(
        manifest: ModManifest,
        config: JsonObject = JsonObject(mapOf()),
        enabled: Boolean = true
    ) {
        this.manifest = manifest
        this.config = config
        this.enabled = enabled
    }

    /**
     * Gets the unique identifier for this mod
     *
     * @return ModUUID containing the mod's unique ID and version
     */
    fun getUUID() = ModUUID(
        uniqueID = manifest.uniqueID,
        version = manifest.version
    )

    /**
     * Merges the provided configuration with the target configuration
     *
     * @param source Source configuration object, values from this will override target values
     * @param target Target configuration object,
     *               its top-level key-value pairs will be preserved unless overridden by source
     * @return This mod instance with merged configuration
     */
    fun mergeConfig(source: JsonObject, target: JsonObject = config): Mod {
        this.config = createMergedConfig(source, target)
        return this
    }

    /**
     * Creates a deep copy of this mod instance
     *
     * @return A new Mod instance with copied manifest, config and enabled status
     */
    fun deepCopy(): Mod {
        return Mod(manifest.deepCopy(), config.deepCopy(), enabled)
    }

    /**
     * Converts this mod to an observable version
     *
     * @return ObservableMod instance with the same data
     */
    fun toObservable(): ObservableMod {
        return ObservableMod(
            manifest,
            config,
            enabled
        )
    }

    companion object {
        /**
         * Checks if a directory contains a valid mod
         *
         * @param filePath Path to the directory to check
         * @return true if the directory contains a valid mod, false otherwise
         */
        fun isDirectoryAValidMod(filePath: String): Boolean {
            return try {
                ModManifest.Companion.fromFile("$filePath/manifest.json")
                true
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Checks if a directory contains a valid mod
         *
         * @param filePath File object representing the directory to check
         * @return true if the directory contains a valid mod, false otherwise
         */
        fun isDirectoryAValidMod(filePath: File): Boolean {
            return isDirectoryAValidMod(filePath.absolutePath)
        }

        /**
         * Checks if a config.json file exists in the specified mod directory
         *
         * @param filePath Path to the mod directory
         * @param fileIO File I/O service for checking file existence (default: retrieved from Koin)
         * @return true if config.json exists, false otherwise
         */
        fun isConfigFileExist(filePath: String, fileIO: IFileIO = GlobalContext.get().get()): Boolean {
            return fileIO.exists("$filePath/config.json")
        }

        /**
         * Creates a merged configuration object from source and target configurations
         * Values in source will override values in target. Nested objects are merged recursively.
         *
         * @param source Source configuration object
         * @param target Target configuration object,
         *               its top-level key-value pairs will be preserved unless overridden by source
         * @return Merged configuration object
         */
        fun createMergedConfig(source: JsonObject, target: JsonObject): JsonObject {
            return buildJsonObject {
                // 先复制target中的所有键值对
                target.forEach { (key, value) ->
                    put(key, value)
                }
                // 遍历source中的键值对，执行合并逻辑
                source.forEach { (key, sourceValue) ->
                    val targetValue = target[key]

                    if (targetValue is JsonObject && sourceValue is JsonObject) {
                        // 如果source和target中对应值都是对象，则递归合并
                        val mergedObject = createMergedConfig(sourceValue, targetValue)
                        put(key, mergedObject)
                    } else {
                        // 否则使用source中的值进行覆盖
                        put(key, sourceValue)
                    }
                }
            }
        }
    }
}