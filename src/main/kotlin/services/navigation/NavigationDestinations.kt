package services.navigation

import models.mod.observable.ObservableModPack
import pages.download.DownloadPage
import pages.general.MainTabsController
import pages.general.MainTabsPage
import pages.introduce.IntroducePage
import pages.launch.LaunchPage
import pages.modpack.PackageManagePage
import pages.modpack.detail.ModPackDetailPage
import pages.settings.SettingsPage

/**
 * 导航目标定义
 */
object NavigationDestinations {
    val MainTabs = NavigationPage(
        id = "main_tabs",
        content = { MainTabsPage(MainTabsController) }
    )
    // 顶层页面
    val Launch = NavigationPage(
        id = "launch",
        content = { LaunchPage() }
    )

    val PackageManage = NavigationPage(
        id = "package_manage",
        content = { PackageManagePage() }
    )

    fun ModPackDetailedInfo(modPack: ObservableModPack): NavigationPage {
        return NavigationPage(
            id = "modpack_detailed_info",
            content = { ModPackDetailPage(modPack) }
        )
    }

    val Download = NavigationPage(
        id = "download",
        content = { DownloadPage() }
    )

    val Settings = NavigationPage(
        id = "settings",
        content = { SettingsPage() }
    )

    val Introduce = NavigationPage(
        id = "introduce",
        content = { IntroducePage() }
    )
}