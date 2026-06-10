package com.reread.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.reread.app.R
import com.reread.app.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etUsername        = findViewById(R.id.et_username)
        etEmail           = findViewById(R.id.et_email)
        etPassword        = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        spinnerRole       = findViewById(R.id.spinner_role)
        btnRegister       = findViewById(R.id.btn_register)
        tvGoToLogin       = findViewById(R.id.tv_go_to_login)
        progressBar       = findViewById(R.id.progress_bar)

        val roles = arrayOf("Buyer", "Seller")
        spinnerRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        btnRegister.setOnClickListener {
            val role = if (spinnerRole.selectedItemPosition == 1) "seller" else "buyer"
            viewModel.register(
                etUsername.text.toString(),
                etEmail.text.toString(),
                etPassword.text.toString(),
                etConfirmPassword.text.toString(),
                role
            )
        }

        tvGoToLogin.setOnClickListener { finish() }

        observeState()
    }

    private fun observeState() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is AuthViewModel.RegisterState.Idle -> setLoading(false)
                is AuthViewModel.RegisterState.Loading -> setLoading(true)
                is AuthViewModel.RegisterState.Success -> {
                    setLoading(false)
                    Toast.makeText(this, "Account created! Welcome, ${state.user.username}!", Toast.LENGTH_SHORT).show()
                    navigateToGenreSelection()
                    viewModel.resetRegisterState()
                }
                is AuthViewModel.RegisterState.Error -> {
                    setLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetRegisterState()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !loading
    }

    private fun navigateToGenreSelection() {
        val intent = Intent(this, GenreSelectionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}