package ir.logicbase.mojmessenger.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.contact.AccountHelper;
import ir.logicbase.mojmessenger.activity.fragment.FragmentSmsInput;
import ir.logicbase.mojmessenger.activity.fragment.FragmentSmsSplash;
import ir.logicbase.mojmessenger.activity.fragment.FragmentSmsVerify;
import ir.logicbase.mojmessenger.util.PrefManager;

/**
 * this activity contains 3 fragment :
 * 1- Splash fragment to show overall information about this application
 * 2- Input fragment to get user sim card number and send it to server
 * 3- Verify fragment to verify user passCode with server and complete verification
 */
public class ActivitySmsVerification extends AppCompatActivity implements
        FragmentSmsSplash.onContinueButtonClickListener,
        FragmentSmsInput.OnRequestOTPListener,
        FragmentSmsVerify.OnVerifyOTPListener {

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_verification);
        if (savedInstanceState == null) {
            currentFragment = new FragmentSmsSplash();
            swapFragment(currentFragment);
        }else {
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, "CurrentFragment");
            swapFragment(currentFragment);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "CurrentFragment", currentFragment);
    }

    private void swapFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();  // call fragment manager from parent activity
        ft.replace(R.id.fragment_sms_verification_container, fragment, "CurrentFragment");
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);  // add animation to replacement process
        ft.commit();
    }

    @Override
    public void onContinueButtonClick() {
        currentFragment = new FragmentSmsInput();
        swapFragment(currentFragment);
    }

    @Override
    public void onOTPSent(String phone) {
        Bundle b = new Bundle();
        b.putString(FragmentSmsVerify.ARG_KEY_PHONE, phone);
        FragmentSmsVerify fragment = new FragmentSmsVerify();
        fragment.setArguments(b);
        currentFragment = fragment;
        swapFragment(currentFragment);
    }

    @Override
    public void onWrongNumberTextViewClick() {
        currentFragment = new FragmentSmsInput();
        swapFragment(currentFragment);
    }

    @Override
    public void onTimerExpire() {
        currentFragment = new FragmentSmsInput();
        swapFragment(currentFragment);
    }

    @Override
    public void onRegisterComplete(String phone) {
        // commit user to preference
        PrefManager pref = new PrefManager(getApplicationContext());
        pref.putIsRegistered(true);
        pref.putPhoneNumber(phone);
        // register new account type
        AccountHelper.addAccount(getApplicationContext(), phone, AccountHelper.ACCOUNT_TYPE);
        // redirect to home screen
        Intent intent = new Intent(ActivitySmsVerification.this, ActivityHome.class);
        startActivity(intent);
        finish();
    }
}
