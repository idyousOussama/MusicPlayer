package com.example.jamplayer.AppDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.jamplayer.AppDatabase.Daos.AlbumDao
import com.example.jamplayer.AppDatabase.Daos.MusicFileDao
import com.example.jamplayer.AppDatabase.Daos.PlayListDao
import com.example.jamplayer.AppDatabase.Daos.SettingsDao
import com.example.jamplayer.AppDatabase.Daos.UserDao
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.Converter
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList
import com.example.jamplayer.Moduls.Settings
import com.example.jamplayer.Moduls.User

@Database(entities = arrayOf(MusicFile::class , Album ::class, Settings::class ,User::class , PlayList :: class), version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class JamRoom : RoomDatabase() {
    abstract fun musicFileDao(): MusicFileDao
    abstract fun AlbumDao(): AlbumDao
    abstract fun SettingsDao(): SettingsDao
    abstract fun userDao(): UserDao
    abstract fun PlaylistDao():PlayListDao
    companion object {
        @Volatile
        private var INSTANCE: JamRoom? = null
        fun getDatabase(context: Context): JamRoom {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    JamRoom::class.java,
                    "jjhjdsjhkkhkkkjb")
                    .build().also { INSTANCE = it
                    }
            }
        }
    }
}
