package ir.logicbase.mojmessenger.activity.fragment;


import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.socket.IncomingGateway;
import ir.logicbase.mojmessenger.socket.OutgoingGateway;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * A simple {@link Fragment} subclass.
 * use OTP code to verify user authentication
 * or let user to change h/er/is number and return to input fragment
 */
public class FragmentSmsVerify extends Fragment implements View.OnClickListener {

    private OnVerifyOTPListener listener;
    private EditText edTextPassCode;
    private TextView txtViewTimer;
    private int timerSecondsLeft = 120;
    private String phone;

    public static final String ARG_KEY_PHONE = "phone";

    public interface OnVerifyOTPListener {
        void onRegisterComplete(String phone);

        void onWrongNumberTextViewClick();

        void onTimerExpire();
    }

    public FragmentSmsVerify() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.listener = (OnVerifyOTPListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.phone = getArguments().getString(ARG_KEY_PHONE);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sms_verify, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            runTimer();

            TextView txtViewWrongNum = (TextView) view.findViewById(R.id.txtView_verify_wrong_num);
            AppCompatButton btnVerify = (AppCompatButton) view.findViewById(R.id.btn_verify);
            TextView txtViewMsg1 = (TextView) view.findViewById(R.id.txtView_verify_msg1);
            TextView txtViewMsg2 = (TextView) view.findViewById(R.id.txtView_verify_msg2);
            edTextPassCode = (EditText) view.findViewById(R.id.edText_verify_pass_code);
            TextView txtViewTimerMsg = (TextView) view.findViewById(R.id.txtView_verify_timer_msg);
            txtViewTimer = (TextView) view.findViewById(R.id.txtView_verify_timer);

            // register listeners
            btnVerify.setOnClickListener(this);
            txtViewWrongNum.setOnClickListener(this);

            // change fonts
            Typeface typeface = TypefaceManager.get(getActivity(), getString(R.string.font_iran_sans));
            txtViewMsg1.setTypeface(typeface);
            txtViewMsg2.setTypeface(typeface);
            txtViewTimerMsg.setTypeface(typeface);
            txtViewTimer.setTypeface(typeface);
            txtViewWrongNum.setTypeface(typeface);
            btnVerify.setTypeface(typeface);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_verify:
                String input = edTextPassCode.getText().toString();
                if (isValidInput(input)) {
                    new VerifyOTPTask().execute(input);
                } else {
                    Toast.makeText(getContext(), "کد وارد شده اشتباه است", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.txtView_verify_wrong_num:
                timerSecondsLeft = -1;
                listener.onWrongNumberTextViewClick();
                break;
        }
    }

    private class VerifyOTPTask extends AsyncTask<String, Integer, Integer>
            implements IncomingGateway.VerifyOTPListener,
            IncomingGateway.RegisterNewUserListener{

        private ConditionVariable threadBlocker = new ConditionVariable(false);
        private FrameLayout progress;
        private String passCode;
        private int verificationResult;

        public VerifyOTPTask() {
            IncomingGateway gateway = IncomingGateway.getInstance();
            gateway.setVerifyOTPListener(this);
            gateway.setRegisterNewUserListener(this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = (FrameLayout) getActivity().findViewById(R.id.frameCircularProgress);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            this.passCode = params[0];
            OutgoingGateway.SendOTPVerify(passCode);
            threadBlocker.block();
            if (verificationResult == 0) {
                OutgoingGateway.SendRegisterNewUser(phone);
                threadBlocker.block();
            }
            return verificationResult;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progress.setVisibility(View.GONE);
            switch (result) {
                case -1 :
                    Toast.makeText(getContext(), "کد وارد شده اشتباه است", Toast.LENGTH_SHORT).show();
                    break;
                case -2 :
                    Toast.makeText(getContext(), "خطایی رخ داد ، لطفا بعدا امتحان کنید", Toast.LENGTH_SHORT).show();
                    break;
                case 0 :
                    timerSecondsLeft = -1;
                    Toast.makeText(getContext(), "ثبت نام شما موفقیت آمیز بود", Toast.LENGTH_LONG).show();
                    listener.onRegisterComplete(phone);
                    break;
            }
        }

        @Override
        public void onOTPVerified(boolean isCorrectOTP) {
            verificationResult = isCorrectOTP ? 0 : -1;
            threadBlocker.open();
        }

        @Override
        public void onUserRegistered(boolean isSuccessful) {
            verificationResult = isSuccessful ? 0 : -2;
            threadBlocker.open();
        }
    }

    private boolean isValidInput(String input) {
        String regex = "^[\\d+]{1,6}$";
        return input.matches(regex);
    }

    private void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (timerSecondsLeft == -1){
                  return;
                } else if (timerSecondsLeft == 0) {
                    Toast.makeText(getContext(), "کد فعال سازی منقضی شد! دوباره امتحان کنید", Toast.LENGTH_LONG).show();
                    listener.onTimerExpire();
                } else {
                    timerSecondsLeft -= 1;
                    txtViewTimer.setText(timerSecondsLeft + " ثانیه ");
                    handler.postDelayed(this, 1000);  // execute run() in the future
                }
            }
        });
    }

}
