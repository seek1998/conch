package com.example.conch.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.conch.R
import com.example.conch.data.model.DownloadRecord
import com.example.conch.data.model.Playlist
import com.example.conch.data.model.Track
import com.example.conch.data.model.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Database(
    entities = [Playlist::class, Track::class, User::class, PlaylistTrackCrossRef::class, DownloadRecord::class],
    version = 1,
    exportSchema = false
)
abstract class ConchRoomDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao

    abstract fun trackDao(): TrackDao

    abstract fun crossRefDao(): CrossRefDao

    abstract fun downloadRecordDao(): DownloadRecordDao

    companion object {
        @Volatile
        private var instance: ConchRoomDatabase? = null

        fun getDatabase(context: Context): ConchRoomDatabase {
            return instance ?: synchronized(this) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConchRoomDatabase::class.java,
                    "conch_database"
                ).setQueryExecutor(MyExecutor.myExecutor)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Executors.newSingleThreadExecutor().execute {
                                addInitialPlaylist(context)
                            }
                        }
                    })
                    .build()

                instance!!
            }
        }

        fun destroyInstance() {
            instance = null
        }

        private fun addInitialPlaylist(context: Context) {

            val playlist = Playlist(
                id = Playlist.PLAYLIST_FAVORITE_ID,
                title = context.resources.getString(R.string.playlist_favorite),
                size = Playlist.NO_TRACK,
                uid = User.LOCAL_USER
            )

            GlobalScope.launch {
                instance?.playlistDao()?.insert(playlist)
            }
        }
    }
}