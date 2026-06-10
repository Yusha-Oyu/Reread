package com.reread.app.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reread.app.R
import com.reread.app.data.AdminRepository

class AdminBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_sheet_admin, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val repo = AdminRepository(requireContext())

        view.findViewById<TextView>(R.id.tv_stat_users).text    = repo.getTotalUsers().toString()
        view.findViewById<TextView>(R.id.tv_stat_listings).text = repo.getTotalListings().toString()
        view.findViewById<TextView>(R.id.tv_stat_orders).text   = repo.getTotalOrders().toString()

        val tabUsers    = view.findViewById<TextView>(R.id.tab_users)
        val tabListings = view.findViewById<TextView>(R.id.tab_listings)
        val rvUsers     = view.findViewById<RecyclerView>(R.id.rv_users)
        val rvListings  = view.findViewById<RecyclerView>(R.id.rv_listings)

        fun setupUsers() {
            rvUsers.layoutManager = LinearLayoutManager(requireContext())
            rvUsers.adapter = AdminUsersAdapter(
                users = repo.getAllUsers().toMutableList(),
                onToggleActive = { user -> repo.setUserActive(user.id, !user.isActive); setupUsers() },
                onDelete = { user -> repo.deleteUser(user.id); setupUsers() }
            )
        }

        fun setupListings() {
            rvListings.layoutManager = LinearLayoutManager(requireContext())
            rvListings.adapter = AdminListingsAdapter(
                listings = repo.getAllListings().toMutableList(),
                onEdit = { book ->
                    val intent = Intent(requireContext(), com.reread.app.ui.listings.AddListingActivity::class.java)
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
                onDelete = { book -> repo.deleteListing(book.id); setupListings() }
            )
        }

        fun showTab(tab: String) {
            val isUsers = tab == "users"
            rvUsers.visibility    = if (isUsers) View.VISIBLE else View.GONE
            rvListings.visibility = if (isUsers) View.GONE else View.VISIBLE
            tabUsers.setBackgroundResource(if (isUsers) R.drawable.chip_selected else R.drawable.chip_unselected)
            tabUsers.setTextColor(requireContext().getColor(if (isUsers) R.color.chip_text_selected else R.color.chip_text_unselected))
            tabListings.setBackgroundResource(if (isUsers) R.drawable.chip_unselected else R.drawable.chip_selected)
            tabListings.setTextColor(requireContext().getColor(if (isUsers) R.color.chip_text_unselected else R.color.chip_text_selected))
        }

        setupUsers()
        setupListings()
        showTab("users")
        tabUsers.setOnClickListener { showTab("users") }
        tabListings.setOnClickListener { showTab("listings") }
    }
}