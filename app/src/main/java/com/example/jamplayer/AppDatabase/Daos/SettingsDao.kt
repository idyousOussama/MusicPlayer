package com.example.jamplayer.AppDatabase.Daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.jamplayer.Moduls.Settings

@Dao
interface SettingsDao {
@Insert
 fun insertSettings(setting : Settings)
 @Query("select * from settings")
 fun getSettings() : Settings
 @Query("update settings set itemType = :itemsType")
  fun upDateItemsType(itemsType : String)
@Query("update settings set FSNIsEnable = :isEnable")
fun upDateFNSSetting(isEnable : Boolean)
  @Query("update settings set SecNIsEnable = :isEnable")
  fun upDateSecNSettings(isEnable : Boolean)

  @Query("UpDate settings set playingTime = playingTime + :playingTime ")
 fun setPlayingTime(playingTime : Int)

}