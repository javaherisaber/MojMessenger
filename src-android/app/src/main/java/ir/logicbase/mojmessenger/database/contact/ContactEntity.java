package ir.logicbase.mojmessenger.database.contact;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "contact",
        indices = {@Index(value = {"phone"},
                unique = true)})
public class ContactEntity implements Comparable<ContactEntity> {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "phone")
    private String phone;

    @ColumnInfo(name = "pic")
    private String pic;

    @ColumnInfo(name = "last_seen")
    private String lastSeen;

    @Ignore
    private boolean isOnline;

    public ContactEntity(int id, String name, String phone, String pic, String lastSeen) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.pic = pic;
        this.lastSeen = lastSeen;
    }

    /**
     * transfer contact between activities
     */
    @Ignore
    public ContactEntity(int id, String name, String phone, String pic, String lastSeen, boolean isOnline) {
        this(id, name, phone, pic, lastSeen);
        this.isOnline = isOnline;
    }

    /**
     * transfer contact from server to local and then set its id from database
     */
    @Ignore
    public ContactEntity(String name, String phone, String pic, String lastSeen, boolean isOnline) {
        this(0, name, phone, pic, lastSeen, isOnline);
    }

    /**
     * transfer contact from server to local
     */
    @Ignore
    public ContactEntity(String phone, String pic, String lastSeen, boolean isOnline) {
        this(phone, phone, pic, lastSeen, isOnline);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setOnline(boolean value) {
        isOnline = value;
    }

    public boolean isOnline() {
        return isOnline;
    }

    /**
     * Required to make this object equal to another
     *
     * @return hashCode of the specified object
     */
    @Override
    public int hashCode() {
        return phone.hashCode();
    }

    /**
     * Required to make this object equal to another
     *
     * @param obj another object to compare
     * @return True if equal and False otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContactEntity) {
            ContactEntity e = (ContactEntity) obj;
            return phone.equals(e.getPhone());
        } else {
            return super.equals(obj);
        }
    }

    /**
     * Required to make this object comparable
     *
     * @param o other object being compared
     * @return negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object
     */
    @Override
    public int compareTo(@NonNull ContactEntity o) {
        return name.compareTo(o.getName());
    }
}
