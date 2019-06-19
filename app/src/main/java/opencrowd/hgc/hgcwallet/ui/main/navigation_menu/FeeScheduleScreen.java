package opencrowd.hgc.hgcwallet.ui.main.navigation_menu;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import java.util.ArrayList;
import java.util.List;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class FeeScheduleScreen extends Screen<FeeScheduleScreenView>{
    @Override
    protected FeeScheduleScreenView createView(Context context) {
        return new FeeScheduleScreenView(context);
    }
}

class FeeScheduleScreenView extends BaseScreenView<FeeScheduleScreen> {
    private RecyclerView recyclerView;
    private FeeSCheduleListAdapter mAdapter;
    public FeeScheduleScreenView(Context context) {
        super(context);
        inflate(context, R.layout.view_accout_list_layout,this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("FEE SCHEDULE");
        recyclerView = (RecyclerView)findViewById(R.id.account_list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        List<String> mList = new ArrayList<>();
        mList.add("Balance Check");
        mList.add("Transaction History");
        mList.add("Make Payment");

        mAdapter = new FeeSCheduleListAdapter(mList);
        recyclerView.setAdapter(mAdapter);
    }
}

class FeeSCheduleListAdapter extends RecyclerView.Adapter<FeeSCheduleListAdapter.MyViewHolder> {

    List<String> mlist = new ArrayList<>();


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mFeeName, hgcWalletValue;

        public MyViewHolder(@NonNull View view) {
            super(view);
            mFeeName = (TextView) view.findViewById(R.id.text_fee_name);
            hgcWalletValue = (TextView) view.findViewById(R.id.hgc_wallet_text);
        }
    }


    public FeeSCheduleListAdapter(List<String> mlist) {
      this.mlist = mlist;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fee_schedule_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
           holder.mFeeName.setText(mlist.get(position));
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }
}