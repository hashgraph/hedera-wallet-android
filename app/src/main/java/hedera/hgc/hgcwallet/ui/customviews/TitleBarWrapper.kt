package hedera.hgc.hgcwallet.ui.customviews

import android.view.View
import android.widget.ImageView
import android.widget.TextView

import hedera.hgc.hgcwallet.R

class TitleBarWrapper(mRootView: View) {
    private val mTextView: TextView
    private val mCloseButton: ImageView

    init {
        mTextView = mRootView.findViewById<TextView>(R.id.text_new_wallet)
        mCloseButton = mRootView.findViewById<ImageView>(R.id.image_close)
    }

    fun setTitle(title: String) {
        mTextView.text = title
    }

    fun setTitle(resId: Int) {
        mTextView.setText(resId)
    }

    fun setImageResource(image: Int) {
        mCloseButton.setImageResource(image)
    }


    fun setCloseButtonHidden(hidden: Boolean) {
        mCloseButton.visibility = if (hidden) View.INVISIBLE else View.VISIBLE
    }

    fun setOnCloseButtonClickListener(listener: (v: View?) -> Unit) {
        mCloseButton.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                listener(v)
            }

        })
    }
}
