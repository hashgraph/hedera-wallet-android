package opencrowd.hgc.hgcwallet.ui.main.account;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.account.Account;
import opencrowd.hgc.hgcwallet.modals.HGCAccountID;
import opencrowd.hgc.hgcwallet.ui.auth.AuthActivity;
import opencrowd.hgc.hgcwallet.local_auth.AuthListener;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class AccountCreateScreen extends Screen<AccountCreateView> implements AuthListener {

    Account account;
    private String titleName;
    private boolean image;

    public AccountCreateScreen(Account account,String title,boolean value) {
        this.account = account;
        this.titleName = title;
        this.image = value;
    }

    @NonNull
    @Override
    protected AccountCreateView createView(@NonNull Context context) {
        return new AccountCreateView(context,account,titleName,image);
    }

    @Override
    public void onAuthSetupSuccess() {

    }

    @Override
    public void onAuthSuccess(int requestCode) {
        if (requestCode == 100)
            getView().showPrivateKey();
    }

    @Override
    public void onAuthSetupFailed(boolean isCancelled) {

    }

    @Override
    public void onAuthFailed(int requestCode,boolean isCancelled) {

    }
}

class AccountCreateView extends BaseScreenView<AccountCreateScreen> {
    private EditText mEditTextNickName,mEditTextAccountId;
    private TextView mPublicKey,mPrivateKey,mDisplayText,mHideText;
    private Button mCancelbtn;
    private ImageView mCopyImageView,mCopyImageViewAccountData;
    @NonNull String publickey = "";
    private boolean isPrivateKeyDisplay = false;


    public AccountCreateView(@NonNull Context context, final @NonNull Account account, @NonNull String title, boolean imageValue) {
        super(context);
        inflate(context, R.layout.view_account_created_layout,this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle(title);
       /* if(imageValue==true)
        {
            titleBar.setCloseButtonHidden(false); //if account created and account details icon
                                                      will be same then this code shoud be deleted
        }*/
       titleBar.setCloseButtonHidden(false);
        titleBar.setOnCloseButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goBack();
            }
        });
        mEditTextNickName = (EditText)findViewById(R.id.edittext_nick_name);
        mEditTextAccountId = (EditText)findViewById(R.id.edittext_account_id);
        mPublicKey = (TextView)findViewById(R.id.textview_public_address);
        mPrivateKey = (TextView)findViewById(R.id.textview_private_key);
        mCancelbtn = (Button)findViewById(R.id.btn_close);
        mCopyImageView = (ImageView)findViewById(R.id.image_copy);
        mCopyImageViewAccountData = (ImageView)findViewById(R.id.image_copy_account_data);
        mDisplayText = (TextView)findViewById(R.id.text_display);
        mHideText = (TextView)findViewById(R.id.text_hide);
        if(account != null) {
            if(account.getName()!=null)
            {
                mEditTextNickName.setText(account.getName());
            }
            publickey = Singleton.INSTANCE.publicKeyString(account);
            if(publickey != null && !publickey.isEmpty()) {
                mPublicKey.setText(publickey);
            }

        }

        mCancelbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goBack();
            }
        });

        if (account.accountID() != null) {
            mEditTextAccountId.setText(account.accountID().stringRepresentation());
        }

        mEditTextAccountId.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                HGCAccountID accountID = HGCAccountID.fromString(mEditTextAccountId.getText().toString());
                if (accountID != null) {
                    HGCAccountID existingAccID = account.accountID();
                    if (existingAccID != null) {
                        if (!existingAccID.equals(accountID)) {
                            Singleton.INSTANCE.clearAccountData(account);
                        }
                    }
                    account.setAccountID(accountID);
                    DBHelper.saveAccount(account);
                }
            }
        });

        mEditTextNickName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
              account.setName(mEditTextNickName.getText().toString());
              DBHelper.saveAccount(account);

            }
        });

        mCopyImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(publickey!=null && !publickey.isEmpty()) {
                    Singleton.INSTANCE.copyToClipBoard(publickey,getScreen().getActivity());
                    Singleton.INSTANCE.showToast(getScreen().getActivity(), getScreen().getActivity().getString(R.string.copy_data_clipboard_message));

                }
            }
        });

        mCopyImageViewAccountData.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String accountData = Singleton.INSTANCE.accountToJSONString(account, isPrivateKeyDisplay);
                Singleton.INSTANCE.copyToClipBoard(accountData,getScreen().getActivity());
                Singleton.INSTANCE.showToast(getScreen().getActivity(), getScreen().getActivity().getString(R.string.copy_data_clipboard_account_data));
            }
        });

        mDisplayText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getScreen().getActivity())
                        .setTitle("Warning")
                        .setMessage("There is some risk involved in showing the private key.\n\nDo you really want to see it?")
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isPrivateKeyDisplay = true ;
                        dialogInterface.dismiss();
                        AuthActivity authActivity = (AuthActivity)getScreen().getActivity();
                        authActivity.requestAuth(100);
                    }
                }).show();
            }
        });

        mHideText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isPrivateKeyDisplay = false ;
                mDisplayText.setVisibility(VISIBLE);
                mHideText.setVisibility(GONE);
                mPrivateKey.setText(R.string.default_private_key);
            }
        });
    }

    void showPrivateKey() {
        String privateKey = Singleton.INSTANCE.privateKeyString(getScreen().account);
        mDisplayText.setVisibility(GONE);
        mHideText.setVisibility(VISIBLE);
        mPrivateKey.setText(privateKey);
    }
}
