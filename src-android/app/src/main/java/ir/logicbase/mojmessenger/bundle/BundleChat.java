package ir.logicbase.mojmessenger.bundle;

/**
 * Created by Mahdi on 5/3/2017.
 * Chat list items needed to cooperate with Adapter and RecyclerView
 */

public class BundleChat {

    private BundleContact contact;
    private String lastMsg;
    private String time;
    private int count;

    public BundleChat(BundleContact contact, String lastMsg, String time, int count) {
        this.contact = contact;
        this.lastMsg = lastMsg;
        this.time = time;
        this.count = count;
    }

    public BundleContact getContact() {
        return contact;
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
}
