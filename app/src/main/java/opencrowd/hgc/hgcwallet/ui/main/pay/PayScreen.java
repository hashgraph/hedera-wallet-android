package opencrowd.hgc.hgcwallet.ui.main.pay;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.hederahashgraph.api.proto.java.Transaction;
import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import opencrowd.hgc.hgcwallet.App;
import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.hapi.APIRequestBuilder;
import opencrowd.hgc.hgcwallet.common.BaseTask;
import opencrowd.hgc.hgcwallet.common.TaskExecutor;
import opencrowd.hgc.hgcwallet.hapi.tasks.TransferTaskAPI;
import opencrowd.hgc.hgcwallet.app_intent.TransferRequestParams;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.database.account.Account;
import opencrowd.hgc.hgcwallet.database.contact.Contact;
import opencrowd.hgc.hgcwallet.database.node.Node;
import opencrowd.hgc.hgcwallet.database.request.PayRequest;
import opencrowd.hgc.hgcwallet.modals.HGCAccountID;
import opencrowd.hgc.hgcwallet.ui.BaseActivity;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;
import opencrowd.hgc.hgcwallet.ui.main.MainActivity;
import opencrowd.hgc.hgcwallet.ui.main.pay.contacts.ContactListScreen;
import opencrowd.hgc.hgcwallet.ui.main.account.AccountListScreen;
import opencrowd.hgc.hgcwallet.ui.scan.QRScanListener;


public class PayScreen extends Screen<PayScreenView> implements AccountListScreen.AccountPickerListener,ContactListScreen.ContactPickerListener,QRScanListener{

    Account fromAccount;

    long fee = 0;
    @NonNull String toName = "", toAccIdStr = "", notes = "", amountStr = "";
    boolean addNote = false, showToAccount = false;

    public PayScreen(Account account) {
        this.fromAccount = account;
    }

    public PayScreen(Account fromAccount, PayRequest toAccount) {
        showToAccount = true;
        toAccIdStr = toAccount.getAccountId();
        toName = toAccount.getName();
        amountStr = Singleton.INSTANCE.toCoins(toAccount.getAmount()) + "";
        this.fromAccount = fromAccount;
    }

    @Override
    protected void onShow(Context context) {
        super.onShow(context);
        updateFee();
        getView().reloadData(this);
    }

    @NonNull
    @Override
    protected PayScreenView createView(@NonNull Context context) {
        return new PayScreenView(context, this);
    }

    @Override
    public void onAccountPick(Account account) {
        this.fromAccount = account;
        getNavigator().goBackTo(this);
    }

    @Override
    public void onContactPick(Contact contact) {
        showToAccount = true;
        toAccIdStr = contact.getAccountId();
        toName = contact.getName();
        getNavigator().goBackTo(this);
    }

    public void updateFee() {
        if (fromAccount.accountID() == null) return;

        double amount = 0;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (Exception e) { }

        Transaction transaction = APIRequestBuilder.requestForTransfer(fromAccount, new HGCAccountID(0,0,0), Singleton.INSTANCE.toNanoCoins(amount), notes, APIRequestBuilder.PLACEHOLDE_FEE, Node.Companion.placeholderNode().accountID());
        fee = APIRequestBuilder.feeForTransferTransaction(transaction);

        PayScreenView view = getView();
        if (view != null) {
            view.updateFee(fee);
        }

    }

    public void onPayBuutonTap() {

        toAccIdStr = getView().mAccountIdEditText.getText().toString();
        toName = getView().mAccountNameEditText.getText().toString();
        amountStr = getView().mHgcAmountEditText.getText().toString();
        notes = getView().mNoteEditText.getText().toString();

        String error = null;
        HGCAccountID toAccountID = HGCAccountID.fromString(toAccIdStr);
        double amount = 0;

        try {
            amount = Double.parseDouble(amountStr);
        } catch (Exception e) { }

        if (toAccountID == null) {
            error = "Invalid to account id";
        } else if (amount == 0) {
            error = "Invalid amount";
        }
        final BaseActivity activity = (BaseActivity) getActivity();
        if (error == null) {
            activity.showActivityProgress("Please wait");
            final TransferTaskAPI task = new TransferTaskAPI(fromAccount, toAccountID, notes,toName, Singleton.INSTANCE.toNanoCoins(amount), fee);
            TaskExecutor taskExecutor = new TaskExecutor();
            taskExecutor.setListner(new TaskExecutor.TaskListner() {
                @Override
                public void onResult(BaseTask task1) {
                    activity.hideActivityProgress();
                    if (task.error == null) {
                        if (activity instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity)activity;
                            mainActivity.synchronizeData(true, false);
                        }
                        Singleton.INSTANCE.showToast(getActivity(), getActivity().getString(R.string.transaction_submitted_successfully));
                        getNavigator().goBack();

                    } else {
                        Singleton.INSTANCE.showToast(getActivity(), task.error);
                    }
                }
            });
            taskExecutor.execute(task);
        } else {
            Singleton.INSTANCE.showToast(getActivity(),error);
        }

    }

    @Override
    public void onQRScanFinished(boolean success, @Nullable String result) {
        if (!success) return;
        if (result != null) {
            TransferRequestParams params = TransferRequestParams.fromBarCode(result);
            if (params != null) {
                toAccIdStr = params.account.stringRepresentation();
                toName = params.name == null ? "" : params.name;
                amountStr = Singleton.INSTANCE.toCoins(params.amount) + "";
                if (params.note != null) {
                    notes = params.note;
                    addNote = true;
                }
                showToAccount = true;
                getView().reloadData(this);
            }
        }

    }
}

