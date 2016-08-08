package com.teenvan.buyhatke;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by navneet on 03/08/16.
 */

public class SendSmsFragment extends Fragment {

    private EditText mPhoneNo, mMessage;
    private Button mSendButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sms_send,container,false);

        MainActivity a = (MainActivity)getActivity();
        a.setButtonEnabled(false);

        mPhoneNo = (EditText)view.findViewById(R.id.phoneNumber);
        mMessage = (EditText)view.findViewById(R.id.messageEditText);
        mSendButton = (Button)view.findViewById(R.id.sendSms);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = mPhoneNo.getText().toString().trim();
                String sms = mMessage.getText().toString();

                if(ContextCompat.checkSelfPermission(getContext(), "android.permission.SEND_SMS") ==
                        PackageManager.PERMISSION_GRANTED){

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phone, null, sms, null, null);
                }else{
                    final int REQUEST_CODE_ASK_PERMISSIONS = 123;
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{"android.permission.SEND_SMS"},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }

            }
        });

        return view;
    }
}
