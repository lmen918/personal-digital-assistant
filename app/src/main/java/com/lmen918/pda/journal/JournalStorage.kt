package com.lmen918.pda.journal

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import android.content.ContentUris

data class JournalEntry(
    val name: String,
    val content: String,
    val modifiedAtEpochSeconds: Long
)

object JournalStorage {
    private const val DIRECTORY_NAME = "retrospective"
    private const val MARKER_FILE_NAME = ".retrospective_init"
    private const val CLEANUP_PREFS = "journal_storage_migrations"
    private const val KEY_MARKER_CLEANUP_DONE = "marker_cleanup_done_v1"

    fun ensureDefaultDirectory(context: Context) {
        runMarkerCleanupIfNeeded(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (markerExists(context)) return
            runCatching {
                saveWithMediaStore(context, MARKER_FILE_NAME, "")
            }
            return
        }

        runCatching {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                DIRECTORY_NAME
            ).mkdirs()
        }
    }

    private fun runMarkerCleanupIfNeeded(context: Context) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cleanupMediaStoreMarkerFiles(context)
            } else {
                cleanupLegacyMarkerFiles(context)
            }
        }
    }

    fun saveJournal(context: Context, fileName: String, content: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(context, fileName, content)
        } else {
            saveWithLegacyStorage(context, fileName, content)
        }
    }

    fun listJournalEntries(context: Context): List<JournalEntry> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listWithMediaStore(context)
        } else {
            listWithLegacyStorage(context)
        }
    }

    fun deleteJournal(context: Context, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deleteWithMediaStore(context, fileName)
        } else {
            deleteWithLegacyStorage(context, fileName)
        }
    }

    fun updateJournal(context: Context, fileName: String, newContent: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            updateWithMediaStore(context, fileName, newContent)
        } else {
            updateWithLegacyStorage(context, fileName, newContent)
        }
    }

    private fun deleteWithMediaStore(context: Context, fileName: String) {
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
        val args = arrayOf(fileName, relativePath())
        resolver.delete(collection, selection, args)
    }

    private fun deleteWithLegacyStorage(context: Context, fileName: String) {
        val publicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            DIRECTORY_NAME
        )
        val appDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DIRECTORY_NAME)
        listOf(publicDir, appDir).forEach { dir -> File(dir, fileName).takeIf { it.exists() }?.delete() }
    }

    private fun updateWithMediaStore(context: Context, fileName: String, newContent: String) {
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
        val args = arrayOf(fileName, relativePath())

        resolver.query(collection, projection, selection, args, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                val uri = ContentUris.withAppendedId(collection, id)

                val pendingOn = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 1) }
                resolver.update(uri, pendingOn, null, null)

                resolver.openOutputStream(uri, "wt")?.use { it.write(newContent.toByteArray()) }

                val pendingOff = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
                resolver.update(uri, pendingOff, null, null)
            }
        }
    }

    private fun updateWithLegacyStorage(context: Context, fileName: String, newContent: String) {
        val publicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            DIRECTORY_NAME
        )
        val appDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DIRECTORY_NAME)
        val file = listOf(publicDir, appDir).map { File(it, fileName) }.firstOrNull { it.exists() }
        file?.writeText(newContent)
    }

    private fun saveWithMediaStore(context: Context, fileName: String, content: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/markdown")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath())
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri("external")
        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("Failed to create journal file")

        resolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray())
        } ?: throw IllegalStateException("Failed to open journal output stream")

        val pendingOff = ContentValues().apply {
            put(MediaStore.MediaColumns.IS_PENDING, 0)
        }
        resolver.update(uri, pendingOff, null, null)

        return uri
    }

    private fun saveWithLegacyStorage(context: Context, fileName: String, content: String): Uri {
        val publicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            DIRECTORY_NAME
        )

        val targetDir = if (publicDir.exists() || publicDir.mkdirs()) {
            publicDir
        } else {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DIRECTORY_NAME).apply {
                mkdirs()
            }
        }

        val file = File(targetDir, fileName)
        file.writeText(content)
        return Uri.fromFile(file)
    }

    private fun markerExists(context: Context): Boolean {
        val resolver = context.contentResolver
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = (
            "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ? AND " +
                "${MediaStore.MediaColumns.RELATIVE_PATH}=?"
            )
        val args = arrayOf("$MARKER_FILE_NAME%", relativePath())

        resolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            args,
            null
        )?.use { cursor ->
            return cursor.moveToFirst()
        }

        return false
    }

    private fun listWithMediaStore(context: Context): List<JournalEntry> {
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.SIZE
        )
        val selection = (
            "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND " +
                "${MediaStore.MediaColumns.DISPLAY_NAME} NOT LIKE ?"
            )
        val args = arrayOf(relativePath(), "$MARKER_FILE_NAME%")

        val entries = mutableListOf<JournalEntry>()
        val markerUrisToDelete = mutableListOf<Uri>()
        
        resolver.query(
            collection,
            projection,
            selection,
            args,
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val modifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex) ?: continue
                if (isMarkerName(name)) {
                    markerUrisToDelete.add(ContentUris.withAppendedId(collection, id))
                    continue
                }
                val size = cursor.getLong(sizeIndex)
                if (size == 0L) {
                    // Skip empty files
                    markerUrisToDelete.add(ContentUris.withAppendedId(collection, id))
                    continue
                }
                val modifiedAt = cursor.getLong(modifiedIndex)
                val uri = ContentUris.withAppendedId(collection, id)
                val content = resolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                entries.add(
                    JournalEntry(
                        name = name,
                        content = content,
                        modifiedAtEpochSeconds = modifiedAt
                    )
                )
            }
        }

        // Clean up any marker files or empty files found
        markerUrisToDelete.forEach { uri ->
            runCatching { resolver.delete(uri, null, null) }
        }

        return entries
    }

    private fun listWithLegacyStorage(context: Context): List<JournalEntry> {
        val publicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            DIRECTORY_NAME
        )
        val appDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            DIRECTORY_NAME
        )

        val files = mutableMapOf<String, File>()
        listOf(publicDir, appDir)
            .filter { it.exists() && it.isDirectory }
            .forEach { dir ->
                dir.listFiles()
                    ?.forEach { file ->
                        if (file.isFile) {
                            if (isMarkerName(file.name) || file.length() == 0L) {
                                runCatching { file.delete() }
                            } else {
                                files[file.absolutePath] = file
                            }
                        }
                    }
            }

        return files.values
            .sortedByDescending { it.lastModified() }
            .map { file ->
                JournalEntry(
                    name = file.name,
                    content = runCatching { file.readText() }.getOrDefault(""),
                    modifiedAtEpochSeconds = file.lastModified() / 1000
                )
            }
    }

    private fun cleanupMediaStoreMarkerFiles(context: Context) {
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = (
            "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND " +
                "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
            )
        val args = arrayOf(relativePath(), "$MARKER_FILE_NAME%")

        val urisToDelete = mutableListOf<Uri>()
        resolver.query(collection, projection, selection, args, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                urisToDelete.add(ContentUris.withAppendedId(collection, id))
            }
        }

        urisToDelete.forEach { uri ->
            runCatching { resolver.delete(uri, null, null) }
        }
    }

    private fun cleanupLegacyMarkerFiles(context: Context) {
        val publicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            DIRECTORY_NAME
        )
        val appDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            DIRECTORY_NAME
        )

        listOf(publicDir, appDir)
            .filter { it.exists() && it.isDirectory }
            .forEach { dir ->
                dir.listFiles()
                    ?.filter { it.isFile && isMarkerName(it.name) }
                    ?.forEach { file -> runCatching { file.delete() } }
            }
    }

    private fun isMarkerName(name: String): Boolean =
        name.contains("retrospective_init") || name.startsWith(MARKER_FILE_NAME)

    private fun relativePath(): String = "${Environment.DIRECTORY_DOCUMENTS}/$DIRECTORY_NAME/"
}