class PayScreenView extends BaseScreenView<PayScreen> {

    private TextView mAccountName;
    private TextView mPublicKey;
    private TextView mChangeText;
    private TextView mCancel;
    private TextView mNoteText, mFeeTextView;
    public EditText mNoteEditText, mHgcAmountEditText,mEditTextDollorAmount, mAccountIdEditText, mAccountNameEditText;

    private Button mNewButton,mScanButton,mExistingButton;
    private Button mPayButton;
    private CheckBox mAddNote;
    private LinearLayout mPayButtonsLayout,mNewButtonLayout,mScanButtonClickLayout;
    private RelativeLayout mAmountRelativeLayout;

    public PayScreenView(@NonNull final Context context, @NonNull PayScreen screen) {
        super(context);
        inflate(context, R.layout.view_pay_layout,this);
        mAccountName = (TextView)findViewById(R.id.text_account_name);
        mPublicKey = (TextView)findViewById(R.id.text_public_key);
        mChangeText = (TextView)findViewById(R.id.text_change);
        mCancel = (TextView)findViewById(R.id.text_cancel);
        mNewButton = (Button)findViewById(R.id.btn_new);
        mScanButton = (Button)findViewById(R.id.btn_scan);
        mPayButton = (Button)findViewById(R.id.btn_pay);
        mExistingButton = (Button)findViewById(R.id.btn_existing);
        mAddNote = (CheckBox) findViewById(R.id.add_note);
        mFeeTextView = findViewById(R.id.text_fee_value);

        //   mRequestNotification = (CheckBox)findViewById(R.id.request_notification);
        mNoteText = (TextView)findViewById(R.id.text_note);
        mNoteEditText = (EditText)findViewById(R.id.edittext_note);
        mHgcAmountEditText = (EditText)findViewById(R.id.edittext_hgc_amount);
        mEditTextDollorAmount = (EditText)findViewById(R.id.edittext_dollor_amount);
        mAccountIdEditText = (EditText)findViewById(R.id.edittext_account_id);
        mAccountNameEditText = (EditText)findViewById(R.id.edittext_account_name);

        mPayButtonsLayout= (LinearLayout)findViewById(R.id.pay_type_btn_layout);
        mNewButtonLayout = (LinearLayout)findViewById(R.id.new_btn_open_layout);
        mAmountRelativeLayout = (RelativeLayout)findViewById(R.id.amount_relative_layout);
        mHgcAmountEditText.addTextChangedListener(hgcEditTextListener);
        mEditTextDollorAmount.addTextChangedListener(dollerEditTextListener);

        reloadData(screen);
        mChangeText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goTo(new AccountListScreen(getScreen()));
            }
        });

        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().showToAccount = false;
                setToAccountViewVisible(false);
            }
        });

        mExistingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goTo(new ContactListScreen(getScreen()));
            }
        });

        mNewButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().showToAccount = true;
                setToAccountViewVisible(true);
            }
        });

        mScanButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new IntentIntegrator(getScreen().getActivity())
                        .setOrientationLocked(true)
                        .setPrompt("Place a QRcode inside the rectangle")
                        .initiateScan();
            }
        });



        mAddNote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getScreen().addNote = isChecked;
                setNoteTextFieldVisible(isChecked);
            }
        });

        mPayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getScreen().onPayBuutonTap();
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

        mAccountNameEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b){
                    getScreen().toName = mAccountNameEditText.getText().toString();
                }
            }
        });

        mAccountIdEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b){
                    getScreen().toAccIdStr = mAccountIdEditText.getText().toString();
                }
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
    }

    public void reloadData(@NonNull PayScreen screen) {
        Account fromAccount = screen.fromAccount;

        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("PAY");
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
            mPublicKey.setText(App.instance.getString(R.string.text_key_short,shortKey));
        }

        setText(mHgcAmountEditText, hgcEditTextListener, screen.amountStr);
        try {
            double coins = Double.parseDouble(screen.amountStr);
            double doller = Singleton.INSTANCE.hgcToUSD(Singleton.INSTANCE.toNanoCoins(coins));
            setText(mEditTextDollorAmount, dollerEditTextListener, Singleton.INSTANCE.toString(doller));
        } catch (Exception e) {

        }


        mNoteEditText.setText(screen.notes);
        mAccountIdEditText.setText(screen.toAccIdStr);
        mAccountNameEditText.setText(screen.toName);
        mAddNote.setChecked(screen.addNote);
        updateFee(screen.fee);
        setToAccountViewVisible(screen.showToAccount);
        setNoteTextFieldVisible(screen.addNote);
    }

    public void updateFee(long fee) {
        mFeeTextView.setText( Singleton.INSTANCE.formatHGC(fee, true));

    }

    public void setToAccountViewVisible(boolean visible) {
        if (visible) {
            mPayButtonsLayout.setVisibility(INVISIBLE);
            mNewButtonLayout.setVisibility(VISIBLE);
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            p.addRule(RelativeLayout.BELOW, R.id.new_btn_open_layout);
            p.setMargins(0, 100, 0, 0);
            mAmountRelativeLayout.setLayoutParams(p);

        } else  {
            mPayButtonsLayout.setVisibility(VISIBLE);
            mNewButtonLayout.setVisibility(INVISIBLE);
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            p.addRule(RelativeLayout.BELOW, R.id.pay_type_btn_layout);
            p.setMargins(0, 30, 0, 0);
            mAmountRelativeLayout.setLayoutParams(p);
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
