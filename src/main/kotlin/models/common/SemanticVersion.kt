package models.common

import kotlinx.serialization.Serializable

/**
 * Represents a Semantic Version, following SemVer 2.0.0 specification.
 * Format: MAJOR.MINOR.PATCH[-PRERELEASE][+BUILDMETADATA]
 *
 * Examples: 1.0.0, 1.0.0-alpha, 1.0.0-alpha.1, 1.0.0-beta.2, 1.0.0-rc.1, 1.0.0+build.123, 1.0.0-alpha+exp.5
 */
@Serializable(with = SemanticVersionSerializer::class) // 使用自定义序列化器
data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int? = null,
    val preRelease: List<String> = emptyList(), // 点分隔的预发布标识符
    val buildMetadata: String? = null // 构建元数据，在比较时忽略
) : Comparable<SemanticVersion> {

    init {
        require(major >= 0) { "Major version must be non-negative." }
        require(minor >= 0) { "Minor version must be non-negative." }
        patch?.let { require(it >= 0) { "Patch version must be non-negative." } }
        preRelease.forEach { id ->
            require(id.isNotBlank()) { "Pre-release identifiers cannot be blank." }
            require(id.all { it.isLetterOrDigit() || it == '-' }) { "Pre-release identifiers must contain only alphanumeric characters and hyphens." }
            if (id.startsWith("0") && id.length > 1 && id.all { it.isDigit() }) {
                throw IllegalArgumentException("Numeric pre-release identifiers must not have leading zeros.")
            }
        }
        if (buildMetadata != null) {
            require(buildMetadata.all { it.isLetterOrDigit() || it == '-' || it == '.' }) { "Build metadata must contain only alphanumeric characters, hyphens, and dots." }
        }
    }

    companion object {
        private val VERSION_REGEX =
            """^(\d+)\.(\d+)\.?(\d+)?(?:-([0-9A-Za-z.-]+))?(?:\+([0-9A-Za-z.-]+))?$""".toRegex()

        /**
         * Parses a version string into a SemanticVersion object.
         * Throws IllegalArgumentException if the string is not a valid SemVer format.
         */
        fun parse(versionString: String): SemanticVersion {
            val matchResult = VERSION_REGEX.matchEntire(versionString)
                ?: throw IllegalArgumentException("Invalid SemVer format: $versionString")

            val (majorStr, minorStr, patchStr, preReleaseStr, buildMetadata) = matchResult.destructured

            val major = majorStr.toInt()
            val minor = minorStr.toInt()
            val patch = patchStr.takeIf { it.isNotBlank() }?.toInt()

            val preRelease = preReleaseStr.takeIf { it.isNotBlank() }?.split('.') ?: emptyList()

            return SemanticVersion(
                major,
                minor,
                patch,
                preRelease,
                buildMetadata.takeIf { it.isNotBlank() && it != "0" })
        }
    }

    // 实现 Comparable 接口以进行版本比较
    override fun compareTo(other: SemanticVersion): Int {
        // 1. MAJOR 比较
        if (major != other.major) return major.compareTo(other.major)
        // 2. MINOR 比较
        if (minor != other.minor) return minor.compareTo(other.minor)
        // 3. PATCH 比较
        if (patch != other.patch)
            if (patch != null) {
                return if (other.patch != null) patch.compareTo(other.patch) else 1
            }

        // 4. 预发布版本比较
        // 如果两者都有预发布版本
        if (preRelease.isNotEmpty() && other.preRelease.isNotEmpty()) {
            val minSize = minOf(preRelease.size, other.preRelease.size)
            for (i in 0 until minSize) {
                val id1 = preRelease[i]
                val id2 = other.preRelease[i]

                val isId1Numeric = id1.all { it.isDigit() }
                val isId2Numeric = id2.all { it.isDigit() }

                // 数字标识符比字母数字标识符小
                if (isId1Numeric && !isId2Numeric) return -1
                if (!isId1Numeric && isId2Numeric) return 1

                // 如果都是数字标识符
                if (isId1Numeric && isId2Numeric) {
                    val num1 = id1.toInt()
                    val num2 = id2.toInt()
                    if (num1 != num2) return num1.compareTo(num2)
                } else { // 都是字母数字标识符
                    val stringCompare = id1.compareTo(id2)
                    if (stringCompare != 0) return stringCompare
                }
            }
            // 如果一个预发布版本有更多标识符，它更大
            return preRelease.size.compareTo(other.preRelease.size)
        }

        // 5. 预发布版本与正式版本比较 (没有预发布版本的版本比有预发布版本的版本大)
        if (preRelease.isEmpty() && other.preRelease.isNotEmpty()) return 1
        if (preRelease.isNotEmpty() && other.preRelease.isEmpty()) return -1

        // 6. 构建元数据在比较时忽略，所以到这里就是相等
        return 0
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("$major.$minor")
        if (patch != null) {
            sb.append(".$patch")
        }
        if (preRelease.isNotEmpty()) {
            sb.append("-").append(preRelease.joinToString("."))
        }
        if (buildMetadata != null) {
            sb.append("+").append(buildMetadata)
        }
        return sb.toString()
    }

    fun toReadableString(showPreRelease: Boolean = false, showBuildMetadata: Boolean = false): String {
        val sb = StringBuilder()
        sb.append("$major.$minor")
        if (patch != null) {
            sb.append(".$patch")
        }
        if (preRelease.isNotEmpty() && showPreRelease) {
            sb.append(" Pre $preRelease")
        }
        if (buildMetadata != null && showBuildMetadata) {
            sb.append(" Build $buildMetadata")
        }
        return sb.toString()
    }
}