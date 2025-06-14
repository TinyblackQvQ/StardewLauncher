package pages.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import services.resources.color.ThemeManager

@Composable
fun UnderStructure() {
    MaterialTheme(colorScheme = ThemeManager.currentColorScheme.collectAsState().value) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Construction,
                    contentDescription = "Work in progress",
                    modifier = Modifier.size(36.dp)
                )
                Text(text = "Construction in Progress", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}