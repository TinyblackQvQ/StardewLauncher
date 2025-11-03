package components.overlay

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Stable
class OverlayController {
    internal var currentJob by mutableStateOf<IOverlayJob?>(null)
        private set

    internal fun finalizeJob() {
        currentJob = null
    }

    /**
     * 显示一个覆盖层并挂起协程，直到它通过 onResult 回调返回一个结果。
     *
     * 此方法会创建一个带有动画效果的覆盖层，用户与之交互后可以通过 onResult 回调返回结果，
     * 然后协程会恢复执行并返回该结果。
     *
     * @param content 要显示在覆盖层中的可组合内容，它接收一个 onResult 回调函数作为参数
     * @param isDismissible 标识覆盖层是否可以通过点击背景或调用 dismiss() 方法关闭
     * @param animation 覆盖层的进入和退出动画配置
     * @param containerModifier 应用于覆盖层容器的修饰符
     * @param backgroundColor 覆盖层背景颜色，默认为半透明黑色
     * @return T? 用户通过 onResult 回调返回的结果，如果覆盖层被取消则返回 null
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun <T> request(
        isDismissible: Boolean = true,
        animation: OverlayAnimationConfig = OverlayAnimationConfig(),
        containerModifier: Modifier = Modifier,
        backgroundColor: Color = Color.Black.copy(alpha = 0.4f),
        content: @Composable (onResult: (T) -> Unit) -> Unit,
    ): T? {
        if (currentJob != null) {
            throw IllegalStateException(
                "Cannot start a new request while another overlay job is active. " +
                        "Please ensure the UI state prevents concurrent requests."
            )
        }

        val deferred = CompletableDeferred<T?>()
        val request = OverlayRequestJob(
            content = content,
            isDismissible = isDismissible,
            animation = animation,
            containerModifier = containerModifier,
            backgroundColor = backgroundColor,
            deferred = deferred
        )
        currentJob = request
        // 将目标状态设为 true，触发进入动画
        request.visibilityState.targetState = true

        try {
            return deferred.await()
        } finally {}
    }

    /**
     * 显示一个覆盖层而不挂起。UI 内容会接收一个 dismiss 回调，
     * 它可以自己调用来关闭自己，或者由外部调用 dismiss() 来关闭。
     */
    fun show(
        isDismissible: Boolean = true,
        animation: OverlayAnimationConfig = OverlayAnimationConfig(),
        containerModifier: Modifier = Modifier,
        backgroundColor: Color = Color.Black.copy(alpha = 0.4f),
        content: @Composable (dismiss: () -> Unit) -> Unit,
    ) {
        if (currentJob != null) return
        val showJob = OverlayShowJob(
            content = content,
            isDismissible = isDismissible,
            animation = animation,
            containerModifier = containerModifier,
            backgroundColor = backgroundColor
        )
        currentJob = showJob
        // 将目标状态设为 true，触发进入动画
        showJob.visibilityState.targetState = true
    }

    fun dismiss() {
        currentJob?.let { job ->
            // 如果是 RequestJob，取消它并返回 null
            (job as? OverlayRequestJob<*>)?.complete(null)
            // 触发退出动画
            job.visibilityState.targetState = false
        }
    }
}