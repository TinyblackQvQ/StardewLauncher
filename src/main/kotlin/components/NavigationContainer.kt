package components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import services.navigation.*
import org.koin.compose.koinInject

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationContainer(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = koinInject()
) {
    val currentTarget by navigationManager.currentTarget.collectAsState()
    val navigationDirection by navigationManager.navigationDirection.collectAsState()
    val isDrawerVisible by navigationManager.isDrawerVisible.collectAsState()
    val currentDrawerTarget by navigationManager.currentDrawerTarget.collectAsState()

    Box(modifier = modifier) {
        // 主页面内容
        currentTarget?.let { target ->
            when (target.type) {
                NavigationTargetType.PAGE -> {
                    // 使用动画过渡显示页面
                    AnimatedContent(
                        targetState = target,
                        transitionSpec = {
                            when (navigationDirection) {
                                NavigationDirection.FORWARD -> {
                                    // 前进动画：从右向左滑入
                                    slideInHorizontally(
                                        initialOffsetX = { fullWidth -> fullWidth },
                                        animationSpec = tween(300)
                                    ).togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> -fullWidth },
                                            animationSpec = tween(300)
                                        )
                                    )
                                }

                                NavigationDirection.BACKWARD -> {
                                    // 后退动画：从左向右滑入
                                    slideInHorizontally(
                                        initialOffsetX = { fullWidth -> -fullWidth },
                                        animationSpec = tween(300)
                                    ).togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth },
                                            animationSpec = tween(300)
                                        )
                                    )
                                }

                                NavigationDirection.NONE -> {
                                    // 无动画：直接切换
                                    fadeIn(animationSpec = tween(300)).togetherWith(fadeOut(animationSpec = tween(300)))
                                }
                            }
                        }
                    ) { currentTarget ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            currentTarget.content()
                        }
                    }
                }

                NavigationTargetType.POPUP -> {
                    // 显示弹出窗口
                    Dialog(
                        onDismissRequest = { navigationManager.closePopup() },
                        properties = DialogProperties(
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true,
                            usePlatformDefaultWidth = false
                        )
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(target.width)
                                .height(target.height),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            target.content()
                        }
                    }
                }

                NavigationTargetType.DRAWER -> {
                    // 抽屉由 DrawerContainer 处理
                }
            }
        }

        // 抽屉内容
        if (isDrawerVisible && currentDrawerTarget != null) {
            DrawerContainer(
                target = currentDrawerTarget!!,
                onDismiss = { navigationManager.closeDrawer() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContainer(
    target: NavigationTarget,
    onDismiss: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed) {
            onDismiss()
        }
    }

    when (target.drawerEdge) {
        DrawerEdge.START -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(target.width)
                    ) {
                        target.content()
                    }
                }
            ) {
                // 空内容，因为主内容已经在 NavigationContainer 中显示
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        DrawerEdge.END -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(target.width)
                    ) {
                        target.content()
                    }
                },
                gesturesEnabled = true
            ) {
                // 空内容，因为主内容已经在 NavigationContainer 中显示
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        DrawerEdge.TOP -> {
            // 顶部抽屉
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                    confirmValueChange = { true }
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(target.height)
                ) {
                    target.content()
                }
            }
        }

        DrawerEdge.BOTTOM -> {
            // 底部抽屉
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                    confirmValueChange = { true }
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(target.height)
                ) {
                    target.content()
                }
            }
        }
    }
} 