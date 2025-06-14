package services.window

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPlacement.*
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import services.logger.AppLogger
import services.logger.LogModule
import java.awt.*
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener

enum class WindowInitialPosition {
    Center, Unspecified
}

data class ScreenAvailableArea(
    val size: IntSize,
    val startLocation: IntOffset
)

data class WindowPlacementState(
    val screenAvailableArea: ScreenAvailableArea?,
    var size: DpSize,
    var position: WindowPosition
) {
    fun updateFromWindow(windowManager: WindowManager) {
        position = windowManager.windowState.value.position
        size = windowManager.windowState.value.size
    }

    fun applyToWindow(windowManager: WindowManager) {
        windowManager._setPosition(position)
        windowManager._setSize(size)
    }
}

/**
 * TODO: Known ISSUE: [0] 存在多显示屏时，窗口在屏幕边缘部分移动时，会出现错位情况
 * TODO:              [1] 存在多显示屏时，在非主屏幕使用挪动顶栏退出最大化会导致恢复位置错误
 * TODO:              [2] 存在多显示屏时，
 * TODO: FEATURE wanted: [0] 不兼容系统原生的窗口管理方法，目前窗口行为仅为程序模拟，需要获得原生支持
 * TODO:                 [1] 如果无法获取原生支持，可以加入窗口最大化动画
 * */
class WindowManager {
    private var lastFloatingWindowPlacement: WindowPlacementState = WindowPlacementState(
        getScreenAvailableArea(),
        DpSize(800.dp, 500.dp),
        position = WindowPosition(0.dp, 0.dp)
    )
    private var _isWindowDragging = MutableStateFlow(false)
    val isWindowDragging = _isWindowDragging.asStateFlow()

    private val _windowState = MutableStateFlow(WindowState())
    val windowState: StateFlow<WindowState> = _windowState.asStateFlow()

    private val _isWindowMaximized = MutableStateFlow(false)
    val isWindowMaximized: StateFlow<Boolean> = _isWindowMaximized.asStateFlow()

    // 存储对底层 AWT 窗口的引用，以便直接操作其位置和大小
    private var composeWindow: ComposeWindow? = null
    private var density: Density? = null
    private var windowInitialPosition = WindowInitialPosition.Unspecified
    private var windowInitialSize = DpSize(800.dp, 600.dp)

    private var dragPointerStartLocation: Point? = null
    private var dragWindowStartLocation: Point? = null

    /**
     * 初始化窗口管理器，传入 Compose 窗口实例。
     * 必须在窗口创建后调用。
     */
    fun setComposeWindow(window: ComposeWindow) {
        this.composeWindow = window

        abstract class WindowEventListener : ComponentListener {
            abstract override fun componentResized(e: ComponentEvent?)
            abstract override fun componentMoved(e: ComponentEvent?)
            abstract override fun componentShown(e: ComponentEvent?)
            abstract override fun componentHidden(e: ComponentEvent?)
        }

        window.addComponentListener(object : WindowEventListener() {
            override fun componentResized(e: ComponentEvent?) {
                onWindowSizeChange()
            }
            override fun componentMoved(e: ComponentEvent?) {}
            override fun componentShown(e: ComponentEvent?) {}
            override fun componentHidden(e: ComponentEvent?) {}
        })

        // 应用初始化设置
        /** (Fixed) ISSUE: 必须使用底层 [sun.awt] 进行设置，直接针对 [_windowState] 的修改是无效的 */
        when (windowInitialPosition) {
            WindowInitialPosition.Center -> {
                getScreenAvailableArea()?.let { area ->
                    val centerX =
                        (area.size.width - _windowState.value.size.width.value.toInt()) / 2 + area.startLocation.x
                    val centerY =
                        (area.size.height - _windowState.value.size.height.value.toInt()) / 2 + area.startLocation.y
//                    setPosition(WindowPosition.Absolute(centerX.dp, centerY.dp))
                    composeWindow?.setLocation(centerX, centerY)
                }
            }

            WindowInitialPosition.Unspecified -> {}
        }

//        setSize(windowInitialSize)
        composeWindow?.setSize(windowInitialSize.width.value.toInt(), windowInitialSize.height.value.toInt())
    }

    fun setDensity(density: Density) {
        this.density = density
    }

    fun setInitialPosition(windowInitialPosition: WindowInitialPosition) {
        this.windowInitialPosition = windowInitialPosition
    }

    fun setInitialSize(width: Dp, height: Dp) {
        this.windowInitialSize = DpSize(width, height)
//        _setSize(this.windowInitialSize)
    }

