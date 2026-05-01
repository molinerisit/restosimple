package ar.ticketsimple.pos.infrastructure.backup

import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BackupService {
    private val log = LoggerFactory.getLogger(javaClass)
    private val fmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    fun backup(dataDir: String = "data", backupDir: String = "data/backups"): String? {
        return try {
            val src = File("$dataDir/ticketsimple.db")
            if (!src.exists()) { log.warn("DB no encontrada para backup"); return null }
            File(backupDir).mkdirs()
            val dst = File("$backupDir/ticketsimple_${LocalDateTime.now().format(fmt)}.db")
            src.copyTo(dst, overwrite = false)
            log.info("Backup creado: ${dst.name}")
            dst.absolutePath
        } catch (e: Exception) {
            log.error("Error al crear backup: ${e.message}")
            null
        }
    }

    fun listBackups(backupDir: String = "data/backups"): List<BackupEntry> {
        val dir = File(backupDir)
        if (!dir.exists()) return emptyList()
        return dir.listFiles { f -> f.extension == "db" }
            ?.sortedByDescending { it.lastModified() }
            ?.map { BackupEntry(it.name, it.length(), it.lastModified()) }
            ?: emptyList()
    }
}

data class BackupEntry(val name: String, val sizeBytes: Long, val timestampMs: Long) {
    val sizeKb: Long get() = sizeBytes / 1024
}
