package components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import services.resources.color.ThemeManager

@Composable
fun InfoLabel(
    modifier: Modifier = Modifier,
    text: String,
    textSize: TextUnit? = null,
    contentColor: Color? = null,
    containerColor: Color? = null,
    leadIcon: ImageVector? = null,
    contentDescription: String = "",
    shape: Shape = RoundedCornerShape(50),
    onClick: (() -> Unit)? = null
) {
    MaterialTheme(colorScheme = ThemeManager.currentColorScheme.collectAsState().value) {
        val textColor = contentColor ?: MaterialTheme.colorScheme.onTertiaryContainer
        val backgroundColor = containerColor ?: MaterialTheme.colorScheme.tertiaryContainer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier
                .clip(shape)
                .background(backgroundColor)
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .then(onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier)
        ) {
            if (leadIcon != null) {
                Icon(
                    imageVector = leadIcon,
                    contentDescription = contentDescription,
                    tint = textColor
                )
                Spacer(modifier = Modifier.padding(end = 4.dp))
            }
            Text(
                text = text,
                style = textSize?.let { MaterialTheme.typography.bodySmall.copy(fontSize = it) }
                    ?: MaterialTheme.typography.bodySmall,
                color = textColor
            )
        }
    }
}