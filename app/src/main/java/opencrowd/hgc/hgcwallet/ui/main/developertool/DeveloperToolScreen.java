package opencrowd.hgc.hgcwallet.ui.main.developertool;


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

public class DeveloperToolScreen extends Screen<DeveloperToolScreenView>{
    @Override
    protected DeveloperToolScreenView createView(Context context) {
        return new DeveloperToolScreenView(context);
    }
}

class DeveloperToolScreenView extends BaseScreenView<DeveloperToolScreen> {

    private List<String> mDeveloperToolList = new ArrayList<>();
    private RecyclerView recyclerView;
    private DeveloperToolListAdapter mAdapter;

    public DeveloperToolScreenView(Context context) {
        super(context);
        inflate(context, R.layout.view_developer_tool,this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("Developer Tool");
        recyclerView = (RecyclerView)findViewById(R.id.tool_list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        mDeveloperToolList.add("Manage Nodes");
        mDeveloperToolList.add("Logs");
        mAdapter = new DeveloperToolListAdapter(mDeveloperToolList);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnToolClick(new DeveloperToolListAdapter.OnItemClick() {
            @Override
            public void onTool(int position) {
                if(position == 0) {
                    getScreen().getNavigator().goTo(new NodeScreen());
                } else {

                }
            }
        });
    }
}

class DeveloperToolListAdapter extends RecyclerView.Adapter<DeveloperToolListAdapter.MyViewHolder> {

    private List<String> mDeveloperToolList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mName;

        public MyViewHolder(@NonNull View view) {
            super(view);
            mName = (TextView) view.findViewById(R.id.text_node_host);

        }
    }


    public DeveloperToolListAdapter(List<String> mDeveloperToolList) {
        this.mDeveloperToolList = mDeveloperToolList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.developer_tool_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.mName.setText(mDeveloperToolList.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onToolClick != null) {
                    onToolClick.onTool(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDeveloperToolList.size();
    }


    public interface OnItemClick {
        public void onTool(int position);
    }

    OnItemClick onToolClick;

    public void setOnToolClick(OnItemClick onitemClick) {
        this.onToolClick = onitemClick;
    }
}

