package com.mst.karsac.messages;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.R;

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
        File file = new File(uri.getPath());
        int type = 0;
        double[] location = getLocation();
        double lat = location[0];
        double lon = location[1];
        int rating = 0;
        long size = file.length() / 1024;
        String destAddr = "";
        String sourceMac = GlobalApp.source_mac;
        String format = FilenameUtils.getExtension(file.getName());
        String fileName = FilenameUtils.getName(file.getName());
        String tagsForCurrentImg = "";
        Log.d("NAme", FilenameUtils.getName(file.getName()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String timestamp = String.valueOf(sdf.format(file.lastModified()));
        Log.d("timestamp:", timestamp);
        Messages messages = new Messages(uri.toString(), timestamp, tagsForCurrentImg,
                fileName, format, sourceMac, destAddr, rating, 0, size, lat, lon,
                0.0f, 0.0f, 0.0f);

        if (image == null)
            messages.imgPath = getRealPathFromURI(this, uri);
        else
            messages.imgPath = image.getAbsolutePath();
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

    private double[] getLocation() {
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return new double[]{latitude, longitude};
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
