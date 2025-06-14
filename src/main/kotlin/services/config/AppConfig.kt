package services.config

import services.logger.AppLogger
import services.logger.LogModule
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

data class RootConfig(
    val general: GeneralConfig
) {
    fun getAllConfigOptions(): List<IConfigOption<*>> {
        val options = mutableListOf<IConfigOption<*>>()
        fun collectOptions(obj: Any) {
            obj::class.memberProperties.forEach { property ->
                if (property is KProperty1<*, *>) {
                    val value = try {
                        if (property.name in arrayOf("settings", "cachedKeyPath")) {
                            return
                        }
                        property.getter.call(obj)
                    } catch (e: Exception) {
                        // 捕获可能由于反射访问私有或初始化顺序导致的异常
                        // AppLogger.error(LogModule.CONFIG, .*)
                        null
                    }

                    if (value is IConfigOption<*>) {
                        options.add(value)
                    } else if (value != null && value::class.isData) { // 递归遍历数据类
                        collectOptions(value)
                    }
                }
            }
        }
        collectOptions(this) // 从 RootConfig 自身开始收集
        return options
    }

    fun resetAll() {
        AppLogger.info(LogModule.Config, "Resetting all config options to their defaults...")
        getAllConfigOptions().forEach { option ->
            option.reset()
        }
        AppLogger.info(LogModule.Config, "All config options reset.")
    }

    fun updateAll() {
        AppLogger.info(LogModule.Config, "Loading all configs from properties...")
        getAllConfigOptions().forEach { option ->
            option.read()
        }
        AppLogger.info(LogModule.Config, "All config options read.")
    }
}

data class GeneralConfig(
    val isAppFirstLaunch: IConfigOption<Boolean>,
    val lastSelectedModPackName: IConfigOption<String>,
    val appLanguage: IConfigOption<String>,
    val colorMode: IConfigOption<String>,
    val gameDirectory: IConfigOption<String>,
    @Deprecated("This property is not in use for now.") val modLibraryDirectory: IConfigOption<String>
)

/**
 * AppConfig will NOT invoke UI updates like StateFlow, you need to register your update function to listen the changes
 *
 * Don't be afraid of one configuration may could be not loaded, all configs will be loaded when the app launch, when missing, it will go with defaults
 *
 * Also, its values is mutable, so if you want to read it at the time you want instead of live update, just go forward.
 * */
val AppConfig = RootConfig(
    general = GeneralConfig(
        isAppFirstLaunch = ConfigOption(
            default = true,
            isVisible = false
        ),
        lastSelectedModPackName = ConfigOption(
            default = "",
            isVisible = false
        ),
        appLanguage = ConfigOption(
            default = "en_us"
        ),
        colorMode = ConfigOption(
            default = "LIGHT"
        ),
        gameDirectory = ConfigOption(
            default = "",
            isDirectory = true
        ),
        modLibraryDirectory = ConfigOption(
            default = "./mods",
            isDirectory = true
        )
    )
)
