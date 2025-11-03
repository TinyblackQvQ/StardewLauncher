package components.overlay

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CompletableDeferred

sealed interface IOverlayJob {
    val id: Long
    val isDismissible: Boolean
    val animation: OverlayAnimationConfig
    val visibilityState: MutableTransitionState<Boolean>
    val containerModifier: Modifier
    val backgroundColor: Color
}

internal class OverlayRequestJob<T>(
    override val id: Long = System.currentTimeMillis(),
    override val isDismissible: Boolean,
    override val animation: OverlayAnimationConfig,
    override val containerModifier: Modifier,
    override val backgroundColor: Color,
    val content: @Composable (onResult: (T) -> Unit) -> Unit,
    private val deferred: CompletableDeferred<T?>
): IOverlayJob {
    override val visibilityState = MutableTransitionState(false)
    fun complete(result: T?) {
        if (deferred.isActive) deferred.complete(result)
    }
}

internal class OverlayShowJob(
    override val id: Long = System.currentTimeMillis(),
    override val isDismissible: Boolean,
    override val animation: OverlayAnimationConfig,
    override val containerModifier: Modifier,
    override val backgroundColor: Color,
    val content: @Composable (dismiss: () -> Unit) -> Unit,
): IOverlayJob {
    override val visibilityState = MutableTransitionState(false)
}