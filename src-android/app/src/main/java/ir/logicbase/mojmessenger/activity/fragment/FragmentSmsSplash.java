package ir.logicbase.mojmessenger.activity.fragment;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ir.logicbase.mojmessenger.R;
import ir.logicbase.mojmessenger.util.TypefaceManager;

/**
 * A simple {@link Fragment} subclass.
 * Display some information about your application
 */
public class FragmentSmsSplash extends Fragment implements View.OnClickListener{

    public interface onContinueButtonClickListener{
        void onContinueButtonClick();
    }

    private onContinueButtonClickListener listener;

    public FragmentSmsSplash() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.listener = (onContinueButtonClickListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sms_splash, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if(view != null){
            TextView txtSplashWelcome = (TextView) view.findViewById(R.id.txtView_splash_welcome);
            TextView txtSplashMsg1 = (TextView) view.findViewById(R.id.txtView_splash_msg1);
            TextView txtSplashMsg2 = (TextView) view.findViewById(R.id.txtView_splash_msg2);
            TextView txtSplashMsg3 = (TextView) view.findViewById(R.id.txtView_splash_msg3);
            AppCompatButton btnContinue = (AppCompatButton) view.findViewById(R.id.btn_splash_continue);

            btnContinue.setOnClickListener(this);

            Typeface typeface = TypefaceManager.get(getActivity(), getString(R.string.font_iran_sans));
            btnContinue.setTypeface(typeface);
            txtSplashWelcome.setTypeface(typeface);
            txtSplashMsg1.setTypeface(typeface);
            txtSplashMsg2.setTypeface(typeface);
            txtSplashMsg3.setTypeface(typeface);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_splash_continue:
                listener.onContinueButtonClick();
                break;
        }
    }

}
