package com.example.conch.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.data.model.User

@Database(
    entities = [Playlist::class, Track::class, User::class],
    version = 1,
    exportSchema = false
)
abstract class ConchRoomDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao

    abstract fun trackDao(): TrackDao

    companion object {
        @Volatile
        private var instance: ConchRoomDatabase? = null

        fun getDatabase(context: Context): ConchRoomDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    ConchRoomDatabase::class.java,
                    "conch_database"
                ).build()
                instance = newInstance
                instance!!
            }
        }
    }


}