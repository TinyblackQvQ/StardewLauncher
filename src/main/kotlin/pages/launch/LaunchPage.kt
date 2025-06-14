package pages.launch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import models.mod.observable.ObservableModPack
import models.mod.serializable.ModPack
import org.koin.compose.koinInject
import pages.AppPage
import services.config.AppConfig
import services.defaults.IGameDefaults
import services.navigation.NavigationDestinations
import services.navigation.NavigationManager
import services.resources.color.StaticColor
import views.ModResourceView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LaunchPage(gameDefaults: IGameDefaults = koinInject(), navigationManager: NavigationManager = koinInject()) {
    val gameVersion = gameDefaults.getCurrentGameVersion()
    val smapiVersion = gameDefaults.getCurrentSMAPIVersion()

    /** Bottom Launch Controller Animations */
    val isModPackSelectDrawerShow = remember { mutableStateOf(false) }
    val bottomBarOnClickAnimationDebouncer = remember { mutableStateOf(false) }
    LaunchedEffect(bottomBarOnClickAnimationDebouncer.value) {
        if (bottomBarOnClickAnimationDebouncer.value) {
            delay(150)
            bottomBarOnClickAnimationDebouncer.value = false
        }
    }
    LaunchedEffect(isModPackSelectDrawerShow.value) {
        bottomBarOnClickAnimationDebouncer.value = true
    }
    val bottomBarOffsetY = animateFloatAsState(if (bottomBarOnClickAnimationDebouncer.value) 8f else 0f)
    val modPackSelectDrawerHeight = animateFloatAsState(if (isModPackSelectDrawerShow.value) 300f else 40f)
    val bottomBarRoundedCornerSize = animateFloatAsState(if (isModPackSelectDrawerShow.value) 0f else 20f)

    /**
     * 注册和管理lastSelectedModPack
     * */
    val lastSelectedModPack: MutableState<ObservableModPack?> = remember {
        mutableStateOf(
            ModResourceView.getModPackInstanceByName(
                AppConfig.general.lastSelectedModPackName.read() ?: ""
            )
        )
    }
    LaunchedEffect(Unit) {
        AppConfig.general.lastSelectedModPackName.onValueUpdate = { modPackName ->
            lastSelectedModPack.value = modPackName?.let { ModResourceView.getModPackInstanceByName(it) }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            AppConfig.general.lastSelectedModPackName.onValueUpdate = {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(36.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize().align(Alignment.TopStart)) {
            AppPage()
        }

        // Game Launch Controller
        val bottomBarShape = RoundedCornerShape(20.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth().offset(0.dp, bottomBarOffsetY.value.dp)
                .height(modPackSelectDrawerHeight.value.dp)
                .clip(bottomBarShape)
                .shadow(16.dp, bottomBarShape)
                .align(Alignment.BottomCenter)
        ) {
            /** ModPack Select Drawer */
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Select ModPack",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Row(
                        modifier = Modifier.onClick { navigationManager.navigateTo(NavigationDestinations.PackageManage) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "View More",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "View More",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                LazyColumn {
                    items(ModResourceView.modPacks.size) { modPack ->
                        val modPack = ModResourceView.modPacks[modPack]
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    AppConfig.general.lastSelectedModPackName.write(modPack.name)
                                    isModPackSelectDrawerShow.value = false
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(modPack.name, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            /** Bottom Bar */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(
                        bottomBarShape.copy(
                            topStart = CornerSize(bottomBarRoundedCornerSize.value.dp),
                            topEnd = CornerSize(bottomBarRoundedCornerSize.value.dp)
                        )
                    )
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .align(Alignment.BottomCenter)
                    .clickable {
                        // 召起模组包选单抽屉
                        isModPackSelectDrawerShow.value = !isModPackSelectDrawerShow.value
                    }
            ) {
                // Game Info
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .zIndex(3f)
                        .clip(bottomBarShape.copy(topStart = CornerSize(bottomBarRoundedCornerSize.value.dp)))
                        .background(StaticColor.Apricot)
                ) {
                    Text(
                        "StadewValley ${gameVersion?.toReadableString() ?: "Unknown"}",
                        modifier = Modifier.padding(horizontal = 16.dp).align(Alignment.Center),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                // Api Info
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .offset(x = (-32).dp)
                        .zIndex(2f)
                        .clip(bottomBarShape)
                        .background(StaticColor.LemonYellow)
                ) {
                    Text(
                        "SMAPI ${smapiVersion?.toReadableString() ?: "Unknown"}",
                        modifier = Modifier.padding(start = 44.dp, end = 16.dp).align(Alignment.Center),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                // ModPack Info
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .offset(x = (-24).dp)
                        .weight(1f)
                        .zIndex(1f)
                ) {
                    Text(
                        "ModPack ${lastSelectedModPack.value?.name ?: " No mod pack selected"}",
                        modifier = Modifier.align(Alignment.CenterStart),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                // Launch Button
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .zIndex(2f)
                        .clip(bottomBarShape.copy(topEnd = CornerSize(bottomBarRoundedCornerSize.value.dp)))
                        .background(if (lastSelectedModPack.value == null) StaticColor.Grey else StaticColor.Azure)
                        .then(if (lastSelectedModPack.value == null) Modifier else Modifier.clickable {
                            // Launch Game

                        })
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Launch Game",
                            tint = StaticColor.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Launch",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}


