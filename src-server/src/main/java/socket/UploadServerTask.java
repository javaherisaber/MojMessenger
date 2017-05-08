package socket;

import com.google.gson.JsonObject;
import database.DatabaseGateway;
import entity.ClientEntity;
import org.apache.commons.codec.binary.Base64;
import util.FileManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by Mahdi on 12/10/2017.
 * Serve a client upload request
 */
class UploadServerTask implements Callable<Void> {

    private Socket socket;

    UploadServerTask(Socket socket) {
        this.socket = socket;
    }

    @Override
    public Void call() {

        try {
            InputStream input = socket.getInputStream();

            // read new message from stream
            HashMap<String, String> header = MessageHelper.readHeaderFromStream(input);
            int bytesToRead = Integer.parseInt(header.get(MessageHelper.HEADER_KEY_CONTENT_LENGTH));
            byte[] body = MessageHelper.readBodyFromStream(input, bytesToRead);
            String senderPhone = header.get(MessageHelper.HEADER_KEY_SENDER_PHONE);

            // save the image to file system
            String encodedImageStr = new String(body, Charset.forName("UTF-8"));
            byte[] imageByteArray = Base64.decodeBase64(encodedImageStr);
            String filePath = FileManager.getFileSystemProfilePicsPath();
            String fileName = senderPhone + ".jpg";
            fileName = fileName.replace("+", "");
            FileOutputStream imageOutFile = new FileOutputStream(filePath + "\\" + fileName);
            imageOutFile.write(imageByteArray);
            imageOutFile.close();

            // update user pic in database
            String picHostAddress = FileManager.getHostProfilePicsPath() + "/" + fileName;
            DatabaseGateway.updateUserPic(senderPhone, picHostAddress);

            // inform sender
            ClientTask senderTask = ServerSocketBroker.connectedClients.get(senderPhone).getTask();
            if (senderTask != null) {
                JsonObject responseRoot = new JsonObject();
                responseRoot.addProperty("status", "ok");
                responseRoot.addProperty("url", picHostAddress);
                byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
                HashMap<String, String> responseHeaders = MessageHelper.getCommandHeader(
                        MessageHelper.HEADER_VALUE_METHOD_UPLOAD_PHOTO, responseBody.length);
                byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
                senderTask.addToOutbox(responsePacket);
            }

            // inform sender's friends
            ArrayList<String> senderFriendsPhone = DatabaseGateway.getUserFriendsPhone(senderPhone);
            senderFriendsPhone.stream().filter(ServerSocketBroker.connectedClients::containsKey).forEach(friendPhone -> {
                if (ServerSocketBroker.connectedClients.get(friendPhone).isOnline()) {
                    ClientTask friendTask = ServerSocketBroker.connectedClients.get(friendPhone).getTask();
                    if (friendTask != null) {
                        JsonObject responseRoot = new JsonObject();
                        responseRoot.addProperty("ContactPhone", senderPhone);
                        responseRoot.addProperty("PicUrl", picHostAddress);
                        if (ServerSocketBroker.connectedClients.get(senderPhone).isOnline()) {
                            responseRoot.addProperty("isOnline", true);
                        } else {
                            responseRoot.addProperty("isOnline", false);
                        }
                        byte[] responseBody = responseRoot.toString().getBytes(Charset.forName("UTF-8"));
                        HashMap<String, String> responseHeaders = MessageHelper.getCommandHeader(
                                MessageHelper.HEADER_VALUE_METHOD_CHANGE_PROFILE_PIC, responseBody.length);
                        byte[] responsePacket = MessageHelper.createPacket(responseHeaders, responseBody);
                        friendTask.addToOutbox(responsePacket);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
