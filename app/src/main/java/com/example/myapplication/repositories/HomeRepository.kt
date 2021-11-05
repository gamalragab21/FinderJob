package com.example.myapplication.repositories

import com.example.myapplication.data.network.ApiJobService
import com.example.myapplication.helpers.Resource
import com.example.myapplication.models.ParentJob
import com.example.myapplication.qualifiers.IOThread
import com.example.myapplication.utils.safeCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiJobService: ApiJobService,
    @IOThread
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun getListJob(currentJob: Int):Resource<ParentJob> = withContext(Dispatchers.IO){
        safeCall {
            val result=apiJobService.getListJobs(currentJob)
            Resource.Success(result)
        }
    }



}