package hedera.hgc.hgcwallet.ui.auth.pin

import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView

import com.andrognito.pinlockview.IndicatorDots
import com.andrognito.pinlockview.PinLockListener
import com.andrognito.pinlockview.PinLockView
import hedera.hgc.hgcwallet.Config

import hedera.hgc.hgcwallet.R

import hedera.hgc.hgcwallet.common.BaseTask
import hedera.hgc.hgcwallet.common.Singleton
import hedera.hgc.hgcwallet.common.TaskExecutor
import hedera.hgc.hgcwallet.local_auth.AuthManager

class PinAuthActivity : AppCompatActivity() {

    private var pinLockView: PinLockView? = null
    private var mIndicatorDots: IndicatorDots? = null
    private var previousPin: String? = null
    private var forSetup = false
    private var mTextViewHgcIcon: TextView? = null
    private var mTextViewProfileName: TextView? = null
    private var tvError: TextView? = null
    private var tvMessage: TextView? = null

    private val mPinLockListener = object : PinLockListener {
        override fun onComplete(pin: String) {
            if (forSetup) {
                if (previousPin != null) {
                    if (previousPin != pin) {
                        showMessage("Previous pin and new pin did not match. Please try again.", true)
                        previousPin = null
                        resetPinView()

                    } else {
                        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
                        progressBar.visibility = View.VISIBLE
                        mTextViewHgcIcon!!.visibility = View.INVISIBLE
                        val taskExecutor = TaskExecutor()
                        taskExecutor.setListner { task1 ->
                            if (task1.error == null) {
                                Singleton.setPinLength(pin.length)
                                setResult(RESULT_OK)
                                finish()
                            } else {
                                task1.error?.let { showMessage(it, true) }
                                resetPinView()
                                progressBar.visibility = View.GONE
                                mTextViewHgcIcon!!.visibility = View.VISIBLE
                            }
                        }
                        taskExecutor.execute(object : BaseTask() {
                            override fun main() {
                                try {
                                    AuthManager.setPIN(pin)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    error = "Something went wrong"
                                }

                            }
                        })


                    }
                } else {
                    previousPin = pin
                    showMessage("Please enter your PIN again.", false)
                    resetPinView()
                }
            } else {
                val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
                progressBar.visibility = View.VISIBLE
                mTextViewHgcIcon!!.visibility = View.INVISIBLE
                val taskExecutor = TaskExecutor()
                taskExecutor.setListner { task1 ->
                    if (task1.error == null) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        task1.error?.let { showMessage(it, true) }
                        resetPinView()
                        progressBar.visibility = View.GONE
                        mTextViewHgcIcon!!.visibility = View.VISIBLE
                    }
                }
                taskExecutor.execute(object : BaseTask() {
                    override fun main() {
                        try {
                            if (!AuthManager.verifyPIN(pin)) {
                                error = "Wrong PIN. Please try again."
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            error = "Something went wrong"
                        }

                    }
                })
            }
        }

        override fun onEmpty() {

        }

        override fun onPinChange(pinLength: Int, intermediatePin: String) {
            val errorTV = findViewById<TextView>(R.id.error_message)
            errorTV.visibility = View.GONE
        }
    }

    private fun showMessage(msg: String, isError: Boolean) {
        if (isError) {
            tvError?.apply {
                text = msg
                visibility = View.VISIBLE
            }
            tvMessage?.visibility = View.GONE
        } else {
            tvError?.visibility = View.GONE
            tvMessage?.apply {
                text = msg
                visibility = View.VISIBLE
            }

        }
    }

    private fun resetPinView() {
        val handler = Handler()
        handler.postDelayed({ pinLockView!!.resetPinLockView() }, 300)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_pin_auth)
        if (intent.getStringExtra(EXTRA_TYPE) == TYPE_ENABLE)
            forSetup = true


        mIndicatorDots = findViewById<IndicatorDots>(R.id.indicator_dots)?.apply {
            indicatorType = IndicatorDots.IndicatorType.FILL_WITH_ANIMATION
            pinLength = getPinAuthLength()
        }

        pinLockView = findViewById<PinLockView>(R.id.pin_lock_view)?.apply {
            attachIndicatorDots(mIndicatorDots)
            setPinLockListener(mPinLockListener)
            pinLength = getPinAuthLength()
            textColor = ContextCompat.getColor(this@PinAuthActivity, R.color.white)
        }


        mTextViewHgcIcon = findViewById<View>(R.id.text_hgc_icon) as TextView
        mTextViewProfileName = findViewById<TextView>(R.id.profile_name)?.apply {
            visibility = View.GONE
        }

        tvError = findViewById<TextView>(R.id.error_message)
        tvMessage = findViewById<TextView>(R.id.message)
        showMessage(resources.getString(if (forSetup) R.string.create_pin else R.string.enter_pin), false)
    }

    private fun getPinAuthLength(): Int {

        return if (forSetup) Config.passcodeLength else Singleton.getPinLength()

    }

    companion object {

        val TAG = "PinLockView"
        val EXTRA_TYPE = "EXTRA_TYPE"
        val TYPE_UNLOCK = "TYPE_UNLOCK"
        val TYPE_ENABLE = "TYPE_ENABLE"
    }
}
