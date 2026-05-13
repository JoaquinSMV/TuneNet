package com.example.tunenet.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tunenet.data.local.CommentEntity
import com.example.tunenet.data.local.FavoriteEntity
import com.example.tunenet.data.local.UserPreferences
import com.example.tunenet.data.model.DeezerTrack
import com.example.tunenet.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MusicRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // Estado de búsqueda
    var searchText by mutableStateOf("")
        private set

    private val _searchResults = MutableStateFlow<List<DeezerTrack>>(emptyList())
    val searchResults: StateFlow<List<DeezerTrack>> = _searchResults.asStateFlow()

    // Favoritos
    val favorites: StateFlow<List<FavoriteEntity>> = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Preferencias de usuario
    val username: StateFlow<String> = userPreferences.username
        .stateIn(viewModelScope, SharingStarted.Lazily, "Usuario")

    val themeSelection: StateFlow<String> = userPreferences.themeSelection
        .stateIn(viewModelScope, SharingStarted.Lazily, "System")

    // Eventos de UI
    var toastMessage by mutableStateOf<String?>(null)
        private set

    fun onSearchTextChanged(newText: String) {
        searchText = newText
        if (newText.isNotBlank()) {
            searchTracks(newText)
        } else {
            _searchResults.value = emptyList()
        }
    }

    private fun searchTracks(query: String) {
        viewModelScope.launch {
            val results = repository.searchTracks(query)
            _searchResults.value = results
        }
    }

    fun addFavorite(track: DeezerTrack) {
        viewModelScope.launch {
            try {
                repository.addFavorite(track)
                toastMessage = "Añadido a favoritos"
            } catch (e: Exception) {
                toastMessage = e.message ?: "Error al añadir"
            }
        }
    }

    fun removeFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            repository.removeFavorite(favorite)
            toastMessage = "Eliminado de favoritos"
        }
    }

    fun clearToastMessage() {
        toastMessage = null
    }

    fun saveUsername(name: String) {
        viewModelScope.launch {
            userPreferences.saveUsername(name)
        }
    }

    fun saveTheme(theme: String) {
        viewModelScope.launch {
            userPreferences.saveTheme(theme)
        }
    }

    fun getComments(trackId: Long): StateFlow<List<CommentEntity>> {
        return repository.getComments(trackId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addComment(trackId: Long, author: String, content: String) {
        viewModelScope.launch {
            val comment = CommentEntity(trackId = trackId, author = author, content = content)
            repository.addComment(comment)
        }
    }
}

class MainViewModelFactory(
    private val repository: MusicRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
