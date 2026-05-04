package com.mrdevilex.smssaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;

            Object[] pdus = (Object[]) bundle.get("pdus");
            String format = bundle.getString("format");
            if (pdus == null) return;

            DatabaseHelper db = new DatabaseHelper(context);

            for (Object pdu : pdus) {
                SmsMessage smsMessage;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
                } else {
                    smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                }
                if (smsMessage != null) {
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    String body = smsMessage.getMessageBody();
                    long date = smsMessage.getTimestampMillis();
                    db.insertSms(new SmsModel(sender, body, date));
                }
            }
        }
    }
}
