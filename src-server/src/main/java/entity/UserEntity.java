package entity;

/**
 * Created by Mahdi on 9/2/2017.
 */
public class UserEntity {

    private int id;
    private String phone;
    private String pic;
    private String lastSeen;

    public UserEntity(int id, String phone, String pic, String lastSeen) {
        this.id = id;
        this.phone = phone;
        this.pic = pic;
        this.lastSeen = lastSeen;
    }

    public int getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getPic() {
        return pic;
    }

    public String getLastSeen() {
        return lastSeen;
    }
}
