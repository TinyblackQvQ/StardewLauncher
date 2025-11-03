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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowCornerPreference
import com.mayakapps.compose.windowstyler.WindowFrameStyle
import com.mayakapps.compose.windowstyler.WindowStyle
import components.AppTopBar
import koin.appModule
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import services.config.AppConfig
import services.config.AppProperties
import services.logger.AppLogger
import services.logger.LogModule
import services.navigation.NavigationDestinations
import services.navigation.NavigationHost
import services.navigation.NavigationManager
import services.resources.color.ThemeManager
import services.resources.color.ThemeMode
import services.resources.i18n.I18nManager
import services.window.WindowInitialPosition
import services.window.WindowManager
import java.io.FileNotFoundException

/**
 * ### Known Issue
 * - TODO: WindowStyle.backdropType 即使被设定，程序启动后仍然存在概率无法被应用，必须最小化，再恢复窗口后才可以应用属性。
 * */

fun main() {
    startKoin {
        modules(appModule)
    }
    application {
        val strings = I18nManager.currentStrings.collectAsState().value
        val colors = ThemeManager.currentColorScheme.collectAsState().value
        val windowStateManager: WindowManager = koinInject<WindowManager>()
        val navigationManager: NavigationManager = koinInject<NavigationManager>()
        val mainWindowState by windowStateManager.windowState.collectAsState()
        val localDensity = LocalDensity.current
        val snackbarHostState = koinInject<SnackbarHostState>()
        /** 设置 WindowStateManager 初始化状态 */
        windowStateManager.setInitialSize(800.dp, 500.dp)
        windowStateManager.setInitialPosition(WindowInitialPosition.Center)

        LaunchedEffect(Unit) {
            /** 从配置文件读取配置 */
            try {
                AppProperties.loadInitialConfigProperties()
                AppConfig.updateAll()
            } catch (_: FileNotFoundException) {
                AppLogger.warn(
                    LogModule.ConfigProperty,
                    "Cannot found property file: ${AppProperties.DEFAULT_CONFIG_FILE_PATH}, will create it with default values"
                )
                AppConfig.resetAll()
            } catch (e: Exception) {
                AppLogger.error(LogModule.Config, "Failed to load config from local file, err: $e")
            }
            // 设置初始导航目标
            navigationManager.navigateTo(NavigationDestinations.MainTabs)
        }

        MaterialTheme(colorScheme = colors) {
            Window(
                onCloseRequest = ::exitApplication,
                title = strings.general.title,
                state = mainWindowState,
                undecorated = true,
                transparent = true
            ) {
                /** 注册 WindowStateManager */
                LaunchedEffect(Unit) {
                    windowStateManager.setComposeWindow(window)
                    windowStateManager.setDensity(localDensity)
                }
                WindowStyle(
                    isDarkTheme = ThemeManager.currentThemeMode.collectAsState().value == ThemeMode.DARK,
                    backdropType = WindowBackdrop.Acrylic(MaterialTheme.colorScheme.primary.copy(alpha = 0f)),
                    frameStyle = WindowFrameStyle(cornerPreference = WindowCornerPreference.ROUNDED)
                )
                Column(modifier = Modifier.fillMaxSize()) {
                    AppTopBar(
                        windowState = mainWindowState,
                        exitApp = ::exitApplication
                    )
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(snackbarHostState)
                        },
                    ) { paddingValues ->
                        NavigationHost(
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceBright)
                                .padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}
