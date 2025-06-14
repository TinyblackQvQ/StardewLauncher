package components.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// 自定义的复制+新建图标
val Icons.Filled.ContentCopyAdd: ImageVector
    get() {
        if (_contentCopyAdd != null) {
            return _contentCopyAdd!!
        }
        _contentCopyAdd = ImageVector.Builder(
            name = "Filled.ContentCopyAdd",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        ).apply {
            // 绘制复制图标的部分
            path {
                moveTo(16.0f, 1.0f)
                lineTo(4.0f, 1.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                horizontalLineToRelative(2.0f)
                lineTo(4.0f, 3.0f)
                horizontalLineToRelative(12.0f)
                lineTo(16.0f, 1.0f)
                close()
                moveTo(19.0f, 5.0f)
                lineTo(8.0f, 5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(11.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 21.0f)
                lineTo(8.0f, 21.0f)
                lineTo(8.0f, 7.0f)
                horizontalLineToRelative(11.0f)
                verticalLineToRelative(14.0f)
                close()
            }

            // 在复制图标的右下角绘制加号
            // 加号的中心大致在 (19.0f, 19.0f) 附近，具体需要根据效果微调
            // 假设加号的大小为 6x6
            val plusSize = 4.0f // 加号的长度
            val plusOffset = 18.0f // 加号的左上角X和Y坐标，用于将加号放置在右下角

            path {
                // 水平线
                moveTo(plusOffset - plusSize / 2, plusOffset) // 从左侧开始
                horizontalLineToRelative(plusSize) // 水平向右绘制
                // 垂直线
                moveTo(plusOffset, plusOffset - plusSize / 2) // 从上方开始
                verticalLineToRelative(plusSize) // 垂直向下绘制
            }
        }.build()
        return _contentCopyAdd!!
    }

private var _contentCopyAdd: ImageVector? = null