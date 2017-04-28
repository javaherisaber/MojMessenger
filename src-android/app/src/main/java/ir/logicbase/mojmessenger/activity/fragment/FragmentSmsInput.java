package ir.logicbase.mojmessenger.activity.fragment;


import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.socket.ConnectionHandler;
import ir.logicbase.mojmessenger.socket.IncomingGateway;
import ir.logicbase.mojmessenger.socket.OutgoingGateway;
import ir.logicbase.mojmessenger.util.FileManager;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * A simple {@link Fragment} subclass.
 * exploit user sim card number to generate an OTP passCode (one-time password)
 */
public class FragmentSmsInput extends Fragment implements View.OnClickListener {


    private EditText edTextInputNumber;
    private AppCompatSpinner spnInputCountry;

    public interface OnRequestOTPListener {
        void onOTPSent(String phone);
    }

    private OnRequestOTPListener listener;

    public FragmentSmsInput() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.listener = (OnRequestOTPListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sms_input, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            AppCompatButton btnInputReceive = (AppCompatButton) view.findViewById(R.id.btn_input_receive);
            spnInputCountry = (AppCompatSpinner) view.findViewById(R.id.spn_input_country);
            edTextInputNumber = (EditText) view.findViewById(R.id.edText_input_number);
            TextView txtViewInputMsg = (TextView) view.findViewById(R.id.txtView_input_msg);

            btnInputReceive.setOnClickListener(this);
            setSpinnerAdapter(spnInputCountry);

            Typeface typeface = TypefaceManager.get(getActivity(), getString(R.string.font_iran_sans));
            btnInputReceive.setTypeface(typeface);
//            edTextInputNumber.setTypeface(TypefaceManager.get(getActivity(), getString(R.string.font_iran_sans)));
            txtViewInputMsg.setTypeface(typeface);
        }
    }

    private void setSpinnerAdapter(Spinner spinner) {
        FileManager fileManager = new FileManager();
        ArrayList<String> countries = fileManager.readTextFileFromAssets(getActivity(), "countries.txt");
        ArrayList<String> output = new ArrayList<>();

        // Split country codes
        for (String element :
                countries) {
            String[] fields = element.split(";");
            element = "+" + fields[0] + "-" + fields[1];
            output.add(element);
        }
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, output);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        // Change default selection to iran
        int spinnerPosition = dataAdapter.getPosition("+98-IR");
        spinner.setSelection(spinnerPosition);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_input_receive:
                String phone = edTextInputNumber.getText().toString();
                String[] temp = spnInputCountry.getSelectedItem().toString().split("-");
                String countryCode = temp[0].replace("+", "");
                if (isValidPhoneNumber(countryCode, phone)) {
                    String phoneNumber = createPhoneNumber(countryCode, phone);
                    new RequestOTPTask().execute(phoneNumber);
                } else {
                    Toast.makeText(getContext(), "شماره وارد شده صحیح نیست", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private class RequestOTPTask extends AsyncTask<String, Boolean, Boolean>
            implements IncomingGateway.SendOTPListener,
            ConnectionHandler.ConnectionListener {

        private ConditionVariable threadBlocker = new ConditionVariable(false);
        private FrameLayout progress;
        private String phoneNumber;
        private boolean smsResult;

        public RequestOTPTask() {
            IncomingGateway demultiplexer = IncomingGateway.getInstance();
            demultiplexer.setSendOTPListener(this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = (FrameLayout) getActivity().findViewById(R.id.frameCircularProgress);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            this.phoneNumber = params[0];
            ConnectionHandler connection = ConnectionHandler.getInstance();
            connection.setListener(this);
            if (!connection.isUserOnline()) {
                connection.startConnection();
            } else {
                OutgoingGateway.SendRequestOTP(this.phoneNumber);
            }
            threadBlocker.block();
            return smsResult;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progress.setVisibility(View.GONE);
            if (result) {
                listener.onOTPSent(phoneNumber);
            } else {
                Toast.makeText(getContext(), "خطایی در ارسال پیامک رخ داد ، لطفا بعدا امتحان کنید", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onOTPSent(boolean isSuccessful) {
            smsResult = isSuccessful;
            threadBlocker.open();
        }

        @Override
        public void waitingForNetwork() {

        }

        @Override
        public void connecting() {

        }

        @Override
        public void onUserGoesOnline() {
            OutgoingGateway.SendRequestOTP(this.phoneNumber);
        }

        @Override
        public void disconnected() {

        }
    }

    private boolean isValidPhoneNumber(String countryCode, String phone) {
        FileManager fileManager = new FileManager();
        ArrayList<String> countries = fileManager.readTextFileFromAssets(getActivity(), "countries.txt");
        for (String element :
                countries) {
            String[] fields = element.split(";");
            if (fields[0].equals(countryCode)) {
                String digitTemplate = fields[3];
                int digitCount = digitTemplate.split("X", -1).length - 1;
                char[] charPhone = phone.toCharArray();
                String regex = "^[0-9]+$";
                if (charPhone.length == digitCount) {
                    if (phone.matches(regex))
                        return true;
                } else if (charPhone.length == digitCount + 1) {
                    if (phone.matches(regex) && charPhone[0] == '0')
                        return true;
                }
                break;
            }
        }
        return false;
    }

    private String createPhoneNumber(String countryCode, String phone) {
        char[] phoneArray = phone.toCharArray();
        String phoneNumber = "+" + countryCode;
        if (phoneArray[0] == '0') {
            phoneNumber += new String(phoneArray, 1, phoneArray.length - 1);
        } else {
            phoneNumber += phone;
        }
        return phoneNumber;
    }
}
