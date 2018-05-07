package com.mst.karsac.messages;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.R;
import com.mst.karsac.Utils.LocationHandler;

public class InboxActivity extends AppCompatActivity implements MyListener {

    private static final int CHOOSE_FILE_GAL_RESULT_CODE = 101;
    private static final int CHOOSE_FILE_CAM_RESULT_CODE = 102;

    RecyclerView msgRecyclerview;
    List<Messages> messagesList = new ArrayList<>();
    public static MsgAdapter msgAdapter;
    DbHelper messageDbHelper;
    FloatingActionButton fab_cam, fab_gal;
    public static int type;
    Uri uriSavedImage;
    File image;

    @Override
    protected void onResume() {
        super.onResume();
        notifyChange(type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        msgRecyclerview = findViewById(R.id.recycler_msgs);
        msgAdapter = new MsgAdapter(this, messagesList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        msgRecyclerview.setLayoutManager(layoutManager);
        msgRecyclerview.setItemAnimator(new DefaultItemAnimator());
        msgRecyclerview.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        msgRecyclerview.setAdapter(msgAdapter);
        messageDbHelper = GlobalApp.dbHelper;

        fab_gal = (FloatingActionButton) findViewById(R.id.fab_gallery);
        fab_gal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, CHOOSE_FILE_GAL_RESULT_CODE);
            }
        });

        fab_cam = findViewById(R.id.fab_cam);
        fab_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File imagesFolder = new File(Environment.getExternalStorageDirectory(), "DTN-Images");
                if (!imagesFolder.exists()) {
                    imagesFolder.mkdirs();
                }
                image = new File(imagesFolder, "IMG_" + timestamp + ".jpg");
                uriSavedImage = Uri.fromFile(image);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                startActivityForResult(intent, CHOOSE_FILE_CAM_RESULT_CODE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CHOOSE_FILE_GAL_RESULT_CODE) {
            Uri uri = data.getData();
            if (uri != null) {
                Log.d("INBOX", "Yeah made it!");
                handleResult(uri, image);
            }
        }
        if (requestCode == CHOOSE_FILE_CAM_RESULT_CODE) {
            Log.d("INBOX", "Yeah made it!");
            handleResult(uriSavedImage, image);
        }
    }


    private void handleResult(Uri uri, File image) {
        String img_path;
        if (image == null)
            img_path = getRealPathFromURI(this, uri);
        else
            img_path = image.getAbsolutePath();
        File file = new File(img_path);
        int type = 0;
        double[] location = new LocationHandler().getLocation(getApplicationContext());
        double lat = location[0];
        double lon = location[1];
        int rating = 0;
        long size = file.length() / 1024;
        String destAddr = "";
        String sourceMac = GlobalApp.source_mac;
        String fileName = file.getName();
        String format = file.getName().substring(fileName.indexOf(".") + 1, fileName.length());
        Log.d("Inbox", "Format:" + format);
        Log.d("Inbox", "Filename:" + fileName);
        String tagsForCurrentImg = "";
        String uuid = UUID.randomUUID().toString() + GlobalApp.source_mac;
        Log.d("InboxActivity", uuid);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String timestamp = String.valueOf(sdf.format(file.lastModified()));
        Log.d("timestamp:", timestamp);
        Messages messages = new Messages(uri.toString(), timestamp, tagsForCurrentImg,
                fileName, format, sourceMac, destAddr, rating, 0, size, lat, lon,
                0, 0, 0, uuid);
        messages.imgPath = img_path;
        Log.d("FilePath", messages.imgPath);
        messageDbHelper.insertImageRecord(messages);
        notifyChange(type);
    }

    public void notifyChange(int type) {
        messagesList.clear();
        messagesList.addAll(messageDbHelper.getAllMessages(type));
        Log.d("SIZE", messagesList.size() + " ");
        msgAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inbox_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.spinner_inbox);
        Spinner spinner_inbox = (Spinner) menuItem.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.messages_drop, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_inbox.setAdapter(adapter);
        spinner_inbox.setPadding(4, 0, 4, 0);

        spinner_inbox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int selectionNum, long l) {
                if (selectionNum == 1) {
                    fab_cam.setVisibility(View.GONE);
                    fab_gal.setVisibility(View.GONE);
                    type = 1;
                    notifyChange(type);

                } else {
                    fab_gal.setVisibility(View.VISIBLE);
                    fab_cam.setVisibility(View.VISIBLE);
                    type = 0;
                    notifyChange(type);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return true;
    }



    @Override
    public void callback(int type) {
        notifyChange(type);
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(contentUri, null, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


}
