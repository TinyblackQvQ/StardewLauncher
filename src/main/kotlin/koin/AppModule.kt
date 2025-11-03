/**
 * Copyright (C) 2025 Miluko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package koin

import androidx.compose.material3.SnackbarHostState
import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import services.config.AppConfig
import services.config.AppProperties
import services.config.AppProperties.saveConfigProperties
import services.defaults.IGameDefaults
import services.defaults.WindowsGameDefaults
import services.file.DesktopFileIO
import services.file.IFileIO
import services.logger.AppLogger
import services.navigation.NavigationManager
import services.resources.color.ThemeManager
import services.resources.i18n.I18nManager
import services.window.WindowManager

@OptIn(ExperimentalSerializationApi::class)
val appModule = module {
    single<Settings> {
        PropertiesSettings(AppProperties.properties, { _ -> saveConfigProperties() })
    }
    single { AppConfig }
    single { AppProperties }
    single { AppLogger }
    single { I18nManager }
    single { ThemeManager }
    single { WindowManager() }
    single { NavigationManager() }
    single<IFileIO> { DesktopFileIO() }
    single<IGameDefaults> { WindowsGameDefaults() }
    single<Json> {
        Json {
            allowComments = true
            allowTrailingComma = true
            ignoreUnknownKeys = true
        }
    }
    single { SnackbarHostState() }
}
