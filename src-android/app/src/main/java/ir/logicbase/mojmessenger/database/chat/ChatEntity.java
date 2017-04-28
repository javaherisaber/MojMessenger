package ir.logicbase.mojmessenger.database.chat;

import ir.logicbase.mojmessenger.database.contact.ContactEntity;

public class ChatEntity {

    private ContactEntity contactEntity;
    private String lastMsg;
    private String time;
    private int count;

    public ChatEntity(ContactEntity contactEntity, String lastMsg, String time, int count) {
        this.contactEntity = contactEntity;
        this.lastMsg = lastMsg;
        this.time = time;
        this.count = count;
    }

    public ContactEntity getContactEntity() {
        return contactEntity;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public String getTime() {
        return time;
    }

    public int getCount() {
        return count;
    }

    public void setContactEntity(ContactEntity contactEntity) {
        this.contactEntity = contactEntity;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
