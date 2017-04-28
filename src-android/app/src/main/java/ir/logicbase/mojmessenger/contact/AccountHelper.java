package ir.logicbase.mojmessenger.contact;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * Created by Mahdi on 8/3/2017.
 * User Account custom type in Settings -> Accounts
 */

public class AccountHelper {

    public static final String ACCOUNT_TYPE = "ir.logicbase.mojmessenger";

    /**
     * @param context  which place to use
     * @param name     will be displayed as account name
     * @param type     will be your package name
     * @return True if account added completely False if account already exist
     */
    public static boolean addAccount(Context context, String name, String type) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = new Account(name, type);
        return accountManager.addAccountExplicitly(account, null, null);
    }
}
