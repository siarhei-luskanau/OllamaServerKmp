package io.ollama.server.core.ollama

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OllamaForegroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var ollamaProcess: Process? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        startOllamaProcess()
        return START_STICKY
    }

    private fun startOllamaProcess() {
        serviceScope.launch {
            try {
                val binary = prepareOllamaBinary()
                val modelsDir =
                    getExternalFilesDir(null)
                        ?.resolve("models/ollama")
                        ?: filesDir.resolve("models/ollama")
                modelsDir.mkdirs()

                ollamaProcess =
                    ProcessBuilder(binary.absolutePath, "serve")
                        .apply {
                            environment()["OLLAMA_HOST"] = "127.0.0.1:11434"
                            environment()["OLLAMA_MODELS"] = modelsDir.absolutePath
                            environment()["HOME"] = filesDir.absolutePath
                            redirectErrorStream(true)
                        }.start()

                ollamaProcess?.inputStream?.bufferedReader()?.forEachLine { line ->
                    Log.d(TAG, line)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start Ollama process", e)
                stopSelf()
            }
        }
    }

    private fun prepareOllamaBinary(): java.io.File {
        val binary = filesDir.resolve("ollama")
        if (!binary.exists()) {
            assets.open("arm64-v8a/ollama").use { input ->
                binary.outputStream().use { output -> input.copyTo(output) }
            }
        }
        binary.setExecutable(true)
        return binary
    }

    override fun onDestroy() {
        ollamaProcess?.destroyForcibly()
        ollamaProcess = null
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Ollama Server",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Ollama LLM server running" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        Notification
            .Builder(this, CHANNEL_ID)
            .setContentTitle("Ollama Server")
            .setContentText("Running on port 11434")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()

    companion object {
        private const val TAG = "OllamaForegroundService"
        private const val CHANNEL_ID = "ollama_server"
        private const val NOTIFICATION_ID = 1001
    }
}
