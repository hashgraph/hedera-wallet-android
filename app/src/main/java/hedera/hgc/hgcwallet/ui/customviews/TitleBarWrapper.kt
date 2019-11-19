/*
 *
 *  Copyright 2019 Hedera Hashgraph LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
