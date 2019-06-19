package opencrowd.hgc.hgcwallet.ui.customviews;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import opencrowd.hgc.hgcwallet.R;

public class TitleBarWrapper {
    View mRootView;
    TextView mTextView;
    ImageView mCloseButton;

    public  TitleBarWrapper(View rootView) {
        mRootView = rootView;
        mTextView = (TextView)rootView.findViewById(R.id.text_new_wallet);
        mCloseButton = (ImageView) rootView.findViewById(R.id.image_close);
    }

    public void setTitle(String title) {
        mTextView.setText(title);
    }

    public void setTitle(int resId) {
        mTextView.setText(resId);
    }

    public void setImageResource(int image) {mCloseButton.setImageResource(image);}



    public void setCloseButtonHidden(Boolean hidden) {
        mCloseButton.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);
    }

    public void setOnCloseButtonClickListener(View.OnClickListener listener) {
        mCloseButton.setOnClickListener(listener);
    }
}
