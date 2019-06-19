package opencrowd.hgc.hgcwallet.ui.main.pay.contacts;


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

import opencrowd.hgc.hgcwallet.R;
import opencrowd.hgc.hgcwallet.database.DBHelper;
import opencrowd.hgc.hgcwallet.database.contact.Contact;
import opencrowd.hgc.hgcwallet.ui.customviews.TitleBarWrapper;

public class ContactListScreen extends Screen<ContactListView> {

    public interface ContactPickerListener {
        public void onContactPick(Contact contact);
    }

    ContactPickerListener pickerListener;

    public ContactListScreen(ContactPickerListener pickerListener)  {
        this.pickerListener = pickerListener;
    }
    @Override
    protected ContactListView createView(Context context) {
        return new ContactListView(context,pickerListener);
    }
}

class ContactListView extends BaseScreenView<ContactListScreen> {

    private List<Contact> contacts;
    private RecyclerView recyclerView;
    private ContactListAdapter mAdapter;

    public ContactListView(Context context, final @Nullable ContactListScreen.ContactPickerListener pickerListener) {
        super(context);
        inflate(context, R.layout.view_contact_list,this);
        TitleBarWrapper titleBar = new TitleBarWrapper(findViewById(R.id.titleBar));
        titleBar.setCloseButtonHidden(false);
        titleBar.setOnCloseButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreen().getNavigator().goBack();
            }
        });

        contacts = DBHelper.getAllContacts();
        if (contacts != null && !contacts.isEmpty()) {
            titleBar.setTitle("EXISTING CONTACTS");
        } else {
            titleBar.setTitle("NO CONTACTS");
        }

        recyclerView = (RecyclerView)findViewById(R.id.contact_list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        mAdapter = new ContactListAdapter(contacts);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnContactClick(new ContactListAdapter.OnItemClick() {
            @Override
            public void onContact(int position, Contact contact) {
                pickerListener.onContactPick(contact);
            }
        });
    }
    }


class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.MyViewHolder> {

    private List<Contact> contactList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView accountName, accountId, isVerified;

        public MyViewHolder(@NonNull View view) {
            super(view);
            accountName = (TextView) view.findViewById(R.id.text_account_name);
            accountId= (TextView) view.findViewById(R.id.text_account_id);
            isVerified = (TextView) view.findViewById(R.id.text_account_is_verified);
        }
    }


    public ContactListAdapter(List<Contact> contactList) {
        this.contactList = contactList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Contact contact = contactList.get(position);
        holder.accountName.setText((contact.getName() == null || contact.getName().isEmpty()) ? "UNKNOWN" : contact.getName());
        holder.accountId.setText( contact.getAccountId());
        if(!contact.isVerified())
        {
            holder.isVerified.setText(R.string.text_unverified);
            holder.isVerified.setVisibility(View.VISIBLE);
        }else
        {
            holder.isVerified.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onContactClick != null) {
                    onContactClick.onContact(position,contact);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


    public interface OnItemClick {
        public void onContact(int position,Contact contact);
    }

    OnItemClick onContactClick;

    public void setOnContactClick(OnItemClick onitemClick) {
        this.onContactClick = onitemClick;
    }
}
