package models.mod

import models.mod.serializable.Mod
import views.ModResourceView
import java.io.File

class ModIO {
    var manifestFile: File? = null
    var configFile: File? = null
    var directoryFile: File? = null
    var modInstance: Mod? = null

    constructor(targetMod: Mod, modResourceDirectory: String = "./mods") {
        /**
         * 将尝试寻找存有目标mod的目录
         * 1. 使用 mod.manifest.uniqueID-mod.manifest.version 作为目录名进行读取
         * 2. 使用 .mod.manifest.uniqueID-mod.manifest.version 作为目录名进行读取，对于该目录被禁用的情况（即首部存在一个"."将禁用该mod）
         * 3. 强制搜索整个目录来寻找目标mod
         * */
        var modDirectory = "$modResourceDirectory/${targetMod.manifest.uniqueID}-${targetMod.manifest.version}"
        if (!Mod.isDirectoryAValidMod(modDirectory))
            modDirectory = "$modResourceDirectory/.${targetMod.manifest.uniqueID}-${targetMod.manifest.version}"
        if (!Mod.isDirectoryAValidMod(modDirectory))
            for (file in ModResourceView.getAllIsModDirFromTargetDir(modResourceDirectory)) {
                val mod = Mod(file.absolutePath)
                if (mod.manifest.uniqueID == targetMod.manifest.uniqueID && mod.manifest.version == targetMod.manifest.version) {
                    modDirectory = file.absolutePath
                    modInstance = mod
                    break
                }
            }
        /**
         * 经过所有检查后，directory和manifest的存在已经得到保证
         * */
        directoryFile = File(modDirectory)
        manifestFile = File("$modDirectory/manifest.json")
        configFile = File("$modDirectory/config.json")
        if (!configFile!!.exists())
            configFile = null
        if (modInstance == null)
            modInstance = Mod(modDirectory)
    }
}