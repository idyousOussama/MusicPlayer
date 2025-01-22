package com.example.jamplayer.AppDatabase.Daos.VideoDaos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.Moduls.VideoTable

@Dao
interface VideoDao {

@Insert
 fun insertNewVideo(newVideo : Video)
 @Query("select * from video")
 fun getAllVideos() : List<Video>
 @Query("SELECT * from video where id  =:id")
 fun getVideoById(id : String) : Video
 @Query("upDate video set isHide = :isHide where id = :id")
 fun HideAndUnhieVideo(isHide : Boolean , id : String)
 @Query("upDate video set title = :title where id = :id")
 fun upDateVideoTitleById(title : String , id : String)

 @Query("upDate video set isHide = :isHide where id = :id")
 fun upDateVideosHideById( id : String , isHide : Boolean )
 @Query("select * from video where isHide = 1")
 fun getHiddenVideos() : List<Video>
 @Query("delete from video where id = :id")
 fun deleteVideoById(id : String)

}