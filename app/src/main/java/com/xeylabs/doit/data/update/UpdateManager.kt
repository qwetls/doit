package com.xeylabs.doit.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.xeylabs.doit.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class UpdateInfo(
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkFileName: String,
    val expectedSha256: String?,
    val releasePageUrl: String
)

class UpdateException(message: String) : Exception(message)

class UpdateManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "DoitUpdateManager"
        private const val PREFS_NAME = "doit_update_prefs"
        private const val KEY_LAST_NOTIFIED_VERSION = "last_notified_version"
        private const val LATEST_RELEASE_URL = "https://api.github.com/repos/qwetls/doit/releases/latest"
        private const val USER_AGENT = "doit-Android-App"
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 30_000

        @Volatile
        private var instance: UpdateManager? = null

        @Volatile
        var hasCheckedThisSession = false

        fun getInstance(context: Context): UpdateManager {
            return instance ?: synchronized(this) {
                instance ?: UpdateManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private fun httpGetText(url: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", USER_AGENT)
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "GET $url returned ${connection.responseCode}")
                return null
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.w(TAG, "GET $url failed: ${e.message}")
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun fetchExpectedSha256(assets: org.json.JSONArray, apkFileName: String): String? {
        val checksumName = "$apkFileName.sha256"
        var checksumUrl: String? = null
        for (i in 0 until assets.length()) {
            val asset = assets.optJSONObject(i) ?: continue
            if (asset.optString("name", "") == checksumName) {
                checksumUrl = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
                break
            }
        }
        val body = checksumUrl?.let { httpGetText(it) } ?: return null
        val token = body.trim().split("\\s+".toRegex()).firstOrNull() ?: return null
        return if (token.matches(Regex("[0-9a-fA-F]{64}"))) token.lowercase() else null
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(currParts.size, latestParts.size)
        for (i in 0 until maxLen) {
            val currVal = currParts.getOrElse(i) { 0 }
            val latestVal = latestParts.getOrElse(i) { 0 }
            if (latestVal > currVal) return true
            if (latestVal < currVal) return false
        }
        return false
    }

    private fun cleanReleaseNotes(raw: String): String {
        return raw.lines().take(6).joinToString("\n").trim()
    }

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        if (hasCheckedThisSession) return@withContext null
        hasCheckedThisSession = true
        try {
            val body = httpGetText(LATEST_RELEASE_URL) ?: return@withContext null
            val json = JSONObject(body)
            val tagName = json.optString("tag_name", "").removePrefix("v").trim()
            if (tagName.isBlank()) return@withContext null
            if (!isNewerVersion(BuildConfig.VERSION_NAME, tagName)) return@withContext null

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (prefs.getString(KEY_LAST_NOTIFIED_VERSION, null) == tagName) return@withContext null

            val assets = json.optJSONArray("assets") ?: return@withContext null
            var downloadUrl: String? = null
            var fileName: String? = null
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name", "")
                if (name.endsWith("-release.apk", ignoreCase = true)) {
                    downloadUrl = asset.optString("browser_download_url")
                    fileName = name
                    break
                }
            }
            if (downloadUrl.isNullOrBlank() || fileName.isNullOrBlank()) return@withContext null

            val info = UpdateInfo(
                versionName = tagName,
                releaseNotes = cleanReleaseNotes(json.optString("body", "")),
                downloadUrl = downloadUrl,
                apkFileName = fileName,
                expectedSha256 = fetchExpectedSha256(assets, fileName),
                releasePageUrl = "https://github.com/qwetls/doit/releases/tag/v$tagName"
            )
            prefs.edit().putString(KEY_LAST_NOTIFIED_VERSION, tagName).apply()
            info
        } catch (e: Exception) {
            Log.w(TAG, "Update check failed: ${e.message}")
            null
        }
    }

    suspend fun downloadUpdate(info: UpdateInfo, onProgress: (Float) -> Unit): File =
        withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "updates").apply { if (!exists()) mkdirs() }
            val destination = File(dir, info.apkFileName)
            if (destination.exists()) destination.delete()

            var connection: HttpURLConnection? = null
            try {
                connection = (URL(info.downloadUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", USER_AGENT)
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                }
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw UpdateException("Unduhan gagal (server menjawab ${connection.responseCode}).")
                }
                val total = connection.contentLengthLong
                var downloaded = 0L
                connection.inputStream.use { input ->
                    FileOutputStream(destination).use { output ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloaded += read
                            if (total > 0) onProgress(downloaded.toFloat() / total.toFloat())
                        }
                    }
                }
            } catch (e: UpdateException) {
                destination.delete()
                throw e
            } catch (e: Exception) {
                destination.delete()
                throw UpdateException("Unduhan gagal: ${e.message ?: "tidak ada koneksi"}.")
            } finally {
                connection?.disconnect()
            }

            if (!destination.exists() || destination.length() <= 0L) {
                destination.delete()
                throw UpdateException("File unduhan kosong atau rusak.")
            }
            val actualHash = computeSha256Hex(destination)
            val expectedHash = info.expectedSha256
            if (expectedHash.isNullOrBlank() || actualHash == null ||
                !actualHash.equals(expectedHash, ignoreCase = true)
            ) {
                destination.delete()
                throw UpdateException(
                    "Verifikasi gagal: sidik digital APK tidak cocok. File dihapus dan tidak diinstal."
                )
            }
            destination
        }

    private fun computeSha256Hex(file: File): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().buffered().use { stream ->
                val buffer = ByteArray(8192)
                var read: Int
                while (stream.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to hash downloaded update: ${e.message}")
            null
        }
    }

    fun hasInstallPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.packageManager.canRequestPackageInstalls()
            } catch (_: Exception) {
                false
            }
        } else {
            true
        }
    }

    fun installApk(file: File) {
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openUnknownSourcesSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openReleasePage(url: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Unable to open release page: ${e.message}")
        }
    }
}
