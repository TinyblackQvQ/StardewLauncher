package components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import components.overlay.OverlayController
import components.overlay.prefabs.GeneralPopup
import kotlinx.coroutines.launch
import models.mod.observable.ObservableModPack
import org.koin.compose.koinInject
import pages.general.MainTabsController
import services.config.AppConfig
import services.navigation.NavigationDestinations
import services.navigation.NavigationManager
import views.ModResourceView

interface IModPackInfoConfig {
    var canNavigateToDetailedPage: Boolean
    var canEditInfo: Boolean
    var canEditConfigs: Boolean
    var showControllers: Boolean
}

class ModPackInfoConfig : IModPackInfoConfig {
    override var canNavigateToDetailedPage: Boolean = false
    override var canEditInfo: Boolean = false
    override var canEditConfigs: Boolean = false
    override var showControllers: Boolean = false
}

@Composable
fun ModPackInfo(
    modifier: Modifier = Modifier,
    modPack: ObservableModPack,
    overlayController: OverlayController,
    navigationManager: NavigationManager = koinInject(),
    config: IModPackInfoConfig.() -> Unit
) {
    val iconSize = 24.dp
    val configs = ModPackInfoConfig().apply(config)
    val coroutineScope = rememberCoroutineScope()

    /** If directly click this card, will jump to detailed modpack page */
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = modifier.fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(CardDefaults.shape)
            .then(if (configs.canNavigateToDetailedPage) modifier.clickable {
                navigationManager.navigateTo(
                    NavigationDestinations.ModPackDetailedInfo(modPack)
                )
            } else modifier)
    ) {
        /** Inner row */
        Row(
            modifier = Modifier.fillMaxSize().padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            /** ModPack Info */
            Column(modifier = Modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                /** Title & Version info labels */
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = modPack.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    InfoLabel(text = modPack.gameVersion.toReadableString())
                    InfoLabel(text = "SMAPI ${modPack.apiVersion.toReadableString()}")
                    if (configs.canEditInfo) {
                        CircleIcon(imageVector = Icons.Default.Edit, iconSize = 20.dp, modifier = Modifier.clickable {
                            overlayController.show {
                                var editingName by remember { mutableStateOf(modPack.name) }
                                var editingDesc by remember { mutableStateOf(modPack.desc) }
                                GeneralPopup(
                                    title = "Edit ModPack Info",
                                    dialogConfig = mapOf(
                                        "Save" to {
                                            modPack.name = editingName
                                            modPack.desc = editingDesc
                                            coroutineScope.launch {
                                                ModResourceView.saveModPackDataToDisk(modPack.toSerializable())
                                            }
                                            overlayController.dismiss()
                                        },
                                        "Cancel" to {
                                            overlayController.dismiss()
                                        }
                                    )
                                ) {
                                    Column {
                                        OutlinedTextField(
                                            modifier = Modifier.fillMaxWidth(),
                                            value = editingName,
                                            singleLine = true,
                                            onValueChange = { editingName = it },
                                            label = { Text("ModPack Name") })
                                        OutlinedTextField(
                                            modifier = Modifier.fillMaxWidth(),
                                            value = editingDesc,
                                            singleLine = true,
                                            onValueChange = { editingDesc = it },
                                            label = { Text("ModPack Description") })
                                    }
                                }
                            }
                        })
                    }
                }
                /** Description */
                Text(
                    text = modPack.desc,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            /** Operations */
            Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                /** Jump to launch page with this mod */
                CircleIcon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Launch",
                    modifier = Modifier.clickable {
                        AppConfig.general.lastSelectedModPackName.write(modPack.name)
                        navigationManager.seekBackTo(NavigationDestinations.MainTabs)
                        coroutineScope.launch {
                            MainTabsController.state?.animateScrollToPage(0)
                        }
                    })
                /** Create a new modpack with copy of this */
                CircleIcon(
                    imageVector = Icons.Filled.ContentCopy,
                    iconScale = .7f,
                    contentDescription = "Copy",
                    modifier = Modifier.clickable {})
                /** Delete this modpack */
                CircleIcon(
                    imageVector = Icons.Filled.Delete,
                    iconScale = .8f,
                    contentDescription = "Delete",
                    modifier = Modifier.clickable {})
                if (configs.canNavigateToDetailedPage) {
                    Spacer(modifier = Modifier.width(8.dp))
                    /** Jump to detailed modpack page */
                    CircleIcon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Detail",
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
fun ModPackInfoEditPopup(modPack: ObservableModPack) {
    var editingName by remember { mutableStateOf(modPack.name) }
    var editingDesc by remember { mutableStateOf(modPack.desc) }
    Column {
        TextField(value = modPack.name, onValueChange = { editingName = it }, label = { Text("ModPack Name") })
        TextField(value = modPack.desc, onValueChange = { editingDesc = it }, label = { Text("ModPack Description") })
    }
}