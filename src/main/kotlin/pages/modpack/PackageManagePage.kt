package pages.modpack

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import components.CircleIcon
import components.InfoLabel
import models.mod.observable.ObservableModPack
import models.mod.serializable.ModPack
import services.resources.color.ThemeManager
import services.resources.i18n.I18nManager
import views.ModResourceView

@Preview
@Composable
fun PackageManagePage() {
    val strings = I18nManager.currentStrings.collectAsState().value
    val colorScheme = ThemeManager.currentColorScheme.collectAsState().value
    val iconSize = 24.dp
    MaterialTheme(colorScheme = colorScheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(36.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.ui.packageManager.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = strings.ui.packageManager.subTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                /** Create button 新建按钮 */
                CircleIcon(
                    imageVector = Icons.Filled.Add,
                    iconSize = iconSize,
                    contentDescription = "Create",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {})
                /** Import button 导入按钮 */
                CircleIcon(
                    imageVector = Icons.Default.Archive,
                    iconSize = iconSize,
                    contentDescription = "Install",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {})
            }
            LazyColumn {
                items(ModResourceView.modPacks.size) { index ->
                    ModPackEntry(modPack = ModResourceView.modPacks[index])
                }
            }
        }
    }
}

@Composable
@Preview
fun ModPackEntry(modifier: Modifier = Modifier, modPack: ObservableModPack) {
    val iconSize = 24.dp
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp).clip(CardDefaults.shape).clickable {

        }) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = modPack.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    InfoLabel(text = modPack.gameVersion.toReadableString())
                    InfoLabel(text = "SMAPI ${modPack.apiVersion.toReadableString()}")
                }
                Text(
                    text = modPack.desc,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CircleIcon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Launch",
                    modifier = Modifier.clickable {})
                CircleIcon(
                    imageVector = Icons.Filled.ContentCopy,
                    iconScale = .7f,
                    contentDescription = "Copy",
                    modifier = Modifier.clickable {})
                CircleIcon(
                    imageVector = Icons.Filled.Delete,
                    iconScale = .8f,
                    contentDescription = "Delete",
                    modifier = Modifier.clickable {})
                Spacer(modifier = Modifier.width(8.dp))
                CircleIcon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Detail",
                    modifier = Modifier)
            }
        }
    }
}