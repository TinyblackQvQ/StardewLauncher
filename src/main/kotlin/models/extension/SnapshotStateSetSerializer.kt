package models.extension

import androidx.compose.runtime.snapshots.SnapshotStateSet
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json

class SnapshotStateSetSerializer : KSerializer<SnapshotStateSet<@Serializable Any>> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SnapshotStateSet", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SnapshotStateSet<@Serializable Any>) {
        val json = Json { ignoreUnknownKeys = true }
        val serializedValues = value.map { json.encodeToString(it) }
        encoder.encodeString(serializedValues.joinToString(separator = ",", prefix = "[", postfix = "]"))
    }

    override fun deserialize(decoder: Decoder): SnapshotStateSet<@Serializable Any> {
        val newSet = SnapshotStateSet<@Serializable Any>()
        val content = decoder.decodeString().trim().drop(1).dropLast(1) // Remove surrounding brackets
        if (content.isEmpty()) {
            return newSet
        }

        val json = Json { ignoreUnknownKeys = true }
        var braceCount = 0
        var startIndex = 0

        for (i in content.indices) {
            when (content[i]) {
                '{' -> braceCount++
                '}' -> braceCount--
            }

            // When braceCount is 0, we have found a complete top-level JSON object
            if (braceCount == 0 && content[i] == '}') {
                val jsonObjectString = content.substring(startIndex, i + 1)
                try {
                    // Note: For deserializing unknown types, you must configure polymorphic serialization in the Json instance.
                    // Example: val json = Json { serializersModule = myModule }
                    val decodedItem = json.decodeFromString<@Serializable Any>(jsonObjectString)
                    newSet.add(decodedItem)
                } catch (e: Exception) {
                    // Log the error for debugging purposes
                    println("Deserialization failed for chunk: $jsonObjectString. Error: ${e.message}")
                }
                // Find the start of the next object, skipping the comma
                startIndex = i + 1
                while (startIndex < content.length && content[startIndex] != '{') {
                    startIndex++
                }
            }
        }
        return newSet
    }
}