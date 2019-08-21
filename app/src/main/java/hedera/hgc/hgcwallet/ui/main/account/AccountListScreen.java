package hedera.hgc.hgcwallet.ui.main.account;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import java.util.List;

import hedera.hgc.hgcwallet.App;
import hedera.hgc.hgcwallet.R;
import hedera.hgc.hgcwallet.common.Singleton;
import hedera.hgc.hgcwallet.database.DBHelper;
import hedera.hgc.hgcwallet.database.account.Account;
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class AccountListScreen extends Screen<AccountListView> {
    public interface AccountPickerListener {
        public void onAccountPick(Account account);
    }

    AccountPickerListener pickerListener;

    public AccountListScreen(AccountPickerListener pickerListener)  {
        this.pickerListener = pickerListener;
    }

    @NonNull
    @Override
    protected AccountListView createView(@NonNull Context context)  {
        return new AccountListView(context,pickerListener);
    }
}

class AccountListView extends BaseScreenView<AccountListScreen> {

    private List<Account> accounts;
    private RecyclerView recyclerView;
    private AccountListAdapter mAdapter;

    public AccountListView(Context context, final @Nullable AccountListScreen.AccountPickerListener pickerListener) {
        super(context);
        inflate(context, R.layout.view_accout_list_layout,this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        accounts = DBHelper.INSTANCE.getAllAccounts();
        int totalAccount = accounts.size();
        titleBar.setTitle(String.valueOf(totalAccount) + " ACTIVE ACCOUNT" +((totalAccount == 1)?"":"S"));

        if (pickerListener != null) {
            titleBar.setCloseButtonHidden(true);
            titleBar.setTitle("Please select an account");
        } else {
            titleBar.setCloseButtonHidden(true);
            titleBar.setImageResource(R.drawable.ic_add_account);
            titleBar.setOnCloseButtonClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getScreen().getNavigator().goTo(new AddAccountScreen());
                }
            });
        }
        recyclerView = (RecyclerView)findViewById(R.id.account_list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        mAdapter = new AccountListAdapter(accounts);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnAccountClick(new AccountListAdapter.OnItemClick() {
            @Override
            public void onAccount(int position, Account account) {
                if(pickerListener == null) {
                    getScreen().getNavigator().goTo(new AccountCreateScreen(account, "ACCOUNT DETAILS", true));

                } else {
                    pickerListener.onAccountPick(account);
                }
            }
        });
    }
}

class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.MyViewHolder> {

    private List<Account> accountList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView accountName, hgcWalletValue, key,dollerValue;

        public MyViewHolder(@NonNull View view) {
            super(view);
            accountName = (TextView) view.findViewById(R.id.text_account_name);
            hgcWalletValue = (TextView) view.findViewById(R.id.hgc_wallet_text);
            key = (TextView) view.findViewById(R.id.text_key);
            dollerValue = (TextView)view.findViewById(R.id.dollor_text);
        }
    }


    public AccountListAdapter(List<Account> accountList) {
        this.accountList = accountList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Account account = accountList.get(position);
        holder.accountName.setText(account.getName());
        String shortKey = Singleton.INSTANCE.publicKeyStringShort(account);
        holder.key.setText(App.Companion.getInstance().getString(R.string.text_key_short, shortKey));
        long nanoCoins = account.getBalance();
        holder.hgcWalletValue.setText(Singleton.INSTANCE.formatHGCShort(nanoCoins, true));
        holder.dollerValue.setText(Singleton.INSTANCE.formatUSD(nanoCoins, true));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onAccountClick != null) {
                    onAccountClick.onAccount(position,account);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }


    public interface OnItemClick {
        public void onAccount(int position,Account account);
    }

    OnItemClick onAccountClick;

    public void setOnAccountClick(OnItemClick onitemClick) {
        this.onAccountClick = onitemClick;
    }
}
