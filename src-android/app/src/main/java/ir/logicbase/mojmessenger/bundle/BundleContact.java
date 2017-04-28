package ir.logicbase.mojmessenger.bundle;

/**
 * Created by Mahdi on 5/4/2017.
 * Contact list items which needed to cooperate with Adapter and RecyclerView
 */

public class BundleContact extends BundleRetContact{

    private int id;
    private String name;

    public BundleContact(String name, String phone, String pic, String lastSeen, boolean isOnline) {
        super(phone, pic, lastSeen, isOnline);
        this.name = name;
    }

    public BundleContact(int id, String name, String phone, String pic, String lastSeen, boolean isOnline) {
        super(phone, pic, lastSeen, isOnline);
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
