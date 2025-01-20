package com.example.jamplayer.AppDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.jamplayer.AppDatabase.Daos.SongsDaos.AlbumDao
import com.example.jamplayer.AppDatabase.Daos.SongsDaos.MusicFileDao
import com.example.jamplayer.AppDatabase.Daos.SongsDaos.PlayListDao
import com.example.jamplayer.AppDatabase.Daos.SongsDaos.SettingsDao
import com.example.jamplayer.AppDatabase.Daos.SongsDaos.UserDao
import com.example.jamplayer.AppDatabase.Daos.VideoDaos.VideoDao
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.Converter
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList
import com.example.jamplayer.Moduls.Settings
import com.example.jamplayer.Moduls.User
import com.example.jamplayer.Moduls.VideoTable
@Database(entities = arrayOf(MusicFile::class , Album ::class, Settings::class ,User::class , PlayList :: class , VideoTable :: class), version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class JamRoom : RoomDatabase() {
    abstract fun musicFileDao(): MusicFileDao
    abstract fun AlbumDao(): AlbumDao
    abstract fun SettingsDao(): SettingsDao
    abstract fun userDao(): UserDao
    abstract fun PlaylistDao(): PlayListDao
    abstract fun videoDao(): VideoDao
    companion object {
        @Volatile
        private var INSTANCE: JamRoom? = null
        fun getDatabase(context: Context): JamRoom {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    JamRoom::class.java,
                    "ousskkkjhjssdsamkidyus")
                    .build().also { INSTANCE = it
                    }
            }
        }
    }
}
