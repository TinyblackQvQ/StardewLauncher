package services.config

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.context.GlobalContext
import services.logger.AppLogger
import services.logger.LogModule
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class UnknownConfigOptionTypeException : Exception() {
    override val message: String?
        get() = "Unknown type"
}

interface IValueRange<T> {
    val min: T
    val max: T
    fun isValid(value: T): Boolean
}

class ValueRange<T>(override val min: T, override val max: T) : IValueRange<T> {
    override fun isValid(value: T): Boolean {
        return when (value) {
            is Number -> value.toDouble() > (min as Number).toDouble() && value.toDouble() < (max as Number).toDouble()
            is String -> false
            else -> false
        }
    }
}

interface IConfigOption<T> {
    val i18nKey: String
    val default: T?
    val isVisible: Boolean
    val isColor: Boolean
    val isDirectory: Boolean
    val isOption: Boolean
    val isCheckbox: Boolean
    val options: List<T>?
    val isSlider: Boolean
    val valueRange: IValueRange<T>?
    var onValueUpdate: (newValue: T?) -> Unit
    fun read(): T?
    fun write(value: T)
    fun reset()
    fun getChildren(): MutableList<IConfigOption<*>>
    fun resolveKeyPath(): String
}

data class ConfigOption<T>(
    override val default: T?,
    override var onValueUpdate: (newValue: T?) -> Unit = {},
    override val isVisible: Boolean = true,
    override val isColor: Boolean = false,
    override val isDirectory: Boolean = false,
    override val isOption: Boolean = false,
    override val isCheckbox: Boolean = false,
    override val options: List<T>? = null,
    override val isSlider: Boolean = false,
    override val valueRange: IValueRange<T>? = null,
    private val settings: Settings = GlobalContext.get().get(),
) : IConfigOption<T> {
    private val cachedKeyPath: String by lazy { resolveKeyPathInternal() }
    private val cachedI18nKey: String by lazy { "settings.${cachedKeyPath}" }

    override val i18nKey: String
        get() = cachedI18nKey

    private val _title = MutableStateFlow("")
    private val _desc = MutableStateFlow("")

    override fun read(): T? {
        val key = resolveKeyPath()
        return try {
            val value = when (default) {
                is Boolean -> settings.get(key, default as Boolean) as T?
                is Int -> settings.get(key, default as Int) as T?
                is Long -> settings.get(key, default as Long) as T?
                is Float -> settings.get(key, default as Float) as T?
                is Double -> settings.get(key, default as Double) as T?
                is String -> settings.get(key, default as String) as T?
                else -> throw UnknownConfigOptionTypeException()
            }
            onValueUpdate(value)
            value
        } catch (e: Exception) {
            AppLogger.error(LogModule.Config, ("Failed to read $key: ${e.message}"))
            default
        }
    }

    override fun write(value: T) {
        val key = resolveKeyPath()
        try {
            AppLogger.info(LogModule.Config, ("Will write $key = $value"))
            when (value) {
                is Boolean -> settings[key] = value
                is Int -> settings[key] = value
                is Long -> settings[key] = value
                is Float -> settings[key] = value
                is Double -> settings[key] = value
                is String -> settings[key] = value
                else -> AppLogger.warn(LogModule.Config, ("Unsupported type for writing: ${value?.let { it::class.simpleName }}"))
            }
            onValueUpdate(value)
        } catch (e: Exception) {
            AppLogger.error(LogModule.Config, "Failed to write $key: ${e.message}")
        }
    }

    override fun reset() {
        if (default != null) {
            AppLogger.info(LogModule.Config, ("Will reset $cachedKeyPath to default value: $default"))
            write(default as T)
        } else {
            AppLogger.error(LogModule.Config, "Cannot reset $cachedKeyPath, default value is null")
        }
    }

    override fun getChildren(): MutableList<IConfigOption<*>> {
        val children = mutableListOf<IConfigOption<*>>()
        for (property in this::class.memberProperties) {
            val value = try {
                property.getter.call(this)
            } catch (e: Exception) {
                continue
            }
            if (value is IConfigOption<*>) {
                children.add(value)
            }
        }
        return children
    }

    fun resolveKeyPathInternal(): String {
        // Helper function to recursively search for 'this' instance
        fun findPath(currentObject: Any, currentPath: List<String>): String? {
            // If the current object is the target ConfigOption instance, return the path
            if (currentObject === this@ConfigOption) {
                return currentPath.joinToString(".")
            }
            // Iterate over properties of the current object
            val properties = currentObject::class.memberProperties
            for (property in properties) {
                // Ensure it's a value property (not a function etc.)
                if (property is KProperty1<*, *>) {
                    val value = try {
                        if (property.name in arrayOf("settings", "cachedKeyPath")) {
                            continue
                        }
                        property.getter.call(currentObject)
                    } catch (e: Exception) {
                        continue
                    }

                    if (value != null) {
                        // Recursively search in nested objects
                        val result = findPath(value, currentPath + property.name)
                        if (result != null) {
                            return result
                        }
                    }
                }
            }
            return null // Not found in this branch
        }

        // Start the search from the global AppConfigOptions singleton
        val fullPath = findPath(AppConfig, emptyList())

        if (fullPath == null) {
            throw IllegalStateException("AppConfig: Could not resolve key path for provided ConfigOption. Is it defined in 'AppConfigOptions'?")
        }
        return fullPath
    }

    override fun resolveKeyPath(): String {
        return cachedKeyPath
    }
}