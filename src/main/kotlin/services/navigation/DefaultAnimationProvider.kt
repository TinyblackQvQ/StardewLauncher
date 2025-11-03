package services.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

// 默认动画提供器
object DefaultAnimationProvider {
    const val defaultAnimationTweenMillis = 300
    fun getPageAnimation(direction: NavigationDirection): AnimationConfig {
        return when (direction) {
            NavigationDirection.FORWARD -> AnimationConfig(
                enterAnimation = slideInHorizontally(
                    animationSpec = tween(defaultAnimationTweenMillis),
                    initialOffsetX = { it }
                ),
                exitAnimation = slideOutHorizontally(
                    animationSpec = tween(defaultAnimationTweenMillis),
                    targetOffsetX = { -it }
                )
            )
            NavigationDirection.BACKWARD -> AnimationConfig(
                enterAnimation = slideInHorizontally(
                    animationSpec = tween(defaultAnimationTweenMillis),
                    initialOffsetX = { -it }
                ),
                exitAnimation = slideOutHorizontally(
                    animationSpec = tween(defaultAnimationTweenMillis),
                    targetOffsetX = { it }
                )
            )
            NavigationDirection.NONE -> AnimationConfig(
                enterAnimation = fadeIn(tween(defaultAnimationTweenMillis)),
                exitAnimation = fadeOut(tween(defaultAnimationTweenMillis))
            )
        }
    }
}