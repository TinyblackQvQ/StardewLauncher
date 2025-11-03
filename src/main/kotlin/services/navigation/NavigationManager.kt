package services.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 导航方向
 */
enum class NavigationDirection {
    FORWARD,   // 前进（从左到右）
    BACKWARD,  // 后退（从右到左）
    NONE       // 无动画（用于同级切换）
}

// 动画配置
data class AnimationConfig(
    val enterAnimation: EnterTransition,
    val exitAnimation: ExitTransition,
    val animationDuration: Int = 300
)

/**
 * 导航目标
 *
 * @param id 唯一标识符
 * @param content 可组合内容
 */
@Stable
data class NavigationPage(
    val id: String,
    var content: @Composable () -> Unit,
    val animationConfig: AnimationConfig? = null,
    val onKeyEscPressed: (() -> Unit)? = null
)

/**
 * 导航管理器
 */
class NavigationManager {
    // 导航栈
    private val navigationStack = SnapshotStateList<NavigationPage>()

    // 导航方向
    private val _navigationDirection = MutableStateFlow(NavigationDirection.NONE)
    val navigationDirection: StateFlow<NavigationDirection> = _navigationDirection

    val currentPage by derivedStateOf {
        navigationStack.lastOrNull()
    }

    /** 获取堆栈大小 */
    fun getStackSize(): Int = navigationStack.size

    fun navigateTo(
        target: NavigationPage,
        direction: NavigationDirection = NavigationDirection.FORWARD,
        isDuplicatedPageAllowed: Boolean = false
    ) {
        if (currentPage != null && currentPage!!.id == target.id && !isDuplicatedPageAllowed)
            return
        navigationStack.add(target)
        _navigationDirection.value = direction
    }

    /**
     * 尝试向前找到指定页面，如果未找到，则正常导航至目标页面
     * @param target 目标页面
     * */
    fun seekBackTo(target: NavigationPage) {
        if (currentPage?.id == target.id) return
        val targetIndex = navigationStack.indexOfFirst { it.id == target.id }
        if (targetIndex == -1) navigateTo(target, NavigationDirection.FORWARD)
        else {
            _navigationDirection.value = NavigationDirection.BACKWARD
            navigationStack.subList(targetIndex + 1, navigationStack.size).clear()
        }
    }

    fun replaceTo(target: NavigationPage) {
        navigationStack.removeLast()
        navigateTo(target, NavigationDirection.FORWARD)
    }

    /** 弹出当前页面 */
    fun popBack(): Boolean {
        if (currentPage == null || navigationStack.size <= 1) return false
        _navigationDirection.value = NavigationDirection.BACKWARD
        navigationStack.removeLast()
        return true
    }
} 