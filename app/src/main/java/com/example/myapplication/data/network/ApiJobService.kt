package com.example.myapplication.data.network

import com.example.myapplication.helpers.Resource
import com.example.myapplication.models.ParentJob
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiJobService {

    @GET("remote-jobs")
    suspend fun getListJobs(@Query("limit")  page:Int): ParentJob
}