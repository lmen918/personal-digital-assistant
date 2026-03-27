package com.lmen918.pda.journal

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

object JournalStorage {
    private const val DIRECTORY_NAME = "retrospective"
    private const val MARKER_FILE_NAME = ".retrospective_init"

    fun ensureDefaultDirectory(context: Context) {
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

    fun saveJournal(context: Context, fileName: String, content: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(context, fileName, content)
        } else {
            saveWithLegacyStorage(context, fileName, content)
        }
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
            "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND " +
                "${MediaStore.MediaColumns.RELATIVE_PATH}=?"
            )
        val args = arrayOf(MARKER_FILE_NAME, relativePath())

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

    private fun relativePath(): String = "${Environment.DIRECTORY_DOCUMENTS}/$DIRECTORY_NAME/"
}

