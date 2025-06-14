package services.defaults

import com.sun.jna.platform.win32.VerRsrc
import com.sun.jna.platform.win32.Version
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import models.common.SemanticVersion
import java.io.File


object WindowsDLLChecker {
    fun getDllVersion(dllPath: String): SemanticVersion? {
        require(File(dllPath).exists()) { "DLL file not found" }

        // 1. 获取版本信息大小
        val dummy = IntByReference(0)
        val versionInfoSize = Version.INSTANCE.GetFileVersionInfoSize(dllPath, dummy)
        if (versionInfoSize == 0L.toInt()) return null

        // 2. 分配缓冲区并获取版本信息
        val buffer = com.sun.jna.Memory(versionInfoSize.toLong())
        if (!Version.INSTANCE.GetFileVersionInfo(dllPath, 0, versionInfoSize, buffer)) {
            return null
        }

        // 3. 查询固定版本信息
        val pvi = PointerByReference()
        val len = IntByReference()
        if (!Version.INSTANCE.VerQueryValue(buffer, "\\", pvi, len)) {
            return null
        }

        // 4. 读取版本结构
        val fixedFileInfo = VerRsrc.VS_FIXEDFILEINFO(pvi.value)
        fixedFileInfo.read()

        // 5. 提取四部分版本号
        val major = fixedFileInfo.dwFileVersionMS.toInt() shr 16 and 0xFFFF
        val minor = fixedFileInfo.dwFileVersionMS.toInt() and 0xFFFF
        val build = fixedFileInfo.dwFileVersionLS.toInt() shr 16 and 0xFFFF
        val revision = fixedFileInfo.dwFileVersionLS.toInt() and 0xFFFF

        return SemanticVersion(major, minor, build, buildMetadata = "$revision")
    }

    fun getDllVersionWithPowerShell(dllPath: String): String? {
        val file = File(dllPath)
        if (!file.exists()) return null

        val command = """
            [System.Diagnostics.FileVersionInfo]::GetVersionInfo(
                '$dllPath'
            ).FileVersion
        """.trimIndent()

        return try {
            val process = ProcessBuilder("powershell", "-Command", command)
                .redirectErrorStream(true)
                .start()

            process.inputStream.bufferedReader().use { reader ->
                reader.readLine()?.trim()?.takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            null
        }
    }
}