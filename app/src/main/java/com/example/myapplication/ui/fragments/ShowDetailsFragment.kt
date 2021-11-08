package com.example.myapplication.ui.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.myapplication.R
import com.example.myapplication.databinding.ShowDetailsFragmentBinding
import com.example.myapplication.models.Job
import com.example.myapplication.ui.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.myapplication.databinding.DialogeInfoBinding
import com.example.myapplication.databinding.ItemContainrJobShowBinding
import com.example.myapplication.helpers.EventObserver
import com.example.myapplication.utils.snackbar
import com.google.android.material.bottomsheet.BottomSheetDialog


@AndroidEntryPoint
class ShowDetailsFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager

    val args: ShowDetailsFragmentArgs by navArgs()

    val homeViewModel: HomeViewModel by viewModels()
    private lateinit var titleCurrentJob: String

    private var _binding: ShowDetailsFragmentBinding? = null

    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        val appCompatActivity = activity as AppCompatActivity?
        appCompatActivity!!.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        args.job?.let { jobContent ->
            Log.i("ShowDetailsFragmentArgs", "onViewCreated: $jobContent")
            titleCurrentJob = jobContent.title ?: ""
            bindDataView(jobContent)


            binding.btnMark.setOnClickListener {
                if (jobContent.is_mark) {
                    homeViewModel.deleteMarkedJob(job = jobContent)
                } else {
                    homeViewModel.insertMarkedJob(jobContent)
                }
            }

            binding.btnHowToApply.setOnClickListener {
                showDialogueInfo(jobContent.url)


            }
        }
        binding.textReadMore.setOnClickListener {
            if ( binding.textReadMore.text.toString() == getString(R.string.read_more)) {
                binding.tvJobDescription.maxLines = Int.MAX_VALUE
                binding.tvJobDescription.ellipsize = null
                binding.textReadMore.text = getString(R.string.read_less)
            } else {
                binding.tvJobDescription.maxLines = 4
                binding.tvJobDescription.ellipsize = TextUtils.TruncateAt.END
                binding.textReadMore.text = getString(R.string.read_more)
            }
        }


        subscribeToObservables()
    }

    private fun showDialogueInfo(url: String?) {
        val bindingDialog:DialogeInfoBinding= DialogeInfoBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog=BottomSheetDialog(requireContext(),R.style.BottomSheetDialogStyle)
        dialog.setContentView(bindingDialog.root)
        bindingDialog.message.text=url
        dialog.show()

        bindingDialog.buttonClose.setOnClickListener {
           dialog.dismiss()
        }
        bindingDialog.buttonWebSite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

    }

    private fun subscribeToObservables() {
        homeViewModel.insertJobStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                snackbar(it)
            },
            onLoading = {

            }, {
                if (it > 0) {
                    snackbar("\uD83D\uDE0D Marked $titleCurrentJob")
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun bindDataView(jobContent: Job) {

        binding.toolbar.title = jobContent.category
        loadPhotoImageCompany(jobContent.company_logo_url)
        binding.tvJobTitle.text = jobContent.title
        binding.tvCompanyName.text = jobContent.company_name
        binding.tvJobType.text = if (!jobContent.job_type.isNullOrEmpty())
            jobContent.job_type.toString()
        else "unknown"
        binding.tvJobDescription.text =
            Html.fromHtml(jobContent.description, HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS)
        binding.tvCompanyNameInfo.text = jobContent.company_name
        binding.tvCompanyLocation.text =
            if (!jobContent.candidate_required_location.isNullOrEmpty())
                jobContent.candidate_required_location.toString()
            else "unknown"

        binding.tvCompanyUrl.text = if (!jobContent.url.isNullOrEmpty())
            jobContent.url.toString()
        else "unknown"

        homeViewModel.dataIds.observe(viewLifecycleOwner, { list ->
            list?.let {
                if (it.contains(jobContent.id)) updateMark(1)
                else updateMark(0)
            }


        })


    }

    private fun updateMark(isMarker: Int) {


        if (isMarker <= 0) binding.btnMark.setImageResource(R.drawable.ic_mark)
        else binding.btnMark.setImageResource(R.drawable.ic_marked)
    }

    private fun loadPhotoImageCompany(companyLogoUrl: String?) {

        if (companyLogoUrl.isNullOrEmpty()) {
            binding.progressShimmerPhoto.stopShimmer()
            binding.progressShimmerPhoto.visibility = View.GONE
        }
        companyLogoUrl?.let {
            glide.load(it)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .error(com.example.myapplication.R.drawable.ic_round_business_center_24)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        @Nullable e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressShimmerPhoto.stopShimmer()
                        binding.progressShimmerPhoto.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressShimmerPhoto.stopShimmer()
                        binding.progressShimmerPhoto.visibility = View.GONE

                        return false
                    }
                }).into(binding.photoPreview)
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = ShowDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}