package ak.project.hideitfindit

import ak.project.hideitfindit.datasets.ImageDataClass
import ak.project.hideitfindit.datasets.ImageFolder
import ak.project.hideitfindit.datasets.NoteDataClass
import ak.project.hideitfindit.datasets.VideoDataClass
import ak.project.hideitfindit.datasets.VideoFolder
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NoteDataClass::class, ImageDataClass::class, VideoDataClass::class, ImageFolder::class, VideoFolder::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "app-database"
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}