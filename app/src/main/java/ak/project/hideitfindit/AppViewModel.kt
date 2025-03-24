package ak.project.hideitfindit

import ak.project.hideitfindit.datasets.ImageDataClass
import ak.project.hideitfindit.datasets.ImageFolder
import ak.project.hideitfindit.datasets.NoteDataClass
import ak.project.hideitfindit.datasets.VideoDataClass
import ak.project.hideitfindit.datasets.VideoFolder
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.appDao()

    val notes = mutableStateOf(emptyList<NoteDataClass>())
    val images = mutableStateOf(emptyList<ImageDataClass>())
    val videos = mutableStateOf(emptyList<VideoDataClass>())
    val imageFolders = mutableStateOf(emptyList<ImageFolder>())
    val videoFolders = mutableStateOf(emptyList<VideoFolder>())


    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            notes.value = dao.getAllNotes()
            images.value = dao.getAllImages()
            videos.value = dao.getAllVideos()
            imageFolders.value = dao.getAllImageFolders()
            videoFolders.value = dao.getAllVideoFolders()
        }
    }

    fun addNote(title: String) {
        viewModelScope.launch {
            dao.insertNote(NoteDataClass(title = title, content = ""))
            notes.value = dao.getAllNotes()
        }
    }

    fun deleteNote(note: NoteDataClass) {
        viewModelScope.launch {
            dao.deleteNote(note)
            notes.value = dao.getAllNotes()
        }
    }

    fun updateNote(updatedNote: NoteDataClass) {
        viewModelScope.launch {
            dao.updateNote(updatedNote) // Make sure you have an update method in your DAO
            notes.value = dao.getAllNotes() // Refresh the notes list after updating
        }
    }

    fun addImage(folderId: Int, imageUri: String, context: Context) {
        viewModelScope.launch {
            try {
                // Get the target folder path
                val folder = dao.getFolderById(folderId)
                val targetFolder = File(folder.path)
                if (!targetFolder.exists()) {
                    Log.e("AppViewModel", "Target folder does not exist: ${targetFolder.absolutePath}")
                    return@launch
                }

                val documentFile = DocumentFile.fromSingleUri(context, Uri.parse(imageUri))
                if (documentFile == null || !documentFile.exists()) {
                    Log.e("AppViewModel", "Source file does not exist or is inaccessible: $imageUri")
                    return@launch
                }

                // Copy the image to the hidden folder
                val fileName = documentFile.name ?: "${System.currentTimeMillis()}.jpg"
                val targetFile = File(targetFolder, fileName)

                context.contentResolver.openInputStream(documentFile.uri)?.use { inputStream ->
                    targetFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                }
                Log.d("AppViewModel", "Image copied to hidden folder: ${targetFile.absolutePath}")

                // Add a .nomedia file to the hidden folder to exclude it from media scans
                val noMediaFile = File(targetFolder, ".nomedia")
                if (!noMediaFile.exists()) {
                    noMediaFile.createNewFile()
                    Log.d("AppViewModel", ".nomedia file created in hidden folder")
                }

                // Insert image into the database
                dao.insertImage(
                    ImageDataClass(
                        title = fileName,
                        filePath = targetFile.absolutePath,
                        folderId = folderId,
                        originalPath = documentFile.uri.toString() // Store original URI
                    )
                )
                images.value = dao.getImagesByFolderId(folderId)
                Log.d("AppViewModel", "Image added to database and hidden folder updated")
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error adding image: ${e.message}", e)
            }
        }
    }






    fun deleteImage(image: ImageDataClass, context: Context) {
        viewModelScope.launch {
            try {
                val hiddenFile = File(image.filePath)

                if (hiddenFile.exists()) {
                    // Restore the file to a public directory (e.g., "Pictures" folder)
                    val picturesFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    if (!picturesFolder.exists()) {
                        picturesFolder.mkdirs()
                    }

                    val restoredFile = File(picturesFolder, hiddenFile.name)
                    val moved = hiddenFile.renameTo(restoredFile)

                    if (moved) {
                        Log.d("AppViewModel", "File successfully restored to: ${restoredFile.absolutePath}")

                        // Register the file in MediaStore
                        val contentValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, restoredFile.name) // File name
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // MIME type
                            put(MediaStore.Images.Media.DATA, restoredFile.absolutePath) // Absolute path
                        }

                        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            Log.d("AppViewModel", "Image registered successfully in MediaStore: $uri")
                        } else {
                            Log.e("AppViewModel", "Failed to register image in MediaStore.")
                        }
                    } else {
                        Log.e("AppViewModel", "Failed to restore file to public folder: ${hiddenFile.absolutePath}")
                    }
                } else {
                    Log.w("AppViewModel", "Hidden file does not exist: ${hiddenFile.absolutePath}")
                }

                // Remove image entry from the database
                dao.deleteImage(image)
                Log.d("AppViewModel", "Image deleted from database: ${image.title}")

                // Refresh the image list
                images.value = dao.getImagesByFolderId(image.folderId)
                Log.d("AppViewModel", "Image list updated for folderId: ${image.folderId}")
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error deleting/unhiding image: ${e.message}", e)
            }
        }
    }









    fun addVideo(title: String, filePath: String) {
        viewModelScope.launch {
            dao.insertVideo(VideoDataClass(title = title, filePath = filePath))
            videos.value = dao.getAllVideos()
        }
    }

    fun deleteVideo(video: VideoDataClass) {
        viewModelScope.launch {
            dao.deleteVideo(video)
            videos.value = dao.getAllVideos()
        }
    }


    fun addImageFolder(folderName: String, context: Context) {
        viewModelScope.launch {
            val hiddenImagesFolder = File(context.filesDir, "hidden_notes/Images") // Hidden "Images" folder path
            if (!hiddenImagesFolder.exists()) {
                hiddenImagesFolder.mkdirs() // Ensure the parent folder exists
            }

            val newFolder = File(hiddenImagesFolder, folderName) // Path for the new folder
            if (!newFolder.exists()) {
                val success = newFolder.mkdirs() // Create the new folder
                if (success) {
                    Log.d("AppViewModel", "New folder created: ${newFolder.absolutePath}")
                } else {
                    Log.e("AppViewModel", "Failed to create folder: ${newFolder.absolutePath}")
                }
            }

            // Add the new folder to the database
            dao.insertImageFolder(ImageFolder(title = folderName, path = newFolder.absolutePath)) // Assuming you have a 'path' column
            imageFolders.value = dao.getAllImageFolders() // Refresh the folder list
        }
    }



    fun deleteImageFolder(folder: ImageFolder) {
        viewModelScope.launch {
            dao.deleteImageFolder(folder)
            imageFolders.value = dao.getAllImageFolders()
        }
    }

    fun updateImageFolder(updatedFolder: ImageFolder) {
        viewModelScope.launch {
            dao.updateImageFolder(updatedFolder)
            imageFolders.value = dao.getAllImageFolders()
        }
    }

    // Video Folder operations
    fun addVideoFolder(title: String) {
        viewModelScope.launch {
            dao.insertVideoFolder(VideoFolder(title = title))
            videoFolders.value = dao.getAllVideoFolders()
        }
    }

    fun deleteVideoFolder(folder: VideoFolder) {
        viewModelScope.launch {
            dao.deleteVideoFolder(folder)
            videoFolders.value = dao.getAllVideoFolders()
        }
    }

    fun updateVideoFolder(updatedFolder: VideoFolder) {
        viewModelScope.launch {
            dao.updateVideoFolder(updatedFolder)
            videoFolders.value = dao.getAllVideoFolders()
        }
    }
    fun getNoteById(noteId: Int?): StateFlow<NoteDataClass?> {
        val noteFlow = MutableStateFlow<NoteDataClass?>(null)
        viewModelScope.launch {
            noteFlow.value = try {
                dao.getNoteById(noteId ?: 0)
            } catch (e: Exception) {
                null // Handle the case where the note is not found
            }
        }
        return noteFlow
    }

    fun getImagesByFolderId(folderId: Int) {
        viewModelScope.launch {
            val fetchedImages = dao.getImagesByFolderId(folderId)
            Log.d("AppViewModel", "Folder ID: $folderId, Total images fetched: ${fetchedImages.size}")
            fetchedImages.forEach { image ->
                Log.d("AppViewModel", "Image: id=${image.id}, title=${image.title}, filePath=${image.filePath}, folderId=${image.folderId}")
            }
            images.value = fetchedImages
        }
    }


}
