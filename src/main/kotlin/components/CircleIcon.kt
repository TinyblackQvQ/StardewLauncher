package components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import services.resources.color.ThemeManager

/**
 * Build a circle shaped icon
 *
 * 用于创建圆形图标
 *
 * @param modifier the modifier of the icon 修饰器
 * @param iconSize the size of the icon 图标大小
 * @param outlinedSize the size of the outlined square border of the icon 圆形图标的轮廓大小
 * @param iconScale the scale of the icon 图标缩放，用于解决图标之间内容至边距不同的情况
 * @param imageVector the image vector of the icon 图标矢量
 * @param contentDescription the content description of the icon, default is empty 图标内容描述，默认为空
 * @param tint the tint of the icon 图标颜色
 * */
@Composable
fun CircleIcon(
    modifier: Modifier = Modifier,
    iconSize: Dp = 28.dp,
    outlinedSize: Dp? = null,
    iconScale: Float = 1f,
    imageVector: ImageVector,
    contentDescription: String = "",
    tint: Color? = null
) {
    val colorScheme = ThemeManager.currentColorScheme.collectAsState().value
    // use 1.414f to calculate the outlined square border length of the Icon Circle
    // outlinedSize has a higher priority than iconSize, if it is not null, iconSize will be overwritten
    // 使用 1.414f 来计算图标圆形的轮廓边长
    // outlinedSize 的优先级高于 iconSize，如果它不为空，则 iconSize 将被覆盖
    val outlinedSize = outlinedSize ?: (iconSize * 1.414f)
    val iconSize = outlinedSize / 1.414f
    MaterialTheme(colorScheme) {
        Box(
            modifier = Modifier
                .size(outlinedSize)
                .clip(CircleShape)
                .then(modifier)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize * iconScale).align(Alignment.Center),
                tint = tint ?: colorScheme.onSurface
            )
        }
    }
}