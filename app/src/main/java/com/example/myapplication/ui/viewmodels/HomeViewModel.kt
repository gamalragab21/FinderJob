package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.*
import com.example.myapplication.helpers.Event
import com.example.myapplication.helpers.Resource
import com.example.myapplication.models.Job
import com.example.myapplication.models.ParentJob
import com.example.myapplication.qualifiers.MainThread
import com.example.myapplication.repositories.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject constructor(
        private val repository: HomeRepository,
        @MainThread
        private val dispatcher: CoroutineDispatcher

    ):ViewModel() {

    private val _listJobStatus= MutableLiveData<Event<Resource<ParentJob>>>()
    val listJobStatus: LiveData<Event<Resource<ParentJob>>> =_listJobStatus

    private val _insertJobStatus= MutableLiveData<Event<Resource<Long>>>()
    val insertJobStatus: LiveData<Event<Resource<Long>>> =_insertJobStatus

    private val _listJobMarkerStatus= MutableLiveData<Event<Resource<List<Job>>>>()
    val listJobMarkerStatus: LiveData<Event<Resource<List<Job>>>> =_listJobMarkerStatus

        fun  getAllJobs(currentJob: Int) {

            _listJobStatus.postValue(Event(Resource.Loading()))

            viewModelScope.launch(dispatcher) {
                val result=repository.getListJob(currentJob)
                _listJobStatus.postValue(Event(result))
            }

        }

    fun  getAllMarkerJobs() {

        _listJobMarkerStatus.postValue(Event(Resource.Loading()))

        viewModelScope.launch(dispatcher) {
            val result=repository.getMarkerList()
            _listJobMarkerStatus.postValue(Event(result))
        }

    }

    fun insertMarkedJob(job: Job) {

            _insertJobStatus.postValue(Event(Resource.Loading()))

            viewModelScope.launch (dispatcher){
                val result=repository.insertMarkedJob(job)
                _insertJobStatus.postValue(Event(result))

            }


    }


}