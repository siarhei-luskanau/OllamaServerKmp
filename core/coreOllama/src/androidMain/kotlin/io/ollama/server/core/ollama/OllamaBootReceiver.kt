package io.ollama.server.core.ollama

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OllamaBootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICK_BOOT_COMPLETED") {
            context.startForegroundService(Intent(context, OllamaForegroundService::class.java))
        }
    }
}
