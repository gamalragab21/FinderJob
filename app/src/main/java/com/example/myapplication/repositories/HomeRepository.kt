package com.example.myapplication.repositories

import androidx.lifecycle.LiveData
import com.example.myapplication.data.network.ApiJobService
import com.example.myapplication.helpers.Resource
import com.example.myapplication.models.Job
import com.example.myapplication.models.ParentJob
import com.example.myapplication.qualifiers.IOThread
import com.example.myapplication.utils.safeCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import myappnew.com.conserve.data.JobDao
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiJobService: ApiJobService,
    private val jobDao: JobDao,
    @IOThread
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun getListJob(currentJob: Int):Resource<ParentJob> = withContext(dispatcher){
        safeCall {
            val result=apiJobService.getListJobs(currentJob)
            Resource.Success(result)
        }
    }

    suspend fun getSearchListJob(limitJob: Int, currentKeyWord: String): Resource<ParentJob> = withContext(dispatcher) {
           safeCall {
               val result=apiJobService.searchJobList(limitJob,currentKeyWord)
               Resource.Success(result)
           }
    }

  suspend  fun insertMarkedJob(job: Job): Resource<Long> = withContext(dispatcher){
      safeCall {
        val result=jobDao.insertNote(job)
          Resource.Success(result)
      }
  }
    suspend  fun deleteJob(job: Job): Resource<Int> = withContext(dispatcher){
      safeCall {
        val result=jobDao.delete(job.id)
          Resource.Success(result)
      }
  }


     fun   getMarkerListLimmited(): LiveData<List<Job>> = jobDao.getAllJobs()

     fun   getMarkerList(): LiveData<List<Job>> = jobDao.getLiveDataMarked()

     fun   getIdsList(): LiveData<List<Int>> = jobDao.getIds()

//   suspend fun getMarkerList(): Resource<List<Job>> = withContext(dispatcher){
//       safeCall {
//           val result=jobDao.getAllNote()
//           Resource.Success(result)
//       }
//   }

}