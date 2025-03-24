package ak.project.hideitfindit

import ak.project.hideitfindit.datasets.ImageDataClass
import ak.project.hideitfindit.datasets.ImageFolder
import ak.project.hideitfindit.datasets.NoteDataClass
import ak.project.hideitfindit.datasets.VideoDataClass
import ak.project.hideitfindit.datasets.VideoFolder
import androidx.room.*

@Dao
interface AppDao {

        // Note DAO
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertNote(note: NoteDataClass)

        @Query("UPDATE notes SET title = :title WHERE id = :id")
        suspend fun updateNoteById(id: Int, title: String)

        @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
        suspend fun getNoteById(id: Int): NoteDataClass?


        @Query("SELECT * FROM notes")
        suspend fun getAllNotes(): List<NoteDataClass>

        @Update
        suspend fun updateNote(note: NoteDataClass)

        @Delete
        suspend fun deleteNote(note: NoteDataClass)

        // Image DAO
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertImage(image: ImageDataClass)

        @Query("SELECT * FROM images")
        suspend fun getAllImages(): List<ImageDataClass>

        @Query("SELECT * FROM images WHERE folderId = :folderId")
        suspend fun getImagesByFolderId(folderId: Int): List<ImageDataClass>

        @Update
        suspend fun updateImage(image: ImageDataClass)

        @Delete
        suspend fun deleteImage(image: ImageDataClass)

        // Video DAO
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertVideo(video: VideoDataClass)

        @Query("SELECT * FROM videos")
        suspend fun getAllVideos(): List<VideoDataClass>

        @Update
        suspend fun updateVideo(video: VideoDataClass)

        @Delete
        suspend fun deleteVideo(video: VideoDataClass)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertImageFolder(folder: ImageFolder)

        @Query("SELECT * FROM image_folders")
        suspend fun getAllImageFolders(): List<ImageFolder>

        @Update
        suspend fun updateImageFolder(folder: ImageFolder)

        @Delete
        suspend fun deleteImageFolder(folder: ImageFolder)

        // Video Folder DAO
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertVideoFolder(folder: VideoFolder)

        @Query("SELECT * FROM video_folders")
        suspend fun getAllVideoFolders(): List<VideoFolder>

        @Update
        suspend fun updateVideoFolder(folder: VideoFolder)

        @Delete
        suspend fun deleteVideoFolder(folder: VideoFolder)

        @Query("SELECT * FROM image_folders WHERE id = :id LIMIT 1")
        suspend fun getFolderById(id: Int): ImageFolder

}
