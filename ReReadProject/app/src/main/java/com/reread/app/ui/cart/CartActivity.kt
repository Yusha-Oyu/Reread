package com.reread.app.ui.cart

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reread.app.R
import com.reread.app.data.CartManager
import com.reread.app.ui.checkout.CheckoutActivity

class CartActivity : AppCompatActivity() {

    private lateinit var adapter: CartAdapter
    private lateinit var tvTotal: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnCheckout: Button

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

        setContentView(R.layout.activity_cart)

        val toolbarColor = if (isDarkMode()) Color.parseColor("#1A1A1A") else Color.WHITE
        val textColor    = if (isDarkMode()) Color.WHITE else Color.parseColor("#333333")
        val iconColor    = if (isDarkMode()) Color.WHITE else Color.parseColor("#1A1A1A")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(toolbarColor))
        toolbar.setTitleTextColor(textColor)
        toolbar.navigationIcon?.setTint(iconColor)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        tvTotal      = findViewById(R.id.tv_total)
        tvEmpty      = findViewById(R.id.tv_empty)
        recyclerView = findViewById(R.id.rv_cart)
        btnCheckout  = findViewById(R.id.btn_checkout)

        adapter = CartAdapter { item ->
            CartManager.removeItem(item.bookId)
            refreshCart()
            Toast.makeText(this, "${item.title} removed", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnCheckout.setOnClickListener {
            if (CartManager.count() == 0) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this, CheckoutActivity::class.java))
            }
        }

        refreshCart()
    }

    private fun refreshCart() {
        val items = CartManager.getItems()
        adapter.submitList(items)
        tvTotal.text = "\$${String.format("%.2f", CartManager.getTotal())}"
        tvEmpty.visibility      = if (items.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        btnCheckout.isEnabled   = items.isNotEmpty()
    }
}