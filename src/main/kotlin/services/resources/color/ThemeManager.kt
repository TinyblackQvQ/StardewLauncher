package services.resources.color

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamicColorScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import services.config.AppConfig
import services.logger.AppLogger
import services.logger.LogModule

enum class ThemeMode {
    LIGHT, DARK, SYSTEM;

    companion object {
        fun fromString(str: String): ThemeMode? {
            return entries.find { it.name == str.uppercase() }
        }
    }
}

object ThemeManager {
    private val _currentSeedColor = MutableStateFlow(Color(0xA1FF00)) // Default Material Purple
    val currentSeedColor: StateFlow<Color> = _currentSeedColor.asStateFlow()

    private val _currentThemeMode = MutableStateFlow(ThemeMode.LIGHT) // Default to Light
    val currentThemeMode: StateFlow<ThemeMode> = _currentThemeMode.asStateFlow()

    private val _currentColorScheme = MutableStateFlow(
        createDynamicColorScheme(
            _currentSeedColor.value,
            _currentThemeMode.value == ThemeMode.DARK
        )
    )
    val currentColorScheme: StateFlow<ColorScheme> = _currentColorScheme.asStateFlow()

    private fun createDynamicColorScheme(
        seedColor: Color,
        useDarkTheme: Boolean,
        isAmoled: Boolean = false
    ): ColorScheme {
        return dynamicColorScheme(
            seedColor = seedColor,
            isDark = useDarkTheme,
            isAmoled = isAmoled
        )
    }

    init {
        AppConfig.general.colorMode.onValueUpdate = { mode ->
            mode?.let { ThemeMode.fromString(it) }?.let { setThemeMode(it) }
        }
    }

    /**
     * 设置颜色种子
     * @param color 种子颜色
     * */
    fun setSeedColor(color: Color) {
        _currentSeedColor.value = color
        _currentColorScheme.value = createDynamicColorScheme(
            _currentSeedColor.value,
            _currentThemeMode.value == ThemeMode.DARK
        )
    }

    /**
     * 设置主题颜色模式
     * @param mode 颜色模式
     * */
    fun setThemeMode(mode: ThemeMode) {
        _currentThemeMode.value = mode
        _currentColorScheme.value = createDynamicColorScheme(
            _currentSeedColor.value,
            _currentThemeMode.value == ThemeMode.DARK
        )
        AppLogger.info(LogModule.Theme, "Theme loaded: $mode")
    }

    /**
     * 通过切换来设置主题颜色模式
     * */
    fun toggleThemeMode() {
        setThemeMode(
            when (_currentThemeMode.value) {
                ThemeMode.LIGHT -> ThemeMode.DARK
                ThemeMode.DARK -> ThemeMode.LIGHT
                ThemeMode.SYSTEM -> ThemeMode.LIGHT // You might want to add platform-specific logic here
            }
        )
    }
}