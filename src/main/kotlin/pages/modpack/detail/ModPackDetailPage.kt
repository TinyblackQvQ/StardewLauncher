package pages.modpack.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.IndeterminateCheckBox
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.*
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import components.CircleIcon
import components.Direction
import components.ExpandableContainer
import components.ModPackInfo
import components.overlay.OverlayController
import components.overlay.OverlayHost
import components.overlay.prefabs.GeneralPopup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.mod.observable.ObservableMod
import models.mod.observable.ObservableModPack
import org.koin.compose.koinInject
import views.ModResourceView

enum class SelectMode {
    None, Multi, Full
}

sealed interface ISortType {
    val name: String
    fun sort(mods: List<ObservableMod>): Map<String, List<ObservableMod>>
}

sealed class SortType : ISortType {
    object Name : SortType() {
        override val name: String = "Name"
        override fun sort(mods: List<ObservableMod>): Map<String, List<ObservableMod>> {
            return mapOf(Pair("All", mods.sortedBy {
                it.manifest.name
            }))
        }
    }

    object Update : SortType() {
        override val name: String = "Update"
        override fun sort(mods: List<ObservableMod>): Map<String, List<ObservableMod>> {
            // TODO: 待实现，需要联网获取更新信息
            return mapOf(Pair("All", mods.sortedBy { it.manifest.name }))
        }
    }

