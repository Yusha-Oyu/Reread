package com.reread.app.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reread.app.R
import com.reread.app.data.Book
import java.io.File

class BookAdapter(
    private val onBookClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView     = itemView.findViewById(R.id.tv_book_title)
        private val tvAuthor: TextView    = itemView.findViewById(R.id.tv_book_author)
        private val tvPrice: TextView     = itemView.findViewById(R.id.tv_book_price)
        private val tvCondition: TextView = itemView.findViewById(R.id.tv_book_condition)
        private val tvCategory: TextView  = itemView.findViewById(R.id.tv_book_category)
        private val tvSeller: TextView    = itemView.findViewById(R.id.tv_book_seller)
        private val ivCover: ImageView    = itemView.findViewById(R.id.iv_book_cover)
        private val tvPlaceholder: View   = itemView.findViewById(R.id.tv_book_placeholder)

        fun bind(book: Book) {
            tvTitle.text     = book.title
            tvAuthor.text    = book.author
            tvPrice.text     = "\$${String.format("%.2f", book.price)}"
            tvCondition.text = book.condition
            tvCategory.text  = book.category
            tvSeller.text    = "by ${book.sellerUsername}"

            // Color code condition badge
            val conditionColor = when (book.condition) {
                "New"      -> android.graphics.Color.parseColor("#1B5E20")
                "Like New" -> android.graphics.Color.parseColor("#288B57")
                "Good"     -> android.graphics.Color.parseColor("#F57F17")
                "Fair"     -> android.graphics.Color.parseColor("#E65100")
                "Poor"     -> android.graphics.Color.parseColor("#B71C1C")
                else       -> android.graphics.Color.parseColor("#288B57")
            }
            tvCondition.backgroundTintList =
                android.content.res.ColorStateList.valueOf(conditionColor)

            // Load image
            if (book.imagePath.isNotBlank()) {
                try {
                    if (book.imagePath.startsWith("drawable:")) {
                        val drawableName = book.imagePath.removePrefix("drawable:")
                        val resId = itemView.context.resources.getIdentifier(
                            drawableName, "drawable", itemView.context.packageName
                        )
                        if (resId != 0) {
                            ivCover.setImageResource(resId)
                            ivCover.visibility       = View.VISIBLE
                            tvPlaceholder.visibility = View.GONE
                        } else {
                            ivCover.visibility       = View.GONE
                            tvPlaceholder.visibility = View.VISIBLE
                        }
                    } else {
                        val file = File(book.imagePath)
                        if (file.exists()) {
                            ivCover.setImageURI(Uri.fromFile(file))
                        } else {
                            ivCover.setImageURI(Uri.parse(book.imagePath))
                        }
                        ivCover.visibility       = View.VISIBLE
                        tvPlaceholder.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    ivCover.visibility       = View.GONE
                    tvPlaceholder.visibility = View.VISIBLE
                }
            } else {
                ivCover.visibility       = View.GONE
                tvPlaceholder.visibility = View.VISIBLE
            }

            itemView.setOnClickListener { onBookClick(book) }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Book, newItem: Book) = oldItem == newItem
    }
}