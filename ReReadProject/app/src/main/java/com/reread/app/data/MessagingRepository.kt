package com.reread.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class MessagingRepository(context: Context) {

    private val db = DatabaseHelper(context).writableDatabase

    fun getOrCreateConversation(
        bookId: Int, bookTitle: String,
        buyerId: Int, buyerUsername: String,
        sellerId: Int, sellerUsername: String
    ): Int {
        val cursor = db.rawQuery(
            "SELECT id FROM conversations WHERE book_id = ? AND buyer_id = ? AND seller_id = ?",
            arrayOf(bookId.toString(), buyerId.toString(), sellerId.toString())
        )
        val existing = cursor.use {
            if (it.moveToFirst()) it.getInt(0) else -1
        }
        if (existing != -1) return existing

        val values = ContentValues().apply {
            put("book_id",          bookId)
            put("book_title",       bookTitle)
            put("buyer_id",         buyerId)
            put("buyer_username",   buyerUsername)
            put("seller_id",        sellerId)
            put("seller_username",  sellerUsername)
            put("last_message",     "")
            put("last_message_at",  System.currentTimeMillis())
        }
        return db.insert("conversations", null, values).toInt()
    }

    fun getConversationsForUser(userId: Int): List<Conversation> {
        val cursor = db.rawQuery(
            """SELECT * FROM conversations
               WHERE buyer_id = ? OR seller_id = ?
               ORDER BY last_message_at DESC""",
            arrayOf(userId.toString(), userId.toString())
        )
        return cursor.use { it.toConversationList() }
    }

    fun sendMessage(conversationId: Int, senderId: Int, senderUsername: String, content: String) {
        val now = System.currentTimeMillis()
        val values = ContentValues().apply {
            put("conversation_id", conversationId)
            put("sender_id",       senderId)
            put("sender_username", senderUsername)
            put("content",         content)
            put("sent_at",         now)
            put("is_read",         0)
        }
        db.insert("messages", null, values)

        val update = ContentValues().apply {
            put("last_message",    content)
            put("last_message_at", now)
        }
        db.update("conversations", update, "id = ?", arrayOf(conversationId.toString()))
    }

    fun getMessages(conversationId: Int): List<Message> {
        val cursor = db.rawQuery(
            "SELECT * FROM messages WHERE conversation_id = ? ORDER BY sent_at ASC",
            arrayOf(conversationId.toString())
        )
        return cursor.use { it.toMessageList() }
    }

    fun getUnreadCount(conversationId: Int, currentUserId: Int): Int {
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM messages WHERE conversation_id = ? AND sender_id != ? AND is_read = 0",
            arrayOf(conversationId.toString(), currentUserId.toString())
        )
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    fun markMessagesAsRead(conversationId: Int, currentUserId: Int) {
        val values = ContentValues().apply { put("is_read", 1) }
        db.update(
            "messages", values,
            "conversation_id = ? AND sender_id != ? AND is_read = 0",
            arrayOf(conversationId.toString(), currentUserId.toString())
        )
    }

    private fun Cursor.toConversationList(): List<Conversation> {
        val list = mutableListOf<Conversation>()
        while (moveToNext()) {
            list.add(
                Conversation(
                    id             = getInt(getColumnIndexOrThrow("id")),
                    bookId         = getInt(getColumnIndexOrThrow("book_id")),
                    bookTitle      = getString(getColumnIndexOrThrow("book_title")),
                    buyerId        = getInt(getColumnIndexOrThrow("buyer_id")),
                    buyerUsername  = getString(getColumnIndexOrThrow("buyer_username")),
                    sellerId       = getInt(getColumnIndexOrThrow("seller_id")),
                    sellerUsername = getString(getColumnIndexOrThrow("seller_username")),
                    lastMessage    = getString(getColumnIndexOrThrow("last_message")) ?: "",
                    lastMessageAt  = getInt(getColumnIndexOrThrow("last_message_at")).toString()
                )
            )
        }
        return list
    }

    private fun Cursor.toMessageList(): List<Message> {
        val list = mutableListOf<Message>()
        while (moveToNext()) {
            list.add(
                Message(
                    id             = getInt(getColumnIndexOrThrow("id")),
                    conversationId = getInt(getColumnIndexOrThrow("conversation_id")),
                    senderId       = getInt(getColumnIndexOrThrow("sender_id")),
                    senderUsername = getString(getColumnIndexOrThrow("sender_username")),
                    content        = getString(getColumnIndexOrThrow("content")),
                    createdAt      = getInt(getColumnIndexOrThrow("sent_at")).toString()
                )
            )
        }
        return list
    }
}