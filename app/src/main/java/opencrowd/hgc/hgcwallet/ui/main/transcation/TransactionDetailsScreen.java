package opencrowd.hgc.hgcwallet.ui.main.transcation;


import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.database.transaction.TxnRecord;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class TransactionDetailsScreen extends Screen<TransactionDetailsView> {

    public TxnRecord txnRecord;

    public TransactionDetailsScreen(TxnRecord txnRecord) {
        this.txnRecord = txnRecord;
    }

    @Override
    protected TransactionDetailsView createView(Context context) {
        return new TransactionDetailsView(context, txnRecord);
    }
}

class TransactionDetailsView extends BaseScreenView<TransactionDetailsScreen> {

    private TextView mTextViewHgcAmount, mTextViewDollorAmount, mTextViewTime, mTextViewFromAccountName, mTextViewFromAccountId, mTextViewHgcIcon;
    private TextView mTextViewToAccountName, mTextViewToAccountId, mTextViewFromVefiry, mTextViewToVerify, mTextViewFeeValue;
    private EditText mEditTextNote;
    private ImageView mImageViewCopyFromId, mImageViewCopyToId;

    public TransactionDetailsView(Context context, final TxnRecord txnRecord) {
        super(context);
        inflate(context, R.layout.view_transaction_details_layout, this);

        mTextViewHgcAmount = (TextView) findViewById(R.id.text_hgc_amount);
        mTextViewDollorAmount = (TextView) findViewById(R.id.text_dollor_amount);
        mTextViewTime = (TextView) findViewById(R.id.text_time);
        mTextViewFromAccountName = (TextView) findViewById(R.id.text_from_account_name);
        mTextViewFromAccountId = (TextView) findViewById(R.id.text_from_account_id);
        mTextViewToAccountName = (TextView) findViewById(R.id.text_to_account_name);
        mTextViewToAccountId = (TextView) findViewById(R.id.text_to_account_id);
        mTextViewFromVefiry = (TextView) findViewById(R.id.text_verify_from_account);
        mTextViewToVerify = (TextView) findViewById(R.id.text_verify_to_account);
        mTextViewFeeValue = (TextView) findViewById(R.id.text_fee_value);
        mEditTextNote = (EditText) findViewById(R.id.edittext_note);
        mImageViewCopyFromId = (ImageView) findViewById(R.id.image_copy_from_acount);
        mImageViewCopyToId = (ImageView) findViewById(R.id.image_copy_to_account);
        mTextViewHgcIcon = (TextView) findViewById(R.id.hgc_image);

        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("TRANSACTION DETAILS");
        titleBar.setCloseButtonHidden(false);

        titleBar.setOnCloseButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goBack();
            }
        });

        long nanoCoins = 0;
        nanoCoins = txnRecord.getAmount();
        if (txnRecord.isPositive() == true) {
            mTextViewHgcAmount.setTextColor(getResources().getColor(R.color.positive, null));
            mTextViewDollorAmount.setTextColor(getResources().getColor(R.color.positive, null));
            mTextViewHgcIcon.setTextColor(getResources().getColor(R.color.positive, null));
        } else {
            mTextViewHgcAmount.setTextColor(getResources().getColor(R.color.negative, null));
            mTextViewDollorAmount.setTextColor(getResources().getColor(R.color.negative, null));
            mTextViewHgcIcon.setTextColor(getResources().getColor(R.color.negative, null));
        }
        mTextViewHgcAmount.setText(Singleton.INSTANCE.formatHGCShort(nanoCoins));
        mTextViewDollorAmount.setText(Singleton.INSTANCE.formatUSD(Singleton.INSTANCE.hgcToUSD(nanoCoins), true));

        String status = "status: unknown";
        if (txnRecord.getStatus() == TxnRecord.Status.SUCCESS) {
            status = "status: success";
        } else if (txnRecord.getStatus() == TxnRecord.Status.FAILED) {
            status = "status: failed";
        }
        mTextViewTime.setText(Singleton.INSTANCE.getDateFormat(txnRecord.getCreatedDate()) + "  " + status);
        mTextViewFromAccountId.setText(txnRecord.getFromAccId());
        mTextViewToAccountId.setText(txnRecord.getToAccountId());

        if (txnRecord.getFromAccount() != null) {
            mTextViewFromAccountName.setText(txnRecord.getFromAccount().getName());
            mTextViewFromVefiry.setVisibility(txnRecord.getFromAccount().isVerified() ? GONE : VISIBLE);
        } else {
            mTextViewFromAccountName.setText("UNKNOWN");
        }
        if (txnRecord.getToAccount()!= null) {
            mTextViewToAccountName.setText(txnRecord.getToAccount().getName());
            mTextViewToVerify.setVisibility(txnRecord.getToAccount().isVerified() ? GONE : VISIBLE);
        } else {
            mTextViewToAccountName.setText("UNKNOWN");

        }
        mEditTextNote.setText(txnRecord.getNotes());
        mTextViewFeeValue.setText(Singleton.INSTANCE.formatHGC(txnRecord.getFee(), true));

        mTextViewFromVefiry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goTo(new VerifyAliasScreen(txnRecord.getFromAccount()));
            }
        });

        mTextViewToVerify.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goTo(new VerifyAliasScreen(txnRecord.getToAccount()));
            }
        });

        mImageViewCopyFromId.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Singleton.INSTANCE.copyToClipBoard(txnRecord.getFromAccId(), getScreen().getActivity());
                Singleton.INSTANCE.showToast(getScreen().getActivity(), getScreen().getActivity().getString(R.string.copy_data_clipboard_account_id));
            }
        });

        mImageViewCopyToId.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Singleton.INSTANCE.copyToClipBoard(txnRecord.getToAccountId(), getScreen().getActivity());
                Singleton.INSTANCE.showToast(getScreen().getActivity(), getScreen().getActivity().getString(R.string.copy_data_clipboard_account_id));
            }
        });
    }
}
