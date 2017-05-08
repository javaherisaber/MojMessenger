package otp;

import otp.provider.TimeBasedOtpGenerator;

import javax.crypto.KeyGenerator;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mahdi on 7/4/2017.
 * Generate's 6 digits otp password and save's it into our data structure
 * when timestamp expires password will be deleted and no longer can be used
 */
public class OTP {

    private static final HashMap<Integer, Long> passwords = new HashMap<>();
    private static final int COUNTER_OFFSET_SECOND = 2;
    private static final int COUNTER_SECOND = 120;
    private static TimeBasedOtpGenerator tOTP;
    private static Key secretKey;

    static {
        try {
            tOTP = new TimeBasedOtpGenerator(COUNTER_SECOND, TimeUnit.SECONDS);
            // Generate secret key
            {
                final KeyGenerator keyGenerator = KeyGenerator.getInstance(tOTP.getAlgorithm());

                // SHA-1 and SHA-256 prefer 64-byte (512-bit) keys; SHA512 prefers 128-byte keys
                keyGenerator.init(512);

                secretKey = keyGenerator.generateKey();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 6 digits password and 0 if exception occur's
     */
    public static int generateOTP(){
        final Date now = new Date();
        try {
            int oneTimePassword = tOTP.generateOneTimePassword(secretKey, now);
            passwords.put(oneTimePassword, now.getTime());
            return oneTimePassword;
        } catch (InvalidKeyException e) {
            return 0;
        }
    }

    /**
     * @param password previously generated
     * @return True if valid otp, or False on the contrary
     */
    public static boolean isValidOTP(int password){
        Date now = new Date();
        long currentMilli = now.getTime();
        long counterTimeMilli = TimeUnit.SECONDS.toMillis(COUNTER_SECOND + COUNTER_OFFSET_SECOND);
        if(!passwords.isEmpty()){
            for(HashMap.Entry<Integer, Long> entry : passwords.entrySet()){
                if( ((currentMilli - entry.getValue()) <= counterTimeMilli ) && entry.getKey() == password )
                    return true;
                else if((currentMilli - entry.getValue()) > counterTimeMilli){
                    passwords.remove(entry.getKey(), entry.getValue());  // removing expired password
                }
            }
        }
        return false;
    }

}
