package com.mst.karsac.messages;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.ExifInterface;
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
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.R;

public class InboxActivity extends AppCompatActivity implements MyListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int CHOOSE_FILE_GAL_RESULT_CODE = 101;
    private static final int CHOOSE_FILE_CAM_RESULT_CODE = 102;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters


    RecyclerView msgRecyclerview;
    List<Messages> messagesList = new ArrayList<>();
    public static MsgAdapter msgAdapter;
    String ownMacAddr;
    DbHelper messageDbHelper;
    FloatingActionButton fab_cam, fab_gal;
    public static int type;
    Uri uriSavedImage;

    @Override
    protected void onResume() {
        super.onResume();
        notifyChange(type);
        checkPlayServices();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        msgRecyclerview = findViewById(R.id.recycler_msgs);
        msgAdapter = new MsgAdapter(this, messagesList);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final WifiInfo info = wifiManager.getConnectionInfo();
        ownMacAddr = info.getMacAddress();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        msgRecyclerview.setLayoutManager(layoutManager);
        msgRecyclerview.setItemAnimator(new DefaultItemAnimator());
        msgRecyclerview.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        msgRecyclerview.setAdapter(msgAdapter);
        messageDbHelper = GlobalApp.dbHelper;

        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        fab_gal = (FloatingActionButton) findViewById(R.id.fab_gallery);
        fab_gal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_FILE_GAL_RESULT_CODE);
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
                File image = new File(imagesFolder, "IMG_" + timestamp + ".jpg");
                uriSavedImage = Uri.fromFile(image);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                startActivityForResult(intent, CHOOSE_FILE_CAM_RESULT_CODE);
            }
        });
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_FILE_GAL_RESULT_CODE) {
            Uri uri = data.getData();
            if (uri != null) {
                Log.d("INBOX", "Yeah made it!");
                handleResult(uri);
            }
        }
        if (requestCode == CHOOSE_FILE_CAM_RESULT_CODE) {
            Log.d("INBOX", "Yeah made it!");

           handleResult(uriSavedImage);
        }
    }

    private void handleResult(Uri uri) {
        File file = new File(uri.getPath());
        int type = 0;
        double[] location = getLocation();
        double lon = location[0];
        double lat = location[1];
        int rating = 0;
        long size = file.length() / 1024;
        String destAddr = "Not set";
        String sourceMac = ownMacAddr;
        String format = FilenameUtils.getExtension(file.getName());
        String fileName = FilenameUtils.getName(file.getName());
        String tagsForCurrentImg = "";
        Log.d("NAme", FilenameUtils.getName(file.getName()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String timestamp = String.valueOf(sdf.format(file.lastModified()));
        Messages messages = new Messages(uri.toString(), timestamp, tagsForCurrentImg,
                fileName, format, sourceMac, destAddr, rating, 0, size, lat, lon,
                0.0f, 0.0f, 0.0f);

        messages.imgPath = uri.toString();
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

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

        } else {

           Log.d("TAG","Couldn't get the location. Make sure location is enabled on the device");
        }
        return new double[]{latitude, longitude};
    }

    @Override
    public void callback(int type) {
        notifyChange(type);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
