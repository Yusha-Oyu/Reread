package com.reread.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.reread.app.data.Book
import com.reread.app.data.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BookRepository(application)

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadAllBooks()
    }

    fun loadAllBooks() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.getAllBooks()
            _books.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun loadBooksForUser(preferredGenres: List<String>) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val allBooks = repo.getAllBooks()
            val preferred = allBooks.filter { book ->
                preferredGenres.any { genre ->
                    book.category.equals(genre, ignoreCase = true)
                }
            }
            val others = allBooks.filter { book ->
                preferredGenres.none { genre ->
                    book.category.equals(genre, ignoreCase = true)
                }
            }
            _books.postValue(preferred + others)
            _isLoading.postValue(false)
        }
    }

    fun search(query: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = if (query.isBlank()) repo.getAllBooks() else repo.searchBooks(query)
            _books.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun filterByCategory(category: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = if (category == "All") repo.getAllBooks() else repo.getBooksByCategory(category)
            _books.postValue(result)
            _isLoading.postValue(false)
        }
    }
}