package ir.logicbase.mojmessenger.conversation;

import java.util.List;

import ir.logicbase.mojmessenger.database.contact.ContactEntity;
import ir.logicbase.mojmessenger.database.message.MessageEntity;

/**
 * Created by Mahdi on 12/9/2017.
 * Specify the contract between the view and the presenter
 */

interface ConversationView {

    void initiateConversationList(int contactId, List<MessageEntity> messages);

    void insertNewMessage(MessageEntity message);

    void updateContact(ContactEntity contact);

    void deliverMessage(MessageEntity message);

    void seenAck(MessageEntity message);

    void textMessageAck(MessageEntity message);
}
