package models.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SemanticVersionSerializer : KSerializer<SemanticVersion> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SemanticVersion", PrimitiveKind.STRING)

    // 反序列化：从字符串读取并解析
    override fun deserialize(decoder: Decoder): SemanticVersion {
        val versionString = decoder.decodeString()
        return SemanticVersion.parse(versionString)
    }

    // 序列化：将 SemanticVersion 对象转换为其字符串表示
    override fun serialize(encoder: Encoder, value: SemanticVersion) {
        encoder.encodeString(value.toString())
    }
}