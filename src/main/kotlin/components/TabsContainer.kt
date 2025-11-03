package pages.common

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import services.navigation.NavigationPage

@Composable
fun TabsContainer(
    modifier: Modifier,
    pagerState: PagerState,
    tabs: List<NavigationPage>
) {
    HorizontalPager(modifier = modifier, state = pagerState) {
        tabs[it].content()
    }
}