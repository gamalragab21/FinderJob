package myappnew.com.conserve.data

import androidx.room.*
import com.example.myapplication.models.Job


@Dao
interface JobDao {

    @Query("select * from job_table order by id desc")
    suspend fun getAllNote(): List<Job>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note : Job):Long

    @Query("DELETE FROM job_table WHERE id = :id")
    suspend fun delete(id : Int):Int





    @Query("SELECT * FROM job_table WHERE title LIKE :keyword OR company_name LIKE :keyword OR category LIKE :keyword")
   suspend fun searchNotes(keyword :String): List<Job>
}