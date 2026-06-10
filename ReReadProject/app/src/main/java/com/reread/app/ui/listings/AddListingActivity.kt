package com.reread.app.ui.listings

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.reread.app.R
import com.reread.app.utils.SessionManager
import java.io.File
import java.io.FileOutputStream

class AddListingActivity : AppCompatActivity() {

    private lateinit var ivBookImage: ImageView
    private lateinit var btnPickImage: Button
    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etIsbn: EditText
    private lateinit var etPrice: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerCondition: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var photoPlaceholder: View

    private var selectedImagePath: String = ""
    private var cameraImageUri: Uri? = null

    private val conditions = arrayOf("New", "Like New", "Good", "Fair", "Poor")
    private val categories = arrayOf("Academic", "Fiction", "Non-Fiction", "Biography", "Documentary")

    private fun isDarkMode(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                val path = copyImagePermanently(uri)
                if (path.isNotBlank()) {
                    selectedImagePath = path
                    ivBookImage.setImageURI(Uri.fromFile(File(path)))
                    ivBookImage.visibility = View.VISIBLE
                    photoPlaceholder.visibility = View.GONE
                }
            }
        }
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraImageUri?.let { uri ->
                    val path = copyImagePermanently(uri)
                    if (path.isNotBlank()) {
                        selectedImagePath = path
                        ivBookImage.setImageURI(Uri.fromFile(File(path)))
                        ivBookImage.visibility = View.VISIBLE
                        photoPlaceholder.visibility = View.GONE
                    }
                }
            } else {
                Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show()
            }
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

        setContentView(R.layout.activity_add_listing)

        val toolbarColor = if (isDarkMode()) Color.parseColor("#1A1A1A") else Color.WHITE
        val textColor    = if (isDarkMode()) Color.WHITE else Color.parseColor("#333333")
        val iconColor    = if (isDarkMode()) Color.WHITE else Color.parseColor("#1A1A1A")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(toolbarColor))
        toolbar.setTitleTextColor(textColor)
        toolbar.navigationIcon?.setTint(iconColor)
        toolbar.setNavigationOnClickListener { finish() }

        ivBookImage      = findViewById(R.id.iv_book_image)
        btnPickImage     = findViewById(R.id.btn_pick_image)
        etTitle          = findViewById(R.id.et_title)
        etAuthor         = findViewById(R.id.et_author)
        etIsbn           = findViewById(R.id.et_isbn)
        etPrice          = findViewById(R.id.et_price)
        etDescription    = findViewById(R.id.et_description)
        spinnerCondition = findViewById(R.id.spinner_condition)
        spinnerCategory  = findViewById(R.id.spinner_category)
        btnSubmit        = findViewById(R.id.btn_submit)
        progressBar      = findViewById(R.id.progress_bar)
        photoPlaceholder = findViewById(R.id.photo_placeholder)

        setupSpinners()

        val editBookId = intent.getIntExtra("edit_book_id", -1)
        if (editBookId != -1) {
            supportActionBar?.title = "Edit Listing"
            btnSubmit.text = "Update Listing"
            etTitle.setText(intent.getStringExtra("edit_title") ?: "")
            etAuthor.setText(intent.getStringExtra("edit_author") ?: "")
            etIsbn.setText(intent.getStringExtra("edit_isbn") ?: "")
            etPrice.setText(intent.getDoubleExtra("edit_price", 0.0).let {
                if (it == 0.0) "" else it.toString()
            })
            etDescription.setText(intent.getStringExtra("edit_description") ?: "")

            val editCondition = intent.getStringExtra("edit_condition") ?: ""
            val conditionIndex = conditions.indexOf(editCondition)
            if (conditionIndex >= 0) spinnerCondition.setSelection(conditionIndex)

            val editCategory = intent.getStringExtra("edit_category") ?: ""
            val categoryIndex = categories.indexOf(editCategory)
            if (categoryIndex >= 0) spinnerCategory.setSelection(categoryIndex)

            val editImage = intent.getStringExtra("edit_image") ?: ""
            selectedImagePath = editImage
            if (editImage.isNotBlank()) {
                if (editImage.startsWith("drawable:")) {
                    val drawableName = editImage.removePrefix("drawable:")
                    val resId = resources.getIdentifier(drawableName, "drawable", packageName)
                    if (resId != 0) {
                        ivBookImage.setImageResource(resId)
                        ivBookImage.visibility = View.VISIBLE
                        photoPlaceholder.visibility = View.GONE
                    }
                } else {
                    val file = File(editImage)
                    if (file.exists()) {
                        ivBookImage.setImageURI(Uri.fromFile(file))
                        ivBookImage.visibility = View.VISIBLE
                        photoPlaceholder.visibility = View.GONE
                    }
                }
            }
        } else {
            supportActionBar?.title = "Add Listing"
        }

        btnPickImage.setOnClickListener { showImagePickerDialog() }
        btnSubmit.setOnClickListener { submitListing(editBookId) }
    }

    private fun setupSpinners() {
        spinnerCondition.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, conditions
        )
        spinnerCategory.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, categories
        )
    }

    private fun showImagePickerDialog() {
        AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openCamera()
            else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

    private fun openCamera() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            return
        }
        val photoFile = File(cacheDir, "book_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(
            this, "${packageName}.fileprovider", photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        cameraLauncher.launch(cameraImageUri)
    }

    private fun copyImagePermanently(uri: Uri): String {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return ""
            val fileName = "book_img_${System.currentTimeMillis()}.jpg"
            val destFile = File(filesDir, fileName)
            val outputStream = FileOutputStream(destFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun submitListing(editBookId: Int) {
        val title       = etTitle.text.toString().trim()
        val author      = etAuthor.text.toString().trim()
        val isbn        = etIsbn.text.toString().trim()
        val priceStr    = etPrice.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val condition   = spinnerCondition.selectedItem.toString()
        val category    = spinnerCategory.selectedItem.toString()

        if (title.isBlank())    { etTitle.error  = "Required"; return }
        if (author.isBlank())   { etAuthor.error = "Required"; return }
        if (priceStr.isBlank()) { etPrice.error  = "Required"; return }
        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) { etPrice.error = "Enter a valid price"; return }

        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled    = false

        val session   = SessionManager(this)
        val imagePath = if (selectedImagePath.isNotBlank()) selectedImagePath else ""
        val repo      = com.reread.app.data.WritableBookRepository(this)

        if (editBookId != -1) {
            repo.updateBook(editBookId, title, author, isbn, price, condition, category, description, imagePath)
            Toast.makeText(this, "Listing updated!", Toast.LENGTH_SHORT).show()
        } else {
            repo.addBook(session.userId, title, author, isbn, price, condition, category, description, imagePath)
            Toast.makeText(this, "Listing posted!", Toast.LENGTH_SHORT).show()
        }

        progressBar.visibility = View.GONE
        setResult(RESULT_OK)
        finish()
    }
}