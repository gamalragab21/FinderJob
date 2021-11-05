package com.example.myapplication.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Job(

    @SerializedName("candidate_required_location")
    @Expose
    val candidate_required_location: String,

    @SerializedName("category")
    @Expose
    val category: String,

    @SerializedName("company_logo_url")
    @Expose
    val company_logo_url: String,

    @SerializedName("company_name")
    @Expose
    val company_name: String,

    @SerializedName("description")
    @Expose
    val description: String,

    @SerializedName("id")
    @Expose
    val id: Int,

    @SerializedName("job_type")
    @Expose
    val job_type: String,

    @SerializedName("publication_date")
    @Expose
    val publication_date: String,

    @SerializedName("salary")
    @Expose
    val salary: String,

    @SerializedName("tags")
    @Expose
    val tags: List<String>,

    @SerializedName("title")
    @Expose
    val title: String,

    @SerializedName("url")
    @Expose
    val url: String
)