package ir.logicbase.mojmessenger.chat;


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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.conversation.ActivityConversation;
import ir.logicbase.mojmessenger.database.chat.ChatEntity;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHomeChatList extends Fragment implements ChatView {

    private AdapterChatList adapter;
    private ProgressBar progressBar;
    private ChatPresenter presenter;

    public FragmentHomeChatList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // init views
        View root = inflater.inflate(R.layout.fragment_home_chat_list, container, false);
        RecyclerView recViewChatList = root.findViewById(R.id.recView_chat_list);
        TextView txtViewEmpty = root.findViewById(R.id.txtView_chat_list_empty);
        progressBar = root.findViewById(R.id.progress_chat_list);

        // change font
        Typeface typeface = TypefaceManager.get(getContext(), getString(R.string.font_iran_sans));
        txtViewEmpty.setTypeface(typeface);

        // configure recyclerView
        adapter = new AdapterChatList(txtViewEmpty);
        recViewChatList.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recViewChatList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recViewChatList.getContext(),
                layoutManager.getOrientation());
        recViewChatList.addItemDecoration(dividerItemDecoration);  // horizontal divider between cards

        // set adapter listener
        adapter.setListener(this::goToConversation);

        presenter = new ChatPresenter(getContext(), this);
        presenter.initiateChatList();
        progressBar.setVisibility(View.VISIBLE);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        presenter.startListening(getActivity().getApplicationContext(),this);
        presenter.initiateChatList();
    }

    @Override
    public void onStop() {
        super.onStop();
        progressBar.setVisibility(View.GONE);
        presenter.stopListening();
    }

    private void goToConversation(ContactEntity contact) {
        Intent intent = new Intent(getActivity(), ActivityConversation.class);
        intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_ID, contact.getId());
        intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_NAME, contact.getName());
        intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_PIC, contact.getPic());
        intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_LAST_SEEN, contact.getLastSeen());
        intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_PHONE, contact.getPhone());
        intent.putExtra(ActivityConversation.INTENT_KEY_CONTACT_IS_ONLINE, contact.isOnline());
        getActivity().startActivity(intent);
    }

    @Override
    public void initializeChatList(List<ChatEntity> chats) {
        progressBar.setVisibility(View.GONE);
        adapter.swapDataSource(chats);
    }

    @Override
    public void updateContact(ContactEntity contact) {
        adapter.updateContact(contact);
    }

    @Override
    public void updateChat(ChatEntity chat) {
        adapter.updateChat(chat);
    }
}
