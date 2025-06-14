package models.mod.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import models.mod.serializable.ModUpdateKey

class UpdateKeySerializer: KSerializer<ModUpdateKey> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UpdateKey", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ModUpdateKey {
        val string = decoder.decodeString()
        return ModUpdateKey.Companion.parse(string)
    }

    override fun serialize(encoder: Encoder, value: ModUpdateKey) {
        encoder.encodeString(value.toString())
    }
}