package com.reread.app.ui.checkout

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.reread.app.R
import com.reread.app.data.CartManager
import com.reread.app.data.Order

class CheckoutActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAddress: EditText
    private lateinit var rgPayment: RadioGroup
    private lateinit var tvOrderTotal: TextView
    private lateinit var tvOrderSubtotal: TextView
    private lateinit var tvServiceFee: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvItemCount: TextView
    private lateinit var btnPlaceOrder: Button
    private lateinit var layoutCardFields: LinearLayout
    private lateinit var etCardName: EditText
    private lateinit var etCardNumber: EditText
    private lateinit var etCardExpiry: EditText
    private lateinit var etCardCvv: EditText

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

        setContentView(R.layout.activity_checkout)

        val toolbarColor = if (isDarkMode()) Color.parseColor("#1A1A1A") else Color.WHITE
        val textColor    = if (isDarkMode()) Color.WHITE else Color.parseColor("#333333")
        val iconColor    = if (isDarkMode()) Color.WHITE else Color.parseColor("#1A1A1A")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Checkout"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(toolbarColor))
        toolbar.setTitleTextColor(textColor)
        toolbar.navigationIcon?.setTint(iconColor)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        etFullName       = findViewById(R.id.et_full_name)
        etEmail          = findViewById(R.id.et_email)
        etAddress        = findViewById(R.id.et_address)
        rgPayment        = findViewById(R.id.rg_payment)
        tvOrderTotal     = findViewById(R.id.tv_order_total)
        tvOrderSubtotal  = findViewById(R.id.tv_order_subtotal)
        tvServiceFee     = findViewById(R.id.tv_service_fee)
        tvTax            = findViewById(R.id.tv_tax)
        tvItemCount      = findViewById(R.id.tv_item_count)
        btnPlaceOrder    = findViewById(R.id.btn_place_order)
        layoutCardFields = findViewById(R.id.layout_card_fields)
        etCardName       = findViewById(R.id.et_card_name)
        etCardNumber     = findViewById(R.id.et_card_number)
        etCardExpiry     = findViewById(R.id.et_card_expiry)
        etCardCvv        = findViewById(R.id.et_card_cvv)

        val items    = CartManager.getItems()
        val subtotal = CartManager.getTotal()
        val service  = subtotal * 0.05
        val tax      = subtotal * 0.08
        val total    = subtotal + service + tax

        tvItemCount.text     = "${items.size} item${if (items.size != 1) "s" else ""}"
        tvOrderSubtotal.text = "\$${String.format("%.2f", subtotal)}"
        tvServiceFee.text    = "\$${String.format("%.2f", service)}"
        tvTax.text           = "\$${String.format("%.2f", tax)}"
        tvOrderTotal.text    = "\$${String.format("%.2f", total)}"

        // MM/YY auto format
        etCardExpiry.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deletingSlash = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                deletingSlash = count == 1 && s?.getOrNull(start) == '/'
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val digits = s.toString().replace("/", "")
                val formatted = when {
                    deletingSlash -> digits.take(1)
                    digits.length >= 2 -> "${digits.take(2)}/${digits.drop(2).take(2)}"
                    else -> digits
                }
                s?.replace(0, s.length, formatted)
                isFormatting = false
            }
        })

        rgPayment.setOnCheckedChangeListener { _, checkedId ->
            layoutCardFields.visibility =
                if (checkedId == R.id.rb_card) View.VISIBLE else View.GONE
        }

        btnPlaceOrder.setOnClickListener { placeOrder(total) }
    }

    private fun placeOrder(total: Double) {
        val fullName = etFullName.text.toString().trim()
        val email    = etEmail.text.toString().trim()
        val address  = etAddress.text.toString().trim()

        if (fullName.isBlank()) { etFullName.error = "Required"; return }
        if (email.isBlank())    { etEmail.error    = "Required"; return }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"; return
        }
        if (address.isBlank()) { etAddress.error = "Required"; return }

        val selectedPayment = when (rgPayment.checkedRadioButtonId) {
            R.id.rb_card      -> "Credit / Debit Card"
            R.id.rb_applepay  -> "Apple Pay"
            R.id.rb_googlepay -> "Google Pay"
            R.id.rb_cashapp   -> "Cash App"
            else -> {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (rgPayment.checkedRadioButtonId == R.id.rb_card) {
            val cardName   = etCardName.text.toString().trim()
            val cardNumber = etCardNumber.text.toString().trim()
            val cardExpiry = etCardExpiry.text.toString().trim()
            val cardCvv    = etCardCvv.text.toString().trim()
            if (cardName.isBlank())      { etCardName.error   = "Required"; return }
            if (cardNumber.length != 16) { etCardNumber.error = "Enter 16-digit card number"; return }
            if (cardExpiry.length != 5)  { etCardExpiry.error = "Enter valid MM/YY"; return }
            if (cardCvv.length < 3)      { etCardCvv.error    = "Enter valid CVV"; return }
        }

        val order = Order(
            items         = CartManager.getItems(),
            total         = total,
            fullName      = fullName,
            email         = email,
            address       = address,
            paymentMethod = selectedPayment
        )

        val collectionRepo = com.reread.app.data.CollectionRepository(this)
        val session        = com.reread.app.utils.SessionManager(this)
        collectionRepo.savePurchase(session.userId, order.id, order.items)
        val bookRepo = com.reread.app.data.BookRepository(this)
        order.items.forEach { item -> bookRepo.deleteBookById(item.bookId) }
        CartManager.clear()

        val intent = Intent(this, OrderConfirmationActivity::class.java)
        intent.putExtra("order_id",      order.id)
        intent.putExtra("order_total",   order.total)
        intent.putExtra("order_name",    order.fullName)
        intent.putExtra("order_email",   order.email)
        intent.putExtra("order_payment", order.paymentMethod)
        intent.putExtra("order_count",   order.items.size)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}