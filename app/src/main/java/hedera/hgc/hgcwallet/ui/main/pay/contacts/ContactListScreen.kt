package hedera.hgc.hgcwallet.ui.main.pay.contacts


import android.content.Context
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen

import hedera.hgc.hgcwallet.R
import hedera.hgc.hgcwallet.database.DBHelper
import hedera.hgc.hgcwallet.database.contact.Contact
import hedera.hgc.hgcwallet.ui.customviews.TitleBarWrapper

class ContactListScreen(pickerListener: ContactPickerListener?, forThirdParty: Boolean) : Screen<ContactListView>() {

    data class Params(val contacts: List<Contact>, val pickerListener: ContactPickerListener?, val forThirdParty: Boolean)

    private val params: Params

    init {
        var allContacts = DBHelper.getAllContacts() ?: listOf()
        if (forThirdParty)
            allContacts = allContacts.filter { it.isThirdPartyContact() }
        params = Params(allContacts, pickerListener, forThirdParty)
    }

    interface ContactPickerListener {
        fun onContactPick(contact: Contact)
    }

    override fun createView(context: Context): ContactListView {
        return ContactListView(context, params)
    }

    internal fun goBack() {
        navigator?.goBack()
    }

    internal fun onContactPick(contact: Contact) {
        params.pickerListener?.onContactPick(contact)
    }
}

class ContactListView(context: Context, val params: ContactListScreen.Params) : BaseScreenView<ContactListScreen>(context) {

    private val recyclerView: RecyclerView?
    private val mAdapter: ContactListAdapter
    private val titleBar: TitleBarWrapper

    constructor(context: Context) : this(context, ContactListScreen.Params(listOf(), null, false))

    init {
        View.inflate(context, R.layout.view_contact_list, this)
        titleBar = TitleBarWrapper(findViewById(R.id.titleBar)).apply {
            setCloseButtonHidden(false)
            setOnCloseButtonClickListener { screen?.goBack() }
        }

        mAdapter = ContactListAdapter(params.contacts).apply {
            OnContactClick(object : ContactListAdapter.OnItemClick {
                override fun onContact(position: Int, contact: Contact) {
                    screen?.onContactPick(contact)
                }
            })
        }

        recyclerView = findViewById<RecyclerView>(R.id.contact_list_recyclerview)?.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = mAdapter
        }

        reloadData()
    }

    fun reloadData() {
        titleBar.setTitle(if (params.contacts.isEmpty()) "NO CONTACTS" else "EXISTING CONTACTS")

    }
}


internal class ContactListAdapter(private val contactList: List<Contact>) : RecyclerView.Adapter<ContactListAdapter.MyViewHolder>() {

    private var contactClick: OnItemClick? = null

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var accountName: TextView
        var accountId: TextView
        var isVerified: TextView

        init {
            accountName = view.findViewById<TextView>(R.id.text_account_name)
            accountId = view.findViewById<TextView>(R.id.text_account_id)
            isVerified = view.findViewById<TextView>(R.id.text_account_is_verified)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.contact_list_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val contact = contactList[position]
        holder.accountName.text = if (contact.name == null || contact.name!!.isEmpty()) "UNKNOWN" else contact.name
        holder.accountId.text = contact.accountId
        holder.isVerified.setText("")

        holder.itemView.setOnClickListener {
            contactClick?.onContact(position, contact)
        }
    }

    override fun getItemCount(): Int {
        return contactList.size
    }


    interface OnItemClick {
        fun onContact(position: Int, contact: Contact)
    }

    fun OnContactClick(onitemClick: OnItemClick) {
        this.contactClick = onitemClick
    }
}
