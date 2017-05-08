package database;

import entity.MessageEntity;
import entity.UserEntity;
import util.FileManager;
import util.Time;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahdi on 8/29/2017.
 * connect to database and execute query
 */
public class DatabaseGateway {

    /**
     * @return id of created user ,-2 if user exist, -1 if error occurs
     */
    public static int createNewUser(String phone) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            Statement stmt = conn.createStatement();
            String sqlCheckUser = DatabaseContract.getSQLCountUserByPhone(phone);
            ResultSet resultSet = stmt.executeQuery(sqlCheckUser);
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                resultSet.close();
                if (count != 0) {
                    return -2;
                } else {
                    String timestamp = Time.getUTCTimestamp();
                    String defaultPic = FileManager.getHostDefaultProfilePic();
                    String sql = DatabaseContract.getSqlInsertNewUser(phone, defaultPic, timestamp);
                    stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next())
                        return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            return -1;
        }
        return -1;
    }

    /**
     * @return corresponding user id, or -1 if error occurred
     */
    public static int getUserIDByPhone(String phone) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            Statement stmt = conn.createStatement();
            String sql = DatabaseContract.getSqlSelectUserIdByPhone(phone);
            ResultSet result = stmt.executeQuery(sql);
            result.first();
            return result.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }

    public static void insertContacts(int fkUserId, ArrayList<Integer> contactUserIdList) {
        for (int contactUserId : contactUserIdList) {
            try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
                Statement stmt = conn.createStatement();
                String sql = DatabaseContract.getSqlInsertIntoContact(fkUserId, contactUserId);
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                // do nothing
            }
        }
    }

    public static ArrayList<UserEntity> getUsersByNumbers(String[] numbers) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            Statement stmt = conn.createStatement();
            ArrayList<UserEntity> users = new ArrayList<>();
            for (String phone : numbers) {
                String sql = DatabaseContract.getSqlSelectUserByPhone(phone);
                ResultSet result = stmt.executeQuery(sql);
                if (result.next()) {
                    // user available
                    users.add(new UserEntity(result.getInt(BaseColumns.ID),
                            result.getString(DatabaseContract.User.COLUMN_PHONE),
                            result.getString(DatabaseContract.User.COLUMN_PIC),
                            result.getString(DatabaseContract.User.COLUMN_LAST_SEEN)));
                }
            }
            return users;
        } catch (SQLException e) {
            return null;
        }
    }

    public static UserEntity getUserById(int userId) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            Statement stmt = conn.createStatement();
            String sql = DatabaseContract.getSqlSelectUserById(userId);
            ResultSet result = stmt.executeQuery(sql);
            if (result.next()) {
                int id = result.getInt(BaseColumns.ID);
                String phone = result.getString(DatabaseContract.User.COLUMN_PHONE);
                String pic = result.getString(DatabaseContract.User.COLUMN_PIC);
                String lastSeen = result.getString(DatabaseContract.User.COLUMN_LAST_SEEN);
                return new UserEntity(id, phone, pic, lastSeen);
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    public static UserEntity getUserByPhone(String phone) {
        int userId = getUserIDByPhone(phone);
        return getUserById(userId);
    }

    public static void updateUserLastSeen(String phone) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            Statement stmt = conn.createStatement();
            String timestamp = Time.getUTCTimestamp();
            String sql = DatabaseContract.getSqlUpdateUserLastSeen(timestamp, phone);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // do nothing
            System.out.print(e.getMessage());
        }
    }

    public static void updateUserPic(String phone, String pic) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            Statement stmt = conn.createStatement();
            String sql = DatabaseContract.getSqlUpdateUserPic(phone, pic);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // do nothing
            System.out.print(e.getMessage());
        }
    }

    public static ArrayList<String> getUserFriendsPhone(String phone) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            int userId = getUserIDByPhone(phone);
            Statement stmt = conn.createStatement();
            String sql = DatabaseContract.getSqlSelectContact(userId);
            ResultSet resultSet = stmt.executeQuery(sql);
            ArrayList<String> phoneList = new ArrayList<>();
            while (resultSet.next()) {
                int contactUserId = resultSet.getInt(DatabaseContract.Contact.COLUMN_CONTACT_USER_ID);
                UserEntity user = getUserById(contactUserId);
                if (user != null) {
                    phoneList.add(user.getPhone());
                }
            }
            return phoneList;
        } catch (SQLException e) {
            // do nothing
            return new ArrayList<>();
        }
    }

    public static int insertNewMessage(int senderUserId, int recipientUserId, String timestamp, String content) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            Statement stmt = conn.createStatement();
            String sql = DatabaseContract.getSqlInsertIntoMessage(senderUserId, recipientUserId, timestamp, content, 0, 0, 0);
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
        return -1;
    }

    public static void updateMessageSeen(int id, int seen) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            Statement stmt = conn.createStatement();
            String sql = DatabaseContract.getSqlUpdateMessageSeen(id, seen);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // do nothing
        }
    }

    public static void updateMessageSenderSync(int id, int senderSync) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            Statement stmt = conn.createStatement();
            String sql = DatabaseContract.getSqlUpdateMessageSenderSync(id, senderSync);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // do nothing
        }
    }

    public static void updateMessageRecipientSync(int id, int recipientSync) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            Statement stmt = conn.createStatement();
            String sql = DatabaseContract.getSqlUpdateMessageRecipientSync(id, recipientSync);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // do nothing
        }
    }

    public static List<Integer> getNotDeliveredSeenMessages(String phone) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            int userId = getUserIDByPhone(phone);
            Statement stmt = conn.createStatement();
            String sql = DatabaseContract.getSqlSelectNotDeliveredSeenMessages(userId);
            ResultSet resultSet = stmt.executeQuery(sql);
            ArrayList<Integer> serverMsgIds = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt(DatabaseContract.Message.ID);
                serverMsgIds.add(id);
            }
            return serverMsgIds;
        } catch (SQLException e) {
            // do nothing
            return new ArrayList<>();
        }
    }

    public static List<MessageEntity> getNotDeliveredTextMessages(String phone) {
        try (Connection conn = DatabaseContract.getDataSource().getConnection()) {
            int userId = getUserIDByPhone(phone);
            Statement stmt = conn.createStatement();
            String sql = DatabaseContract.getSqlSelectNotDeliveredTextMessage(userId);
            ResultSet resultSet = stmt.executeQuery(sql);
            List<MessageEntity> messages = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt(DatabaseContract.Message.ID);
                String content = resultSet.getString(DatabaseContract.Message.COLUMN_CONTENT);
                String timestamp = resultSet.getString(DatabaseContract.Message.COLUMN_TIME_STAMP);
                int senderUserId = resultSet.getInt(DatabaseContract.Message.COLUMN_SENDER_USER_ID);
                messages.add(new MessageEntity(id, content, timestamp, senderUserId));
            }
            return messages;
        } catch (SQLException e) {
            // do nothing
            return new ArrayList<>();
        }
    }

}
