package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.HomeFragmentBinding
import com.example.myapplication.helpers.EventObserver
import com.example.myapplication.models.Job
import com.example.myapplication.ui.adapters.JobsAdapter
import com.example.myapplication.ui.adapters.MarkerAdapter
import com.example.myapplication.ui.viewmodels.HomeViewModel
import com.example.myapplication.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.google.android.material.snackbar.Snackbar
import androidx.core.widget.NestedScrollView








@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null

    private val homeViewMode: HomeViewModel by viewModels()

    private var currentJob: Int = 40
    private var totalAvailableJobs: Int = 1

    private val binding get() = _binding!!

    @Inject
    lateinit var jobsAdapter: JobsAdapter

    @Inject
    lateinit var markerAdapter: MarkerAdapter

    private lateinit var titleCurrentJob:String

    val jobs: ArrayList<Job> by lazy {
        ArrayList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


            getJobs()
            homeViewMode.getAllMarkerJobs()
            setupRecyclerViewJob()
            setupRecyclerViewMarked()
            subscribeToObservers()
        binding.nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if (scrollY >= v.getChildAt(v.childCount - 1)
                        .measuredHeight - v.measuredHeight &&
                    scrollY > oldScrollY
                ) {
                    //code to fetch more data for endless scrolling
//                    binding.recyclerViewJobs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                            super.onScrolled(recyclerView, dx, dy)
//                            if (!binding.recyclerViewJobs.canScrollVertically(1)) {
                                if (currentJob <= totalAvailableJobs) {
                                    currentJob += 40
                                     getJobs()
                               }
//                            }
//
//
//                        }
//
//                    })

                }
            }
        })



        jobsAdapter.setOnItemClickListener { job ->



        }

        jobsAdapter.setOnItemMarkerClickListener { job->
            titleCurrentJob=job.title
            homeViewMode.insertMarkedJob(job)
        }

    }




    private fun subscribeToObservers() {
        homeViewMode.listJobStatus.observe(viewLifecycleOwner, EventObserver(
            onLoading = {
                if(currentJob<=40) {
                    binding.shimmer.startShimmer()
                    binding.shimmer.isVisible = true
                }
                //TransparentProgressDialog.show(requireContext())
                Log.d("herejobs", "loading")

                // binding.homeProgressMore.isVisible = currentJob > 40

            },
            onError = {
                binding.shimmer.stopShimmer()
                binding.shimmer.isVisible = false

                binding.emptyView.isVisible = true
                binding.textEmptyErr.text = it

                //  TransparentProgressDialog.hideProgress()
                Log.d("herejobserror", it)
                snackbar(it)
                // binding.homeProgressMore.isVisible=false
            }
        ) { parentJobs ->
            binding.shimmer.stopShimmer()
            binding.shimmer.isVisible = false
            //  binding.homeProgressMore.isVisible=false

            //   TransparentProgressDialog.hideProgress()
            Log.d("herejobs", parentJobs.job_count.toString())


            binding.emptyView.isVisible = parentJobs.jobs?.isEmpty() ?: false
            totalAvailableJobs = parentJobs.job_count!!
            binding.recommendedTitle.isVisible = parentJobs.job_count > 0

            parentJobs.jobs?.let { newJobs ->
                val oldCount: Int = jobs.size
                jobs.addAll(newJobs)
                jobsAdapter.jobs = jobs
                jobsAdapter.notifyItemRangeChanged(oldCount, jobs.size)
            }

        })


        // insert marked job

        homeViewMode.insertJobStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                snackbar(it)
            },
            onLoading = {

            }, {
//                Snackbar.make(
//                    binding.root,
//                    if (mark === 0) "\uD83D\uDE13 Unmark $title" else "\uD83D\uDE0D Marked $title",
//                    Snackbar.LENGTH_SHORT
//                ).show()

                if (it > 0) snackbar("\"\\uD83D\\uDE0D Marked $titleCurrentJob")
            })
        )

        homeViewMode.listJobMarkerStatus.observe(viewLifecycleOwner, EventObserver(
            onLoading = {

            },
            onError = {


                snackbar(it)
            }
        ) { markerJob ->

            binding.markedTitle.isVisible = markerJob.size > 0
            markerAdapter.markerJobs = markerJob
            markerAdapter.notifyDataSetChanged()

        })

    }


    private fun setupRecyclerViewJob() = binding.recyclerViewJobs.apply {
        itemAnimator = null
        isNestedScrollingEnabled = false
        layoutManager = LinearLayoutManager(requireContext())
        adapter = jobsAdapter

    }

    private fun setupRecyclerViewMarked() = binding.recyclerViewMarked.apply {
        itemAnimator = null
        isNestedScrollingEnabled = false
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
        adapter = markerAdapter

    }

    private fun getJobs() {
        homeViewMode.getAllJobs(currentJob)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}