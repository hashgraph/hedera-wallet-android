package hedera.hgc.hgcwallet.ui.main.account;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import hedera.hgc.hgcwallet.R;
import hedera.hgc.hgcwallet.database.DBHelper;
import hedera.hgc.hgcwallet.database.account.Account;
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class AddAccountScreen extends Screen<AddAccountView> {

    @NonNull
    @Override
    protected AddAccountView createView(@NonNull Context context) {

        return new AddAccountView(context);
    }
}

class AddAccountView extends BaseScreenView<AddAccountScreen> {

    private EditText mEditTextNickName;
    private Button mCancelbtn,mSavebtn;
    public AddAccountView(Context context) {
        super(context);
        inflate(context, R.layout.view_add_account_layout,this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("ADD ACCOUNT");
        mEditTextNickName = (EditText)findViewById(R.id.edittext_nick_name);
        mCancelbtn = (Button)findViewById(R.id.btn_cancel);
        mSavebtn = (Button)findViewById(R.id.btn_save);

        mSavebtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String mEditTextValue = mEditTextNickName.getText().toString();
                if(mEditTextValue!=null && !mEditTextValue.isEmpty()) {
                    Account account = DBHelper.INSTANCE.createNewAccount(mEditTextValue);
                    /*InputMethodManager imm = (InputMethodManager)getScreen().getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEditTextNickName.getWindowToken(), 0);*/
                    getScreen().getNavigator().replace(new AccountCreateScreen(account,"Account Created",false));
                }

            }
        });

        mCancelbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goBack();
            }
        });
    }
}
