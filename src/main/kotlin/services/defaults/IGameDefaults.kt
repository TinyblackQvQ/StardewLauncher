package services.defaults

import models.common.SemanticVersion
import org.koin.core.context.GlobalContext
import services.config.AppConfig
import services.file.IFileIO

interface IGameDefaults {
    val savePath: String
    fun getCurrentGameVersion(
        gameDirectory: String = AppConfig.general.gameDirectory.read() ?: "",
        fileIO: IFileIO = GlobalContext.get().get()
    ): SemanticVersion?

    fun getCurrentSMAPIVersion(
        gameDirectory: String = AppConfig.general.gameDirectory.read() ?: "",
        fileIO: IFileIO = GlobalContext.get().get()
    ): SemanticVersion?
}