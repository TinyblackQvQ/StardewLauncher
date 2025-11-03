package components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed class Direction {
    object Vertical : Direction()
    object Horizontal : Direction()
}

@Composable
fun ExpandableContainer(
    modifier: Modifier = Modifier,
    direction: Direction = Direction.Vertical,
    initialState: Boolean = false,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(initialState) }
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            // Head
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                title()

                Spacer(modifier = Modifier.weight(1f)) // 将图标推到最右侧

                // 3. 根据状态显示不同的图标
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            // Content Area
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    content()
                }
            }
        }
    }
}