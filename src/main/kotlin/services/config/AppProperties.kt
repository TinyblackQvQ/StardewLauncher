package services.config

import services.logger.AppLogger
import services.logger.LogModule
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.Properties

object AppProperties {
    const val DEFAULT_CONFIG_FILE_PATH = "./config.properties"
    val properties = Properties()

    fun loadInitialConfigProperties(properties: Properties = this.properties) {
        try {
            val inputStream = FileInputStream(DEFAULT_CONFIG_FILE_PATH)
            properties.load(inputStream)
            AppLogger.info(LogModule.ConfigProperty, ("Config loaded from: $DEFAULT_CONFIG_FILE_PATH"))
        } catch (e: Exception) {
            when (e) {
                is FileNotFoundException -> {
                    throw FileNotFoundException("Failed to open config file $DEFAULT_CONFIG_FILE_PATH. Please Initialize file with default properties...")
                }
                else -> {
                    AppLogger.error(LogModule.ConfigProperty, ("Error loading config from $DEFAULT_CONFIG_FILE_PATH: ${e.message}"))
                    throw e
                }
            }
        }
    }

    /**
     * Saves the given Properties object to the config file.
     */
    fun saveConfigProperties(properties: Properties = this.properties) {
        try {
            FileOutputStream(DEFAULT_CONFIG_FILE_PATH).use { outputStream ->
                properties.store(outputStream, "Application Configuration")
//                AppLogger.info(LogModule.ConfigProperty, ("Config saved to: $DEFAULT_CONFIG_FILE_PATH"))
            }
        } catch (e: Exception) {
            AppLogger.error(LogModule.ConfigProperty, ("Error saving config to file: ${e.message}"))
        }
    }
}