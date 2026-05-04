package com.mrdevilex.smssaver;

public class SmsModel {
    private String sender;
    private String body;
    private long date;

    public SmsModel(String sender, String body, long date) {
        this.sender = sender;
        this.body = body;
        this.date = date;
    }

    public String getSender() { return sender; }
    public String getBody() { return body; }
    public long getDate() { return date; }
}
