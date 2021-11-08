package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.HomeFragmentBinding
import com.example.myapplication.helpers.EventObserver
import com.example.myapplication.models.Job
import com.example.myapplication.ui.adapters.JobsAdapter
import com.example.myapplication.ui.adapters.MarkerAdapter
import com.example.myapplication.ui.viewmodels.HomeViewModel
import com.example.myapplication.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collect
import android.widget.Toast

import com.example.myapplication.ui.activities.MainActivity
import com.example.myapplication.utils.SessionManger
import com.example.myapplication.utils.setupTheme

import com.haerul.bottomfluxdialog.BottomFluxDialog
import com.haerul.bottomfluxdialog.BottomFluxDialog.OnInputListener
import androidx.core.graphics.drawable.DrawableCompat

import android.graphics.drawable.Drawable

import android.R
import android.graphics.Color

import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat


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

    private var titleCurrentJob: String = ""


    val jobs: ArrayList<Job> by lazy {
        ArrayList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        checkTheme()

        getJobs()
        setupRecyclerViewJob()
        setupRecyclerViewMarked()
        subscribeToObservers()

        binding.swipeRefresh.setOnRefreshListener {
            getJobs()
        }


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

        binding.notif.setOnClickListener {
            if (SessionManger.getMyTheme(requireActivity())) {
                // dark to light
                SessionManger.setThemeMode(requireActivity(), false)
                val unwrappedDrawable =
                    AppCompatResources.getDrawable(
                        requireContext(),
                        com.example.myapplication.R.drawable.ic_round_dark_mode_24
                    )
                val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
                DrawableCompat.setTint(wrappedDrawable, Color.BLACK)
                binding.notif.setImageDrawable(wrappedDrawable)

                setupTheme(false)

            } else {
                //light to dark
                SessionManger.setThemeMode(requireActivity(), true)
                val unwrappedDrawable =
                    AppCompatResources.getDrawable(
                        requireContext(),
                        com.example.myapplication.R.drawable.ic_baseline_light_mode_24
                    )
                val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
                DrawableCompat.setTint(wrappedDrawable, Color.WHITE)
                binding.notif.setImageDrawable(wrappedDrawable)

                setupTheme(true)
            }
        }

        jobsAdapter.setOnItemClickListener { job ->

            val action = HomeFragmentDirections.actionHomeFragmentToShowDetailsFragment(job)
            findNavController().navigate(action)

        }

        jobsAdapter.setOnItemMarkerClickListener { job, pos ,image->
            Log.i("GAMALRAGAB", "setOnItemMarkerClickListener ${  job.is_mark } $pos")

            titleCurrentJob = job.title ?: ""
            if (!job.is_mark) {
                // saved
                homeViewMode.insertMarkedJob(job)
//                updateMark(1, imageMark)
//                jobsAdapter.notifyItemChanged(pos)
                updateMark(1,image)
                setBackGround(true,image)
            } else {
                // un saved
                homeViewMode.deleteMarkedJob(job)
                updateMark(0,image)
                setBackGround(false,image)
//                updateMark(0, imageMark)
//                jobsAdapter.notifyItemChanged(pos)
            }
        }

        markerAdapter.setOnItemMarkedClickListener { job ->
            titleCurrentJob = job.title ?: ""
            homeViewMode.deleteMarkedJob(job)
        }

        markerAdapter.setOnItemClickListener { markedJob ->
            val action = HomeFragmentDirections.actionHomeFragmentToShowDetailsFragment(markedJob)
            findNavController().navigate(action)
        }

        binding.search.setOnClickListener {
            BottomFluxDialog.inputDialog(requireActivity())
                .setTextTitle("Search")
                .setTextMessage("What are looking for?")
                .setRightButtonText("SUBMIT")
                .setInputListener(object : OnInputListener {
                    override fun onSubmitInput(text: String) {
                        if (text.isNotEmpty()) {
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToListJobsFragment(text)
                            findNavController().navigate(action)
                        } else {
                            snackbar("All Fields require")
                        }
                    }

                    override fun onCancelInput() {
                        Toast.makeText(
                            requireContext(),
                            "Button Cancel Clicked!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
                .show()

        }

        binding.showAllMarked.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToListJobsFragment(null)
            findNavController().navigate(action)
        }
    }

    private fun updateMark(isMarker: Int, mark: ImageView) {
        if (isMarker == 0) {
            mark.setImageResource(com.example.myapplication.R.drawable.ic_mark)
            mark.tag = "mark"
        } else {
            mark.setImageResource(com.example.myapplication.R.drawable.ic_marked)
            mark.tag = "marked"
        }
    }

    private fun checkTheme() {
        if (SessionManger.getMyTheme(requireActivity())) {

            val unwrappedDrawable =
                AppCompatResources.getDrawable(
                    requireContext(),
                    com.example.myapplication.R.drawable.ic_baseline_light_mode_24
                )
            val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
            DrawableCompat.setTint(wrappedDrawable, Color.WHITE)
            binding.notif.setImageDrawable(wrappedDrawable)

        } else {
            val unwrappedDrawable =
                AppCompatResources.getDrawable(
                    requireContext(),
                    com.example.myapplication.R.drawable.ic_round_dark_mode_24
                )
            val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
            DrawableCompat.setTint(wrappedDrawable, Color.BLACK)
            binding.notif.setImageDrawable(wrappedDrawable)
        }
    }


    private fun subscribeToObservers() {
        homeViewMode.listJobStatus.observe(viewLifecycleOwner, EventObserver(
            onLoading = {
                if (currentJob <= 40) {
                    binding.shimmer.startShimmer()
                    binding.shimmer.isVisible = true
                    binding.swipeRefresh.isRefreshing = true
                }
            },
            onError = {
                binding.shimmer.stopShimmer()
                binding.shimmer.isVisible = false
                binding.swipeRefresh.isRefreshing = false

                binding.emptyView.isVisible = true
                binding.textEmptyErr.text = it

                //  TransparentProgressDialog.hideProgress()
                snackbar(it)
                // binding.homeProgressMore.isVisible=false
            }
        ) { parentJobs ->
            binding.shimmer.stopShimmer()
            binding.shimmer.isVisible = false
            binding.swipeRefresh.isRefreshing = false

            //  binding.homeProgressMore.isVisible=false

            //   TransparentProgressDialog.hideProgress()


            binding.emptyView.isVisible = parentJobs.jobs?.isEmpty() ?: false
            totalAvailableJobs = parentJobs.job_count!!
            binding.recommendedTitle.isVisible = parentJobs.job_count > 0

            parentJobs.jobs?.let { newJobs ->

                val oldCount: Int = jobs.size
                jobs.addAll(newJobs)
                jobsAdapter.homeViewModel=homeViewMode
                jobsAdapter.jobs = jobs
                jobsAdapter.notifyItemRangeChanged(oldCount, jobs.size)

                getMarkedLiveDataJobs()
            }

        })


        // insert marked job

        homeViewMode.insertJobStatus.observe(viewLifecycleOwner, EventObserver(
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

        homeViewMode.deleteJobStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                snackbar(it)
            },
            onLoading = {

            }, {
                if (it > 0) snackbar("\uD83D\uDE0D UnMarked $titleCurrentJob")
            })
        )


//        homeViewMode.listJobMarkerStatus.observe(viewLifecycleOwner, EventObserver(onLoading = {
//
//            },
//            onError = {
//
//
//                snackbar(it)
//            }
//        ) { markerJob ->
//            binding.markedTitle.isVisible = markerJob.size > 0
//            markerAdapter.markerJobs = markerJob
//            markerAdapter.notifyDataSetChanged()
//
//        })


    }

    private fun setBackGround(isMark: Boolean, mark: ImageView) {
        val sdk = android.os.Build.VERSION.SDK_INT;

        if (isMark) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                mark.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        com.example.myapplication.R.drawable.ic_round_search_24
                    )
                );
            } else {
                mark.background = ContextCompat.getDrawable(requireContext(), com.example.myapplication.R.drawable.ic_round_search_24);
            }
        } else {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                mark.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        com.example.myapplication.R.drawable.ic_mark
                    )
                );
            } else {
                mark.background = ContextCompat.getDrawable(requireContext(), com.example.myapplication.R.drawable.ic_mark);
            }
        }
    }


    private fun getMarkedLiveDataJobs() {


        homeViewMode.data.observe(viewLifecycleOwner, { markerJob ->
            binding.markedTitle.isVisible = markerJob.size > 0
            binding.showAllMarked.isVisible = markerJob.size > 0
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
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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