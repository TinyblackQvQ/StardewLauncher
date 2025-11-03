package services.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import org.koin.compose.koinInject

@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
    controller: NavigationManager = koinInject()
) {
    val currentPage = controller.currentPage
    val currentNavigationDirection by controller.navigationDirection.collectAsState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(controller.currentPage) {
        focusRequester.requestFocus()
    }
    Box(
        modifier = modifier
            // 键盘返回键处理（适用于JVM）
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) controller.popBack()
                true
            }
    ) {
        if (currentPage == null)
            return
        val page = currentPage
        // Page Content
        AnimatedContent(
            targetState = page,
            transitionSpec =
                {
                    val animationConfig = DefaultAnimationProvider.getPageAnimation(currentNavigationDirection)
                    animationConfig.enterAnimation.togetherWith(animationConfig.exitAnimation)
                }
        ) { page ->
            page.content()
        }
    }
}