package components.overlay.prefabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GeneralPopup(
    title: String? = null,
    dialogConfig: Map<String, () -> Unit>? = null,
    contentPaddingValues: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    var scrollState = rememberScrollState()
    Card(modifier = Modifier.sizeIn(maxWidth = 400.dp)) {
        Column(modifier = Modifier.heightIn(max = 300.dp).padding(contentPaddingValues)) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                content()
            }
            Row(
                modifier = Modifier.align(Alignment.End).padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dialogConfig?.forEach { (key, value) ->
                    Button(onClick = { value() }) {
                        Text(text = key, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}