package opencrowd.hgc.hgcwallet.ui.main.developertool;


import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.wealthfront.magellan.BaseScreenView;
import com.wealthfront.magellan.Screen;

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.node.Node;
import opencrowd.hgc.hgcwallet.modals.HGCAccountID;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class CreateNodeScreen extends Screen<CreateNodeScreenView> {

    Node node;
    public CreateNodeScreen(Node node) {
        this.node = node;
    }
    @Override
    protected CreateNodeScreenView createView(Context context) {
        return new CreateNodeScreenView(context,node);
    }
}

class CreateNodeScreenView extends BaseScreenView<CreateNodeScreen> {

    private EditText mEditTextHost,mEditTextPort,mEditTextAccountId;
    private CheckBox mCheckBoxIsActive;
    private Button mButtonsave;
    public CreateNodeScreenView(Context context, final Node paramNode) {
        super(context);
        inflate(context, R.layout.view_node_add_layout,this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setTitle(paramNode == null ? "Add Node" : "Edit Node");
        mEditTextHost = (EditText)findViewById(R.id.edittext_node_host);
        mEditTextPort = (EditText)findViewById(R.id.edittext_node_port);
        mEditTextAccountId = (EditText)findViewById(R.id.edittext_node_account);
        mCheckBoxIsActive = (CheckBox)findViewById(R.id.checkBox_active);
        mButtonsave = (Button)findViewById(R.id.btn_save);

        if(paramNode != null) {
            if(paramNode.getHost() != null) {
                mEditTextHost.setText(paramNode.getHost());
            }
            mEditTextPort.setText(String.valueOf(paramNode.getPort()));
            HGCAccountID accountID = paramNode.accountID();
            mEditTextAccountId.setText(accountID.stringRepresentation());
            mCheckBoxIsActive.setChecked(!paramNode.getDisabled());
        }

        mButtonsave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Node node = paramNode;
                if (paramNode == null) {
                    node = new Node();
                }
                if(!mEditTextHost.getText().toString().isEmpty()) {
                    node.setHost( mEditTextHost.getText().toString());
                }

                if(!mEditTextPort.getText().toString().isEmpty()) {
                    node.setPort(Integer.parseInt(mEditTextPort.getText().toString()));
                }

                if(!mEditTextAccountId.getText().toString().isEmpty()) {
                    HGCAccountID accountID = HGCAccountID.fromString(mEditTextAccountId.getText().toString());
                    if(accountID != null) {
                        node.setAccountID(accountID);
                    }
                }
                node.setDisabled( !mCheckBoxIsActive.isChecked());

                if (paramNode == null) {
                    DBHelper.createNode(node);

                } else  {
                    DBHelper.updateNode(node);

                }
                getScreen().getNavigator().goBack();

            }
        });
    }
}