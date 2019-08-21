package hedera.hgc.hgcwallet.ui.main.request;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import hedera.hgc.hgcwallet.App;
import hedera.hgc.hgcwallet.R;
import hedera.hgc.hgcwallet.app_intent.TransferRequestParams;
import hedera.hgc.hgcwallet.common.Singleton;
import hedera.hgc.hgcwallet.common.UserSettings;
import hedera.hgc.hgcwallet.database.account.Account;
import hedera.hgc.hgcwallet.modals.HGCAccountID;
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper;
import hedera.hgc.hgcwallet.ui.main.account.AccountListScreen;

public class RequestScreen extends Screen<RequestScreenView> implements AccountListScreen.AccountPickerListener{

    Account fromAccount;

    @NonNull String toName = "" , notes = "", amountStr = "";
    boolean addNote = false;

    public RequestScreen(Account account)
    {
        this.fromAccount = account;
    }

    @Override
    protected void onShow(Context context) {
        super.onShow(context);
        getView().reloadData(this);
    }

    @Override
    protected RequestScreenView createView(@NonNull Context context) {
        return new RequestScreenView(context,this);
    }

    @Override
    public void onAccountPick(Account account) {
        this.fromAccount = account;
        getNavigator().goBackTo(this);
    }

    public void onQRButtonTap() {
        if(fromAccount.accountID()!=null) {
            //  toAccIdStr = fromAccount.accountID().stringRepresentation();
            amountStr = getView().mHgcAmountEditText.getText().toString();
            notes = getView().mNoteEditText.getText().toString();

            HGCAccountID toAccountID = HGCAccountID.Companion.fromString(fromAccount.accountID().stringRepresentation());
            double coins = 0;

            try {
                coins = Double.parseDouble(amountStr);
            } catch (Exception e) {
            }


            TransferRequestParams transferRequestParams = new TransferRequestParams(toAccountID);
            transferRequestParams.amount = Singleton.INSTANCE.toNanoCoins(coins);
            transferRequestParams.note = notes;
            String qrString = transferRequestParams.asQRCode();
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(qrString, BarcodeFormat.QR_CODE, 700, 700);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                getView().mQRCodeImage.setImageBitmap(bitmap);
                getView().setToAccountViewVisible(true);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }else {
            Singleton.INSTANCE.showToast(getView().getScreen().getActivity(),getView().getScreen().getActivity().getString(R.string.invalid_account_id));
        }
    }

    public void onSendTap() {
        if (fromAccount.accountID() != null) {
           // toAccIdStr = fromAccount.accountID().stringRepresentation();
            amountStr = getView().mHgcAmountEditText.getText().toString();
            notes = getView().mNoteEditText.getText().toString();

            HGCAccountID toAccountID = HGCAccountID.Companion.fromString(fromAccount.accountID().stringRepresentation());
            double coins = 0;

            try {
                coins = Double.parseDouble(amountStr);
            } catch (Exception e) {
            }


            TransferRequestParams transferRequestParams = new TransferRequestParams(toAccountID);
            transferRequestParams.amount = Singleton.INSTANCE.toNanoCoins(coins);
            transferRequestParams.note = notes;
            transferRequestParams.name = UserSettings.INSTANCE.getValue(UserSettings.KEY_USER_NAME);
            Uri sendUrl = transferRequestParams.asUri();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, sendUrl.toString());
            sendIntent.setType("text/plain");
            getView().getScreen().getActivity().startActivity(sendIntent);
        }else {
            Singleton.INSTANCE.showToast(getView().getScreen().getActivity(),getView().getScreen().getActivity().getString(R.string.invalid_account_id));
        }
    }
}

class RequestScreenView extends BaseScreenView<RequestScreen> {

    private TextView mAccountName,mPublicKey,mChangeText,mNoteText;
    public EditText mNoteEditText, mHgcAmountEditText,mEditTextDollorAmount;
    private CheckBox mAddNote;
    private Button mQRCode,mSend,mCancel;
    public ImageView mQRCodeImage;
    private RelativeLayout mAmountRelativeLayout,mQRGenerateRelativeLayout;

