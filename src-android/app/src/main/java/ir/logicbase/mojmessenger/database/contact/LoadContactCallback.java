package ir.logicbase.mojmessenger.database.contact;

import android.support.annotation.MainThread;

import java.util.List;

/**
 * Callback called when the ContactEntity was loaded from the repository.
 */
public interface LoadContactCallback {
    /**
     * contacts loaded from database
     */
    @MainThread
    void onAllContactsInitiated(List<ContactEntity> contacts);

    /**
     * there was no ContactEntity in the repository.
     */
    @MainThread
    void onDataNotAvailable();
}
