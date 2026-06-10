package com.reread.app.ui.messaging

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reread.app.R
import com.reread.app.data.MessagingRepository
import com.reread.app.utils.NotificationHelper
import com.reread.app.utils.SessionManager

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var repo: MessagingRepository
    private lateinit var session: SessionManager
    private var conversationId: Int = -1
    private var bookTitle: String = ""
    private var otherUsername: String = ""

    private fun isDarkMode(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isDarkMode()) {
            window.statusBarColor = Color.parseColor("#1A1A1A")
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = 0
        } else {
            window.statusBarColor = Color.WHITE
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        setContentView(R.layout.activity_chat)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        conversationId = intent.getIntExtra("conversation_id", -1)
        bookTitle      = intent.getStringExtra("book_title") ?: ""
        otherUsername  = intent.getStringExtra("other_username") ?: ""

        val toolbarColor = if (isDarkMode()) Color.parseColor("#1A1A1A") else Color.WHITE
        val textColor    = if (isDarkMode()) Color.WHITE else Color.parseColor("#333333")
        val iconColor    = if (isDarkMode()) Color.WHITE else Color.parseColor("#1A1A1A")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title    = otherUsername
        supportActionBar?.subtitle = bookTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(toolbarColor))
        toolbar.setTitleTextColor(textColor)
        toolbar.setSubtitleTextColor(Color.parseColor("#888888"))
        toolbar.navigationIcon?.setTint(iconColor)
        toolbar.setNavigationOnClickListener { finish() }

        repo    = MessagingRepository(this)
        session = SessionManager(this)

        recyclerView = findViewById(R.id.rv_messages)
        etMessage    = findViewById(R.id.et_message)
        btnSend      = findViewById(R.id.btn_send)

        adapter = MessageAdapter(emptyList(), session.userId)
        recyclerView.layoutManager = LinearLayoutManager(this).also {
            it.stackFromEnd = true
        }
        recyclerView.adapter = adapter

        loadMessages()

        btnSend.setOnClickListener {
            val content = etMessage.text.toString().trim()
            if (content.isBlank()) return@setOnClickListener
            if (conversationId == -1) {
                Toast.makeText(this, "Invalid conversation", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            repo.sendMessage(conversationId, session.userId, session.username, content)
            etMessage.setText("")
            loadMessages()

            NotificationHelper.showMessageNotification(
                context        = this,
                senderUsername = session.username,
                messageContent = content,
                conversationId = conversationId,
                bookTitle      = bookTitle,
                otherUsername  = otherUsername
            )
        }
    }

    private fun loadMessages() {
        // Mark messages as read when chat is opened
        repo.markMessagesAsRead(conversationId, session.userId)
        val messages = repo.getMessages(conversationId)
        adapter.updateMessages(messages)
        if (adapter.itemCount > 0) {
            recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }
}