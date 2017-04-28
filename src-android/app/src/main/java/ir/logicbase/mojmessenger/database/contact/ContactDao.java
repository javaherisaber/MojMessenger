package ir.logicbase.mojmessenger.database.contact;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ContactDao {

    @Query("SELECT * FROM contact")
    public List<ContactEntity> getAllContacts();

    @Query("SELECT * FROM contact WHERE _id = :id")
    public ContactEntity getContactById(int id);

    @Query("SELECT _id FROM contact WHERE phone = :phone")
    public int getContactId(String phone);

    @Query("SELECT * FROM contact WHERE phone = :phone")
    public ContactEntity getContactByPhone(String phone);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertContact(ContactEntity contactEntity);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long[] insertContacts(ContactEntity... contactEntities);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long[] insertContacts(List<ContactEntity> contactEntities);

    @Update
    public void updateContacts(ContactEntity... contactEntities);

    @Delete
    public void deleteContacts(ContactEntity... contactEntities);
}