    object Dependency : SortType() {
        override val name: String = "Dependency"
        override fun sort(mods: List<ObservableMod>): Map<String, List<ObservableMod>> {
            val dependedMods = mutableListOf<ObservableMod>()
            for (mod in mods) {
                for (dependency in mod.manifest.dependencies) {
                    val modInstanceList = mods.filter { it.manifest.uniqueID == dependency.uniqueID }
                    if (modInstanceList.isNotEmpty() && !dependedMods.contains(modInstanceList[0])) {
                        dependedMods.add(modInstanceList[0])
                    }
                }
            }
            val contentPackMods = mods.filter { mod ->
                mod.manifest.contentPackFor != null
            }
            return mapOf(
                Pair("Depended By Others", dependedMods.sortedBy { it.manifest.name }),
                Pair("Content Packs", contentPackMods.sortedBy { it.manifest.name }),
                Pair("Others", mods.filter {
                    !(dependedMods.contains(it) || contentPackMods.contains(it))
                }.sortedBy { it.manifest.name })
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModPackDetailPage(modPack: ObservableModPack) {
    val localCoroutineScope = rememberCoroutineScope()
    val overlayController = remember { OverlayController() }
    var sortType: SortType by remember { mutableStateOf(SortType.Name) }
    var isSortTypePickerOpened by remember { mutableStateOf(false) }

    /** mod list's sort result */
    val enabledMods = modPack.getEnabledMods()
    val enabledModsUniqueIds = enabledMods.map { it.manifest.uniqueID }
    val enabledModsSortResult = sortType.sort(enabledMods)
    val disabledModsSortResult =
        sortType.sort(ModResourceView.installedMods.mods.filter { it.manifest.uniqueID !in enabledModsUniqueIds })
    var selectMode by remember { mutableStateOf(SelectMode.None) }
    val selectedMods = remember { mutableStateListOf<ObservableMod>() }
    var isDisabledModsShowup by remember { mutableStateOf(false) }
    fun updateSelectMode(targetMode: SelectMode) {
        selectMode = targetMode
        when (targetMode) {
            SelectMode.None -> {
                selectedMods.clear()
            }

            SelectMode.Multi -> {
                // do nothing
            }

            SelectMode.Full -> {
                selectedMods.clear()
                for (mod in modPack.mods) {
                    selectedMods.add(mod)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            /** ModPack info & edit controller */
            item {
                ModPackInfo(
                    modPack = modPack, overlayController = overlayController, config = {
                        canNavigateToDetailedPage = false
                        canEditInfo = true
                        canEditConfigs = true
                    })
            }
            /** Sort controller for mod info list */
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    /** Button for switch select mode */
                    IconButton(onClick = {
                        updateSelectMode(
                            when (selectMode) {
                                SelectMode.None -> SelectMode.Multi
                                SelectMode.Multi -> SelectMode.Full
                                SelectMode.Full -> SelectMode.None
                            }
                        )
                    }) {
                        Icon(
                            imageVector = when (selectMode) {
                                SelectMode.None -> Icons.Default.CheckBoxOutlineBlank
                                SelectMode.Multi -> Icons.Default.IndeterminateCheckBox
                                SelectMode.Full -> Icons.Default.CheckBox
                            }, contentDescription = null, tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    /** Selector for switch sort mode */
                    Box {
                        FilterChip(
                            selected = true,
                            label = { Text(text = "Sort By: ${sortType.name}") },
                            onClick = { isSortTypePickerOpened = !isSortTypePickerOpened })
                        if (isSortTypePickerOpened) {
                            DropdownMenu(
                                expanded = isSortTypePickerOpened,
                                onDismissRequest = { isSortTypePickerOpened = false }) {
                                val types = listOf(SortType.Name, SortType.Update, SortType.Dependency)
                                for (type in types) {
                                    DropdownMenuItem(text = { Text(text = type.name) }, onClick = {
                                        sortType = type
                                        isSortTypePickerOpened = false
                                    })
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    val iconRotation by animateFloatAsState(targetValue = if (isDisabledModsShowup) 180f else 0f)
                    IconButton(onClick = { isDisabledModsShowup = !isDisabledModsShowup }) {
                        Icon(
                            modifier = Modifier.rotate(iconRotation),
                            imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                            contentDescription = null
                        )
                    }
                }
            }
            /** Mod info list */
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortResultContainer(
                        modifier = Modifier.weight(1f),
                        title = "Enabled Mods",
                        sortResult = enabledModsSortResult,
                        selectedMods = selectedMods,
                        selectMode = selectMode,
                        updateSelectMode = ::updateSelectMode,
                        currentModPack = modPack,
                        coroutineScope = localCoroutineScope,
                        overlayController = overlayController,
                        isVersionSwitcherEnabled = true
                    )
                    val animationWeight by animateFloatAsState(if (isDisabledModsShowup) 1f else 0.00001f)
                    SortResultContainer(
                        modifier = Modifier.weight(animationWeight).alpha(animationWeight),
                        title = "Disabled Mods",
                        sortResult = disabledModsSortResult,
                        selectedMods = selectedMods,
                        selectMode = selectMode,
                        updateSelectMode = ::updateSelectMode,
                        currentModPack = modPack,
                        coroutineScope = localCoroutineScope,
                        overlayController = overlayController,
                        isVersionSwitcherEnabled = false
                    )
                }
            }
        }
        val overshootEasing = CubicBezierEasing(0.4f, 2.0f, 0.6f, 1.0f)
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            visible = selectMode != SelectMode.None,
            enter = fadeIn() + expandVertically(
                animationSpec = tween(300, easing = overshootEasing),
                expandFrom = Alignment.CenterVertically
            ),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically)
        ) {
            SelectModeController(selectedMods, selectMode, onUpdateSelectMode = { updateSelectMode(it) })
        }
        OverlayHost(controller = overlayController)
    }
}

@Composable
fun SelectModeController(
    selectedMods: MutableList<ObservableMod>,
    selectMode: SelectMode,
    onUpdateSelectMode: (mode: SelectMode) -> Unit
) {
    val containerShape = RoundedCornerShape(8.dp)
    val hoverInteractionSource = remember { MutableInteractionSource() }
    val isHovered by hoverInteractionSource.collectIsHoveredAsState()
    val offsetHeight = animateFloatAsState(if (isHovered) -4f else 0f)
    Column(
        modifier = Modifier.hoverable(interactionSource = hoverInteractionSource)
            .offset(0.dp, offsetHeight.value.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = containerShape)
            .background(MaterialTheme.colorScheme.primaryContainer, shape = containerShape)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "${selectedMods.size} Mods Selected", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectModeControllerButton("Update", Icons.Default.Upgrade) {}
            SelectModeControllerButton("Select All", Icons.Default.SelectAll) { onUpdateSelectMode(SelectMode.Full) }
            SelectModeControllerButton("Cancel", Icons.Default.Cancel) { onUpdateSelectMode(SelectMode.None) }
        }
    }
}

@Composable
fun SelectModeControllerButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            }
            .padding(start = 2.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModController(
    currentMod: ObservableMod,
    currentModPack: ObservableModPack,
    coroutineScope: CoroutineScope,
    overlayController: OverlayController,
    isVersionSwitcherEnabled: Boolean
) {
    val snackbarHostState = koinInject<SnackbarHostState>()
    Row {
        /**
         * A dropdown to select version
         * when the version is an update one,
         * ask for the user to choose if they want to keep the older version's config
         * when the version is an older one,
         * warn user that it could cause unknown issues, including breaking save data
         * */
        if (isVersionSwitcherEnabled)
            Box {
                var isConfigKept by remember { mutableStateOf(false) }
                var isVersionPickerOpened by remember { mutableStateOf(false) }
                IconButton(onClick = { isVersionPickerOpened = true }) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "SelectVersion",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = isVersionPickerOpened, onDismissRequest = { isVersionPickerOpened = false }) {
                    for (targetMod in ModResourceView.getAllInstalledModVersionsByUniqueID(currentMod.manifest.uniqueID)) {
                        DropdownMenuItem(
                            text = { Text(text = targetMod.manifest.version.toString()) },
                            enabled = targetMod.manifest.version != currentMod.manifest.version,
                            onClick = {
                                overlayController.show {
                                    GeneralPopup(
                                        dialogConfig = mapOf("Confirm" to {
                                            if (isConfigKept) {
                                                currentModPack.updateModInstance(
                                                    targetMod.toSerializable().deepCopy().mergeConfig(currentMod.config)
                                                )
                                            } else {
                                                currentModPack.updateModInstance(
                                                    targetMod.toSerializable().deepCopy()
                                                )
                                            }
                                            coroutineScope.launch {
                                                ModResourceView.saveModPackDataToDisk(currentModPack.toSerializable())
                                            }
                                        }, "Cancel" to {
                                            overlayController.dismiss()
                                        }),
                                        title = if (targetMod.manifest.version > currentMod.manifest.version) "Confirm Change" else "Warning",
                                    ) {
                                        if (targetMod.manifest.version > currentMod.manifest.version) Text(
                                            "Updating to a newer version may cause potential compatibility issues. " +
                                                    "And the older configs will be lost. Proceed?",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        else Text(
                                            text = "Downgrading to an older version may cause critical issues, including save data corruption. " +
                                                    "Backup your save data or create a copy of the current modpack before proceeding.",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Keep Config", style = MaterialTheme.typography.titleSmall)
                                            TooltipBox(
                                                tooltip = {
                                                    RichTooltip {
                                                        Text(
                                                            modifier = Modifier.padding(vertical = 8.dp),
                                                            text = "When enabled, attempts to apply the current configuration to the selected version. " +
                                                                    "This option is only available when both versions have configuration files.",
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }
                                                }, positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                                    TooltipAnchorPosition.Right
                                                ), state = rememberTooltipState(isPersistent = true)
                                            ) {
                                                CircleIcon(
                                                    iconSize = 16.dp,
                                                    imageVector = Icons.AutoMirrored.Default.Help
                                                )
                                            }
                                            Switch(
                                                modifier = Modifier.scale(.8f),
                                                checked = isConfigKept,
                                                enabled = targetMod.config.isNotEmpty() && currentMod.config.isNotEmpty(),
                                                onCheckedChange = { isConfigKept = it })
                                        }
                                    }
                                }
                                currentModPack.updateModInstance(targetMod.toSerializable())
                                isVersionPickerOpened = false
                            })
                    }
                }
            }
        IconButton(onClick = {
            val beforeOperation = currentModPack.getEnabledMods().size
            if (currentMod.enabled) {
                currentModPack.disableMod(currentMod.getUUID())
            } else {
                currentModPack.enableMod(currentMod.getUUID())
            }
            coroutineScope.launch { ModResourceView.saveModPackDataToDisk(currentModPack.toSerializable()) }
            if (beforeOperation == currentModPack.getEnabledMods().size) {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        "Cannot disable mod: required by other mods. Use select mode or disable dependent mods first.",
                        actionLabel = "Okay",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }) {
            Icon(
                imageVector = if (currentMod.enabled) Icons.AutoMirrored.Default.KeyboardArrowRight
                else Icons.AutoMirrored.Default.KeyboardArrowLeft,
                contentDescription = if (currentMod.enabled) "Disable mod" else "Enable mod",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SortResultContainer(
    modifier: Modifier = Modifier,
    title: String,
    sortResult: Map<String, List<ObservableMod>>,
    selectMode: SelectMode,
    selectedMods: MutableList<ObservableMod>,
    currentModPack: ObservableModPack,
    coroutineScope: CoroutineScope,
    overlayController: OverlayController,
    updateSelectMode: (mode: SelectMode) -> Unit,
    isVersionSwitcherEnabled: Boolean = false
) {
    val selectedModIds = selectedMods.map { it.manifest.uniqueID }
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        for (resultKey in sortResult.keys) {
            val mods = sortResult[resultKey]!!
            ExpandableContainer(
                direction = Direction.Vertical, initialState = false, title = {
                    Text(text = "$resultKey (${mods.size})", style = MaterialTheme.typography.bodyMedium)
                }) {
                /** Show all mods in the indexed group */
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (mod in mods) {
                        /** The mod info row */
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .fillMaxWidth()
                                .height(48.dp)
                                /**
                                 * When the row is clicked,
                                 * first check if the select mode is enabled, when it's enabled, do selecting logic,
                                 * when it's not, open a detailed popup for the mod.
                                 * */
                                .clickable {
                                    if (selectMode != SelectMode.None) {
                                        if (mod.manifest.uniqueID in selectedModIds) {
                                            selectedMods
                                                .filter { it.manifest.uniqueID == mod.manifest.uniqueID }
                                                .forEach { selectedMods.remove(it) }
                                            if (selectedMods.isEmpty()) updateSelectMode(SelectMode.None)
                                            else updateSelectMode(SelectMode.Multi)
                                        } else {
                                            selectedMods.add(mod)
                                        }
                                    }
                                }
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            /** The colored block to figure if this mod is selected or not */
                            val heightFraction by animateFloatAsState(
                                if (mod.manifest.uniqueID in selectedModIds) .8f else 0f
                            )
                            Spacer(
                                modifier = Modifier.fillMaxHeight(heightFraction).width((8 * heightFraction).dp)
                                    .clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.primary)
                            )
//                            TODO: Add Mod icon
                            /** The basic info of the mod, including mod name, current version & detail info */
                            Column(modifier = Modifier.weight(1f)) {
                                /** Mod name & version */
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = mod.manifest.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        softWrap = false
                                    )
                                    Text(
                                        text = mod.manifest.version.toString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.secondary
                                        ),
                                        softWrap = false
                                    )
                                }
                                /** Mod Tags & desc */
                                Row {
//                                    TODO: Show Mod Tags
                                    Text(
                                        text = mod.manifest.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.secondary
                                        ),
                                        softWrap = false
                                    )
                                }
                            }
                            /** Mod Controller */
                            ModController(
                                mod,
                                currentModPack,
                                coroutineScope,
                                overlayController,
                                isVersionSwitcherEnabled
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}