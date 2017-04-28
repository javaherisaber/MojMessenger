package ir.logicbase.mojmessenger.chat;

import java.util.List;

import ir.logicbase.mojmessenger.database.chat.ChatEntity;
import ir.logicbase.mojmessenger.database.contact.ContactEntity;

/**
 * Created by Mahdi on 12/9/2017.
 * Specify the contract between the view and the presenter
 */

interface ChatView {

    void initializeChatList(List<ChatEntity> chats);

    void updateContact(ContactEntity contact);

    void updateChat(ChatEntity chat);
}
