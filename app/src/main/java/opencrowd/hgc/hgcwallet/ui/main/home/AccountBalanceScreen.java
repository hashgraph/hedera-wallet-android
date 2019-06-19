package opencrowd.hgc.hgcwallet.ui.main.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import java.util.ArrayList;
import java.util.List;

import opencrowd.hgc.hgcwallet.App;
import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.BaseTask;
import opencrowd.hgc.hgcwallet.common.TaskExecutor;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.common.UserSettings;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.account.Account;
import opencrowd.hgc.hgcwallet.database.contact.Contact;
import opencrowd.hgc.hgcwallet.database.transaction.TxnRecord;
import opencrowd.hgc.hgcwallet.ui.auth.AuthActivity;
import opencrowd.hgc.hgcwallet.local_auth.AuthListener;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;
import opencrowd.hgc.hgcwallet.ui.main.pay.PayScreen;
import opencrowd.hgc.hgcwallet.ui.main.request.RequestScreen;
import opencrowd.hgc.hgcwallet.ui.main.transcation.TransactionDetailsScreen;
import opencrowd.hgc.hgcwallet.ui.onboard.Bip39MigrationScreen;

public class AccountBalanceScreen extends Screen<AccountBalanceView> implements AuthListener {

    @NonNull
    @Override
    protected AccountBalanceView createView(@NonNull Context context) {
        return new AccountBalanceView(context);
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
    public void onAuthFailed(int requestCode, boolean isCancelled) {

    }
}


class AccountBalanceView extends BaseScreenView<AccountBalanceScreen> {

    private @NonNull List<TxnRecord> mTxnList = new ArrayList<>();
    private List<Account> mAccountList;
    private RecyclerView mRecyclerView;
    private TrasactionListAdapter mAdapter;
    private LinearLayout mToggleLayout;
    private View mAccountDetailsView;
    private ImageView mArrowDown;
   private ImageView mArrowUp;
    private OverViewPagerAdapter mCustomAdapter;
    private EditText mEditTextNickName,mEditTextAccountId;
    private TextView mTextViewPublicKey,mTextViewPrivateKey,mTextViewDisplayText,mTextViewHideText;
    private ImageView mCopyImageView,mCopyImageViewAccountData;
    private Account account;
    private TextView mTextViewNoTransaction;
    private boolean isPrivateKeyDisplay = false;


