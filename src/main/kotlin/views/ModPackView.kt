package views

import androidx.compose.runtime.snapshots.SnapshotStateList
import extension.replace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import models.common.SemanticVersion
import models.mod.serializable.Mod
import models.mod.ModIO
import models.mod.common.ModUUID
import models.mod.observable.ObservableMod
import models.mod.observable.ObservableModPack
import models.mod.serializable.ModPack
import org.koin.core.context.GlobalContext
import services.defaults.IGameDefaults
import services.file.IFileIO
import services.logger.AppLogger
import services.logger.LogModule
import services.resources.i18n.I18nManager
import java.io.File
import java.io.FileNotFoundException

enum class ModImportResultStatus {
    Success,
    ModManifestFileNotFound,
    TargetModAlreadyInstalled,
    InvalidSourceDirectory,
    InvalidZipFile,
    FailedToExtractZip,
    FailedToCopyFiles,
    UnknownError
}

data class ModImportResult(
    val status: ModImportResultStatus,
    val modInstance: Mod? = null,
    val message: String? = null
)

object ModResourceView {
    /** The default mods data folder path */
    const val defaultModPath = "./mods"

    /** The default save folder path */
    const val defaultSavePath = "./saves"

    /** The default modpacks data folder path*/
    const val defaultDataPath = "./data"

    /** The default root data file path */
    const val defaultDataJsonPath = "./data/root.json"

    val fileIO: IFileIO = GlobalContext.get().get()

    /**
     * Store data of all available mods
     * 
     * This is essentially a special ModPack that contains all available mods.
     * Its name is fixed, apiVersion/gameVersion is not used, and all mods in it should be enabled.
     * It will be automatically loaded from disk when the application starts.
     */
    var installedMods =
        ObservableModPack(
            "root", "the index of all available mods", SemanticVersion(0, 0, 0),
            apiVersion = SemanticVersion(0, 0, 0),
            mods = mutableSetOf()
        )

    /**
     * Store all modpacks' data
     * 
     * This collection holds all the modpacks that the user has created or imported.
     * Each modpack can have its own set of mods with specific versions.
     */
    val modPacks = SnapshotStateList<ObservableModPack>()

    private val _taskProgress = MutableStateFlow(0f)

    /** The live progress of a task (0f - 1f) */
    val taskProgress: StateFlow<Float> = _taskProgress.asStateFlow()

    private val _taskStatus = MutableStateFlow("")

    /** The live message of a task */
    val taskStatus: StateFlow<String> = _taskStatus.asStateFlow()

    init {
        // load installed mods index data
        try {
            installedMods = Json
                .decodeFromString(ModPack.serializer(), fileIO.read(defaultDataJsonPath))
                .toObservable()
        } catch (_: FileNotFoundException) {
            AppLogger.warn(
                LogModule.ModResource,
                "Cannot find $defaultDataJsonPath, as it means no mod resource data was found, a new file will be created."
            )
            runBlocking { saveModPackDataToDisk(installedMods.toSerializable()) }
        } catch (e: Exception) {
            AppLogger.critical(
                LogModule.ModResource,
                "$e: An unexpected error occurred, due to the effect of the data, program will be closed. Please check the error manually"
            )
            throw e
        }
        // load other modPacks
        try {
            val modPackDataDirectory = File(defaultDataPath)
            modPackDataDirectory.walk().filter { it.isFile() && it.name.contains("json") && it.name != "root.json" }
                .toList().forEach { modPackFile ->
                    val modPack = Json.decodeFromString(ModPack.serializer(), modPackFile.readText())
                    modPacks.add(modPack.toObservable())
                }
        } catch (e: Exception) {
            AppLogger.critical(
                LogModule.ModResource,
                "$e: An unexpected error occurred, due to the effect of the data, program will be closed. Please check the error manually"
            )
            throw e
        }
    }

    /**
     * Add or Update mod pack data from given instance
     *
     * 添加或更新给定实例的模组包数据
     * @param modPack mod pack instance 模组包实例
     * @param overwrite whether to overwrite existing data (default: false) 当存在数据时是否覆盖
     * */
    fun addModPack(modPack: ObservableModPack, overwrite: Boolean = false) {
        modPacks.indexOfFirst { it.name == modPack.name }.let { index ->
            if (index != -1) {
                if (overwrite) {
                    modPacks[index] = modPack
                }
            } else {
                modPacks.add(modPack)
            }
        }
    }

    /**
     * Get modpack instance from loaded modpacks
     *
     * 从已加载的所有modpack中获取实例
     * */
    fun getModPackInstanceByName(name: String): ObservableModPack? {
        return modPacks.firstOrNull { it.name == name }
    }