    /**
     * 给 `LaunchEffect(windowState.size)` 准备的回调函数，请**务必**注册该函数
     * */
    fun onWindowSizeChange() {
        /**
         * 当检测到尺寸变化时：
         * 认定为是用户自己拖动，将尺寸变化结果存储至lastFloatingWindowPlacement中
         * */
        if (!_isWindowMaximized.value) {
            lastFloatingWindowPlacement.updateFromWindow(this)
            _isWindowMaximized.value = false
        }
    }

    /**
     * 用于给 `WindowDraggableArea` 下的组件的 `draggable.onDragStarted` 准备的回调函数
     * 如果有使用 `WindowDraggableArea`，请**务必**注册该函数
     * */
    fun onTopAppBarDragStart() {
        _isWindowDragging.value = true
        dragPointerStartLocation = getMouseScreenPosition().location
        dragWindowStartLocation = composeWindow?.location

        // 如果当前是最大化状态且正在拖拽，执行脱离最大化的逻辑
        if (_isWindowMaximized.value) {
            composeWindow?.let { window ->
                _isWindowMaximized.value = false
                getScreenAvailableArea()?.let { area ->
                    val mouseLocation = getMouseScreenPosition()
                    val lastWindowSizeX = lastFloatingWindowPlacement.size.width.value
                    val xShrinkRadio = lastWindowSizeX / area.size.width.toFloat()
                    val windowInnerX = mouseLocation.x * xShrinkRadio
                    val windowX = mouseLocation.x - windowInnerX
                    val newPosition = Point(windowX.toInt(), window.y)
                    _setSize(lastFloatingWindowPlacement.size)
                    dragWindowStartLocation = newPosition
                }
            }
        }
    }

    operator fun Point.plus(value: Point): Point {
        return Point(x + value.x, y + value.y)
    }

    operator fun Point.minus(value: Point): Point {
        return Point(x - value.x, y - value.y)
    }

    fun Point.toWindowPosition(): WindowPosition {
        return WindowPosition.Absolute(x.dp, y.dp)
    }

    /**
     * 用于给 `WindowDraggableArea` 下的组件的 `draggable.state { delta -> ... }` 准备的回调函数
     * 如果有使用 `WindowDraggableArea`，请**务必**注册该函数
     * */
    fun onTopAppBarDragging() {
        dragPointerStartLocation?.let { pointerStartLocation ->
            val currentPointerLocation = getMouseScreenPosition()
            val dragOffset = pointerStartLocation - currentPointerLocation
            dragWindowStartLocation?.let { setPosition((it - dragOffset).toWindowPosition()) }
        }
    }

    /**
     * 用于给 `WindowDraggableArea` 下的组件的 `draggable.onDragStoped` 准备的回调函数
     * 如果有使用 `WindowDraggableArea`，请**务必**注册该函数
     * */
    fun onTopAppBarDragStop() {
        _isWindowDragging.value = false
        composeWindow?.let { window ->
            val availableArea = getScreenAvailableArea()
            val pointerPosition = getMouseScreenPosition()
            availableArea?.let {
                if (pointerPosition.y <= 2) {
                    // 认定当前鼠标想要触发顶部窗口边缘吸附
                    applyFloatingPlacementFullScreen()
                    return
                }
                if (window.location.y < availableArea.startLocation.y) {
                    // 将窗口拉回屏幕内，保证 TopAppBar 在可见范围内
                    setPosition(x = window.x.dp, y = 0.dp)
                }
            }
        }
    }

    /** Fixed ISSUE: 当使用多屏幕时，将鼠标移至最右侧会导致 MouseInfo.getPointerInfo() 返回 null, 进而导致程序崩溃 */
    private lateinit var lastMouseLocation: Point
    /**
     * 获取当前鼠标在屏幕的位置
     * @return 鼠标位置 (Pixel)
     * */
    fun getMouseScreenPosition(): Point {
        try {
            val pointerInfo: PointerInfo = MouseInfo.getPointerInfo()
            lastMouseLocation = pointerInfo.location
            return pointerInfo.location
        } catch (e: NullPointerException) {

            AppLogger.error(
                LogModule.WindowManager,
                "When try to get mouse position, MouseInfo.getPointerInfo() returned null," +
                        " the error have been caught and returned last mouse location."
            )
        }
        return lastMouseLocation
    }