    public AccountBalanceView(final Context context) {
        super(context);
        inflate(context, R.layout.view_home_layout, this);

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        mToggleLayout = (LinearLayout)findViewById(R.id.toggle_linear_layout);
        mAccountDetailsView = (RelativeLayout)findViewById(R.id.user_layout);
        mArrowUp = (ImageView)findViewById(R.id.arrow_up);
        mArrowDown = (ImageView)findViewById(R.id.arrow_down);
        mEditTextNickName = (EditText)findViewById(R.id.edittext_nick_name);
        mEditTextAccountId = (EditText)findViewById(R.id.edittext_account_id);
        mTextViewPublicKey = (TextView)findViewById(R.id.textview_public_address);
        mTextViewPrivateKey = (TextView)findViewById(R.id.textview_private_key);
        mCopyImageView = (ImageView)findViewById(R.id.image_copy);
        mCopyImageViewAccountData = (ImageView)findViewById(R.id.image_copy_account_data);
        mTextViewDisplayText = (TextView)findViewById(R.id.text_display);
        mTextViewHideText = (TextView)findViewById(R.id.text_hide);
        mTextViewNoTransaction = (TextView)findViewById(R.id.text_no_transaction);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        mAccountList = DBHelper.getAllAccounts();
        mCustomAdapter = new OverViewPagerAdapter(context,mAccountList);
        viewPager.setAdapter(mCustomAdapter);
        mCustomAdapter.setViewListner(new OverViewPagerAdapter.ViewListner() {
            @Override
            public void onPayButtonClick(Account account) {
                getScreen().getNavigator().goTo(new PayScreen(account));
            }

            @Override
            public void onRequestButtonClick(Account account) {
                getScreen().getNavigator().goTo(new RequestScreen(account));
            }
        });

        TextView bip39View = findViewById(R.id.bip39_warning_tv);
        if (UserSettings.instance.getBoolValue(UserSettings.KEY_HAS_SHOWN_BIP39_MNEMONIC))
            bip39View.setVisibility(View.GONE);
         else {
            bip39View.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getScreen().getNavigator().goTo(new Bip39MigrationScreen());
                }
            });
        }

       // mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TrasactionListAdapter(mTxnList);
        mAdapter.setOnTransactionClick(new TrasactionListAdapter.OnTransactionClick() {
            @Override
            public void onTransaction(TxnRecord txnRecord) {
               getScreen().getNavigator().goTo(new TransactionDetailsScreen(txnRecord));
            }
        });


        if(mAccountList.size() > 1) {
            TabLayout tabLayout = (TabLayout) findViewById(R.id.dots);
            tabLayout.setupWithViewPager(viewPager, true);
        }

        mArrowDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mToggleLayout.setVisibility(GONE);
                if(account != null) {
                    if(account.getName()!=null)
                    {
                        mEditTextNickName.setText(account.getName());
                    }
                    String publickey=Singleton.INSTANCE.publicKeyString(account);
                    if(publickey!=null && !publickey.isEmpty()) {
                        mTextViewPublicKey.setText(publickey);
                    }
                }
                Animation animSlideDown = AnimationUtils.loadAnimation(getScreen().getActivity(),R.anim.slide_down);
                mAccountDetailsView.startAnimation(animSlideDown);
                mAccountDetailsView.setVisibility(View.VISIBLE);
                mAccountDetailsView.bringToFront();
                //  mRecyclerView.setVisibility(View.INVISIBLE);

            }
        });

        mArrowUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mToggleLayout.setVisibility(VISIBLE);
                Animation animSlideUp = AnimationUtils.loadAnimation(getScreen().getActivity(),R.anim.slide_up);
                mAccountDetailsView.startAnimation(animSlideUp);
                mAccountDetailsView.setVisibility(View.INVISIBLE);
            }
        });


        mEditTextNickName.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b){
                    if(account!=null) {
                        account.setName(mEditTextNickName.getText().toString());
                        DBHelper.saveAccount(account);
                    }
                }
            }
        });


        mCopyImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String publickey=Singleton.INSTANCE.publicKeyString(account);
                if(publickey!=null && !publickey.isEmpty()) {
                    Singleton.INSTANCE.copyToClipBoard(publickey,getScreen().getActivity());
                    Singleton.INSTANCE.showToast(getScreen().getActivity(),getScreen().getActivity().getString(R.string.copy_data_clipboard_message));

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


        mTextViewDisplayText.setOnClickListener(new OnClickListener() {
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
                        dialogInterface.dismiss();
                        AuthActivity authActivity = (AuthActivity)getScreen().getActivity();
                        authActivity.requestAuth(100);
                    }
                }).show();
            }
        });

        mTextViewHideText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePrivateKey();
            }
        });

        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
            int lastScrolledPagePosition = -1;
            boolean first = true;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (first && positionOffset == 0 && positionOffsetPixels == 0 && position == 0){
                    onPageSelected(0);
                    first = false;
                }
            }

            @Override
            public void onPageSelected(final int position) {
                mRecyclerView.setAdapter(mAdapter);
                if (mAccountList.size() > 1 && position != 0) {
                    account = mAccountList.get(position - 1);
                }  else {
                    account = mAccountList.get(position);
                }
                mAccountDetailsView.setVisibility(View.INVISIBLE);

                if (mAccountList.size() > 1 && position == 0) {
                    mToggleLayout.setVisibility(GONE);
                   /* mArrowup.setVisibility(View.GONE);
                    mArrowdown.setVisibility(GONE);*/
                } else {
                    mToggleLayout.setVisibility(VISIBLE);
                   /* mArrowup.setVisibility(View.VISIBLE);
                    mArrowdown.setVisibility(GONE);*/
                }
                hidePrivateKey();

                TaskExecutor taskExecutor = new TaskExecutor();
                taskExecutor.setListner(new TaskExecutor.TaskListner() {
                    @Override
                    public void onResult(BaseTask task1) {
                        List<TxnRecord> mTxnListViewPager = (List<TxnRecord>) task1.result;
                        mAdapter.setList(mTxnListViewPager);
                        if(mTxnListViewPager.size() > 0 ) {
                            mTextViewNoTransaction.setVisibility(View.INVISIBLE);
                        } else {
                            mTextViewNoTransaction.setVisibility(View.VISIBLE);
                        }
                    }
                });
                taskExecutor.execute(new BaseTask() {
                    @Override
                    public void main() {
                        if (mAccountList.size() > 1 && position == 0) {
                            result = DBHelper.getAllTxnRecord(null);
                        } else {
                            result = DBHelper.getAllTxnRecord(account);
                        }
                    };
                });
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };

        viewPager.addOnPageChangeListener(pageChangeListener);
       // pageChangeListener.onPageSelected(0);
    }

    void showPrivateKey() {
        isPrivateKeyDisplay = true;
        String privateKeyString = Singleton.INSTANCE.privateKeyString(account);
        mTextViewDisplayText.setVisibility(GONE);
        mTextViewHideText.setVisibility(VISIBLE);
        mTextViewPrivateKey.setText(privateKeyString);
    }

    void hidePrivateKey() {
        isPrivateKeyDisplay = false;
        mTextViewDisplayText.setVisibility(VISIBLE);
        mTextViewHideText.setVisibility(GONE);
        mTextViewPrivateKey.setText(R.string.default_private_key);
    }

}



class OverViewPagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<Account> accountsList;


    public OverViewPagerAdapter(Context context, List<Account> accounts) {
        mContext = context;
        this.accountsList = accounts;
    }

    @NonNull
    @Override
    public Object instantiateItem(@Nullable ViewGroup collection, final int position) {
       // CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.home_account_view, collection, false);
        TitleBarWrapper titleBar = new TitleBarWrapper(layout.findViewById(R.id.titleBar));
        final TextView hgcWalletText = (TextView)layout.findViewById(R.id.hgc_wallet_text);
        TextView dollerWalletText = (TextView)layout.findViewById(R.id.dollor_text);
        Button mPay = (Button)layout.findViewById(R.id.btn_pay);
        Button mRequest = (Button)layout.findViewById(R.id.btn_request);
        TextView lastUpdatedText = layout.findViewById(R.id.last_check_date);
        Account account = null;
        if(position == 0) {
            if(accountsList.size()>1) { } else
                account = accountsList.get(position);
        } else {
            account = accountsList.get(position-1);
        }

        long nanoCoins = 0;
        if (account != null) {
            String truncatePublicKey = Singleton.INSTANCE.publicKeyStringShort(account);
            titleBar.setTitle(account.getName() + " ..." + truncatePublicKey);
            nanoCoins = account.getBalance();
            if (account.getLastBalanceCheck() != null)
                lastUpdatedText.setText(App.instance.getString(R.string.text_last_updated,Singleton.INSTANCE.getDateFormat(account.getLastBalanceCheck())));
            else
                lastUpdatedText.setText("");

        } else  {
            titleBar.setTitle("All Accounts");
            nanoCoins =  Singleton.INSTANCE.getTotalBalance();
            lastUpdatedText.setText("");
        }

        hgcWalletText.setText(Singleton.INSTANCE.formatHGCShort(Singleton.INSTANCE.toCoins(nanoCoins)));
        dollerWalletText.setText(Singleton.INSTANCE.formatUSD(nanoCoins, true));

        mPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mViewListner != null) {
                    int pos;
                    if(accountsList.size() > 1 && position !=0 ) {
                    pos = position-1;
                    } else {
                     pos = position;
                    }
                    Account account=accountsList.get(pos);
                    mViewListner.onPayButtonClick(account);
                }
            }
        });

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mViewListner != null) {
                    int pos;
                    if(accountsList.size()>1 && position != 0) {
                        pos = position-1;
                    } else {
                        pos = position;
                    }
                    Account account=accountsList.get(pos);
                    mViewListner.onRequestButtonClick(account);
                }
            }
        });

        final long finalNanoCoins = nanoCoins;
        hgcWalletText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.view_custom_dailog);


                TextView text = (TextView) dialog.findViewById(R.id.hgc_wallet_text);
                text.setText(Singleton.INSTANCE.formatHGC(Singleton.INSTANCE.toCoins(finalNanoCoins),true));

                Button dialogButton = (Button) dialog.findViewById(R.id.btn_cancel_dialog);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return accountsList.size() >1 ? accountsList.size()+1:accountsList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    public interface ViewListner{
        public void onPayButtonClick(Account account);
        public void onRequestButtonClick(Account account);
    }

    ViewListner mViewListner;

    public void setViewListner(ViewListner onitemClick) {
        this.mViewListner = onitemClick;
    }

}