    /**
     * 根据唯一标识符获取所有已安装的mod版本
     *
     * @param uid mod的唯一标识符
     * @return 包含所有匹配的mod版本的列表
     */
    fun getAllInstalledModVersionsByUniqueID(uid: String): List<ObservableMod> {
        return installedMods.mods.filter{ it.manifest.uniqueID == uid }
    }

    fun getModInstanceByUUID(modUUID: ModUUID): ObservableMod? {
        return installedMods.mods.firstOrNull { it.getUUID() == modUUID }
    }

    /**
     * Get all mod directories in one dir's all subdirectories
     *
     * 获取一个目录下所有是mod目录的目录
     * */
    fun getAllIsModDirFromTargetDir(dir: String = defaultModPath): List<File> {
        return File(dir)
            .walk()
            .filter { it.isDirectory() && Mod.isDirectoryAValidMod(it) }
            .toList()
    }

    /**
     * Copy a directory recursively
     *
     * 递归拷贝整个目录
     * */
    fun copyDirectory(source: File, target: File) {
        if (!target.exists()) {
            target.mkdirs()
        }
        source.listFiles()?.forEach { file ->
            val targetFile = File(target, file.name)
            try {
                if (file.isDirectory) {
                    copyDirectory(file, targetFile)
                } else {
                    file.copyTo(targetFile, overwrite = true)
                }
            } catch (e: Exception) {
                AppLogger.error(
                    LogModule.ModResource,
                    "Failed to copy file ${file.name}: ${e.message}"
                )
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun saveModPackDataToDisk(modPack: ModPack) {
        Json.encodeToStream(modPack, fileIO.getOutputStream("$defaultDataPath/${modPack.name}.json"))
    }

    /**
     * 从游戏目录中复制存档文件至指定的ModPack的存档文件目录中
     *
     * Copy save files from game directory to modpack's save file directory
     * */
    suspend fun copySaveFiles(modpack: ObservableModPack, gameDefaults: IGameDefaults = GlobalContext.get().get()) {
        AppLogger.info(LogModule.ModResource, "Copying save files to ${modpack.name}...")
        try {
            _taskStatus.value = I18nManager.currentStrings.value.modpack.import.importingSaves
            val sourceSavePath = gameDefaults.savePath
            val sourceSaveDir = File(sourceSavePath)

            if (!sourceSaveDir.exists() || !sourceSaveDir.isDirectory) {
                AppLogger.warn(
                    LogModule.ModResource,
                    "Source save directory not found: $sourceSavePath"
                )
                return
            }

            val targetSaveDir = File("$defaultSavePath/${modpack.name}")
            if (!targetSaveDir.exists()) {
                targetSaveDir.mkdirs()
            }

            copyDirectory(sourceSaveDir, targetSaveDir)
            _taskStatus.value = I18nManager.currentStrings.value.modpack.import.completed
        } catch (e: Exception) {
            AppLogger.error(
                LogModule.ModResource,
                "Failed to copy save files: ${e.message}"
            )
            _taskStatus.value = I18nManager.currentStrings.value.modpack.import.failed
            throw e
        }
        AppLogger.info(LogModule.ModResource, "Completed copying save files to ${modpack.name}.")
    }

    /**
     * 将指定ModPack的存档文件应用到游戏目录中
     *
     * Apply modpack's save files to game directory
     *
     * !! 警告: 此操作将删除游戏目录中的所有存档文件
     *
     * !! IMPORTANT: Will DELETE ALL SAVE FILES in game directory
     * */
    suspend fun applySaveFiles(modpack: ObservableModPack, gameDefaults: IGameDefaults = GlobalContext.get().get()) {
        AppLogger.info(LogModule.ModResource, "Applying save files to game directory...")
        try {
            _taskStatus.value = I18nManager.currentStrings.value.modpack.import.importingSaves

            val sourceSavePath = "$defaultSavePath/${modpack.name}"
            val sourceSaveDir = File(sourceSavePath)
            if (!sourceSaveDir.exists()) {
                AppLogger.error(
                    LogModule.ModResource,
                    "Source save directory not found: $sourceSavePath"
                )
                return
            }

            val targetSavePath = gameDefaults.savePath
            val targetSaveDir = File(targetSavePath)

            // 删除目标目录中的所有存档文件
            if (targetSaveDir.exists()) {
                targetSaveDir.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        file.deleteRecursively()
                    } else {
                        file.delete()
                    }
                }
            } else {
                targetSaveDir.mkdirs()
            }

            // 复制所有存档文件
            copyDirectory(sourceSaveDir, targetSaveDir)
            _taskStatus.value = I18nManager.currentStrings.value.modpack.import.completed
        } catch (e: Exception) {
            AppLogger.error(
                LogModule.ModResource,
                "Failed to apply save files: ${e.message}"
            )
            _taskStatus.value = I18nManager.currentStrings.value.modpack.import.failed
            throw e
        }
        AppLogger.info(LogModule.ModResource, "Completed applying save files to game directory.")
    }

    /**
     * Update mod data from disk for given modpack, will not update property enabled
     *
     * 从磁盘中为指定MoaPack更新指定的mod数据，不会更新enabled数据
     * */
    fun updateModDataFromDisk(targetMod: Mod, targetModPack: ObservableModPack): Boolean {
        val modIO = ModIO(targetMod)
        modIO.modInstance?.let { newInstance ->
            val oldInstance = targetModPack.getModInstanceByUUID(newInstance.getUUID())
            if (oldInstance == null) {
                return false
            }
            targetModPack.mods.replace(oldInstance, newInstance.toObservable())
            return true
        }
        return false
    }

    /**
     * Update modpack data from disk for given modpack, will not update property enabled
     *
     * 从磁盘中为指定ModPack更新数据，不会更新enabled数据
     * */
    fun updateModPackDataFromDisk(targetModPack: ModPack) {

    }

    /**
     * apply modpack configs to mod files
     * 将modpack的配置（enabled/config.json）应用到mod文件中
     * */
    fun applyModPackConfigToDisk(modPack: ModPack) {

    }

    /**
     * 从单个文件夹导入mod
     * @param path mod文件夹路径
     * @param targetModPack 目标ModPack，如果为null则只复制文件
     * @param willSaveToFile 是否保存到文件
     * @return ModImportResult 导入结果
     */
    suspend fun importModFromFolder(
        path: String,
        targetModPack: ObservableModPack?,
        willSaveToFile: Boolean = true
    ): ModImportResult {
        try {
            // 检查源文件夹是否存在
            val sourceDir = File(path)
            if (!sourceDir.exists() || !sourceDir.isDirectory) {
                return ModImportResult(
                    ModImportResultStatus.InvalidSourceDirectory,
                    message = I18nManager.format(
                        I18nManager.currentStrings.value.modpack.import.invalidSourceDirectory,
                        "path" to path
                    )
                )
            }

            // 检查 Manifest 文件是否存在
            if (!Mod.isDirectoryAValidMod(path)) {
                return ModImportResult(
                    ModImportResultStatus.ModManifestFileNotFound,
                    message = I18nManager.format(
                        I18nManager.currentStrings.value.modpack.import.invalidSourceDirectory,
                        "path" to path
                    )
                )
            }

            val mod = Mod(sourceDir.absolutePath)

            // 检查是否已安装
            val installedMod = installedMods.getModInstanceByUUID(mod.getUUID())
            if (installedMod != null) {
                targetModPack?.let {
                    // 检查目标ModPack是否含有目标mod，如果没有，进行添加
                    if (targetModPack.getModInstanceByUUID(mod.getUUID()) == null) {
                        targetModPack.mods.add(installedMod.toSerializable().deepCopy().toObservable())
                    }
                }
                AppLogger.warn(
                    LogModule.ModResource,
                    "Skipped to import mod ${mod.manifest.name} as it's already installed"
                )
                return ModImportResult(
                    ModImportResultStatus.TargetModAlreadyInstalled,
                    modInstance = mod,
                    message = I18nManager.format(
                        I18nManager.currentStrings.value.modpack.import.modAlreadyInstalled,
                        "name" to mod.manifest.name
                    )
                )
            }

            // 确保目标目录存在
            val targetDir = File(defaultModPath)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            // 构建新的文件夹名
            val newDirName = "${mod.manifest.uniqueID}-${mod.manifest.version}"
            val targetModDir = File(targetDir, newDirName)

            // 如果目标目录已存在，先删除
            if (targetModDir.exists()) {
                targetModDir.deleteRecursively()
            }

            // 复制整个文件夹
            sourceDir.copyRecursively(targetModDir)

            // 更新installedMods
            installedMods.mods.add(mod.toObservable())
            targetModPack?.mods?.add(mod.deepCopy().toObservable())

            // 保存到文件
            if (willSaveToFile) {
                saveModPackDataToDisk(installedMods.toSerializable())
                targetModPack?.let { saveModPackDataToDisk(it.toSerializable()) }
            }

            return ModImportResult(
                ModImportResultStatus.Success,
                modInstance = mod,
                message = I18nManager.format(
                    I18nManager.currentStrings.value.modpack.import.success,
                    "name" to mod.manifest.name
                )
            )
        } catch (e: Exception) {
            AppLogger.error(
                LogModule.ModResource,
                "[$e]Failed to import mod from $path: ${e.message}"
            )
            return ModImportResult(
                ModImportResultStatus.UnknownError,
                message = I18nManager.format(
                    I18nManager.currentStrings.value.modpack.import.failed,
                    "error" to (e.message ?: "Unknown error")
                )
            )
        }
    }

    /**
     * 递归导入文件夹中的所有mod
     * @param path 根文件夹路径
     * @param targetModPack 目标ModPack
     * @return 导入失败的mod列表
     */
    suspend fun importModFromFolderRecursively(
        path: String,
        targetModPack: ObservableModPack? = null
    ): List<ModImportResult> {
        val failedImports = mutableListOf<ModImportResult>()
        try {
            val sourceDir = File(path)
            if (!sourceDir.exists() || !sourceDir.isDirectory) {
                return listOf(
                    ModImportResult(
                        ModImportResultStatus.InvalidSourceDirectory,
                        message = I18nManager.format(
                            I18nManager.currentStrings.value.modpack.import.invalidSourceDirectory,
                            "path" to path
                        )
                    )
                )
            }

            val modDirs = getAllIsModDirFromTargetDir(path)
            var processedCount = 0
            val totalSteps = modDirs.size
            // 重置进度
            _taskProgress.value = 0f
            _taskStatus.value = ""

            modDirs.forEach { dir ->
                _taskStatus.value = I18nManager.format(
                    I18nManager.currentStrings.value.modpack.import.importingMod,
                    "name" to dir.name,
                    "current" to (processedCount + 1).toString(),
                    "total" to totalSteps.toString()
                )

                val result = importModFromFolder(dir.absolutePath, targetModPack, false)
                if (result.status != ModImportResultStatus.Success) {
                    failedImports.add(result)
                }

                processedCount++
                _taskProgress.value = processedCount.toFloat() / totalSteps
            }

            // 保存到文件
            saveModPackDataToDisk(installedMods.toSerializable())
            targetModPack?.let { saveModPackDataToDisk(it.toSerializable()) }
        } catch (e: Exception) {
            AppLogger.error(
                LogModule.ModResource,
                "Failed to import mods from $path: ${e.message}"
            )
            _taskStatus.value = I18nManager.currentStrings.value.modpack.import.failed
            throw e
        }
        return failedImports
    }

    /**
     * 从压缩文件导入mod
     * @param file 压缩文件路径
     * @param targetModPack 目标ModPack
     * @return ModImportResult 导入结果
     */
    suspend fun importModFromZippedFile(
        file: String,
        targetModPack: ObservableModPack?
    ): List<ModImportResult> {
        try {
            val zipFile = File(file)
            if (!zipFile.exists() || !zipFile.isFile) {
                return listOf(
                    ModImportResult(
                        ModImportResultStatus.InvalidZipFile,
                        message = I18nManager.format(
                            I18nManager.currentStrings.value.modpack.import.invalidZipFile,
                            "path" to file
                        )
                    )
                )
            }

            // 创建临时目录
            val tempDir = File.createTempFile("mod_import_", null)
            tempDir.delete()
            tempDir.mkdirs()

            try {
                // 解压文件
                when {
                    file.endsWith(".zip") -> {
                        // TODO: 实现zip解压
                    }

                    file.endsWith(".rar") -> {
                        // TODO: 实现rar解压
                    }

                    file.endsWith(".7z") -> {
                        // TODO: 实现7z解压
                    }

                    else -> {
                        return listOf(
                            ModImportResult(
                                ModImportResultStatus.InvalidZipFile,
                                message = I18nManager.format(
                                    I18nManager.currentStrings.value.modpack.import.unsupportedZipFormat,
                                    "file" to file
                                )
                            )
                        )
                    }
                }

                // 导入解压后的文件
                return importModFromFolderRecursively(tempDir.absolutePath, targetModPack)
            } finally {
                // 清理临时目录
                tempDir.deleteRecursively()
            }
        } catch (e: Exception) {
            AppLogger.error(
                LogModule.ModResource,
                "Failed to import mod from zip $file: ${e.message}"
            )
            return listOf(
                ModImportResult(
                    ModImportResultStatus.FailedToExtractZip,
                    message = I18nManager.format(
                        I18nManager.currentStrings.value.modpack.import.failedToExtract,
                        "error" to (e.message ?: "Unknown error")
                    )
                )
            )
        }
    }
}