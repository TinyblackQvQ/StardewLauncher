package services.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 导航目标类型
 */
enum class NavigationTargetType {
    PAGE,      // 全屏页面
    POPUP,     // 弹出窗口
    DRAWER     // 抽屉式侧边栏
}

/**
 * 导航方向
 */
enum class NavigationDirection {
    FORWARD,   // 前进（从左到右）
    BACKWARD,  // 后退（从右到左）
    NONE       // 无动画（用于同级切换）
}

/**
 * 标签页方向
 */
enum class TabsDirection {
    HORIZONTAL,  // 水平方向
    VERTICAL     // 垂直方向
}

/**
 * 标签页设置
 */
@Stable
data class TabsSettings(
    val direction: TabsDirection,
    val options: List<NavigationTarget>
)

/**
 * 导航目标
 */
@Stable
data class NavigationTarget(
    val id: String,
    val type: NavigationTargetType,
    val content: @Composable () -> Unit,
    val width: Dp = 0.dp,
    val height: Dp = 0.dp,
    val drawerEdge: DrawerEdge = DrawerEdge.START
)

/**
 * 抽屉边缘位置
 */
enum class DrawerEdge {
    START, END, TOP, BOTTOM
}

/**
 * 导航管理器
 */
class NavigationManager {
    // 导航栈
    private val _navigationStack = mutableStateListOf<NavigationTarget>()
    val navigationStack: SnapshotStateList<NavigationTarget> = _navigationStack

    // 当前导航目标
    private val _currentTarget = MutableStateFlow<NavigationTarget?>(null)
    val currentTarget: StateFlow<NavigationTarget?> = _currentTarget

    // 导航方向
    private val _navigationDirection = MutableStateFlow(NavigationDirection.NONE)
    val navigationDirection: StateFlow<NavigationDirection> = _navigationDirection

    // 是否显示抽屉
    private val _isDrawerVisible = MutableStateFlow(false)
    val isDrawerVisible: StateFlow<Boolean> = _isDrawerVisible

    // 当前抽屉目标
    private val _currentDrawerTarget = MutableStateFlow<NavigationTarget?>(null)
    val currentDrawerTarget: StateFlow<NavigationTarget?> = _currentDrawerTarget

    // 标签页栈
    private val _tabsStacks = mutableStateListOf<TabsSettings>()
    val tabsStacks: SnapshotStateList<TabsSettings> = _tabsStacks

    /**
     * 设置标签页栈
     */
    fun setTabsStacks(tabs: List<TabsSettings>) {
        _tabsStacks.clear()
        _tabsStacks.addAll(tabs)
    }

    /**
     * 导航到新目标
     */
    fun navigateTo(target: NavigationTarget) {
        when (target.type) {
            NavigationTargetType.PAGE -> {
                // 查找目标页面所在的标签页层级
                val targetTabsIndex = _tabsStacks.indexOfFirst { tabs ->
                    target.id in tabs.options.map { it.id }
                }

                if (targetTabsIndex != -1) {
                    // 找到目标页面所在的标签页层级
                    val targetTabs = _tabsStacks[targetTabsIndex]
                    val currentTargetId = _currentTarget.value?.id
                    
                    // 查找当前页面所在的标签页层级
                    val currentTabsIndex = if (currentTargetId != null) {
                        _tabsStacks.indexOfFirst { tabs ->
                            currentTargetId in tabs.options.map { it.id }
                        }
                    } else -1

                    // 确定导航方向
                    _navigationDirection.value = when {
                        // 如果当前页面不在任何标签页中，使用前进动画
                        currentTabsIndex == -1 -> NavigationDirection.FORWARD
                        // 如果目标层级比当前层级更靠前，使用前进动画
                        targetTabsIndex < currentTabsIndex -> NavigationDirection.FORWARD
                        // 如果目标层级比当前层级更靠后，使用后退动画
                        targetTabsIndex > currentTabsIndex -> NavigationDirection.BACKWARD
                        // 如果在同一层级，根据标签页方向确定动画方向
                        else -> {
                            val currentIndex = targetTabs.options.indexOfFirst { it.id == currentTargetId }
                            val targetIndex = targetTabs.options.indexOfFirst { it.id == target.id }
                            when (targetTabs.direction) {
                                TabsDirection.HORIZONTAL -> {
                                    if (targetIndex > currentIndex) NavigationDirection.FORWARD
                                    else NavigationDirection.BACKWARD
                                }
                                TabsDirection.VERTICAL -> {
                                    if (targetIndex > currentIndex) NavigationDirection.FORWARD
                                    else NavigationDirection.BACKWARD
                                }
                            }
                        }
                    }

                    // 清空导航栈到目标层级
                    while (_navigationStack.size > targetTabsIndex) {
                        _navigationStack.removeLast()
                    }
                } else {
                    // 目标页面不在任何标签页中，使用前进动画
                    _navigationDirection.value = NavigationDirection.FORWARD
                }

                _navigationStack.add(target)
                _currentTarget.value = target
            }
            NavigationTargetType.POPUP -> {
                _currentTarget.value = target
            }
            NavigationTargetType.DRAWER -> {
                _currentDrawerTarget.value = target
                _isDrawerVisible.value = true
            }
        }
    }

    /**
     * 导航到次级页面
     */
    fun navigateToSubPage(target: NavigationTarget) {
        _navigationDirection.value = NavigationDirection.FORWARD
        _navigationStack.add(target)
        _currentTarget.value = target
    }

    /**
     * 返回上一页
     */
    fun navigateBack() {
        if (_navigationStack.size > 1) {
            _navigationDirection.value = NavigationDirection.BACKWARD
            _navigationStack.removeLast()
            _currentTarget.value = _navigationStack.last()
        }
    }

    /**
     * 关闭当前抽屉
     */
    fun closeDrawer() {
        _isDrawerVisible.value = false
        _currentDrawerTarget.value = null
    }

    /**
     * 关闭当前弹出窗口
     */
    fun closePopup() {
        if (_currentTarget.value?.type == NavigationTargetType.POPUP) {
            _currentTarget.value = null
        }
    }
} 