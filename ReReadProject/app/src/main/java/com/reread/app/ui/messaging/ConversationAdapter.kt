package com.reread.app.ui.messaging

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reread.app.R
import com.reread.app.data.Conversation
import com.reread.app.data.MessagingRepository
import com.reread.app.utils.SessionManager

class ConversationAdapter(
    private val conversations: List<Conversation>,
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount() = conversations.size

    inner class ViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val tvBook: TextView        = itemView.findViewById(R.id.tv_conv_book)
        private val tvOtherUser: TextView   = itemView.findViewById(R.id.tv_conv_other_user)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tv_conv_last_message)
        private val tvBadge: TextView       = itemView.findViewById(R.id.tv_unread_badge)

        fun bind(conv: Conversation) {
            val session = SessionManager(context)
            val repo    = MessagingRepository(context)

            tvBook.text        = conv.bookTitle
            tvOtherUser.text   = "with ${conv.sellerUsername}"
            tvLastMessage.text = if (conv.lastMessage.isBlank()) "No messages yet" else conv.lastMessage

            // Show unread badge
            val unreadCount = repo.getUnreadCount(conv.id, session.userId)
            if (unreadCount > 0) {
                tvBadge.visibility = View.VISIBLE
                tvBadge.text       = if (unreadCount > 99) "99+" else unreadCount.toString()
            } else {
                tvBadge.visibility = View.GONE
            }

            itemView.setOnClickListener { onClick(conv) }
        }
    }
}