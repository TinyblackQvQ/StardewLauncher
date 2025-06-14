package services.resources.i18n

import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import services.config.AppConfig
import services.logger.AppLogger
import services.logger.LogModule
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

data class Language(
    val code: String,
    val desc: String
)

enum class AppLanguages(val lang: Language) {
    UnitedStatesEnglish(Language(code = "en_us", desc = "United States (US)")),
    SimplifiedChinese(Language(code = "zh_cn", desc = "简体中文 (中国)"));

    companion object {
        fun fromCode(code: String): AppLanguages? {
            return entries.find { it.lang.code == code }
        }
    }
}

object I18nManager {
    val defaultLanguage = AppLanguages.UnitedStatesEnglish
    private val _currentStrings = MutableStateFlow(AppString)
    val currentStrings: StateFlow<StringRoot> = _currentStrings.asStateFlow()

    private val _currentLanguage = MutableStateFlow<AppLanguages>(AppLanguages.UnitedStatesEnglish)
    val currentLanguage: StateFlow<AppLanguages> = _currentLanguage.asStateFlow()

    init {
        // 初始加载默认语言
        AppLogger.info(LogModule.I18n, "Will load default language: $defaultLanguage")
        loadLanguage(defaultLanguage)
        // 注册回调函数
        AppConfig.general.appLanguage.onValueUpdate = { language ->
            language?.let { AppLanguages.fromCode(it) }?.let { loadLanguage(it) }
        }
    }

    fun loadLanguage(language: AppLanguages) {
        val fileName = "i18n/${language.lang.code}.yaml"
        try {
            val inputStream: InputStream? = this::class.java.classLoader.getResourceAsStream(fileName)
            if (inputStream == null) {
                AppLogger.info(LogModule.I18n, ("Error: i18n file not found: $fileName"))
                return
            }
            InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
                val loadedStrings = Yaml.default.decodeFromString(StringRoot.serializer(), reader.readText())
                _currentStrings.value = loadedStrings
                _currentLanguage.value = language
                AppLogger.info(LogModule.I18n, ("Language loaded: ${language.lang.code}"))
            }
        } catch (e: Exception) {
            AppLogger.info(LogModule.I18n, ("Error loading i18n file $fileName: ${e.message}"))
            e.printStackTrace()
            // 加载失败时回退到默认或上次成功的语言
            _currentStrings.value = AppString
        }
    }

    fun format(template: String, vararg args: Pair<String, String>): String {
        var result = template
        args.forEach { (key, value) ->
            result = result.replace("{${key}}", value)
        }
        return result
    }
}

fun String.i18nFormat(vararg args: Pair<String, String>): String {
    return I18nManager.format(this, *args)
}