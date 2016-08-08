package com.teenvan.buyhatke;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by navneet on 03/08/16.
 */

public class SmsInboxFragment extends Fragment {
    // Declaration of member variables
    private RecyclerView mSmsList;
    private HashMap<String, String> mSmsMessages;
    private RecyclerView.LayoutManager manager;
    private static final String TAG_FRAGMENT = "SmsIndi";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sms,container,false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("BuyHatke");

        MainActivity a = (MainActivity)getActivity();
        a.setButtonEnabled(true);
        mSmsList = (RecyclerView)rootView.findViewById(R.id.smsList);

        mSmsMessages = new HashMap<>();
        mSmsMessages = getMessageData();

        manager = new LinearLayoutManager(getContext());
        mSmsList.setLayoutManager(manager);
        mSmsList.setHasFixedSize(true);

        SmsAdapter adapter = new SmsAdapter(mSmsMessages, getContext());
        mSmsList.setAdapter(adapter);

        mSmsList.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new
                        RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Bundle bundle = new Bundle();
                        bundle.putString("Address",
                                (new ArrayList<>(mSmsMessages.keySet())).get(position));
                        SmsIndiFragment fragment = new SmsIndiFragment();
                        fragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, fragment,TAG_FRAGMENT)
                                .addToBackStack(null)
                                .commit();

                    }
                })
        );





        return rootView;
    }


    private HashMap<String, String> getMessageData(){
        if(ContextCompat.checkSelfPermission(getContext(), "android.permission.READ_SMS") ==
                PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = getActivity().
                    getContentResolver().query(Uri.parse("content://sms/inbox"),
                    null, null, null, null);
            HashMap<String, String> mMessages = new HashMap<>();

            assert cursor != null;


            int indexAddress = cursor.getColumnIndex("address");
            int indexBody = cursor.getColumnIndex("body");

            if (cursor.moveToFirst()) { // must check the result to prevent exception
                do {
                    mMessages.put(cursor.getString(indexAddress), cursor.getString(indexBody));
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
            HashMap<String, String> mMessages;
            mMessages = getMessageData();
            return mMessages;
        }
    }


    public HashMap<String,String> getMessages(){
        return mSmsMessages;
    }



}