class TrasactionListAdapter extends RecyclerView.Adapter<TrasactionListAdapter.MyViewHolder> {

    private List<TxnRecord> mTxnList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextViewTime, mTextViewAccountName, mTextViewHgcWallet,mTextViewPublicKey,mTextViewDoller;
        private TextView mTextViewPositiveSign,mTextViewNegativeSign;

        public MyViewHolder(@NonNull View view) {
            super(view);
            mTextViewTime = (TextView) view.findViewById(R.id.text_time);
            mTextViewAccountName = (TextView) view.findViewById(R.id.text_account_name);
            mTextViewHgcWallet = (TextView) view.findViewById(R.id.hgc_wallet_text);
            mTextViewPublicKey = (TextView) view.findViewById(R.id.text_key);
            mTextViewDoller = (TextView) view.findViewById(R.id.dollor_text);
            mTextViewPositiveSign = (TextView) view.findViewById(R.id.text_positive_sign);
            mTextViewNegativeSign = (TextView) view.findViewById(R.id.text_negative_sign);

        }
    }


    public TrasactionListAdapter(List<TxnRecord> txnList) {
        this.mTxnList = txnList;
    }

    public void setList(List<TxnRecord> list) {
        mTxnList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trasaction_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        long nanoCoins = 0;
        final TxnRecord txn = mTxnList.get(position);
        nanoCoins = txn.getAmount();
        if(txn.isPositive() == true) {
         holder.mTextViewPositiveSign.setVisibility(View.VISIBLE);
         holder.mTextViewNegativeSign.setVisibility(View.GONE);
        } else {
            holder.mTextViewPositiveSign.setVisibility(View.GONE);
            holder.mTextViewNegativeSign.setVisibility(View.VISIBLE);
        }
        holder.mTextViewTime.setText(Singleton.INSTANCE.getDateFormat(txn.getCreatedDate()));
        Contact accountContact = txn.isPositive() ? txn.getFromAccount() : txn.getToAccount();
        String accountId = txn.isPositive() ? txn.getFromAccId() : txn.getToAccountId();
        holder.mTextViewAccountName.setText((accountContact == null || accountContact.getName() == null || accountContact.getName().isEmpty()) ? "UNKNOWN" : accountContact.getName());
        holder.mTextViewHgcWallet.setText(Singleton.INSTANCE.formatHGCShort(nanoCoins, true));
        holder.mTextViewPublicKey.setText("ENDING IN..."+accountId);
        holder.mTextViewDoller.setText(Singleton.INSTANCE.formatUSD(Singleton.INSTANCE.hgcToUSD(nanoCoins), true));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onTransactionClick != null ) {
                    onTransactionClick.onTransaction(txn);
                }
            }
        });
    }

    @Override
    public int getItemCount() {

        return mTxnList.size();
    }

    public interface OnTransactionClick {
        public void onTransaction(TxnRecord txnRecord);
    }

    OnTransactionClick onTransactionClick;

    public void setOnTransactionClick(OnTransactionClick onTransactionClick) {
        this.onTransactionClick = onTransactionClick;
    }


}

