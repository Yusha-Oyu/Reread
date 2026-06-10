package com.reread.app.ui.admin

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
import com.reread.app.data.AdminRepository
import com.reread.app.ui.account.AccountBottomSheet
import com.reread.app.ui.messaging.InboxActivity
import com.reread.app.utils.SessionManager

class AdminActivity : AppCompatActivity() {

    private lateinit var repo: AdminRepository
    private lateinit var session: SessionManager
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var tabUsers: TextView
    private lateinit var tabListings: TextView
    private lateinit var rvUsers: RecyclerView
    private lateinit var rvListings: RecyclerView
    private lateinit var tvStatUsers: TextView
    private lateinit var tvStatListings: TextView
    private lateinit var tvStatOrders: TextView

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

        setContentView(R.layout.activity_admin)

        val toolbarColor = if (isDarkMode()) Color.parseColor("#1A1A1A") else Color.WHITE
        val textColor    = if (isDarkMode()) Color.WHITE else Color.parseColor("#333333")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Admin Panel"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(toolbarColor))
        toolbar.setTitleTextColor(textColor)

        session        = SessionManager(this)
        repo           = AdminRepository(this)
        tabUsers       = findViewById(R.id.tab_users)
        tabListings    = findViewById(R.id.tab_listings)
        rvUsers        = findViewById(R.id.rv_users)
        rvListings     = findViewById(R.id.rv_listings)
        tvStatUsers    = findViewById(R.id.tv_stat_users)
        tvStatListings = findViewById(R.id.tv_stat_listings)
        tvStatOrders   = findViewById(R.id.tv_stat_orders)
        bottomNav      = findViewById(R.id.bottom_navigation)

        // Remove home button from nav
        bottomNav.menu.removeItem(R.id.nav_home)

        loadStats()
        setupUsersTab()
        setupListingsTab()
        showTab("users")

        tabUsers.setOnClickListener { showTab("users") }
        tabListings.setOnClickListener { showTab("listings") }

        setupBottomNav()
    }

    private fun loadStats() {
        tvStatUsers.text    = repo.getTotalUsers().toString()
        tvStatListings.text = repo.getTotalListings().toString()
        tvStatOrders.text   = repo.getTotalOrders().toString()
    }

    private fun setupUsersTab() {
        val adapter = AdminUsersAdapter(
            users = repo.getAllUsers().toMutableList(),
            onToggleActive = { user ->
                repo.setUserActive(user.id, !user.isActive)
                setupUsersTab()
                loadStats()
            },
            onDelete = { user ->
                repo.deleteUser(user.id)
                setupUsersTab()
                loadStats()
            }
        )
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter
    }

    private fun setupListingsTab() {
        val adapter = AdminListingsAdapter(
            listings = repo.getAllListings().toMutableList(),
            onEdit = { book ->
                val intent = Intent(this, com.reread.app.ui.listings.AddListingActivity::class.java)
                intent.putExtra("edit_book_id",     book.id)
                intent.putExtra("edit_title",       book.title)
                intent.putExtra("edit_author",      book.author)
                intent.putExtra("edit_price",       book.price)
                intent.putExtra("edit_condition",   book.condition)
                intent.putExtra("edit_category",    book.category)
                intent.putExtra("edit_description", book.description)
                intent.putExtra("edit_image",       book.imagePath)
                startActivity(intent)
            },
            onDelete = { book ->
                repo.deleteListing(book.id)
                setupListingsTab()
                loadStats()
            }
        )
        rvListings.layoutManager = LinearLayoutManager(this)
        rvListings.adapter = adapter
    }

    private fun showTab(tab: String) {
        val isUsers = tab == "users"
        rvUsers.visibility    = if (isUsers) View.VISIBLE else View.GONE
        rvListings.visibility = if (isUsers) View.GONE else View.VISIBLE
        tabUsers.setBackgroundResource(
            if (isUsers) R.drawable.chip_selected else R.drawable.chip_unselected
        )
        tabUsers.setTextColor(
            if (isUsers) getColor(R.color.chip_text_selected) else getColor(R.color.chip_text_unselected)
        )
        tabListings.setBackgroundResource(
            if (isUsers) R.drawable.chip_unselected else R.drawable.chip_selected
        )
        tabListings.setTextColor(
            if (isUsers) getColor(R.color.chip_text_unselected) else getColor(R.color.chip_text_selected)
        )
    }

    private fun setupBottomNav() {
        val menu = bottomNav.menu
        menu.findItem(R.id.nav_listings).title = "Admin Panel"
        menu.findItem(R.id.nav_listings).icon =
            ContextCompat.getDrawable(this, R.drawable.ic_admin)

        bottomNav.selectedItemId = R.id.nav_listings
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inbox -> {
                    val intent = Intent(this, InboxActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    false
                }
                R.id.nav_listings -> true
                R.id.nav_account -> {
                    AccountBottomSheet().show(supportFragmentManager, "account")
                    false
                }
                else -> false
            }
        }
    }
}