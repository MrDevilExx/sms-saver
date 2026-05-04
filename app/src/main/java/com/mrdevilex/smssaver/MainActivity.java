package com.mrdevilex.smssaver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 100;
    private RecyclerView recyclerView;
    private SmsAdapter adapter;
    private List<SmsModel> smsList = new ArrayList<>();
    private List<SmsModel> filteredList = new ArrayList<>();
    private DatabaseHelper db;
    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        searchBox = findViewById(R.id.searchBox);
        FloatingActionButton fabExport = findViewById(R.id.fabExport);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SmsAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        checkPermissions();

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSms(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabExport.setOnClickListener(v -> exportToCsv());
    }

    private void checkPermissions() {
        String[] perms = {
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CONTACTS
        };
        List<String> needed = new ArrayList<>();
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                needed.add(p);
            }
        }
        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), PERMISSION_REQUEST);
        } else {
            loadSms();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            loadSms();
        }
    }

    private void loadSms() {
        // Load from device inbox
        readDeviceSms();
        // Load from local DB (received by BroadcastReceiver)
        List<SmsModel> dbSms = db.getAllSms();
        for (SmsModel s : dbSms) {
            if (!containsSms(smsList, s)) {
                smsList.add(s);
            }
        }
        filteredList.clear();
        filteredList.addAll(smsList);
        adapter.notifyDataSetChanged();
    }

    private void readDeviceSms() {
        try {
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor cursor = getContentResolver().query(uri,
                new String[]{"address", "body", "date"}, null, null, "date DESC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String sender = cursor.getString(0);
                    String body = cursor.getString(1);
                    long date = cursor.getLong(2);
                    SmsModel sms = new SmsModel(sender, body, date);
                    smsList.add(sms);
                    db.insertSms(sms);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean containsSms(List<SmsModel> list, SmsModel sms) {
        for (SmsModel s : list) {
            if (s.getSender().equals(sms.getSender()) &&
                s.getBody().equals(sms.getBody()) &&
                s.getDate() == sms.getDate()) {
                return true;
            }
        }
        return false;
    }

    private void filterSms(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(smsList);
        } else {
            for (SmsModel s : smsList) {
                if (s.getSender().toLowerCase().contains(query.toLowerCase()) ||
                    s.getBody().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(s);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void exportToCsv() {
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String filename = "sms_backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date()) + ".csv";
            File file = new File(dir, filename);
            FileWriter writer = new FileWriter(file);
            writer.write("Sender,Message,Date\n");
            for (SmsModel s : smsList) {
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()).format(new Date(s.getDate()));
                writer.write("\"" + s.getSender() + "\",\"" +
                    s.getBody().replace("\"", "'") + "\",\"" + date + "\"\n");
            }
            writer.close();
            Toast.makeText(this, "Exported: " + filename, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
