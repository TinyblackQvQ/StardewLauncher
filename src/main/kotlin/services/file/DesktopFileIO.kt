package services.file

import services.logger.AppLogger
import java.io.File
import java.io.FileOutputStream

class DesktopFileIO : IFileIO {
    override fun getFile(filePath: String): File = File(filePath)
    override fun read(filePath: String, trimBOM: Boolean): String {
        val text = File(filePath).readText(charset = Charsets.UTF_8)
        return if (trimBOM) text.trimStart('\uFEFF') else text
    }
    override fun read(file: File): String = file.readText()
    override fun getInputStream(path: String) = File(path).inputStream()
    override fun write(filePath: String, content: String) = write(File(filePath), content)
    override fun write(file: File, content: String) {
        try {
            val parentDir = file.parentFile
            if (parentDir != null && !parentDir.exists())
                parentDir.mkdirs()
            file.writeText(content)
        } catch (e: Exception) {
            AppLogger.error("When trying to write content to file ${file.name}, an exception occurred.")
            e.printStackTrace()
            throw e
        }
    }

    override fun exists(filePath: String): Boolean = File(filePath).exists()
    override fun getOutputStream(filePath: String): FileOutputStream {
        val file = File(filePath)
        val parentDir = file.parentFile
        if (parentDir != null && !parentDir.exists())
            parentDir.mkdirs()
        return file.outputStream()
    }
    override fun mkdir(dir: String): Boolean {
        val file = File(dir)
        return file.mkdirs()
    }
    override fun makefile(filePath: String) = write(filePath, "")
}