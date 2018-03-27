package com.mst.karsac.messages;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.mst.karsac.R;

public class InboxActivity extends AppCompatActivity {

    private static final int CHOOSE_FILE_RESULT_CODE = 101;

    RecyclerView msgRecyclerview;
    List<Messages> messagesList;
    MsgAdapter msgAdapter;
    String ownMacAddr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        msgRecyclerview = findViewById(R.id.recycler_msgs);
        messagesList = new ArrayList<>();
        msgAdapter = new MsgAdapter(this, messagesList);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        ownMacAddr = info.getMacAddress();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        msgRecyclerview.setLayoutManager(layoutManager);
        msgRecyclerview.setItemAnimator(new DefaultItemAnimator());
        msgRecyclerview.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        msgRecyclerview.setAdapter(msgAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_gallery);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CHOOSE_FILE_RESULT_CODE )
        {
            Uri uri = data.getData();
            if(uri != null)
            {
                Log.d("INBOX", "Yeah made it!");
                Messages messages = new Messages();
                File file = new File(uri.getPath());
                messages.type = 0;
                messages.lon = 0;
                messages.lat = 0;
                messages.rating = 0;
                messages.size = file.length()/1024;
                messages.destAddr = "Not set";
                messages.sourceMac = ownMacAddr;
                messages.format = FilenameUtils.getExtension(file.getName());
                messages.fileName = FilenameUtils.getName(file.getName());
                messages.tagsForCurrentImg = "None as of now !";

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                messages.timestamp = String.valueOf(sdf.format(file.lastModified()));
                messages.imgPath = uri.toString();

                msgAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inbox_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.spinner_inbox);
        Spinner spinner_inbox = (Spinner) menuItem.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.messages_drop, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_inbox.setAdapter(adapter);
        spinner_inbox.setPadding(4,0,4,0);
        return true;
    }
}
