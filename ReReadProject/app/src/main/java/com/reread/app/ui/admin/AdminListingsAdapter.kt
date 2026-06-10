package com.reread.app.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reread.app.R
import com.reread.app.data.Book

class AdminListingsAdapter(
    private val listings: MutableList<Book>,
    private val onEdit: (Book) -> Unit,
    private val onDelete: (Book) -> Unit
) : RecyclerView.Adapter<AdminListingsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_listing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listings[position])
    }

    override fun getItemCount() = listings.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView    = itemView.findViewById(R.id.tv_admin_book_title)
        private val tvAuthor: TextView   = itemView.findViewById(R.id.tv_admin_book_author)
        private val tvPrice: TextView    = itemView.findViewById(R.id.tv_admin_book_price)
        private val tvSeller: TextView   = itemView.findViewById(R.id.tv_admin_book_seller)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_admin_book_category)
        private val btnEdit: Button      = itemView.findViewById(R.id.btn_edit_listing)
        private val btnRemove: Button    = itemView.findViewById(R.id.btn_remove_listing)

        fun bind(book: Book) {
            tvTitle.text    = book.title
            tvAuthor.text   = book.author
            tvPrice.text    = "\$${String.format("%.2f", book.price)}"
            tvSeller.text   = "Seller: ${book.sellerUsername}"
            tvCategory.text = book.category
            btnEdit.setOnClickListener   { onEdit(book) }
            btnRemove.setOnClickListener { onDelete(book) }
        }
    }
}