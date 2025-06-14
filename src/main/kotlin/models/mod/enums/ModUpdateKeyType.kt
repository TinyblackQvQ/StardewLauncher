package models.mod.enums

enum class ModUpdateKeyType {
    Chucklefish, CurseForge, Github, ModDrop, Nexus, UpdateManifest;

    companion object {
        fun fromString(value: String): ModUpdateKeyType? {
            return entries.find { it.name == value }
        }
    }
}