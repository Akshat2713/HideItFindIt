package ak.project.hideitfindit.datasets

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageDataClass(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "filePath") val filePath: String, // Current file path in hidden folder
    @ColumnInfo(name = "folderId") val folderId: Int,
    @ColumnInfo(name = "originalPath") val originalPath: String // Original file path
)
