package com.example.data.repository

import android.content.Context
import android.content.Intent
import com.example.model.SmsThread
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupHelper {

    fun exportToJson(threads: List<SmsThread>): String {
        val root = JSONObject()
        root.put("app", "Pulse Messenger")
        root.put("version", "1.2")
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
        root.put("totalThreads", threads.size)

        val array = JSONArray()
        threads.forEach { thread ->
            val item = JSONObject()
            item.put("threadId", thread.threadId)
            item.put("contactName", thread.displayName)
            item.put("address", thread.address)
            item.put("latestSnippet", thread.snippet)
            item.put("date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(thread.date)))
            item.put("messageCount", thread.messageCount)
            item.put("category", thread.category.name)
            item.put("isPinned", thread.isPinned)
            item.put("isArchived", thread.isArchived)
            array.put(item)
        }
        root.put("conversations", array)

        return root.toString(2)
    }

    fun shareBackup(context: Context, jsonData: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, jsonData)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "Pulse_Messenger_Backup.json")
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Pulse Conversations Backup")
        context.startActivity(shareIntent)
    }
}
