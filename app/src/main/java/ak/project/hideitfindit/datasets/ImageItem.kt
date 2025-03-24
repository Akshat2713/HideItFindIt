package ak.project.hideitfindit.datasets

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_items")
data class ImageItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "folderId") val folderId: Int,
    @ColumnInfo(name = "filePath") val filePath: String
)
