package com.example.myapplication.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.myapplication.databinding.ItemContainrJobShowBinding
import com.example.myapplication.models.Job
import javax.inject.Inject
import androidx.annotation.Nullable
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.example.myapplication.R
import com.example.myapplication.ui.viewmodels.HomeViewModel
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.dateFormatter
import com.squareup.picasso.Picasso
import java.sql.Time
import java.util.*


class JobsAdapter @Inject constructor(
    private val glide: RequestManager,
    private val context: Context,
) : RecyclerView.Adapter<JobsAdapter.SavedViewHolder>() {


    private lateinit var bindingAdapter: ItemContainrJobShowBinding



    var jobs: List<Job>
        get() = differ.currentList
        set(value) = differ.submitList(value)

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
            updateMark(job.is_mark)
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
                job.job_type.toString()
            else "unknown"
            bindingAdapter.tvCreateAt.text = if (!job.publication_date.isNullOrEmpty())

               Constants.getTimeAgo( dateFormatter(job.publication_date),context)

            else "unknown"
        }

        private fun loadPhotoImageCompany(companyLogoUrl: String?) {
            Log.i("GamalJob", "onBindViewHolder: $companyLogoUrl")

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




            bindData(job)

            bindingAdapter.rootClickable.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(job)
                }
            }


            bindingAdapter.mark.setOnClickListener {
                onItemMarkerClickListener?.let { click ->
                    click(job,position)
                }
            }

        }
    }


    override fun getItemCount(): Int = jobs.size

    private var onItemClickListener: ((Job) -> Unit)? = null

    fun setOnItemClickListener(listener: (Job) -> Unit) {
        onItemClickListener = listener
    }
    private var onItemMarkerClickListener: ((Job,Int) -> Unit)? = null

    fun setOnItemMarkerClickListener(listener: (Job,Int) -> Unit) {
        onItemMarkerClickListener = listener
    }


    fun updateMark(isMarker:Int){
        if (isMarker<=0) bindingAdapter.mark.setImageResource(R.drawable.ic_mark)
        else bindingAdapter.mark.setImageResource(R.drawable.ic_marked)
    }


}