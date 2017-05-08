package entity;

/**
 * Created by Mahdi on 12/15/2017.
 *
 */
public class MessageEntity {

    private int serverMsgId;
    private String content;
    private String timestamp;
    private int senderUserId;

    public MessageEntity(int serverMsgId, String content, String timestamp, int senderUserId) {
        this.serverMsgId = serverMsgId;
        this.content = content;
        this.timestamp = timestamp;
        this.senderUserId = senderUserId;
    }

    public int getServerMsgId() {
        return serverMsgId;
    }

    public void setServerMsgId(int serverMsgId) {
        this.serverMsgId = serverMsgId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(int senderUserId) {
        this.senderUserId = senderUserId;
    }
}
