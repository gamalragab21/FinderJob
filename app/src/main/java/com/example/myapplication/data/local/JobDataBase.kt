package myappnew.com.conserve.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.models.Job

@Database(entities = [Job::class] , version = 1, exportSchema = false)
public  abstract class JobDataBase : RoomDatabase() {
    abstract fun noteDao(): JobDao
    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: JobDataBase? = null

        fun getDatabase(context: Context): JobDataBase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JobDataBase::class.java,
                    "Note_Database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

}