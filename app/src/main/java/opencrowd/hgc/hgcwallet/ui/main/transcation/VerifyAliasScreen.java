package opencrowd.hgc.hgcwallet.ui.main.transcation;


import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.contact.Contact;
import opencrowd.hgc.hgcwallet.database.transaction.TxnRecord;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class VerifyAliasScreen extends Screen<VerifyAliasScreenView>{

    Contact contact;

    public VerifyAliasScreen(Contact contact) {
        this.contact = contact;
    }

    @Override
    protected VerifyAliasScreenView createView(Context context) {
        return new VerifyAliasScreenView(context,contact);
    }
}

class VerifyAliasScreenView extends BaseScreenView<VerifyAliasScreen> {

    private TextView mTextViewName,mTextViewAccountId;
    private Button mCancel,mConfirm;

    public VerifyAliasScreenView(Context context, final Contact contact) {
        super(context);
        inflate(context, R.layout.view_verify_alias_layout,this);

        mTextViewName = (TextView)findViewById(R.id.textview_name);
        mTextViewAccountId = (TextView)findViewById(R.id.textview_account_id);
        mCancel = (Button)findViewById(R.id.btn_cancel);
        mConfirm = (Button)findViewById(R.id.btn_confirm);

        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("VERIFY ALIAS");
        titleBar.setCloseButtonHidden(false);

        titleBar.setOnCloseButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goBack();
            }
        });

        mTextViewName.setText(contact.getName());
        mTextViewAccountId.setText(contact.getAccountId());

        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goBack();
            }
        });

        mConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contact.setVerified(true);
                DBHelper.saveContact(contact);
                getScreen().getNavigator().goBack();
            }
        });
    }
}