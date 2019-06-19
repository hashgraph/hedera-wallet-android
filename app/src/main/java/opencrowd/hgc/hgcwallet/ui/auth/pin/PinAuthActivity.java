package opencrowd.hgc.hgcwallet.ui.auth.pin;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import opencrowd.hgc.hgcwallet.R;

import opencrowd.hgc.hgcwallet.common.BaseTask;
import opencrowd.hgc.hgcwallet.common.TaskExecutor;
import opencrowd.hgc.hgcwallet.local_auth.AuthManager;

public class PinAuthActivity extends AppCompatActivity {

    public static final String TAG = "PinLockView";
    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    public static final String TYPE_UNLOCK = "TYPE_UNLOCK";
    public static final String TYPE_ENABLE = "TYPE_ENABLE";

    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private String previousPin;
    private boolean forSetup = false;
    private int pinLength = 4;
    private TextView mTextViewHgcIcon,mTextViewProfileName;

    private PinLockListener mPinLockListener = new PinLockListener() {
        @Override
        public void onComplete(final String pin) {
            if (forSetup) {
                if (previousPin != null) {
                    if (!previousPin.equals(pin)){
                        showError("Previous pin and new pin did not match. Please try again.");
                        previousPin = null;
                        resetPinView();

                    } else {
                        final ProgressBar progressBar = findViewById(R.id.progress_bar);
                        progressBar.setVisibility(View.VISIBLE);
                        mTextViewHgcIcon.setVisibility(View.INVISIBLE);
                        TaskExecutor taskExecutor = new TaskExecutor();
                        taskExecutor.setListner(new TaskExecutor.TaskListner() {
                            @Override
                            public void onResult(BaseTask task1) {
                                if (task1.error == null) {
                                    setResult(RESULT_OK);
                                    finish();
                                } else  {
                                    showError(task1.error);
                                    resetPinView();
                                    progressBar.setVisibility(View.GONE);
                                    mTextViewHgcIcon.setVisibility(View.VISIBLE);
                                }

                            }
                        });
                        taskExecutor.execute(new BaseTask() {
                            @Override
                            public void main() {
                                try {
                                    AuthManager.INSTANCE.setPIN(pin);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    error = "Something went wrong";
                                }
                            };
                        });


                    }
                } else {
                    previousPin = pin;
                    showMessage("Please enter your PIN again.");
                    resetPinView();
                }
            } else {
                final ProgressBar progressBar = findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.VISIBLE);
                mTextViewHgcIcon.setVisibility(View.INVISIBLE);
                TaskExecutor taskExecutor = new TaskExecutor();
                taskExecutor.setListner(new TaskExecutor.TaskListner() {
                    @Override
                    public void onResult(BaseTask task1) {
                        if (task1.error == null) {
                            setResult(RESULT_OK);
                            finish();
                        } else  {
                            showError(task1.error);
                            resetPinView();
                            progressBar.setVisibility(View.GONE);
                            mTextViewHgcIcon.setVisibility(View.VISIBLE);
                        }

                    }
                });
                taskExecutor.execute(new BaseTask() {
                    @Override
                    public void main() {
                        try {
                            if (!AuthManager.INSTANCE.verifyPIN(pin)) {
                                error = "Wrong PIN. Please try again.";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            error = "Something went wrong";
                        }
                    };
                });
            }
        }

        @Override
        public void onEmpty() {

        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {
            TextView errorTV = findViewById(R.id.error_message);
            errorTV.setVisibility(View.GONE);
        }
    };

    private void showMessage(String string) {
        TextView errorTV = findViewById(R.id.error_message);
        TextView messageTV = findViewById(R.id.message);
        errorTV.setVisibility(View.GONE);
        messageTV.setVisibility(View.VISIBLE);
        messageTV.setText(string);
    }

    private void showError(String string) {
        TextView errorTV = findViewById(R.id.error_message);
        TextView messageTV = findViewById(R.id.message);
        errorTV.setVisibility(View.VISIBLE);
        messageTV.setVisibility(View.GONE);
        errorTV.setText(string);
    }

    private void resetPinView() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPinLockView.resetPinLockView();
            }
        },300);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pin_auth);
        if (getIntent().getStringExtra(EXTRA_TYPE).equals(TYPE_ENABLE))
            forSetup = true;

        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);

        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(mPinLockListener);

        mTextViewHgcIcon = (TextView)findViewById(R.id.text_hgc_icon);
        mTextViewProfileName = findViewById(R.id.profile_name);
        //mPinLockView.setCustomKeySet(new int[]{2, 3, 1, 5, 9, 6, 7, 0, 8, 4});
        //mPinLockView.enableLayoutShuffling();

        mPinLockView.setPinLength(4);
        mPinLockView.setTextColor(ContextCompat.getColor(this, R.color.white));

        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
        mIndicatorDots.setPinLength(pinLength);

        mTextViewProfileName.setVisibility(View.GONE);
        showMessage(getResources().getString(forSetup ? R.string.create_pin : R.string.enter_pin));
    }
}
