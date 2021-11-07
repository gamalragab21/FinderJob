package myappnew.com.conserve.data

import androidx.room.*
import com.example.myapplication.models.Job
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.MutableStateFlow


@Dao
interface JobDao {

    @Query("SELECT * FROM job_table ORDER BY publication_date DESC LIMIT 10")
      fun getAllJobs(): LiveData<List<Job>>

    @Query("SELECT * FROM job_table  ORDER BY publication_date DESC")
     fun getLiveDataMarked(): LiveData<List<Job>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note : Job):Long

    @Query("DELETE FROM job_table WHERE id = :id")
    suspend fun delete(id : Int):Int





    @Query("SELECT * FROM job_table WHERE title LIKE :keyword OR company_name LIKE :keyword OR category LIKE :keyword")
   suspend fun searchNotes(keyword :String): List<Job>
}