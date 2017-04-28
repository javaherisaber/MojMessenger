package ir.logicbase.mojmessenger.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ir.logicbase.mojmessenger.chat.FragmentHomeChatList;
import ir.logicbase.mojmessenger.contact.FragmentHomeContactList;

class AdapterHomePager extends FragmentPagerAdapter {

    private static final int pagesNumber = 2;

    AdapterHomePager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return pagesNumber;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentHomeChatList();
            case 1:
                return new FragmentHomeContactList();
            default:
                return null;
        }
    }

}