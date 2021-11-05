package com.example.myapplication.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ParentJob(
    @SerializedName("0-legal-notice")
    @Expose
    val legal_notice: String?,

    @SerializedName("job-count")
    @Expose
    val job_count: Int?,

    @SerializedName("jobs")
    @Expose
    val jobs: ArrayList<Job>?
)