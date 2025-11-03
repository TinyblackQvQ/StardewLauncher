/**
 * Copyright (C) 2025 Miluko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package pages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.ConfigOptionComponent
import components.ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.common.SemanticVersion
import org.koin.compose.koinInject
import services.config.AppConfig
import services.defaults.IGameDefaults
import services.resources.color.ThemeManager
import services.resources.i18n.I18nManager
import views.ModImportResult
import views.ModResourceView

@Composable
@Preview
fun AppPage(gameDefaults: IGameDefaults = koinInject()) {
    val strings = I18nManager.currentStrings.collectAsState().value
    val currentSeedColor = ThemeManager.currentSeedColor.collectAsState().value
    val currentThemeMode = ThemeManager.currentThemeMode.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    suspend fun importMods(): List<ModImportResult>? {
        var results: List<ModImportResult>? = null
        AppConfig.general.gameDirectory.read()
            ?.let { dir ->
                results = ModResourceView.importModFromFolderRecursively(dir)
                val defaultModPack = ModResourceView.installedMods.toSerializable().deepCopy(
                    name = "Default",
                    desc = "Contains all mods you imported from your game",
                    gameVersion = gameDefaults.getCurrentGameVersion() ?: SemanticVersion(0, 0, 0),
                    apiVersion = gameDefaults.getCurrentSMAPIVersion() ?: SemanticVersion(0, 0, 0),
                ).toObservable()
                defaultModPack.mods.forEach { it.enabled = true }
                ModResourceView.addModPack(defaultModPack)
                ModResourceView.saveModPackDataToDisk(defaultModPack.toSerializable())
                ModResourceView.copySaveFiles(ModResourceView.installedMods)
                ModResourceView.copySaveFiles(defaultModPack)
                AppConfig.general.lastSelectedModPackName.write("Default")
            }
        return results
    }

    val progress = ModResourceView.taskProgress.collectAsState().value
    val status = ModResourceView.taskStatus.collectAsState()
    val result = remember { mutableListOf<ModImportResult>() }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            ConfigOptionComponent(AppConfig.general.gameDirectory)
            ProgressBar(progress = progress)
            Text(status.value)
            Button(onClick = {
                coroutineScope.launch {
                    result.clear()
                    withContext(Dispatchers.IO) {
                        importMods().let {
                            it?.let { elements ->
                                result.addAll(elements)
                            }
                        }
                    }
                }
            }) {
                Text("Import Data")
            }
            result.forEach { Text("${it.status.name}: ${it.message}", modifier = Modifier.fillMaxWidth()) }
        }
    }
}