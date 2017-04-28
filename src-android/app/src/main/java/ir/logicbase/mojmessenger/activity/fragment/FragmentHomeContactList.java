package ir.logicbase.mojmessenger.activity.fragment;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.activity.ActivityProfile;
import ir.logicbase.mojmessenger.adapter.AdapterContactList;
import ir.logicbase.mojmessenger.bundle.BundleContact;
import ir.logicbase.mojmessenger.bundle.BundleRetContact;
import ir.logicbase.mojmessenger.contact.AccountHelper;
import ir.logicbase.mojmessenger.contact.ContactHelper;
import ir.logicbase.mojmessenger.database.DatabaseGateway;
import ir.logicbase.mojmessenger.socket.IncomingGateway;
import ir.logicbase.mojmessenger.socket.OutgoingGateway;
import ir.logicbase.mojmessenger.util.Connectivity;
import ir.logicbase.mojmessenger.util.PermissionsRequest;
import ir.logicbase.mojmessenger.util.PrefManager;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHomeContactList extends Fragment {

    private AdapterContactList adapterOnlineContacts, adapterAllContacts;
    private ProgressBar progressAll;
    private TextView txtViewOnlineEmpty, txtViewAllEmpty;
    private SyncContactsTask syncContactsTask;

    public FragmentHomeContactList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_contact_list, container, false);
        RecyclerView recViewOnline = (RecyclerView) rootView.findViewById(R.id.recView_online_contacts);
        RecyclerView recViewAll = (RecyclerView) rootView.findViewById(R.id.recView_all_contacts);
        TextView txtViewOnlineContactsLabel = (TextView) rootView.findViewById(R.id.txtView_contact_online_label);
        TextView txtViewAllContactsLabel = (TextView) rootView.findViewById(R.id.txtView_contact_all_label);
        txtViewOnlineEmpty = (TextView) rootView.findViewById(R.id.txtView_contact_online_empty);
        txtViewAllEmpty = (TextView) rootView.findViewById(R.id.txtView_contact_all_empty);
        progressAll = (ProgressBar) rootView.findViewById(R.id.progress_contact_all);

        /*
        Change Fonts
         */
        Typeface typeface = TypefaceManager.get(getActivity(), getString(R.string.font_iran_sans));
        txtViewOnlineContactsLabel.setTypeface(typeface);
        txtViewAllContactsLabel.setTypeface(typeface);
        txtViewOnlineEmpty.setTypeface(typeface);
        txtViewAllEmpty.setTypeface(typeface);

        // Configure RecyclerView online contacts
        adapterOnlineContacts = new AdapterContactList();
        recViewOnline.setAdapter(adapterOnlineContacts);
        LinearLayoutManager l1 = new LinearLayoutManager(getActivity());
        recViewOnline.setLayoutManager(l1);
        recViewOnline.setNestedScrollingEnabled(false);
        // Configure RecyclerView all contacts
        adapterAllContacts = new AdapterContactList();
        recViewAll.setAdapter(adapterAllContacts);
        LinearLayoutManager l2 = new LinearLayoutManager(getActivity());
        recViewAll.setLayoutManager(l2);
        recViewAll.setNestedScrollingEnabled(false);

        adapterAllContacts.setListener(new AdapterContactList.ContactListListener() {
            @Override
            public void onClick(BundleContact contact) {
                goToProfile(contact);
            }
        });

        adapterOnlineContacts.setListener(new AdapterContactList.ContactListListener() {
            @Override
            public void onClick(BundleContact contact) {
                goToProfile(contact);
            }
        });

        new InitiateContactsFromDBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return rootView;
    }

    private void goToProfile(BundleContact contact) {
        Intent intent = new Intent(getActivity(), ActivityProfile.class);
        intent.putExtra(ActivityProfile.INTENT_KEY_CONTACT_ID, contact.getId());
        intent.putExtra(ActivityProfile.INTENT_KEY_NAME, contact.getName());
        intent.putExtra(ActivityProfile.INTENT_KEY_PIC, contact.getPic());
        intent.putExtra(ActivityProfile.INTENT_KEY_LAST_SEEN, contact.getLastSeen());
        intent.putExtra(ActivityProfile.INTENT_KEY_PHONE, contact.getPhone());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // initialize contact list
        if (PermissionsRequest.checkReadContactsPermission(getContext(), getActivity())) {
            if (Connectivity.isConnected(getContext())) {
                if (syncContactsTask == null){
                    syncContactsTask = new SyncContactsTask();
                    syncContactsTask.execute();
                } else {
                    syncContactsTask.isCancelled = true;
                    syncContactsTask.threadBlocker.open();

                    syncContactsTask = new SyncContactsTask();
                    syncContactsTask.execute();
                }
            }
        }
    }

    private class InitiateContactsFromDBTask extends AsyncTask<Void, Void, ArrayList<BundleContact>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressAll.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<BundleContact> doInBackground(Void... voids) {
            DatabaseGateway gateway = new DatabaseGateway(getContext());
            ArrayList<BundleContact> contacts = gateway.getAllContacts();
            gateway.closeDatabase();
            return contacts;
        }

        @Override
        protected void onPostExecute(ArrayList<BundleContact> result) {
            super.onPostExecute(result);
            progressAll.setVisibility(View.GONE);
            if (result.isEmpty()) {
                txtViewAllEmpty.setVisibility(View.VISIBLE);
            } else {
                adapterAllContacts.swapDataSource(result);
            }
        }
    }

    private class SyncContactsTask extends AsyncTask<Void, Void, ArrayList<BundleContact>>
            implements IncomingGateway.RetContactsListener {

        boolean isCancelled = false;
        ConditionVariable threadBlocker = new ConditionVariable(false);
        private HashMap<String, String> phoneContacts = new HashMap<>();
        private ArrayList<BundleContact> serverContacts = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txtViewAllEmpty.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList<BundleContact> doInBackground(Void... params) {
            IncomingGateway gateway = IncomingGateway.getInstance();
            gateway.setRetContactsListener(this);  // register listener
            sendRetContactsRequest();
            threadBlocker.block();
            if (isCancelled) {
                return null;
            } else {
                DatabaseGateway dbGateway = new DatabaseGateway(getActivity().getApplicationContext());
                dbGateway.insertOrReplaceContacts(serverContacts);
                dbGateway.setContactsIdFromDB(serverContacts);
                dbGateway.closeDatabase();
                return serverContacts;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<BundleContact> result) {
            super.onPostExecute(result);
            if (result != null) {
                txtViewAllEmpty.setVisibility(View.GONE);
                if (result.size() == 0) {
                    txtViewOnlineEmpty.setVisibility(View.VISIBLE);
                    txtViewAllEmpty.setVisibility(View.VISIBLE);
                } else {
                    ArrayList<BundleContact> onlineContacts = new ArrayList<>();
                    for (BundleContact contact : result) {
                        if (contact.isOnline()) {
                            onlineContacts.add(contact);
                        }
                    }
                    if (onlineContacts.size() == 0) {
                        txtViewOnlineEmpty.setVisibility(View.VISIBLE);
                    }
                    adapterAllContacts.swapDataSource(result);
                    adapterOnlineContacts.swapDataSource(onlineContacts);
                }
            }
        }

        private void sendRetContactsRequest() {
            PrefManager pref = new PrefManager(getActivity());
            String accountName = pref.getPhoneNumber();
            String selfPhone = pref.getPhoneNumber();
            phoneContacts = ContactHelper.RetrieveAllContacts(selfPhone, getActivity().getContentResolver(),
                    accountName, AccountHelper.ACCOUNT_TYPE, false);

            ArrayList<String> phoneList = new ArrayList<>(phoneContacts.keySet());  // to send to server
            OutgoingGateway.SendRetrieveContacts(phoneList);
        }

        @Override
        public void onContactsRetrieved(ArrayList<BundleRetContact> contacts) {
            for (BundleRetContact contact : contacts) {
                String name = phoneContacts.get(contact.getPhone());
                serverContacts.add(new BundleContact(name, contact.getPhone(), contact.getPic(), contact.getLastSeen(), contact.isOnline()));
            }
            threadBlocker.open();
        }
    }

}
