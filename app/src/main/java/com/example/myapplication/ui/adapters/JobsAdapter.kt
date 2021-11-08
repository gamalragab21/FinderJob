package com.example.myapplication.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.myapplication.databinding.ItemContainrJobShowBinding
import com.example.myapplication.models.Job
import javax.inject.Inject
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.ui.viewmodels.HomeViewModel
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.dateFormatter


class JobsAdapter @Inject constructor(
    private val glide: RequestManager,
    private val context: Context,
) : RecyclerView.Adapter<JobsAdapter.SavedViewHolder>() {


    private lateinit var bindingAdapter: ItemContainrJobShowBinding


    var jobs: List<Job>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    lateinit var homeViewModel: HomeViewModel

    private val diffCallback = object : DiffUtil.ItemCallback<Job>() {
        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

    }
    private val differ = AsyncListDiffer(this, diffCallback)

    inner class SavedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bindData(job: Job) {
            loadPhotoImageCompany(job.company_logo_url)

            bindingAdapter.tvJobTitle.text = if (!job.title.isNullOrEmpty())
                job.title.toString()
            else "unknown"

            bindingAdapter.tvCompanyName.text = if (!job.company_name.isNullOrEmpty())
                job.company_name.toString()
            else "unknown"
            bindingAdapter.tvCompanyLocation.text =
                if (!job.candidate_required_location.isNullOrEmpty())
                    job.candidate_required_location.toString()
                else "unknown"
            bindingAdapter.tvJobType.text = if (!job.job_type.isNullOrEmpty())
                if (job.job_type.toString() == "full_time") "Full Time" else "Part Time"
            else "unknown"
            bindingAdapter.tvCreateAt.text = if (!job.publication_date.isNullOrEmpty())

                Constants.getTimeAgo(dateFormatter(job.publication_date), context)
            else "unknown"
        }

        private fun loadPhotoImageCompany(companyLogoUrl: String?) {

            if (companyLogoUrl.isNullOrEmpty()) {
                bindingAdapter.progressShimmer.stopShimmer()
                bindingAdapter.progressShimmer.visibility = View.GONE
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
                            bindingAdapter.progressShimmer.stopShimmer()
                            bindingAdapter.progressShimmer.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any,
                            target: Target<Drawable?>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            bindingAdapter.progressShimmer.stopShimmer()
                            bindingAdapter.progressShimmer.visibility = View.GONE

                            return false
                        }
                    }).into(bindingAdapter.photoPreview)
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedViewHolder {
        bindingAdapter =
            ItemContainrJobShowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavedViewHolder(
            bindingAdapter.root
        )
    }

    @SuppressLint("Range")
    override fun onBindViewHolder(holder: SavedViewHolder, position: Int) {

        val job = jobs[position]


        holder.apply {
            homeViewModel.dataIds.observeForever {
                it?.let { list ->
                    if (list.contains(job.id)) {
                        // isSaved
                        setBackGround(true, bindingAdapter.markSaved)
                        updateMark(1, bindingAdapter.markSaved,position)
                        job.is_mark = true

                    } else {

                        updateMark(0, bindingAdapter.markSaved,position)
                         setBackGround(false, bindingAdapter.markSaved)
                        job.is_mark = false
                    }

                }
            }


            bindData(job)

            bindingAdapter.rootClickable.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(job)
                }
            }


            bindingAdapter.markSaved.setOnClickListener {
                onItemMarkerClickListener?.let { click ->
                    click(job, position,bindingAdapter.markSaved)
                }
            }

        }
    }

    private fun setBackGround(isMark: Boolean, mark: ImageView) {
        val sdk = android.os.Build.VERSION.SDK_INT;

        if (isMark) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                mark.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_round_search_24
                    )
                );
            } else {
                mark.background = ContextCompat.getDrawable(context, R.drawable.ic_round_search_24);
            }
        } else {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                mark.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_mark
                    )
                );
            } else {
                mark.background = ContextCompat.getDrawable(context, R.drawable.ic_mark);
            }
        }
    }


    override fun getItemCount(): Int = jobs.size

    private var onItemClickListener: ((Job) -> Unit)? = null

    fun setOnItemClickListener(listener: (Job) -> Unit) {
        onItemClickListener = listener
    }

    private var onItemMarkerClickListener: ((Job, Int,ImageView) -> Unit)? = null

    fun setOnItemMarkerClickListener(listener: (Job, Int,ImageView) -> Unit) {
        onItemMarkerClickListener = listener
    }

    private fun updateMark(isMarker: Int, btnMark: ImageView, position: Int) {

        if (isMarker <= 0) btnMark.setImageResource(R.drawable.ic_mark)
        else btnMark.setImageResource(R.drawable.ic_marked)
    }

private fun clearObservable(){
    homeViewModel.data?.let {
        it.removeObserver {  }
    }
}
}