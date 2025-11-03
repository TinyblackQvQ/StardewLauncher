package components.overlay

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

data class OverlayAnimationConfig(
    val enter: EnterTransition = fadeIn() + scaleIn(),
    val exit: ExitTransition = fadeOut() + scaleOut()
)