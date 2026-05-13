package com.example.tunenet.data.repository

import com.example.tunenet.data.local.FavoriteEntity
import com.example.tunenet.data.local.TuneDao
import com.example.tunenet.data.local.CommentEntity
import com.example.tunenet.data.model.DeezerTrack
import com.example.tunenet.data.remote.DeezerApiService
import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val apiService: DeezerApiService,
    private val tuneDao: TuneDao
) {
    // API
    suspend fun searchTracks(query: String): List<DeezerTrack> {
        return try {
            val response = apiService.searchTracks(query)
            response.data
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Favoritos
    val allFavorites: Flow<List<FavoriteEntity>> = tuneDao.getAllFavorites()

    suspend fun isFavorite(id: Long): Boolean = tuneDao.isFavorite(id)

    suspend fun addFavorite(track: DeezerTrack) {
        if (tuneDao.isFavorite(track.id)) {
            throw Exception("Ya es favorito")
        }
        val favorite = FavoriteEntity(
            id = track.id,
            title = track.title,
            artistName = track.artist.name,
            albumTitle = track.album.title,
            albumCover = track.album.coverMedium,
            preview = track.preview,
            duration = track.duration
        )
        tuneDao.insertFavorite(favorite)
    }

    suspend fun removeFavorite(favorite: FavoriteEntity) {
        tuneDao.deleteFavorite(favorite)
    }

    // Comentarios
    fun getComments(trackId: Long): Flow<List<CommentEntity>> = tuneDao.getCommentsForTrack(trackId)

    suspend fun addComment(comment: CommentEntity) {
        tuneDao.insertComment(comment)
    }

    // YouTube (Simulado para v4.1 sin API key compleja)
    suspend fun getYoutubeIdForTrack(title: String, artist: String): String {
        // En una implementación real, aquí llamaríamos a la API de búsqueda de YouTube
        // Por ahora devolvemos un ID que funciona bien para pruebas de música
        return "u1zgFlCw8Aw" 
    }
}