    public RequestScreenView(@NonNull Context context,@NonNull final RequestScreen screen) {
        super(context);
        inflate(context, R.layout.view_request_layout,this);
        mAccountName = (TextView)findViewById(R.id.text_account_name);
        mPublicKey = (TextView)findViewById(R.id.text_public_key);
        mChangeText = (TextView)findViewById(R.id.text_change);
        mAddNote = (CheckBox) findViewById(R.id.add_note);
     //   mRequestNotification = (CheckBox)findViewById(R.id.request_notification);
        mNoteText = (TextView)findViewById(R.id.text_note);
        mNoteEditText = (EditText)findViewById(R.id.edittext_note);
        mHgcAmountEditText = (EditText)findViewById(R.id.edittext_hgc_amount);
        mEditTextDollorAmount = (EditText)findViewById(R.id.edittext_dollor_amount);
        mQRCode = (Button)findViewById(R.id.btn_qr);
        mSend = (Button)findViewById(R.id.btn_send);
        mCancel = (Button)findViewById(R.id.btn_cancel);
        mAmountRelativeLayout = (RelativeLayout)findViewById(R.id.amount_relative_layout);
        mQRGenerateRelativeLayout = (RelativeLayout)findViewById(R.id.qr_generate_layout);
        mQRCodeImage = (ImageView)findViewById(R.id.imageView_qrcode);
        mHgcAmountEditText.addTextChangedListener(hgcEditTextListener);
        mEditTextDollorAmount.addTextChangedListener(dollerEditTextListener);
        reloadData(screen);

        mChangeText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goTo(new AccountListScreen(getScreen()));
            }
        });

        mHgcAmountEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b){
                    getScreen().amountStr = mHgcAmountEditText.getText().toString();
                }
            }
        });

        mAddNote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getScreen().addNote = isChecked;
                setNoteTextFieldVisible(isChecked);
            }
        });

        mNoteEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b){
                    getScreen().notes = mNoteEditText.getText().toString();
                }
            }
        });

        mQRCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().onQRButtonTap();

            }
        });

        mSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().onSendTap();
            }
        });

        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setToAccountViewVisible(false);
            }
        });
    }

    public void reloadData(@NonNull RequestScreen screen) {
        Account fromAccount = screen.fromAccount;

        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("REQUEST");
        titleBar.setCloseButtonHidden(false);

        titleBar.setOnCloseButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goBack();
            }
        });

        mAccountName.setText(fromAccount.getName());

        if (fromAccount.accountID() != null) {
            mPublicKey.setText(fromAccount.accountID().stringRepresentation());
        } else {
            String shortKey = Singleton.INSTANCE.publicKeyStringShort(fromAccount);
            mPublicKey.setText(App.Companion.getInstance().getString(R.string.text_key_short,shortKey));
        }

        setText(mHgcAmountEditText, hgcEditTextListener, screen.amountStr);
        mNoteEditText.setText(screen.notes);
        mAddNote.setChecked(screen.addNote);

        setNoteTextFieldVisible(screen.addNote);
    }

    public void setToAccountViewVisible(boolean visible) {
        if (visible) {
            mAmountRelativeLayout.setVisibility(INVISIBLE);
            mQRGenerateRelativeLayout.setVisibility(VISIBLE);

        } else  {
            mAmountRelativeLayout.setVisibility(VISIBLE);
            mQRGenerateRelativeLayout.setVisibility(INVISIBLE);
        }
    }

    public void setNoteTextFieldVisible(boolean visible) {
        if(visible) {
            mNoteText.setVisibility(VISIBLE);
            mNoteEditText.setVisibility(VISIBLE);
        } else {
            mNoteText.setVisibility(GONE);
            mNoteEditText.setVisibility(GONE);
        }
    }

    private void setText(EditText editText, TextWatcher textWatcher, String text) {
        editText.removeTextChangedListener(textWatcher);
        editText.setText(text);
        editText.addTextChangedListener(textWatcher);
    }

    private TextWatcher dollerEditTextListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            try {
                double doller = Double.parseDouble(editable.toString());
                double coins = Singleton.INSTANCE.toCoins(Singleton.INSTANCE.USDtoHGC(doller));
                setText(mHgcAmountEditText, hgcEditTextListener, Singleton.INSTANCE.toString(coins));

            } catch (Exception e) {

            }
        }
    };

    private TextWatcher hgcEditTextListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            try {
                double coins = Double.parseDouble(editable.toString());
                double doller = Singleton.INSTANCE.hgcToUSD(Singleton.INSTANCE.toNanoCoins(coins));
                setText(mEditTextDollorAmount, dollerEditTextListener, Singleton.INSTANCE.toString(doller));

            } catch (Exception e) {

            }
        }
    };
}
