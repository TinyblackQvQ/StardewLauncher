package services.logger

import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// 定义日志级别枚举
enum class LogLevel(val levelString: String, val colorCode: String) {
    DEBUG("Debug", "\u001B[36m"),     // 青色
    INFO("Info", "\u001B[34m"),       // 蓝色
    WARN("Warn", "\u001B[33m"),       // 黄色
    ERROR("Error", "\u001B[31m"),      // 红色
    CRITICAL("Critical", "\u001B[35m") // 品红色
}

enum class LogModule(val moduleName: String) {
    Default("Default"),
    App("App"),
    Logger("Logger"),
    FileIO("FileIO"),
    WindowManager("WindowManager"),
    Config("ConfigManager"),
    ConfigProperty("ConfigProperty"),
    I18n("I18nManager"),
    Theme("ThemeManager"),
    ModLoader("ModLoader"),
    ModResource("ModResourceLoader"),
    ModPack("ModPackLoader"),
    Network("Network"),
    UI("UI"),
}

object AppLogger {
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private const val RESET_COLOR = "\u001B[0m" // 重置颜色

    // 日志文件配置
    private const val LOG_DIR_NAME = "log"
    private val CURRENT_DATE_TIME_FOR_FILENAME =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"))
    private val LOG_FILE_NAME = "$LOG_DIR_NAME/$CURRENT_DATE_TIME_FOR_FILENAME.log"

    private lateinit var logWriter: PrintWriter // 使用 lateinit，因为可能在初始化时失败

    // 初始化日志文件写入器
    init {
        try {
            val logFile = File(LOG_FILE_NAME)
            val logDir = logFile.parentFile

            if (logDir != null && !logDir.exists()) {
                logDir.mkdirs()
                println(
                    createColoredString(
                        LogLevel.INFO,
                        LogModule.Logger,
                        "Created log directory: ${logDir.absolutePath}"
                    )
                )
            }

            if (!logFile.exists()) {
                logFile.createNewFile()
            }

            logWriter = PrintWriter(FileWriter(logFile, true), true)
            println(
                createColoredString(
                    LogLevel.INFO,
                    LogModule.Logger,
                    "Logger initialized. Log file: ${logFile.absolutePath}"
                )
            )
        } catch (e: IOException) {
            println(
                createColoredString(
                    LogLevel.ERROR,
                    LogModule.Logger,
                    "Error initializing logger: Could not open log file '$LOG_FILE_NAME'."
                )
            )
            e.printStackTrace()
        }
    }

    private fun createColoredString(level: LogLevel, module: LogModule, message: String): String {
        val now = LocalDateTime.now()
        val formattedDate = now.format(dateTimeFormatter)
        val rawLogString = "[$formattedDate] [${level.levelString.padStart(8)}] [${module.moduleName.padEnd(17)}]\t$message"
        val coloredConsoleLogString = "${level.colorCode}$rawLogString$RESET_COLOR"
        return coloredConsoleLogString
    }

    // 私有方法用于实际的日志输出逻辑
    private fun log(level: LogLevel, module: LogModule, message: String, throwable: Throwable? = null) {
        val now = LocalDateTime.now()
        val formattedDate = now.format(dateTimeFormatter)

        // 构造不带颜色的原始日志字符串（用于文件写入）
        val rawLogString = "[$formattedDate] [${level.levelString.padStart(8)}] [${module.moduleName.padEnd(17)}] $message"

        // 构造带颜色的控制台日志字符串
        val coloredConsoleLogString = "${level.colorCode}$rawLogString$RESET_COLOR"

        // --- 控制台输出 ---
        if (level.ordinal >= minConsoleLogLevel.ordinal) {
            println(coloredConsoleLogString)
            throwable?.printStackTrace() // 堆栈跟踪默认打印到 System.err，不带颜色
        }

        // --- 文件输出 ---
        if (::logWriter.isInitialized && level.ordinal >= minFileLogLevel.ordinal) {
            try {
                logWriter.println(rawLogString) // 写入不带颜色的原始日志
                throwable?.printStackTrace(logWriter) // 将堆栈跟踪写入文件
            } catch (e: Exception) {
                System.err.println("Error writing to log file: ${e.message}")
            }
        }
    }

    // 在应用程序关闭时，务必调用此方法来关闭文件写入器
    fun close() {
        if (::logWriter.isInitialized) {
            try {
                logWriter.close()
                println("Logger closed. Log file '$LOG_FILE_NAME' closed.")
            } catch (e: Exception) {
                System.err.println("Error closing log writer: ${e.message}")
            }
        }
    }

    // 可以选择性地添加一个最低日志级别控制
    var minConsoleLogLevel: LogLevel = LogLevel.INFO
    var minFileLogLevel: LogLevel = LogLevel.INFO     // 文件最低日志级别

    // 公开的日志方法
    fun debug(module: LogModule, message: String) {
        log(LogLevel.DEBUG, module, message)
    }

    fun info(module: LogModule, message: String) {
        if (LogLevel.INFO.ordinal >= minConsoleLogLevel.ordinal) {
            log(LogLevel.INFO, module, message)
        }
    }

    fun warn(module: LogModule, message: String) {
        if (LogLevel.WARN.ordinal >= minConsoleLogLevel.ordinal) {
            log(LogLevel.WARN, module, message)
        }
    }

    fun error(module: LogModule, message: String, throwable: Throwable? = null) {
        if (LogLevel.ERROR.ordinal >= minConsoleLogLevel.ordinal) {
            log(LogLevel.ERROR, module, message)
            throwable?.printStackTrace() // 打印堆栈跟踪，堆栈跟踪本身可能不会带颜色，取决于终端
        }
    }

    fun critical(module: LogModule, message: String) {
        if (LogLevel.CRITICAL.ordinal >= minConsoleLogLevel.ordinal) {
            log(LogLevel.CRITICAL, module, message)
        }
    }

    // 重载方法，不带模块参数
    fun debug(message: String) = debug(LogModule.Default, message)
    fun info(message: String) = info(LogModule.Default, message)
    fun warn(message: String) = warn(LogModule.Default, message)
    fun error(message: String, throwable: Throwable? = null) = error(LogModule.Default, message, throwable)
    fun critical(message: String) = critical(LogModule.Default, message)
}