    /**
     * 获取当前主屏幕的可用尺寸和起始点
     * @return 可用尺寸：ScreenAvailableArea.size；起始点：ScreenAvailableArea.startLocation
     * */
    private fun getScreenAvailableArea(): ScreenAvailableArea? {
        composeWindow?.let { window ->
            val config = window.graphicsConfiguration
            val screenBounds = config.bounds
            val insets = Toolkit.getDefaultToolkit().getScreenInsets(config)

            val x = screenBounds.x + insets.left
            val y = screenBounds.y + insets.top
            val width = screenBounds.width - insets.left - insets.right
            val height = screenBounds.height - insets.top - insets.bottom

            return ScreenAvailableArea(
                size = IntSize(width, height),
                startLocation = IntOffset(x, y)
            )
        }
        return null
    }

    /**
     * 在 `undecorated == true` 的状态下应用伪全屏效果
     * */
    private fun applyFloatingPlacementFullScreen() {
        getScreenAvailableArea()?.let { area ->
            composeWindow?.let { window ->
                lastFloatingWindowPlacement.updateFromWindow(this)
                _setSize(area.size)
                _setPosition(area.startLocation)
            }
            _isWindowMaximized.value = true
        }
    }


    fun _setSize(width: Dp, height: Dp) {
        _windowState.value.size = DpSize(width, height)
    }

    fun _setSize(width: Int, height: Int) {
        _windowState.value.size = DpSize(width.dp, height.dp)
    }

    fun _setSize(size: DpSize) {
        _windowState.value.size = size
    }

    fun _setSize(size: IntSize) = _setSize(size.width, size.height)

    fun _setPosition(x: Int, y: Int) {
//        composeWindow?.setLocation(x, y)
        _windowState.value.position = WindowPosition.Absolute(x.dp, y.dp)
    }

    fun _setPosition(x: Dp, y: Dp) {
//        composeWindow?.setLocation(x.value.toInt(), y.value.toInt())
        _windowState.value.position = WindowPosition.Absolute(x, y)
    }

    fun _setPosition(position: IntOffset) = _setPosition(position.x, position.y)
    fun _setPosition(position: WindowPosition) {
        when (position) {
            is WindowPosition.Absolute -> _setPosition(position.x, position.y)
            WindowPosition.PlatformDefault -> {
                // 使用操作系统默认位置
                composeWindow?.setLocationRelativeTo(null)
            }

            is WindowPosition.Aligned -> {
                _windowState.value.position = WindowPosition.Aligned(position.alignment)
            }
        }
    }

    /**
     * 更新窗口的大小，仅供外部使用。
     */
    fun setSize(width: Dp, height: Dp) {
        _setSize(width, height)
        composeWindow?.let { window ->
            lastFloatingWindowPlacement.updateFromWindow(this)
        }
    }

    fun setSize(width: Int, height: Int) {
        _setSize(width, height)
        composeWindow?.let { window ->
            lastFloatingWindowPlacement.updateFromWindow(this)
        }
    }

    fun setSize(size: DpSize) {
        _setSize(size)
        composeWindow?.let { window ->
            lastFloatingWindowPlacement.updateFromWindow(this)
        }
    }

    /**
     * 更新窗口的位置，仅供外部使用。
     */
    fun setPosition(x: Dp, y: Dp) {
        _setPosition(x, y)
        composeWindow?.let { window ->
            lastFloatingWindowPlacement.updateFromWindow(this)
        }
    }

    fun setPosition(x: Int, y: Int) {
        _setPosition(x, y)
        composeWindow?.let { window ->
            lastFloatingWindowPlacement.updateFromWindow(this)
        }
    }

    fun setPosition(position: WindowPosition) {
        _setPosition(position)
        composeWindow?.let { window ->
            lastFloatingWindowPlacement.updateFromWindow(this)
        }
    }

    /**
     * 设置窗口为最小化状态。
     */
    fun setMinimized() {
        composeWindow?.let { window ->
            lastFloatingWindowPlacement.updateFromWindow(this)
        }
        _windowState.value.isMinimized = true
    }

    /**
     * 设置窗口的放置模式（如最大化、浮动、全屏）。
     */
    fun setPlacement(placement: WindowPlacement) {
        when (placement) {
            Floating -> {
                // 在 `undecorated == true` 的状态，需要从原保存尺寸中恢复尺寸大小
                _isWindowMaximized.value = false
                composeWindow?.let { window ->
                    lastFloatingWindowPlacement.applyToWindow(this)
                }
            }

            Maximized -> {
                // 在 `undecorated == true` 的状态，需要应用伪全屏效果
                applyFloatingPlacementFullScreen()
            }

            Fullscreen -> {
                // 暂时没有用到 FullScreen，不考虑
                composeWindow?.let { window ->
                    lastFloatingWindowPlacement.applyToWindow(this)
                }
                _windowState.value.placement = placement
            }
        }
    }
}