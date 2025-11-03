package pages.general

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pages.common.TabsContainer
import services.navigation.NavigationDestinations

data object MainTabsController {
    val tabs = listOf(
        NavigationDestinations.Launch,
        NavigationDestinations.PackageManage,
        NavigationDestinations.Download,
        NavigationDestinations.Settings
    )
    var state: PagerState? = null
}

@Composable
fun MainTabsPage(controller: MainTabsController) {
    controller.state = controller.state ?: rememberPagerState(
        initialPage = 0,
        pageCount = { controller.tabs.size }
    )
    TabsContainer(
        modifier = Modifier.fillMaxSize(),
        pagerState = controller.state!!,
        tabs = controller.tabs
    )
}