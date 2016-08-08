package com.teenvan.buyhatke;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by navneet on 08/08/16.
 */

public class SmsIndiFragment extends Fragment {
    // Declaration of member variables
    private RecyclerView mSmsList;
    private ArrayList<String> mMessages;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sms,container,false);



        String address = getArguments().getString("Address");
        if(address != null)
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(address);

        mSmsList = (RecyclerView)rootView.findViewById(R.id.smsList);
        mSmsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mSmsList.setHasFixedSize(true);

        SmsIndiAdapter adapter = new SmsIndiAdapter(getMessageData(address), getContext());
        mSmsList.setAdapter(adapter);


        return rootView;
    }



    private ArrayList<String> getMessageData(String address){
        if(ContextCompat.checkSelfPermission(getContext(), "android.permission.READ_SMS") ==
                PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = getActivity().
                    getContentResolver().query(Uri.parse("content://sms/inbox"),
                    null, "address='"+address+"'", null, null);
            ArrayList<String> mMessages = new ArrayList<>();

            assert cursor != null;

            int indexBody = cursor.getColumnIndex("body");

            if (cursor.moveToFirst()) { // must check the result to prevent exception
                do {
                    mMessages.add(cursor.getString(indexBody));
                } while (cursor.moveToNext());
                cursor.close();
            } else {
                // empty box, no SMS
                cursor.close();
            }

            return mMessages;
        }else{
            final int REQUEST_CODE_ASK_PERMISSIONS = 123;
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
            ArrayList<String> mMessages;
            mMessages = getMessageData(address);
            return mMessages;
        }
    }
}
