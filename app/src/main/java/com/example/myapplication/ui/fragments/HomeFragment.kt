package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.HomeFragmentBinding
import com.example.myapplication.helpers.EventObserver
import com.example.myapplication.helpers.TransparentProgressDialog
import com.example.myapplication.models.Job
import com.example.myapplication.ui.adapters.JobsAdapter
import com.example.myapplication.ui.viewmodels.HomeViewModel
import com.example.myapplication.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_fragment.*
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null

    private val homeViewMode: HomeViewModel by viewModels()

    private var currentJob: Int = 40
    private var totalAvailableJobs: Int = 1

    private val binding get() = _binding!!

    @Inject
    lateinit var jobsAdapter: JobsAdapter

    val jobs: ArrayList<Job> by lazy {
        ArrayList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.swipeRefresh.post {
            getJobs()
            setupRecyclerView()
            subscribeToObservers()

        }

        binding.swipeRefresh.setOnRefreshListener {
            jobs.clear()
            jobsAdapter.jobs=jobs
            jobsAdapter.notifyDataSetChanged()
            getJobs()
        }









        binding.recyclerviewJobs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!binding.recyclerviewJobs.canScrollVertically(1)) {
                    if (currentJob <= totalAvailableJobs) {
                        currentJob += 40
                        getJobs()
                    }
                }


            }

        })


        jobsAdapter.setOnItemClickListener { job ->

            Log.i("GamalJob", "onViewCreated: ${job.company_logo_url}")

        }

    }

    private fun subscribeToObservers() {
        homeViewMode.listJobStatus.observe(viewLifecycleOwner, EventObserver(
            onLoading = {
                binding.swipeRefresh.isRefreshing = true
                binding.shimmer.startShimmer()
                binding.shimmer.isVisible=true
                //TransparentProgressDialog.show(requireContext())
                Log.d("herejobs", "loading")
                // binding.homeProgressMore.isVisible = currentJob > 40

            },
            onError = {
                binding.swipeRefresh.isRefreshing = false
                binding.shimmer.stopShimmer()
                binding.shimmer.isVisible=false

                binding.emptyView.isVisible=true
                binding.textEmptyErr.text=it

                //  TransparentProgressDialog.hideProgress()
                Log.d("herejobserror", it)
                snackbar(it)
                // binding.homeProgressMore.isVisible=false
            }
        ) { parentJobs ->
            binding.swipeRefresh.isRefreshing = false
            binding.shimmer.stopShimmer()
            binding.shimmer.isVisible=false
            //  binding.homeProgressMore.isVisible=false

            //   TransparentProgressDialog.hideProgress()
            Log.d("herejobs", parentJobs.job_count.toString())


             binding.emptyView.isVisible=parentJobs.jobs?.isEmpty()?:false
                totalAvailableJobs = parentJobs.job_count!!

                parentJobs.jobs?.let { jobs ->
                    val oldCount: Int = jobs.size
                    jobs.addAll(jobs)
                    jobsAdapter.jobs = jobs
                    jobsAdapter.notifyItemRangeChanged(oldCount, jobs.size)
                }

        })

    }


    private fun setupRecyclerView() = binding.recyclerviewJobs.apply {
        itemAnimator = null
        isNestedScrollingEnabled = false
        layoutManager = LinearLayoutManager(requireContext())
        adapter = jobsAdapter

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