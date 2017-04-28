package ir.logicbase.mojmessenger.activity.fragment;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.activity.ActivityConversation;
import ir.logicbase.mojmessenger.adapter.AdapterChatList;
import ir.logicbase.mojmessenger.bundle.BundleChat;
import ir.logicbase.mojmessenger.database.DatabaseGateway;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHomeChatList extends Fragment {

    public FragmentHomeChatList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home_chat_list, container, false);
        RecyclerView recViewChatList = (RecyclerView) root.findViewById(R.id.recView_chat_list);
        TextView txtViewEmpty = (TextView) root.findViewById(R.id.txtView_chat_list_empty);

        // change font
        Typeface typeface = TypefaceManager.get(getContext(), getString(R.string.font_iran_sans));
        txtViewEmpty.setTypeface(typeface);

        DatabaseGateway dbGateway = new DatabaseGateway(getContext());
        ArrayList<BundleChat> chatLists = dbGateway.getAllChats();
        dbGateway.closeDatabase();
        if (chatLists.size() == 0) {
            txtViewEmpty.setVisibility(View.VISIBLE);
            // TODO: 12/6/2017 ship this empty view to adapter like what i did with adapterConversationList
        }

        AdapterChatList adapter = new AdapterChatList(chatLists);
        recViewChatList.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recViewChatList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recViewChatList.getContext(),
                layoutManager.getOrientation());
        recViewChatList.addItemDecoration(dividerItemDecoration);  // horizontal divider between cards

        adapter.setListener((contact) -> {
            Intent intent = new Intent(getActivity(), ActivityConversation.class);
            intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_ID, contact.getId());
            intent.putExtra(ActivityConversation.INTENT_KEY_CONVERSATION_TITLE, contact.getName());
            intent.putExtra(ActivityConversation.INTENT_KEY_CONVERSATION_PIC, contact.getPic());
            intent.putExtra(ActivityConversation.INTENT_KEY_USER_LAST_SEEN, contact.getLastSeen());
            intent.putExtra(ActivityConversation.INTENT_KEY_USER_PHONE, contact.getPhone());
            getActivity().startActivity(intent);
        });

        return root;
    }

}
