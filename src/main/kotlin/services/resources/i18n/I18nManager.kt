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

/**
 * 表示一种语言的信息
 * @param code 语言代码，如"en_us"表示美式英语
 * @param desc 语言描述，如"United States (US)"表示美国(US)
 */
data class Language(
    val code: String,
    val desc: String
)

/**
 * 应用程序支持的语言列表
 * 每个枚举值包含一个具体的语言信息
 */
enum class AppLanguages(val lang: Language) {
    /** 美式英语 */
    UnitedStatesEnglish(Language(code = "en_us", desc = "United States (US)")),
    
    /** 简体中文 */
    SimplifiedChinese(Language(code = "zh_cn", desc = "简体中文 (中国)"));

    companion object {
        /**
         * 根据语言代码获取对应的枚举值
         * 
         * @param code 要查找的语言代码
         * @return 匹配的AppLanguages枚举值，如果没有找到则返回null
         */
        fun fromCode(code: String): AppLanguages? {
            return entries.find { it.lang.code == code }
        }
    }
}

/**
 * 国际化管理器
 * 负责加载和管理应用程序的多语言资源
 */
object I18nManager {
    // 默认语言设置为美式英语
    val defaultLanguage = AppLanguages.UnitedStatesEnglish
    
    // 当前字符串资源的可变状态流
    private val _currentStrings = MutableStateFlow(AppString)
    
    // 只读状态流，供外部观察当前字符串资源
    val currentStrings: StateFlow<StringRoot> = _currentStrings.asStateFlow()

    // 当前语言的可变状态流
    private val _currentLanguage = MutableStateFlow<AppLanguages>(AppLanguages.UnitedStatesEnglish)
    
    // 只读状态流，供外部观察当前语言
    val currentLanguage: StateFlow<AppLanguages> = _currentLanguage.asStateFlow()

    init {
        // 初始加载默认语言
        AppLogger.info(LogModule.I18n, "Will load default language: $defaultLanguage")
        loadLanguage(defaultLanguage)
        
        // 注册回调函数，当语言设置发生变化时更新当前语言
        AppConfig.general.appLanguage.onValueUpdate = { language ->
            language?.let { AppLanguages.fromCode(it) }?.let { loadLanguage(it) }
        }
    }

    /**
     * 加载指定语言的国际化资源
     * 
     * @param language 要加载的语言
     */
    fun loadLanguage(language: AppLanguages) {
        val fileName = "i18n/${language.lang.code}.yaml"
        try {
            val inputStream: InputStream? = this::class.java.classLoader.getResourceAsStream(fileName)
            if (inputStream == null) {
                AppLogger.info(LogModule.I18n, "Error: i18n file not found: $fileName")
                return
            }
            InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
                val loadedStrings = Yaml.default.decodeFromString(StringRoot.serializer(), reader.readText())
                _currentStrings.value = loadedStrings
                _currentLanguage.value = language
                AppLogger.info(LogModule.I18n, "Language loaded: ${language.lang.code}")
            }
        } catch (e: Exception) {
            AppLogger.info(LogModule.I18n, "Error loading i18n file $fileName: ${e.message}")
            e.printStackTrace()
            // 加载失败时回退到默认或上次成功的语言
            _currentStrings.value = AppString
        }
    }

    /**
     * 使用当前语言的字符串资源格式化模板字符串
     * 
     * @param template 模板字符串，可以包含占位符如{key}
     * @param args 可变参数，每个参数是一个键值对，用于替换模板中的占位符
     * @return 替换后的字符串
     */
    fun format(template: String, vararg args: Pair<String, String>): String {
        var result = template
        args.forEach { (key, value) ->
            result = result.replace("{${key}}", value)
        }
        return result
    }
}

/**
 * 为String类添加扩展函数，方便使用国际化功能
 * 
 * @param args 可变参数，每个参数是一个键值对，用于替换模板中的占位符
 * @return 替换后的字符串
 */
fun String.i18nFormat(vararg args: Pair<String, String>): String {
    return I18nManager.format(this, *args)
}