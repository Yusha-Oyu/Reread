package com.reread.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.reread.app.R
import com.reread.app.ui.home.HomeActivity
import com.reread.app.utils.SessionManager

class GenreSelectionActivity : AppCompatActivity() {

    private val selectedGenres = mutableSetOf<String>()
    private val allGenres = listOf(
        "Academic", "Fiction", "Non-Fiction",
        "Biography", "Documentary", "Science",
        "History", "Self-Help", "Technology", "Art"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genre_selection)

        val session   = SessionManager(this)
        val container = findViewById<ChipGroup>(R.id.chip_group_genres)

        allGenres.forEach { genre ->
            val chip = Chip(this).apply {
                text                 = genre
                isCheckable          = true
                isCheckedIconVisible = false
                chipBackgroundColor  = ContextCompat.getColorStateList(context, R.color.chip_selector)
                setTextColor(ContextCompat.getColorStateList(context, R.color.chip_text_selector))
                chipStrokeWidth      = 2f
                chipStrokeColor      = ContextCompat.getColorStateList(context, R.color.chip_selector)
                textSize             = 14f
                chipCornerRadius     = 50f
                chipMinHeight        = 56f
                chipStartPadding     = 24f
                chipEndPadding       = 24f
                textStartPadding     = 8f
                textEndPadding       = 8f
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedGenres.add(genre)
                else selectedGenres.remove(genre)
            }
            container.addView(chip)
        }

        findViewById<Button>(R.id.btn_continue).setOnClickListener {
            if (selectedGenres.isEmpty()) {
                Toast.makeText(this, "Please select at least one genre!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            session.saveGenrePreferences(selectedGenres.toList())
            goHome()
        }

        findViewById<Button>(R.id.btn_skip).setOnClickListener {
            session.saveGenrePreferences(emptyList())
            goHome()
        }
    }

    private fun goHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}