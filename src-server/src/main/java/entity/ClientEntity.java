package entity;

import socket.ClientTask;

/**
 * Created by Mahdi on 12/6/2017.
 *
 */
public class ClientEntity {

    private ClientTask task;
    private boolean isOnline;

    public ClientEntity(ClientTask task, boolean isOnline) {
        this.task = task;
        this.isOnline = isOnline;
    }

    public ClientTask getTask() {
        return task;
    }

    public void setTask(ClientTask task) {
        this.task = task;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
