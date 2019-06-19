package opencrowd.hgc.hgcwallet.ui.onboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.modals.HGCKeyType;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class TermsScreen extends Screen<TermsView> {

    public boolean isAcceptBtnShow;
    public String content, title;

    public TermsScreen(boolean isAcceptBtnShow, String content, String title) {
        this.isAcceptBtnShow = isAcceptBtnShow;
        this.content = content;
        this.title = title;
    }

    @NonNull
    @Override
    protected TermsView createView(@NonNull Context context) {

        return new TermsView(context,isAcceptBtnShow, content, title);
    }
}


class TermsView extends BaseScreenView<TermsScreen> {
    Button mTermAccept;

    public TermsView(@NonNull Context context,boolean isAcceptBtnShow, String content, String title) {
        super(context);
        inflate(context, R.layout.fragment_term_layout,this);
        new TitleBarWrapper(findViewById(R.id.titleBar)).setTitle(title);
        mTermAccept = (Button)findViewById(R.id.btn_term_accept);
        TextView textView = findViewById(R.id.textView);
        textView.setText(content);

        if(isAcceptBtnShow == true) {
            mTermAccept.setVisibility(VISIBLE);
        } else {
            mTermAccept.setVisibility(GONE);
        }

        mTermAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goTo(new NewWalletCreateScreen(HGCKeyType.ED25519));
            }
        });
    }

}