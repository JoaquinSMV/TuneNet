package com.example.tunenet.data.local

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artistName: String,
    val albumTitle: String,
    val albumCover: String,
    val preview: String,
    val duration: Int
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trackId: Long,
    val author: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface TuneDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: Long): Boolean

    @Query("SELECT * FROM comments WHERE trackId = :trackId ORDER BY timestamp DESC")
    fun getCommentsForTrack(trackId: Long): Flow<List<CommentEntity>>

    @Insert
    suspend fun insertComment(comment: CommentEntity)
}

@Database(entities = [FavoriteEntity::class, CommentEntity::class], version = 1, exportSchema = false)
abstract class TuneDatabase : RoomDatabase() {
    abstract fun tuneDao(): TuneDao

    companion object {
        @Volatile
        private var INSTANCE: TuneDatabase? = null

        fun getDatabase(context: Context): TuneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TuneDatabase::class.java,
                    "tune_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
