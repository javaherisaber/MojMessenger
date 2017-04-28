package ir.logicbase.mojmessenger.database.message;

import java.util.List;

/**
 * Created by Mahdi on 12/15/2017.
 * When someone need serverMsg ids
 */

public interface GetServerMsgIdsCallback {

    void onServerMsgIdsLoaded(List<Integer> serverMsgIds);
}
