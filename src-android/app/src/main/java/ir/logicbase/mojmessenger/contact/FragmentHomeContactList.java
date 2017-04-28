package ir.logicbase.mojmessenger.contact;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.profile.ActivityProfile;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHomeContactList extends Fragment implements ContactView {

    private AdapterContactList adapterOnlineContacts, adapterAllContacts;
    private ProgressBar progressAll;
    private ContactPresenter presenter;

    public FragmentHomeContactList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize view id
        View rootView = inflater.inflate(R.layout.fragment_home_contact_list, container, false);
        RecyclerView recViewOnline = rootView.findViewById(R.id.recView_online_contacts);
        RecyclerView recViewAll = rootView.findViewById(R.id.recView_all_contacts);
        TextView txtViewOnlineContactsLabel = rootView.findViewById(R.id.txtView_contact_online_label);
        TextView txtViewAllContactsLabel = rootView.findViewById(R.id.txtView_contact_all_label);
        TextView txtViewOnlineEmpty = rootView.findViewById(R.id.txtView_contact_online_empty);
        TextView txtViewAllEmpty = rootView.findViewById(R.id.txtView_contact_all_empty);
        progressAll = rootView.findViewById(R.id.progress_contact_all);

        // Change Font
        Typeface typeface = TypefaceManager.get(getActivity(), getString(R.string.font_iran_sans));
        txtViewOnlineContactsLabel.setTypeface(typeface);
        txtViewAllContactsLabel.setTypeface(typeface);
        txtViewOnlineEmpty.setTypeface(typeface);
        txtViewAllEmpty.setTypeface(typeface);

        // Configure RecyclerView online contacts
        adapterOnlineContacts = new AdapterContactList(txtViewOnlineEmpty, true);
        recViewOnline.setAdapter(adapterOnlineContacts);
        LinearLayoutManager l1 = new LinearLayoutManager(getActivity());
        recViewOnline.setLayoutManager(l1);
        recViewOnline.setNestedScrollingEnabled(false);

        // Configure RecyclerView all contacts
        adapterAllContacts = new AdapterContactList(txtViewAllEmpty, false);
        recViewAll.setAdapter(adapterAllContacts);
        LinearLayoutManager l2 = new LinearLayoutManager(getActivity());
        recViewAll.setLayoutManager(l2);
        recViewAll.setNestedScrollingEnabled(false);

        // Set adapters listener
        adapterAllContacts.setListener(this::goToProfile);  // using java 8 method reference
        adapterOnlineContacts.setListener(this::goToProfile);

        presenter = new ContactPresenter(getContext(), this);
        progressAll.setVisibility(View.VISIBLE);
        presenter.initiate();

        return rootView;
    }

    private void goToProfile(ContactEntity contact) {
        Intent intent = new Intent(getActivity(), ActivityProfile.class);
        intent.putExtra(ActivityProfile.INTENT_KEY_CONTACT_ID, contact.getId());
        intent.putExtra(ActivityProfile.INTENT_KEY_NAME, contact.getName());
        intent.putExtra(ActivityProfile.INTENT_KEY_PIC, contact.getPic());
        intent.putExtra(ActivityProfile.INTENT_KEY_LAST_SEEN, contact.getLastSeen());
        intent.putExtra(ActivityProfile.INTENT_KEY_PHONE, contact.getPhone());
        intent.putExtra(ActivityProfile.INTENT_KEY_IS_ONLINE, contact.isOnline());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        progressAll.setVisibility(View.GONE);
        presenter.startListening(getActivity().getApplicationContext(),this);
        presenter.syncContactsStatus(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        progressAll.setVisibility(View.GONE);
        presenter.stopListening();
    }

    @Override
    public void initiateAllContacts(List<ContactEntity> contacts) {
        progressAll.setVisibility(View.GONE);
        adapterAllContacts.swapDataSource(contacts);
    }

    @Override
    public void updateContactLists(List<ContactEntity> contacts) {
        List<ContactEntity> onlineContacts = new ArrayList<>();
        for (ContactEntity contact : contacts) {
            if (contact.isOnline()) {
                onlineContacts.add(contact);
            }
        }
        adapterAllContacts.updateDataSource(contacts);
        adapterOnlineContacts.updateDataSource(onlineContacts);
    }

    @Override
    public void updateContact(ContactEntity contact) {
        adapterOnlineContacts.updateItem(contact);
        adapterAllContacts.updateItem(contact);
    }

}
