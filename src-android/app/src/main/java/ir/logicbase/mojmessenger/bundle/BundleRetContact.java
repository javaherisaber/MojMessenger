package ir.logicbase.mojmessenger.bundle;

/**
 * Created by Mahdi on 9/2/2017.
 */

public class BundleRetContact {

    private String phone;
    private String pic;
    private String lastSeen;
    private boolean isOnline;

    public BundleRetContact(String phone, String pic, String lastSeen, boolean isOnline) {
        this.phone = phone;
        this.pic = pic;
        this.lastSeen = lastSeen;
        this.isOnline = isOnline;
    }

    public String getPic() {
        return pic;
    }

    public String getPhone() {
        return phone;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public boolean isOnline() {
        return isOnline;
    }
}
