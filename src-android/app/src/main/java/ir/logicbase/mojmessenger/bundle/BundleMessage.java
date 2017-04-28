package ir.logicbase.mojmessenger.bundle;

/**
 * Created by Mahdi on 6/13/2017.
 * Conversation list items
 * used to exchange data between classes
 * cooperate with Adapter and RecyclerView
 */

public class BundleMessage {

    private int id;
    private int serverMsgId;
    private boolean isYours;
    private String timestamp;
    private String msgContent;
    private boolean seen;
    private boolean sync;

    public BundleMessage(boolean isYours, String timestamp, String msg) {
        this(-1, -1, isYours, timestamp, msg, false, false);
    }

    public BundleMessage(int id, int serverMsgId, boolean isYours, String timestamp, String msg, boolean seen, boolean sync) {
        this.id = id;
        this.serverMsgId = serverMsgId;
        this.isYours = isYours;
        this.timestamp = timestamp;
        this.msgContent = msg;
        this.seen = seen;
        this.sync = sync;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isYours() {
        return isYours;
    }

    public int getId() {
        return id;
    }

    public int getServerMsgId() {
        return serverMsgId;
    }

    public boolean isSeen() {
        return seen;
    }

    public boolean isSync() {
        return sync;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public void setServerMsgId(int serverMsgId) {
        this.serverMsgId = serverMsgId;
    }
}
