package services.file

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

interface IFileIO {
    fun getFile(filePath: String): File
    fun read(filePath: String, trimBOM: Boolean = false): String
    fun read(file: File): String
    fun write(filePath: String, content: String)
    fun write(file: File, content: String)
    fun mkdir(dir: String): Boolean
    fun makefile(filePath: String)
    fun exists(filePath: String): Boolean
    fun getOutputStream(filePath: String): FileOutputStream
    fun getInputStream(path: String): FileInputStream
}