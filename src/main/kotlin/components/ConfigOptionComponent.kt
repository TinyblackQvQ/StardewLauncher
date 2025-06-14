package components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import services.config.IConfigOption
import services.resources.i18n.I18nManager
import javax.swing.JFileChooser

@Composable
fun ConfigOptionComponent(
    option: IConfigOption<*>,
    modifier: Modifier = Modifier
) {
    if (!option.isVisible) return

    val strings = I18nManager.currentStrings.collectAsState().value
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    LaunchedEffect(strings, option.i18nKey) {
        title = getI18nValue(strings, option.i18nKey, "title")
        desc = getI18nValue(strings, option.i18nKey, "desc")
    }

    Column(modifier = modifier) {
        // 标题和描述
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        if (desc.isNotEmpty()) {
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 根据类型显示不同的输入控件
        when (option.default) {
            is Boolean -> BooleanConfigInput(option as IConfigOption<Boolean>)
            is Int -> NumberConfigInput(option as IConfigOption<Int>)
            is Float -> NumberConfigInput(option as IConfigOption<Float>)
            is Double -> NumberConfigInput(option as IConfigOption<Double>)
            is String -> StringConfigInput(option as IConfigOption<String>)
            else -> TextConfigInput(option as IConfigOption<String>)
        }
    }
}

private fun getI18nValue(root: Any, i18nKey: String, field: String): String {
    return try {
        val keys = i18nKey.split('.') + field
        var current: Any? = root
        for (k in keys) {
            current = current?.let {
                val prop = it::class.members.find { m -> m.name == k } ?: return i18nKey
                prop.call(it)
            }
        }
        current as? String ?: i18nKey
    } catch (e: Exception) {
        i18nKey
    }
}

@Composable
private fun BooleanConfigInput(option: IConfigOption<Boolean>) {
    var value by remember { mutableStateOf(option.read() ?: option.default ?: false) }

    Switch(
        checked = value,
        onCheckedChange = { newValue ->
            value = newValue
            option.write(newValue)
        }
    )
}

@Composable
private fun <T : Number> NumberConfigInput(option: IConfigOption<T>) {
    var value by remember { mutableStateOf(option.read()?.toString() ?: option.default?.toString() ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                value = newValue
                try {
                    val number = when (option.default) {
                        is Int -> newValue.toInt()
                        is Float -> newValue.toFloat()
                        is Double -> newValue.toDouble()
                        else -> return@OutlinedTextField
                    }

                    // 检查范围
                    option.valueRange?.let { range ->
                        if (!range.isValid(number as T)) {
                            error = "值必须在 ${range.min} 和 ${range.max} 之间"
                            return@OutlinedTextField
                        }
                    }

                    error = null
                    option.write(number as T)
                } catch (e: NumberFormatException) {
                    error = "请输入有效的数字"
                }
            },
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
            modifier = Modifier.weight(1f)
        )

        // 增减按钮
        Column {
            IconButton(onClick = {
                try {
                    val current = value.toFloat()
                    val step = when (option.default) {
                        is Int -> 1
                        is Float -> 0.1f
                        is Double -> 0.1
                        else -> 1
                    }
                    val newValue = (current + step as Float).toString()
                    value = newValue
                    option.write(newValue.toFloat() as T)
                } catch (e: NumberFormatException) {
                    // 忽略无效输入
                }
            }) {
                Icon(Icons.Default.KeyboardArrowUp, "增加")
            }
            IconButton(onClick = {
                try {
                    val current = value.toFloat()
                    val step = when (option.default) {
                        is Int -> 1
                        is Float -> 0.1f
                        is Double -> 0.1
                        else -> 1
                    }
                    val newValue = (current - step as Float).toString()
                    value = newValue
                    option.write(newValue.toFloat() as T)
                } catch (e: NumberFormatException) {
                    // 忽略无效输入
                }
            }) {
                Icon(Icons.Default.KeyboardArrowDown, "减少")
            }
        }
    }
}

@Composable
private fun StringConfigInput(option: IConfigOption<String>) {
    var value by remember { mutableStateOf(option.read() ?: option.default ?: "") }
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                value = newValue
                option.write(newValue)
            },
            modifier = Modifier.weight(1f)
        )

        // 根据不同类型添加不同的按钮
        when {
            option.isDirectory -> {
                IconButton(onClick = {
                    val fileChooser = JFileChooser()
                    fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        value = fileChooser.selectedFile.absolutePath
                        option.write(value)
                    }
                }) {
                    Icon(Icons.Default.Folder, "选择文件夹")
                }
            }

            option.isOption -> {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, "选择选项")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        option.options?.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt.toString()) },
                                onClick = {
                                    value = opt.toString()
                                    option.write(value)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            option.isCheckbox -> {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.CheckBox, "选择多个选项")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        option.options?.forEach { opt ->
                            val isSelected = value.split(",").contains(opt.toString())
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null
                                        )
                                        Text(opt.toString())
                                    }
                                },
                                onClick = {
                                    val values = value.split(",").toMutableList()
                                    if (isSelected) {
                                        values.remove(opt.toString())
                                    } else {
                                        values.add(opt.toString())
                                    }
                                    value = values.joinToString(",")
                                    option.write(value)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextConfigInput(option: IConfigOption<String>) {
    var value by remember { mutableStateOf(option.read()?.toString() ?: option.default?.toString() ?: "") }

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            value = newValue
            option.write(newValue)
        },
        modifier = Modifier.fillMaxWidth()
    )
} 