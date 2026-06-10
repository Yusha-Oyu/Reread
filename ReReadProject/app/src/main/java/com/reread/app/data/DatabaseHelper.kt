package com.reread.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "reread.db"
        const val DATABASE_VERSION = 6

        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD_HASH = "password_hash"
        const val COL_ROLE = "role"
        const val COL_IS_ACTIVE = "is_active"
        const val COL_CREATED_AT = "created_at"

        const val TABLE_BOOKS = "books"
        const val COL_BOOK_ID = "id"
        const val COL_SELLER_ID = "seller_id"
        const val COL_TITLE = "title"
        const val COL_AUTHOR = "author"
        const val COL_ISBN = "isbn"
        const val COL_PRICE = "price"
        const val COL_CONDITION = "condition"
        const val COL_CATEGORY = "category"
        const val COL_SUBCATEGORY = "subcategory"
        const val COL_DESCRIPTION = "description"
        const val COL_IMAGE_PATH = "image_path"
        const val COL_IS_AVAILABLE = "is_available"
        const val COL_BOOK_CREATED_AT = "created_at"

        const val TABLE_CONVERSATIONS = "conversations"
        const val TABLE_MESSAGES = "messages"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID       INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME      TEXT    NOT NULL UNIQUE,
                $COL_EMAIL         TEXT    NOT NULL UNIQUE,
                $COL_PASSWORD_HASH TEXT    NOT NULL,
                $COL_ROLE          TEXT    NOT NULL DEFAULT 'buyer',
                $COL_IS_ACTIVE     INTEGER NOT NULL DEFAULT 1,
                $COL_CREATED_AT    TEXT    NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_BOOKS (
                $COL_BOOK_ID         INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SELLER_ID       INTEGER NOT NULL,
                $COL_TITLE           TEXT    NOT NULL,
                $COL_AUTHOR          TEXT    NOT NULL,
                $COL_ISBN            TEXT    DEFAULT '',
                $COL_PRICE           REAL    NOT NULL,
                $COL_CONDITION       TEXT    NOT NULL,
                $COL_CATEGORY        TEXT    NOT NULL,
                $COL_SUBCATEGORY     TEXT    DEFAULT '',
                $COL_DESCRIPTION     TEXT    DEFAULT '',
                $COL_IMAGE_PATH      TEXT    DEFAULT '',
                $COL_IS_AVAILABLE    INTEGER NOT NULL DEFAULT 1,
                $COL_BOOK_CREATED_AT TEXT    NOT NULL,
                FOREIGN KEY($COL_SELLER_ID) REFERENCES $TABLE_USERS($COL_USER_ID)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE purchased_books (
                id           INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id      INTEGER NOT NULL,
                book_id      INTEGER NOT NULL,
                order_id     TEXT    NOT NULL,
                title        TEXT    NOT NULL,
                author       TEXT    NOT NULL,
                price        REAL    NOT NULL,
                condition    TEXT    NOT NULL,
                purchased_at TEXT    NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_CONVERSATIONS (
                id               INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id          INTEGER NOT NULL,
                book_title       TEXT    NOT NULL,
                buyer_id         INTEGER NOT NULL,
                buyer_username   TEXT    NOT NULL,
                seller_id        INTEGER NOT NULL,
                seller_username  TEXT    NOT NULL,
                last_message     TEXT    DEFAULT '',
                last_message_at  INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_MESSAGES (
                id               INTEGER PRIMARY KEY AUTOINCREMENT,
                conversation_id  INTEGER NOT NULL,
                sender_id        INTEGER NOT NULL,
                sender_username  TEXT    NOT NULL,
                content          TEXT    NOT NULL,
                sent_at          INTEGER NOT NULL,
                is_read          INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(conversation_id) REFERENCES $TABLE_CONVERSATIONS(id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO $TABLE_USERS ($COL_USERNAME, $COL_EMAIL, $COL_PASSWORD_HASH, $COL_ROLE, $COL_IS_ACTIVE, $COL_CREATED_AT)
            VALUES ('admin', 'admin@reread.com', '${PasswordUtils.hash("admin123")}', 'admin', 1, '${System.currentTimeMillis()}')
            """.trimIndent()
        )

        seedSampleBooks(db)
    }

    private fun seedSampleBooks(db: SQLiteDatabase) {
        val now = System.currentTimeMillis().toString()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKS")
            db.execSQL("DROP TABLE IF EXISTS purchased_books")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CONVERSATIONS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
            onCreate(db)
            return
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN is_read INTEGER NOT NULL DEFAULT 0")
        }
    }
}