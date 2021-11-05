package com.example.myapplication.helpers

import android.app.Dialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ProgressDialogeBinding
import dagger.hilt.android.qualifiers.ApplicationContext

object TransparentProgressDialog {
    lateinit var binding: ProgressDialogeBinding


    lateinit var dialog: CustomDialog

    fun show(@ApplicationContext context: Context): Dialog {
        return show(context, null)
    }

    fun show(context: Context, title: CharSequence?): Dialog {
        binding =ProgressDialogeBinding.inflate(LayoutInflater.from(context),null,false)


        if (title != null) {
            binding.cpTitle.text = title
        }

        // Card Color
        binding.cpCardview.setCardBackgroundColor(Color.parseColor("#70000000"))

        // Progress Bar Color
        setColorFilter(binding.cpPbar.indeterminateDrawable, ResourcesCompat.getColor(context.resources,  R.color.colorPrimary, null))

        // Text Color
        binding.cpTitle.setTextColor(Color.WHITE)

        dialog = CustomDialog(context)
        dialog.setContentView(binding.root)
        dialog.show()
        return dialog
    }

    private fun setColorFilter(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    class CustomDialog(context: Context) : Dialog(context) {
        init {
            // Set Semi-Transparent Color for Dialog Background
            //   window?.decorView?.rootView?.setBackgroundResource( gamal.myappnew.com.fooddoor.R.color.dialogBackground)
            window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
                insets.consumeSystemWindowInsets()
            }
        }
    }

    fun hideProgress() {
        return dialog.dismiss()
    }
}