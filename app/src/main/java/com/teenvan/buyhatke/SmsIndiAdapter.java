package com.teenvan.buyhatke;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by navneet on 03/08/16.
 */

public class SmsIndiAdapter extends RecyclerView.Adapter<SmsIndiAdapter.ViewHolder> {

    private ArrayList<String> messages;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
         TextView mSmsBody;


        public ViewHolder(View v) {
            super(v);
            mSmsBody = (TextView)v.findViewById(R.id.smsBody);
        }
    }

    public SmsIndiAdapter(ArrayList<String> messages, Context context){
        this.messages = messages;
        mContext = context;
    }

    @Override
    public SmsIndiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sms_message, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(SmsIndiAdapter.ViewHolder holder, int position) {
        String body = messages.get(position);
        holder.mSmsBody.setText(body);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
