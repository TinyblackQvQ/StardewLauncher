package components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import services.resources.color.ThemeManager

enum class ProgressBarDetailLocation {
    Left, Right, Invisible
}

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    barHeight: Dp = 4.dp,
    barColor: Color? = null,
    backgroundColor: Color? = null,
    detailLocation: ProgressBarDetailLocation = ProgressBarDetailLocation.Right
) {
    MaterialTheme(ThemeManager.currentColorScheme.collectAsState().value) {
        // 使用动画来平滑过渡进度值
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            label = "progressAnimation"
        )

        // 确定进度条颜色，如果未指定则使用主题 primary
        val actualBarColor = barColor ?: MaterialTheme.colorScheme.primary
        // 确定进度条背景色，如果未指定则使用主题 surfaceVariant
        val actualBackgroundColor = backgroundColor ?: MaterialTheme.colorScheme.surfaceVariant

        // 将进度转换为百分比字符串
        val progressText = "%.2f%%".format(animatedProgress * 100)

        Row(
            modifier = modifier
                .fillMaxWidth() // 确保进度条横向填充父容器
                .height(IntrinsicSize.Min), // 让 Row 的高度适应其内容，方便对齐
            verticalAlignment = Alignment.CenterVertically // 垂直居中对齐子项
        ) {
            // 如果详细信息在左侧，则显示文本
            if (detailLocation == ProgressBarDetailLocation.Left) {
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.bodySmall, // 使用小号字体
                    color = MaterialTheme.colorScheme.onSurface, // 文本颜色
                    modifier = Modifier.padding(end = 8.dp) // 右侧留白
                )
            }

            // 进度条主体
            Box(
                modifier = Modifier
                    .weight(1f) // 让进度条占据 Row 的剩余空间
                    .height(barHeight) // 设置进度条高度
                    .clip(RoundedCornerShape(barHeight / 2)) // 圆角，使两端半圆形
                    .background(actualBackgroundColor) // 进度条背景
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress) // 根据进度填充宽度
                        .height(barHeight) // 高度与背景一致
                        .clip(RoundedCornerShape(barHeight / 2)) // 圆角
                        .background(actualBarColor) // 进度颜色
                )
            }

            // 如果详细信息在右侧，则显示文本
            if (detailLocation == ProgressBarDetailLocation.Right) {
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp) // 左侧留白
                )
            }
        }
    }
}