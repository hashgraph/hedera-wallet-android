package opencrowd.hgc.hgcwallet.ui.main.request;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import java.util.List;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.account.Account;
import opencrowd.hgc.hgcwallet.database.request.PayRequest;
import opencrowd.hgc.hgcwallet.modals.HGCAccountID;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;
import opencrowd.hgc.hgcwallet.ui.main.pay.PayScreen;

public class RequestListScreen extends Screen<RequestListView> {

    @Override
    protected RequestListView createView(Context context) {
        return new RequestListView(context);
    }
}

class RequestListView  extends BaseScreenView<RequestListScreen> {

    private List<PayRequest> mRequestList;
    private RecyclerView mRecyclerView;
    private RequestListAdapter mAdapter;


    public RequestListView(Context context) {
        super(context);
        inflate(context, R.layout.view_accout_list_layout,this);
        mRequestList = DBHelper.getAllRequests();
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("REQUESTS");
        if (mRequestList.size() > 0) {
            mRecyclerView = (RecyclerView) findViewById(R.id.account_list_recyclerview);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setHasFixedSize(true);
            mAdapter = new RequestListAdapter(mRequestList);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setmRequestListner(new RequestListAdapter.OnPayRequestClick() {
                @Override
                public void onRequestPick(PayRequest toAccount) {
                    // below code should be discuss
                    List<Account> accountList = DBHelper.getAllAccounts();
                    Account fromAccount = accountList.get(0);
                    getScreen().getNavigator().goTo(new PayScreen(fromAccount, toAccount));
                }
            });
        }
    }
}

class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.MyViewHolder> {

    private List<PayRequest> mRequestList;

    public RequestListAdapter(List<PayRequest> requestList) {
        this.mRequestList = requestList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.request_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @NonNull


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final PayRequest payRequest = mRequestList.get(position);
        holder.mTextViewAccountName.setVisibility(View.VISIBLE);
        if(payRequest.getName() != null && !payRequest.getName().isEmpty()) {
            holder.mTextViewAccountName.setText(payRequest.getName());
        }else {
            holder.mTextViewAccountName.setText("UNKNOWN");
        }
        if(payRequest.getNotes() != null && !payRequest.getNotes().isEmpty()) {
            holder.mTextViewNotes.setText(payRequest.getNotes());
            holder.mTextViewNotes.setVisibility(View.VISIBLE);
        }else {
            holder.mTextViewNotes.setVisibility(View.GONE);
        }
        if(payRequest.getAccountId() != null && !payRequest.getAccountId().isEmpty()) {
            holder.mTextViewAccountId.setText(payRequest.getAccountId());
            holder.mTextViewAccountId.setVisibility(View.VISIBLE);
        }else {
            holder.mTextViewAccountId.setVisibility(View.GONE);
        }


        if(payRequest.getImportDate() != null) {
            holder.mTextViewTime.setText(Singleton.INSTANCE.getDateFormat(payRequest.getImportDate()));
            holder.mTextViewTime.setVisibility(View.VISIBLE);
        }else {
            holder.mTextViewTime.setVisibility(View.GONE);
        }

        if (payRequest.getAmount() > 0) {
            holder.mTextViewDollerValue.setText(Singleton.INSTANCE.formatUSD(payRequest.getAmount(),true));
            holder.mTextViewHgcWalletValue.setText(Singleton.INSTANCE.formatHGCShort(payRequest.getAmount(), true));
            holder.mTextViewDollerValue.setVisibility(View.VISIBLE);
            holder.mTextViewHgcWalletValue.setVisibility(View.VISIBLE);
        } else {
            holder.mTextViewDollerValue.setVisibility(View.INVISIBLE);
            holder.mTextViewHgcWalletValue.setVisibility(View.INVISIBLE);
        }

        holder.mButtonPay.setVisibility(View.VISIBLE);

        holder.mButtonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRequestListner != null) {
                    mRequestListner.onRequestPick(payRequest);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mRequestList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewTime,mTextViewAccountName, mTextViewHgcWalletValue, mTextViewAccountId,mTextViewDollerValue,mTextViewNotes;
        public Button mButtonPay;
        public MyViewHolder(@NonNull View view) {
            super(view);
            mTextViewTime = (TextView) view.findViewById(R.id.text_time);
            mTextViewAccountName = (TextView) view.findViewById(R.id.text_account_name);
            mTextViewHgcWalletValue = (TextView) view.findViewById(R.id.hgc_wallet_text);
            mTextViewAccountId = (TextView) view.findViewById(R.id.text_key);
            mTextViewDollerValue = (TextView)view.findViewById(R.id.dollor_text);
            mTextViewNotes = (TextView)view.findViewById(R.id.notes_text);
            mButtonPay = (Button)view.findViewById(R.id.btn_pay);
        }
    }

    public interface OnPayRequestClick {
        public void onRequestPick(PayRequest payRequest);
    }

    OnPayRequestClick mRequestListner;

    public void setmRequestListner(OnPayRequestClick onRequestClick) {
        this.mRequestListner = onRequestClick;
    }
}
