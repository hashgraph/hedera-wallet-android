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

import opencrowd.hgc.hgcwallet.App;
import opencrowd.hgc.hgcwallet.BuildConfig;
import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.database.node.Node;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class NodeScreen extends Screen<NodeScreenView>{


    @Override
    protected NodeScreenView createView(Context context) {
        return new NodeScreenView(context);
    }
}

class NodeScreenView extends BaseScreenView<NodeScreen> {

    private List<Node> mNodeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NodeListAdapter mAdapter;
    private boolean mTestMode;

    public NodeScreenView(Context context) {
        super(context);
        inflate(context, R.layout.view_developer_tool,this);
        mTestMode = BuildConfig.ALLOW_EDITING_NET;
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle("Nodes");
        if(mTestMode) {
            titleBar.setCloseButtonHidden(false);
            titleBar.setImageResource(R.drawable.ic_add_account);
            titleBar.setOnCloseButtonClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getScreen().getNavigator().goTo(new CreateNodeScreen(null));
                }
            });
        }
        recyclerView = (RecyclerView)findViewById(R.id.tool_list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        mNodeList = App.instance.addressBook.getList(false);
        mAdapter = new NodeListAdapter(mNodeList,context,mTestMode);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnNodeClick(new NodeListAdapter.OnNodeClick() {
            @Override
            public void onNode(Node node) {
                getScreen().getNavigator().goTo(new CreateNodeScreen(node));
            }
        });
    }
}

class NodeListAdapter extends RecyclerView.Adapter<NodeListAdapter.MyViewHolder> {

    private List<Node> mNodeList;
    private Context context;
    private boolean mTestMode;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mNodeHost,mNodeAccountId;


        public MyViewHolder(@NonNull View view) {
            super(view);
            mNodeHost = (TextView) view.findViewById(R.id.text_node_host);
            mNodeAccountId = (TextView)view.findViewById(R.id.text_node_account_id);
        }
    }


    public NodeListAdapter(List<Node> mNodeList,Context context,boolean mTestmode) {
        this.mNodeList = mNodeList;
        this.context = context;
        this.mTestMode = mTestmode;
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
        final Node node = mNodeList.get(position);

            holder.mNodeHost.setText(node.getHost() + ":" + String.valueOf(node.getPort()));
            holder.mNodeAccountId.setText(node.accountID().stringRepresentation());
            holder.mNodeAccountId.setVisibility(View.VISIBLE);
            if(node.getDisabled() == false) {
                holder.mNodeHost.setTextColor(context.getResources().getColor(R.color.text_primary,null));
                holder.mNodeAccountId.setTextColor(context.getResources().getColor(R.color.text_primary,null));
            } else {
                holder.mNodeHost.setTextColor(context.getResources().getColor(R.color.text_light,null));
                holder.mNodeAccountId.setTextColor(context.getResources().getColor(R.color.text_light,null));
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onNodeClick != null && mTestMode) {
                        onNodeClick.onNode(node);
                    }
                }
            });


    }

    @Override
    public int getItemCount() {
        return mNodeList.size();
    }

    public interface OnNodeClick {
        public void onNode(Node node);
    }

    OnNodeClick onNodeClick;

    public void setOnNodeClick(OnNodeClick onitemClick) {
        this.onNodeClick = onitemClick;
    }
}

