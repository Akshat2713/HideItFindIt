package ak.project.hideitfindit.datasets

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//
@Entity(tableName = "notes")
data class NoteDataClass(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title")val title: String,
    var content: String
)

