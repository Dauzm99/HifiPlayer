package com.example.hifiplayer.downloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.hifiplayer.data.MusicRepository
import com.example.hifiplayer.network.YTMusicService
import com.example.hifiplayer.network.YTTrack
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import android.content.pm.ServiceInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MusicRepository,
    private val ytMusicService: YTMusicService
) : CoroutineWorker(context, workerParams) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val videoId = inputData.getString("VIDEO_ID") ?: return@withContext Result.failure()
        val title = inputData.getString("TITLE") ?: "Unknown"
        val artist = inputData.getString("ARTIST") ?: "Unknown"
        val thumbnail = inputData.getString("THUMBNAIL")
        
        createNotificationChannel()
        setForeground(createForegroundInfo("Preparing $title..."))

        try {
            // STEP 1: Get Real Stream URL
            val streamUrl = ytMusicService.getStreamUrl(videoId) ?: return@withContext Result.failure()
            
            // STEP 2: Download
            setForeground(createForegroundInfo("Downloading $title..."))
            val tempFile = downloadToTempFile(streamUrl, title)

            // STEP 3: Structured Storage
            val artistDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "EchoStream/$artist")
            if (!artistDir.exists()) artistDir.mkdirs()
            
            // Note: YouTube streams are usually opus/m4a. 
            // We'll use .m4a as a safe container for metadata baking if it's AAC, or just .opus
            val extension = if (streamUrl.contains("audio/mp4")) "m4a" else "opus"
            val finalFile = File(artistDir, "$title.$extension")
            tempFile.renameTo(finalFile)

            // STEP 4: Tagging (Simplified, jaudiotagger supports many formats)
            tagFile(finalFile, title, artist)

            // STEP 5: Refresh
            repository.refreshLibrary()
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun downloadToTempFile(streamUrl: String, title: String): File {
        val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
        val url = URL(streamUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()

        BufferedInputStream(connection.inputStream).use { input ->
            FileOutputStream(tempFile).use { output ->
                val data = ByteArray(8192)
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    output.write(data, 0, count)
                }
            }
        }
        return tempFile
    }

    private fun tagFile(file: File, title: String, artist: String) {
        try {
            val audioFile = AudioFileIO.read(file)
            val tag: Tag = audioFile.tagOrCreateAndSetDefault
            tag.setField(FieldKey.TITLE, title)
            tag.setField(FieldKey.ARTIST, artist)
            audioFile.commit()
        } catch (e: Exception) {
            // Tagging might fail on some formats without specific headers, skip for now
        }
    }

    private fun createForegroundInfo(progressText: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("HiFiPlayer Downloader")
            .setContentText(progressText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()
            
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID = 101
    }

    data class TrackMetadata(
        val title: String,
        val artist: String,
        val album: String,
        val year: String,
        val genre: String,
        val isrc: String
    )
}
