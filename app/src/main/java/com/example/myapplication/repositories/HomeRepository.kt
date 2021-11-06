package com.example.myapplication.repositories

import com.example.myapplication.data.network.ApiJobService
import com.example.myapplication.helpers.Resource
import com.example.myapplication.models.Job
import com.example.myapplication.models.ParentJob
import com.example.myapplication.qualifiers.IOThread
import com.example.myapplication.utils.safeCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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

  suspend  fun insertMarkedJob(job: Job): Resource<Long> = withContext(dispatcher){
      safeCall {
        val result=jobDao.insertNote(job)
          Resource.Success(result)
      }
  }

   suspend fun getMarkerList(): Resource<List<Job>> = withContext(dispatcher){
       safeCall {
           val result=jobDao.getAllNote()
           Resource.Success(result)
       }
   }

}