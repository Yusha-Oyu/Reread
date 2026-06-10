package com.reread.app.ui.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reread.app.R
import com.reread.app.ui.admin.AdminActivity
import com.reread.app.ui.auth.LoginActivity
import com.reread.app.ui.home.HomeActivity
import com.reread.app.utils.SessionManager
import androidx.core.graphics.toColorInt

class AccountBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_sheet_account, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = SessionManager(requireContext())

        view.findViewById<TextView>(R.id.tv_username).text = session.username
        view.findViewById<TextView>(R.id.tv_email).text    = session.email
        view.findViewById<TextView>(R.id.tv_role).text     = session.role.replaceFirstChar { it.uppercase() }

        val btnBuyer  = view.findViewById<Button>(R.id.btn_role_buyer)
        val btnSeller = view.findViewById<Button>(R.id.btn_role_seller)
        val btnAdmin  = view.findViewById<Button>(R.id.btn_role_admin)

        if (session.username != "admin") {
            btnAdmin.visibility = View.GONE
        } else {
            btnAdmin.visibility = View.VISIBLE
        }

        updateButtons(btnBuyer, btnSeller, btnAdmin, session.role)

        btnBuyer.setOnClickListener {
            session.setRole("buyer")
            dismiss()
            restartApp(session)
        }
        btnSeller.setOnClickListener {
            session.setRole("seller")
            dismiss()
            restartApp(session)
        }
        btnAdmin.setOnClickListener {
            session.setRole("admin")
            dismiss()
            restartApp(session)
        }

        // Dark mode toggle
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switch_dark_mode)
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        switchDarkMode.isChecked = currentMode == AppCompatDelegate.MODE_NIGHT_YES

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                session.setDarkMode(true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                session.setDarkMode(false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        view.findViewById<Button>(R.id.btn_logout).setOnClickListener {
            session.clear()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun restartApp(session: SessionManager) {
        val intent = if (session.role == "admin") {
            Intent(requireContext(), AdminActivity::class.java)
        } else {
            Intent(requireContext(), HomeActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun updateButtons(btnBuyer: Button, btnSeller: Button, btnAdmin: Button, role: String) {
        val active       = "#288B57".toColorInt()
        val inactive     = "#F0F0F0".toColorInt()
        val activeText   = "#FFFFFF".toColorInt()
        val inactiveText = "#333333".toColorInt()
        listOf(btnBuyer to "buyer", btnSeller to "seller", btnAdmin to "admin").forEach { (btn, r) ->
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(
                if (r == role) active else inactive
            )
            btn.setTextColor(if (r == role) activeText else inactiveText)
        }
    }
}