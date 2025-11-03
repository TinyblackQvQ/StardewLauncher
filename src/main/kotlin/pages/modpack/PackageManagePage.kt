package pages.modpack

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.CircleIcon
import components.ModPackInfo
import components.overlay.OverlayController
import components.overlay.OverlayHost
import services.resources.i18n.I18nManager
import views.ModResourceView

@Preview
@Composable
fun PackageManagePage() {
    val strings = I18nManager.currentStrings.collectAsState().value
    val overlayController = remember { OverlayController() }
    val iconSize = 24.dp
    Box(modifier = Modifier.fillMaxSize()) {
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
                    ModPackInfo(
                        modPack = ModResourceView.modPacks[index],
                        overlayController = overlayController,
                        config = { canNavigateToDetailedPage = true })
                }
            }
        }
        OverlayHost(controller = overlayController)
    }
}