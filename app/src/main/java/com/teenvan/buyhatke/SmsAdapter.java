package com.teenvan.buyhatke;

import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by navneet on 03/08/16.
 */

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.ViewHolder> {

    private HashMap<String, String> messages;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
         TextView mSmsSender;
         TextView mSmsBody;


        public ViewHolder(View v) {
            super(v);
            mSmsSender = (TextView)v.findViewById(R.id.smsSender);
            mSmsBody = (TextView)v.findViewById(R.id.smsBody);
        }
    }

    public SmsAdapter(HashMap<String, String> messages, Context context){
        this.messages = messages;
        mContext = context;
    }

    @Override
    public SmsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sms_message, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(SmsAdapter.ViewHolder holder, int position) {
        String sender = (new ArrayList<>(messages.keySet())).get(position);
        String body = (new ArrayList<>(messages.values())).get(position);
        holder.mSmsSender.setText(sender);
        holder.mSmsBody.setText(body);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
