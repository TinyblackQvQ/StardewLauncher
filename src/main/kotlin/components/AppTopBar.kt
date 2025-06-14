package components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import org.koin.compose.koinInject
import services.config.AppConfig
import services.navigation.NavigationDestinations
import services.navigation.NavigationManager
import services.resources.color.ThemeManager
import services.resources.color.ThemeMode
import services.resources.i18n.I18nManager
import services.window.WindowManager


@Composable
@Preview
fun AppTopBar(windowState: WindowState, exitApp: () -> Unit, navigationManager: NavigationManager = koinInject()) {
    val strings = I18nManager.currentStrings.collectAsState().value
    val colors = ThemeManager.currentColorScheme.collectAsState().value
    val windowStateManager: WindowManager = koinInject<WindowManager>()
    val isWindowMaximized = windowStateManager.isWindowMaximized.collectAsState()
    val themeMode = ThemeManager.currentThemeMode.collectAsState().value

    MaterialTheme(colorScheme = colors) {
        Row(
            modifier = Modifier
                .fillMaxWidth(1f)
                .height(50.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                // 拖拽手势
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { windowStateManager.onTopAppBarDragStart() },
                        onDragEnd = { windowStateManager.onTopAppBarDragStop() },
                        onDrag = { change, dragAmount -> windowStateManager.onTopAppBarDragging() }
                    )
                }
                // 双击手势
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            // 切换全屏/最大化状态
                            if (isWindowMaximized.value) {
                                windowStateManager.setPlacement(WindowPlacement.Floating)
                            } else {
                                windowStateManager.setPlacement(WindowPlacement.Maximized)
                            }
                        }
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 标题
            Text(
                text = strings.general.title,
                modifier = Modifier.padding(start = 12.dp),
                color =
                    if (themeMode == ThemeMode.DARK) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleSmall
            )
            // 菜单栏
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                AppMenu(icon = Icons.Default.PlayArrow, content = strings.general.menu.launch) {
                    navigationManager.navigateTo(
                        NavigationDestinations.Launch
                    )
                }
                AppMenu(icon = Icons.Default.Inbox, content = strings.general.menu.packageManager) {
                    navigationManager.navigateTo(
                        NavigationDestinations.PackageManage
                    )
                }
                AppMenu(icon = Icons.Default.Download, content = strings.general.menu.download) {
                    navigationManager.navigateTo(
                        NavigationDestinations.Download
                    )
                }
                AppMenu(icon = Icons.Default.Settings, content = strings.general.menu.settings) {
                    navigationManager.navigateTo(
                        NavigationDestinations.Settings
                    )
                }
            }
            // Window Controller
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight(1f)
            ) {
                AppWindowControllerButton(
                    icon = Icons.Default.Minimize,
                    desc = "Minimize App",
                    onClick = {
                        windowStateManager.setMinimized()
                    }
                )
                if (!isWindowMaximized.value)
                    AppWindowControllerButton(
                        icon = Icons.Default.Fullscreen,
                        desc = "Switch Fullscreen",
                        onClick = { windowStateManager.setPlacement(WindowPlacement.Maximized) }
                    )
                else
                    AppWindowControllerButton(
                        icon = Icons.Default.FullscreenExit,
                        desc = "Switch Fullscreen",
                        onClick = { windowStateManager.setPlacement(WindowPlacement.Floating) }
                    )
                AppWindowControllerButton(icon = Icons.Default.Close, desc = "Close App", onClick = exitApp)
            }
        }
    }
}

@Composable
fun AppMenu(icon: ImageVector, content: String, onClick: () -> Unit = {}) {
    val backgroundColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.onTertiary
    Row(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .fillMaxHeight(.6f)
            .clip(shape = RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable { onClick.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = icon,
            contentDescription = content,
            tint = surfaceColor,
            modifier = Modifier.size(20.dp).padding(end = 4.dp)
        )
        Text(
            content,
            color = surfaceColor,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun AppWindowControllerButton(icon: ImageVector, desc: String, onClick: () -> Unit = {}) {
    val themeMode = ThemeManager.currentThemeMode.collectAsState().value
    Box(modifier = Modifier.fillMaxHeight(1f).aspectRatio(1f)) {
        Box(
            modifier = Modifier
                .fillMaxHeight(.7f)
                .aspectRatio(1f)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(50))
                .clickable {
                    onClick.invoke()
                }) {
            Icon(
                modifier = Modifier
                    .fillMaxHeight(.7f)
                    .align(Alignment.Center),
                imageVector = icon,
                contentDescription = desc,
                tint =
                    if (themeMode == ThemeMode.DARK) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}