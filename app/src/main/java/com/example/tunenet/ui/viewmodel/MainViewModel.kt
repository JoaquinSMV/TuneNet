package com.example.tunenet.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.tunenet.data.local.CommentEntity
import com.example.tunenet.data.local.FavoriteEntity
import com.example.tunenet.data.local.UserPreferences
import com.example.tunenet.data.model.DeezerTrack
import com.example.tunenet.data.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val repository: MusicRepository,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    // ExoPlayer instance
    private var _exoPlayer: ExoPlayer? = null
    val exoPlayer: ExoPlayer? get() = _exoPlayer

    // Estado de reproducción
    var currentPlayingTrack by mutableStateOf<DeezerTrack?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var playbackProgress by mutableStateOf(0f)
        private set
    
    private var progressJob: Job? = null

    init {
        _exoPlayer = ExoPlayer.Builder(application).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                    // USAMOS EL PUNTERO A LA CLASE PARA EVITAR EL CONFLICTO
                    this@MainViewModel.isPlaying = isPlayingChanged
                    if (isPlayingChanged) {
                        startProgressUpdate()
                    } else {
                        stopProgressUpdate()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        this@MainViewModel.isPlaying = false // AQUÍ TAMBIÉN
                        playbackProgress = 0f
                        stopProgressUpdate()
                    }
                }
            })
        }
    }

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

    // Lógica de reproducción
    fun playTrack(track: DeezerTrack) {
        if (currentPlayingTrack?.id == track.id) {
            if (isPlaying) _exoPlayer?.pause() else _exoPlayer?.play()
            return
        }

        currentPlayingTrack = track
        _exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(track.preview))
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        _exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(progress: Float) {
        _exoPlayer?.let {
            val duration = it.duration
            if (duration > 0) {
                it.seekTo((duration * progress).toLong())
            }
        }
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isPlaying) {
                _exoPlayer?.let {
                    if (it.duration > 0) {
                        playbackProgress = it.currentPosition.toFloat() / it.duration
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    // Favoritos y Comentarios
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

    override fun onCleared() {
        super.onCleared()
        _exoPlayer?.release()
        _exoPlayer = null
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val repository: MusicRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
