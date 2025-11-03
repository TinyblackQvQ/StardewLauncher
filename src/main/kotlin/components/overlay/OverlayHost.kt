package components.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

/**
 * 可组合函数，用于显示覆盖层内容
 *
 * 该组件会在屏幕中央显示一个覆盖层，带有半透明背景，并在其中心显示由控制器提供的内容。
 * 当控制器没有提供内容时，此组件不会渲染任何内容。
 *
 * @param controller 控制覆盖层显示内容的控制器
 */
@Composable
fun OverlayHost(
    controller: OverlayController
) {
    val request = controller.currentJob ?: return

    LaunchedEffect(request.visibilityState.currentState, request.visibilityState.targetState) {
        // 当动画完全关闭后
        if (!request.visibilityState.currentState && !request.visibilityState.targetState) {
            controller.finalizeJob()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize().zIndex(99f),
            visibleState = request.visibilityState,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(request.backgroundColor).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = request.isDismissible
                ) {
                    controller.dismiss()
                }
            )
        }
        // 内容
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize().zIndex(100f),
            visibleState = request.visibilityState,
            enter = request.animation.enter,
            exit = request.animation.exit
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                when (request) {
                    is OverlayRequestJob<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val requestJob = request as OverlayRequestJob<Any?>
                        requestJob.content { result ->
                            requestJob.complete(result)
                        }
                    }

                    is OverlayShowJob -> {
                        request.content {
                            // show 的内容自己调用 dismiss 来触发关闭
                            controller.dismiss()
                        }
                    }
                }
            }
        }
    }
}