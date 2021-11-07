package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.helpers.Event
import com.example.myapplication.helpers.Resource
import com.example.myapplication.models.ParentJob
import com.example.myapplication.qualifiers.MainThread
import com.example.myapplication.repositories.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchViewModel
@Inject constructor(
private val repository: HomeRepository,
@MainThread
private val dispatcher: CoroutineDispatcher

): ViewModel() {
    private val _listJobStatus= MutableLiveData<Event<Resource<ParentJob>>>()
    val listJobStatus: LiveData<Event<Resource<ParentJob>>> =_listJobStatus

    val data=repository.getMarkerList()

    fun searchJob(limitJob:Int,currentKeyWord: String) {
        _listJobStatus.postValue(Event(Resource.Loading()))

        viewModelScope.launch(dispatcher) {
            val result=repository.getSearchListJob(limitJob,currentKeyWord)
            _listJobStatus.postValue(Event(result))
        }
    }






}