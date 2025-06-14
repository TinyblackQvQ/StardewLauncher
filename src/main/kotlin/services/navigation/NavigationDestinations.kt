package services.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import pages.download.DownloadPage
import pages.introduce.IntroducePage
import pages.launch.LaunchPage
import pages.modpack.PackageManagePage
import pages.settings.SettingsPage

/**
 * 导航目标定义
 */
object NavigationDestinations {
    // 顶层页面
    val Launch = NavigationTarget(
        id = "launch",
        type = NavigationTargetType.PAGE,
        content = { LaunchPage() }
    )

    val PackageManage = NavigationTarget(
        id = "package_manage",
        type = NavigationTargetType.PAGE,
        content = { PackageManagePage() }
    )
    val Download = NavigationTarget(
        id = "download",
        type = NavigationTargetType.PAGE,
        content = { DownloadPage() }
    )

    val Settings = NavigationTarget(
        id = "settings",
        type = NavigationTargetType.PAGE,
        content = { SettingsPage() }
    )

    val Introduce = NavigationTarget(
        id = "introduce",
        type = NavigationTargetType.PAGE,
        content = { IntroducePage() }
    )

    // 默认标签页设置
    val defaultTabsSettings = listOf(
        TabsSettings(
            direction = TabsDirection.HORIZONTAL,
            options = listOf(Launch, PackageManage, Download, Settings)
        )
    )

    // 弹出窗口示例
    fun createPopup(
        id: String,
        title: String,
        width: Int = 400,
        height: Int = 300,
        content: @Composable () -> Unit
    ) = NavigationTarget(
        id = id,
        type = NavigationTargetType.POPUP,
        width = width.dp,
        height = height.dp,
        content = content
    )

    // 抽屉示例
    fun createDrawer(
        id: String,
        title: String,
        width: Int = 300,
        height: Int = 0,
        content: @Composable () -> Unit
    ) = NavigationTarget(
        id = id,
        type = NavigationTargetType.DRAWER,
        width = width.dp,
        height = height.dp,
        content = content
    )
}