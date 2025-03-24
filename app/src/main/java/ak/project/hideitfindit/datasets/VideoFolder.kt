package ak.project.hideitfindit.datasets

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_folders")
data class VideoFolder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
//    @ColumnInfo(name = "filePath") val filePath: String
)
