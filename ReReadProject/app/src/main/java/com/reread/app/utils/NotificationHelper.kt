package com.reread.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.reread.app.R

object NotificationHelper {

    private const val CHANNEL_ID   = "reread_messages"
    private const val CHANNEL_NAME = "Messages"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New message notifications"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showMessageNotification(
        context: Context,
        senderUsername: String,
        messageContent: String,
        conversationId: Int,
        bookTitle: String,
        otherUsername: String
    ) {
        val intent = Intent(context, com.reread.app.ui.messaging.ChatActivity::class.java).apply {
            putExtra("conversation_id", conversationId)
            putExtra("book_title",      bookTitle)
            putExtra("other_username",  otherUsername)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            conversationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_inbox)
            .setContentTitle("New message from $senderUsername")
            .setContentText(messageContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(conversationId, notification)
    }
}