package com.reread.app.ui.home

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.reread.app.R
import com.reread.app.data.Book
import com.reread.app.data.CartManager
import com.reread.app.data.MessagingRepository
import com.reread.app.ui.cart.CartActivity
import com.reread.app.ui.messaging.ChatActivity
import com.reread.app.utils.SessionManager
import java.io.File

class BookDetailActivity : AppCompatActivity() {

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

        setContentView(R.layout.activity_book_detail)

        val toolbarColor = if (isDarkMode()) Color.parseColor("#1A1A1A") else Color.WHITE
        val textColor    = if (isDarkMode()) Color.WHITE else Color.parseColor("#333333")
        val iconColor    = if (isDarkMode()) Color.WHITE else Color.parseColor("#1A1A1A")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(toolbarColor))
        toolbar.setTitleTextColor(textColor)
        toolbar.navigationIcon?.setTint(iconColor)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val bookId    = intent.getIntExtra("book_id", 0)
        val title     = intent.getStringExtra("book_title") ?: ""
        val author    = intent.getStringExtra("book_author") ?: ""
        val price     = intent.getDoubleExtra("book_price", 0.0)
        val condition = intent.getStringExtra("book_condition") ?: ""
        val category  = intent.getStringExtra("book_category") ?: ""
        val desc      = intent.getStringExtra("book_description") ?: "No description provided."
        val seller    = intent.getStringExtra("book_seller") ?: ""
        val sellerId  = intent.getIntExtra("seller_id", -1)
        val bookImage = intent.getStringExtra("book_image") ?: ""

        findViewById<TextView>(R.id.tv_detail_title).text       = title
        findViewById<TextView>(R.id.tv_detail_author).text      = "by $author"
        findViewById<TextView>(R.id.tv_detail_price).text       = "\$${String.format("%.2f", price)}"
        findViewById<TextView>(R.id.tv_detail_condition).text   = condition
        findViewById<TextView>(R.id.tv_detail_category).text    = category
        findViewById<TextView>(R.id.tv_detail_description).text = desc
        findViewById<TextView>(R.id.tv_detail_seller).text      = "Sold by: $seller"

        val ivCover       = findViewById<ImageView>(R.id.iv_detail_cover)
        val tvPlaceholder = findViewById<TextView>(R.id.tv_detail_placeholder)

        if (bookImage.isNotBlank()) {
            try {
                if (bookImage.startsWith("drawable:")) {
                    val drawableName = bookImage.removePrefix("drawable:")
                    val resId = resources.getIdentifier(drawableName, "drawable", packageName)
                    if (resId != 0) {
                        ivCover.setImageResource(resId)
                        ivCover.visibility       = View.VISIBLE
                        tvPlaceholder.visibility = View.GONE
                    }
                } else {
                    val file = File(bookImage)
                    if (file.exists()) {
                        ivCover.setImageURI(Uri.fromFile(file))
                    } else {
                        ivCover.setImageURI(Uri.parse(bookImage))
                    }
                    ivCover.visibility       = View.VISIBLE
                    tvPlaceholder.visibility = View.GONE
                }
            } catch (e: Exception) {
                ivCover.visibility       = View.GONE
                tvPlaceholder.visibility = View.VISIBLE
            }
        }

        val btnAddToCart     = findViewById<Button>(R.id.btn_add_to_cart)
        val btnMessageSeller = findViewById<Button>(R.id.btn_message_seller)
        val tvSwitchMode     = findViewById<TextView>(R.id.tv_switch_to_buyer)
        val session          = SessionManager(this)

        when (session.role) {
            "buyer" -> {
                btnAddToCart.visibility     = View.VISIBLE
                btnMessageSeller.visibility = View.VISIBLE
                tvSwitchMode.visibility     = View.GONE

                updateCartButton(btnAddToCart, bookId)

                btnAddToCart.setOnClickListener {
                    if (CartManager.isInCart(bookId)) {
                        startActivity(Intent(this, CartActivity::class.java))
                    } else {
                        val book = Book(
                            id             = bookId,
                            sellerId       = 0,
                            sellerUsername = seller,
                            title          = title,
                            author         = author,
                            price          = price,
                            condition      = condition,
                            category       = category,
                            description    = desc
                        )
                        CartManager.addItem(book)
                        updateCartButton(btnAddToCart, bookId)
                        Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show()
                    }
                }

                btnMessageSeller.setOnClickListener {
                    if (sellerId == -1) {
                        Toast.makeText(this, "Cannot message this seller", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val repo = MessagingRepository(this)
                    val conversationId = repo.getOrCreateConversation(
                        bookId         = bookId,
                        bookTitle      = title,
                        buyerId        = session.userId,
                        buyerUsername  = session.username,
                        sellerId       = sellerId,
                        sellerUsername = seller
                    )
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("conversation_id", conversationId)
                    intent.putExtra("book_title",      title)
                    intent.putExtra("other_username",  seller)
                    startActivity(intent)
                }
            }
            else -> {
                btnAddToCart.visibility     = View.GONE
                btnMessageSeller.visibility = View.GONE
                tvSwitchMode.visibility     = View.VISIBLE
            }
        }
    }

    private fun updateCartButton(btn: Button, bookId: Int) {
        if (CartManager.isInCart(bookId)) {
            btn.text = "🛒  View Cart"
            btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1F6F45"))
        } else {
            btn.text = "🛒  Add to Cart"
            btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#288B57"))
        }
    }
}