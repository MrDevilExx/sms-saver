package com.mrdevilex.smssaver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.ViewHolder> {

    private List<SmsModel> list;

    public SmsAdapter(List<SmsModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_sms, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SmsModel sms = list.get(position);
        holder.sender.setText(sms.getSender());
        holder.body.setText(sms.getBody());
        String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a",
            Locale.getDefault()).format(new Date(sms.getDate()));
        holder.date.setText(date);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sender, body, date;

        ViewHolder(View v) {
            super(v);
            sender = v.findViewById(R.id.tvSender);
            body = v.findViewById(R.id.tvBody);
            date = v.findViewById(R.id.tvDate);
        }
    }
}
