package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ListJobsFragmentBinding
import com.example.myapplication.databinding.SplashFragmentBinding
import com.example.myapplication.helpers.EventObserver
import com.example.myapplication.models.Job
import com.example.myapplication.ui.adapters.JobsAdapter
import com.example.myapplication.ui.adapters.MarkerAdapter
import com.example.myapplication.ui.viewmodels.HomeViewModel
import com.example.myapplication.ui.viewmodels.SearchViewModel
import com.example.myapplication.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ListJobsFragment : Fragment() {
    private var titleCurrentJob: String = ""

    private var _binding: ListJobsFragmentBinding? = null

    private val binding get() = _binding!!

    private  var currentKeyWord: String?=null

    private val searchViewModel: SearchViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private var currentJob: Int = 40
    private var totalAvailableJobs: Int = 1

    private val args:ListJobsFragmentArgs by navArgs()

    val jobs: ArrayList<Job> by lazy {
        ArrayList()
    }

    @Inject
    lateinit var jobsAdapter: JobsAdapter




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ListJobsFragmentBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        val appCompatActivity = activity as AppCompatActivity?
        appCompatActivity!!.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return binding.root
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        jobsAdapter.setOnItemClickListener { job ->

            val action = ListJobsFragmentDirections.actionListJobsFragmentToShowDetailsFragment(job)
            findNavController().navigate(action)

        }
        jobsAdapter.setOnItemMarkerClickListener { job, pos ,ima->
            Log.i("GAMALRAGAB", "setOnItemMarkerClickListener ${  job.is_mark } $pos")

            titleCurrentJob = job.title ?: ""
            if (!job.is_mark) {
                // saved
                homeViewModel.insertMarkedJob(job)
//                updateMark(1, imageMark)
//                jobsAdapter.notifyItemChanged(pos)
            } else {
                // un saved
                homeViewModel.deleteMarkedJob(job)
//                updateMark(0, imageMark)
//                jobsAdapter.notifyItemChanged(pos)
            }
        }

        binding.swipeRefresh.post {
            subscribeToObservers()
            setupRecyclerViewJob()


            args.keyWord?.let {keywords->
                binding.toolbar.title="Search Jobs $keywords"
                currentKeyWord=keywords
                getJobs(keywords)
            }.let {
                if (args.keyWord==null)
                {
                    binding.toolbar.title="Your Marked Jobs"
                    getAllMarked()
                }
                }



        }

        binding.swipeRefresh.setOnRefreshListener {
            Log.i("GAMALRAGAB", "onViewCreated: $currentKeyWord")
             currentKeyWord?.let {
                 getJobs(it)
             }.let {
                 if (currentKeyWord.isNullOrEmpty())  getAllMarked()
             }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!binding.recyclerView.canScrollVertically(1)) {
                    if (currentJob <= totalAvailableJobs) {
                        currentJob += 40
                        getJobs(currentKeyWord?:"")
                    }
                }


            }
//
        })




    }



    private fun getAllMarked() {
        searchViewModel.data.observe(viewLifecycleOwner, { markerJob ->
            binding.shimmer.stopShimmer()
            binding.shimmer.isVisible = false
            binding.swipeRefresh.isRefreshing=false
            if (markerJob.isEmpty()){

                binding.emptyView.isVisible = true
            }else{


                binding.emptyView.isVisible = false
                jobsAdapter.homeViewModel=homeViewModel
            jobsAdapter.jobs = markerJob
            jobsAdapter.notifyDataSetChanged()}
        })
    }


    private fun subscribeToObservers() {

        searchViewModel.listJobStatus.observe(viewLifecycleOwner, EventObserver(
            onLoading = {
                if(currentJob<=40) {
                    binding.shimmer.startShimmer()
                    binding.shimmer.isVisible = true
                    binding.swipeRefresh.isRefreshing=true
                }
            },
            onError = {
                binding.shimmer.stopShimmer()
                binding.shimmer.isVisible = false
                binding.swipeRefresh.isRefreshing=false

                binding.emptyView.isVisible = true
                binding.textEmptyErr.text = it

                //  TransparentProgressDialog.hideProgress()
                snackbar(it)
                // binding.homeProgressMore.isVisible=false
            }
        ) { parentJobs ->
            binding.shimmer.stopShimmer()
            binding.shimmer.isVisible = false
            binding.swipeRefresh.isRefreshing=false

            //  binding.homeProgressMore.isVisible=false

            //   TransparentProgressDialog.hideProgress()


            binding.emptyView.isVisible = parentJobs.jobs?.isEmpty() ?: false
            totalAvailableJobs = parentJobs.job_count!!

            parentJobs.jobs?.let { newJobs ->
                binding.toolbar.title="Search Jobs $currentKeyWord (${parentJobs.job_count})"
                val oldCount: Int = jobs.size
                jobs.addAll(newJobs)
                jobsAdapter.homeViewModel=homeViewModel
                jobsAdapter.jobs = jobs
                jobsAdapter.notifyItemRangeChanged(oldCount, jobs.size)

            }

        })

        // insert marked job

        homeViewModel.insertJobStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                snackbar(it)
            },
            onLoading = {

            }, {
                if (it > 0) {
                    snackbar("\uD83D\uDE0D Marked $titleCurrentJob")
                    //imageMarkerJobsAdapter.setImageResource(R.drawable.ic_marked)
                }
            })
        )

        homeViewModel.deleteJobStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                snackbar(it)
            },
            onLoading = {

            }, {
                if (it > 0) snackbar("\uD83D\uDE0D UnMarked $titleCurrentJob")
            })
        )


    }

    private fun setupRecyclerViewJob()=binding.recyclerView.apply {
        itemAnimator = null
        isNestedScrollingEnabled = false
        layoutManager = LinearLayoutManager(requireContext())
        adapter = jobsAdapter
    }

    private fun getJobs(currentKeyWord: String) {
     searchViewModel.searchJob(currentJob,currentKeyWord)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}