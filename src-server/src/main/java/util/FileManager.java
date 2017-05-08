package util;

/**
 * Created by Mahdi on 9/1/2017.
 */
public class FileManager {

    private static final String FILE_SYSTEM_ROOT_PATH = "C:\\domains\\logicbase.ir\\MojMessenger";
    private static final String HOST_ROOT_PATH = "http://logicbase.ir/MojMessenger";
    private static final String PROFILE_PICS_PATH = "profile_pics";
    private static final String DEFAULT_PERSON_PIC = "default_person_pic.png";

    public static String getHostDefaultProfilePic() {
        return HOST_ROOT_PATH + "/" + PROFILE_PICS_PATH + "/" + DEFAULT_PERSON_PIC;
    }

    public static String getHostProfilePicsPath() {
        return HOST_ROOT_PATH + "/" + PROFILE_PICS_PATH;
    }

    public static String getFileSystemProfilePicsPath() {
        return FILE_SYSTEM_ROOT_PATH + "\\" + PROFILE_PICS_PATH;
    }
}
