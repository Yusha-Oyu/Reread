package com.reread.app.ui.messaging

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.reread.app.R
import com.reread.app.data.MessagingRepository
import com.reread.app.ui.account.AccountBottomSheet
import com.reread.app.ui.admin.AdminActivity
import com.reread.app.ui.collection.CollectionBottomSheet
import com.reread.app.ui.home.HomeActivity
import com.reread.app.ui.listings.MyListingsBottomSheet
import com.reread.app.utils.SessionManager

class InboxActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var bottomNav: BottomNavigationView

    private fun isDarkMode(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

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

        setContentView(R.layout.activity_inbox)

        session = SessionManager(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Inbox"
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(if (isDarkMode()) Color.parseColor("#1A1A1A") else Color.WHITE)
        )
        toolbar.setTitleTextColor(
            if (isDarkMode()) Color.WHITE else Color.parseColor("#333333")
        )

        val repo          = MessagingRepository(this)
        val conversations = repo.getConversationsForUser(session.userId)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_conversations)
        val tvEmpty      = findViewById<TextView>(R.id.tv_empty)
        bottomNav        = findViewById(R.id.bottom_navigation)

        if (conversations.isEmpty()) {
            tvEmpty.visibility      = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility      = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = ConversationAdapter(conversations) { conversation ->
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("conversation_id", conversation.id)
                intent.putExtra("book_title",      conversation.bookTitle)
                intent.putExtra("other_username",
                    if (session.userId == conversation.buyerId)
                        conversation.sellerUsername
                    else
                        conversation.buyerUsername
                )
                startActivity(intent)
            }
        }

        setupBottomNav()
    }

    private fun setupBottomNav() {
        val role = session.role
        val menu = bottomNav.menu

        // Remove home button for admin
        if (role == "admin") {
            menu.removeItem(R.id.nav_home)
        }

        when (role) {
            "admin" -> {
                menu.findItem(R.id.nav_listings).title = "Admin Panel"
                menu.findItem(R.id.nav_listings).icon =
                    ContextCompat.getDrawable(this, R.drawable.ic_admin)
            }
            "buyer" -> {
                menu.findItem(R.id.nav_listings).title = "Collection"
                menu.findItem(R.id.nav_listings).icon =
                    ContextCompat.getDrawable(this, R.drawable.ic_collection)
            }
            else -> {
                menu.findItem(R.id.nav_listings).title = "My Listings"
                menu.findItem(R.id.nav_listings).icon =
                    ContextCompat.getDrawable(this, R.drawable.ic_listings)
            }
        }

        bottomNav.selectedItemId = R.id.nav_inbox
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_inbox -> true
                R.id.nav_listings -> {
                    when (role) {
                        "admin" -> {
                            val intent = Intent(this, AdminActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            startActivity(intent)
                            overridePendingTransition(0, 0)
                            finish()
                        }
                        "buyer" -> CollectionBottomSheet().show(supportFragmentManager, "collection")
                        else    -> MyListingsBottomSheet().show(supportFragmentManager, "listings")
                    }
                    false
                }
                R.id.nav_account -> {
                    AccountBottomSheet().show(supportFragmentManager, "account")
                    false
                }
                else -> false
            }
        }
    }
}