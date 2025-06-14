package models.mod.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import models.common.SemanticVersion
import models.mod.serializable.ModContentPack
import models.mod.serializable.ModDependency
import models.mod.serializable.ModManifest
import models.mod.serializable.ModUpdateKey

class ObservableModManifest(
    name: String,
    author: String,
    version: SemanticVersion,
    description: String = "",
    uniqueID: String,
    entryDLL: String? = null,
    minimumApiVersion: SemanticVersion? = null,
    minimumGameVersion: SemanticVersion? = null,
    updateKeys: List<ModUpdateKey>? = null,
    contentPackFor: ModContentPack? = null,
    dependencies: List<ModDependency>? = null,
    schema: String? = null
) {
    // 创建可观察的属性
    val innerName = mutableStateOf(name)
    val innerAuthor = mutableStateOf(author)
    val innerVersion = mutableStateOf(version)
    val innerDescription = mutableStateOf(description)
    val innerUniqueID = mutableStateOf(uniqueID)
    val innerEntryDLL = mutableStateOf(entryDLL)
    val innerMinimumApiVersion = mutableStateOf(minimumApiVersion)
    val innerMinimumGameVersion = mutableStateOf(minimumGameVersion)
    val contentPackFor = contentPackFor?.toObservable()
    val innerSchema = mutableStateOf(schema)
    // 暴露给外部的属性
    var name by innerName
    var author by innerAuthor
    var version by innerVersion
    var description by innerDescription
    var uniqueID by innerUniqueID
    var entryDLL by innerEntryDLL
    var minimumApiVersion by innerMinimumApiVersion
    var minimumGameVersion by innerMinimumGameVersion
    var schema by innerSchema
    val updateKeys = mutableStateListOf<ObservableModUpdateKey>().apply {
        updateKeys?.forEach { add(it.toObservable()) }
    }
    val dependencies = mutableStateListOf<ObservableModDependency>().apply {
        dependencies?.forEach { add(it.toObservable()) }
    }
    /**
     * 转换为可序列化的对象
     * */
    fun toSerializable(): ModManifest {
        return ModManifest(
            name = name,
            author = author,
            version = version,
            description = description,
            uniqueID = uniqueID,
            entryDLL = entryDLL,
            minimumApiVersion = minimumApiVersion,
            minimumGameVersion = minimumGameVersion,
            updateKeys = updateKeys.map { it.toSerializable() },
            contentPackFor = contentPackFor?.toSerializable(),
            dependencies = dependencies.map { it.toSerializable() },
            schema = schema
        )
    }
}