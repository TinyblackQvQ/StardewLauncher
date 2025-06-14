package services.defaults

import models.common.SemanticVersion
import org.koin.core.context.GlobalContext
import services.file.IFileIO

/**
 * TODO: Other platform are not supported for now
 *
 * Linux&MacOS: ~/.config/StardewValley/Saves
 *
 * Android: /storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves
 *
 * read more at [Saves](https://www.stardewvalleywiki.com/Saves)
 * */

class WindowsGameDefaults: IGameDefaults {
    override val savePath = "${System.getenv("APPDATA")}\\StardewValley\\Saves"
    override fun getCurrentGameVersion(gameDirectory: String, fileIO: IFileIO): SemanticVersion? {
        val dllPath = "$gameDirectory/Stardew Valley.dll"
        if (!fileIO.exists(dllPath)) {
            return null
        }
        return WindowsDLLChecker.getDllVersion(dllPath)
    }

    override fun getCurrentSMAPIVersion(gameDirectory: String, fileIO: IFileIO): SemanticVersion? {
        val dllPath = "$gameDirectory/StardewModdingAPI.dll"
        if (!fileIO.exists(dllPath)) {
            return null
        }
        return WindowsDLLChecker.getDllVersion(dllPath)
    }
}