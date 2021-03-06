package com.mst.karsac;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.nearby.Nearby;
import com.mst.karsac.Bluetooth.BluetoothService;
import com.mst.karsac.NearbySupports.NearbyService;
import com.mst.karsac.NearbySupports.StatusListener;
import com.mst.karsac.Settings.Setting;
import com.mst.karsac.Utils.SharedPreferencesHandler;
import com.mst.karsac.cardivewProg.Album;
import com.mst.karsac.cardivewProg.AlbumsAdapter;
import com.mst.karsac.cardivewProg.GridSpacingItemDecoration;

public class MainActivity extends AppCompatActivity implements StatusListener{

    private RecyclerView recyclerView;
    private AlbumsAdapter adapter;
    private List<Album> albumList;
    FloatingActionButton fab_connect, fab_cancel;
    private static final int REQUEST_ENABLE_BT = 3;
    Intent intent, connectionIntent;

    public static final String TAG = "MainActivity";

    String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initCollapsingToolbar();
        checkPermissions();
        GlobalApp.mainActivityContext = this;
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        fab_connect = findViewById(R.id.fab_advertise);
        intent = new Intent(MainActivity.this, NearbyService.class);
        connectionIntent = new Intent(MainActivity.this, BluetoothService.class);

        fab_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String connection_type = SharedPreferencesHandler.
                        getConnectionPreferences(getApplicationContext(), SharedPreferencesHandler.CONNECTION_TYPE);
                if(connection_type.contains(SharedPreferencesHandler.NEARBY)) {
                    stopService(connectionIntent);
                    Toast.makeText(MainActivity.this, "Starting to discover and connect Nearby devices!", Toast.LENGTH_SHORT).show();
                    startService(intent);
                }
                else {
                    stopService(intent);
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if(mBluetoothAdapter == null){
                        Log.d(TAG, "Device does not have bluetooth feature");
                        Toast.makeText(MainActivity.this, "No bluetooth feature in this mobile!", Toast.LENGTH_SHORT).show();
                    }
                    if(!mBluetoothAdapter.isEnabled()){
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Starting to discover and connect nearby Bluetooth devices!", Toast.LENGTH_SHORT).show();
                        startService(connectionIntent);
                    }
                }
            }
        });

        fab_cancel = findViewById(R.id.cancel_connect);
        fab_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Stopping all the connections!", Toast.LENGTH_SHORT).show();
                stopService(intent);
                stopService(connectionIntent);
            }
        });

        albumList = new ArrayList<>();
        adapter = new AlbumsAdapter(this, albumList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        prepareAlbums();

        try {
            Glide.with(this).load(R.drawable.maxresdefault).into((ImageView) findViewById(R.id.backdrop));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    Toast.makeText(MainActivity.this, "Starting to discover and connect nearby Bluetooth devices!", Toast.LENGTH_SHORT).show();
                    stopService(intent);
                    startService(connectionIntent);
                }
        }
    }

    private void checkPermissions() {
        boolean flag = false;
        for(String permission : permissions)
        {
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                flag = true;
                break;
            }
        }
        if(flag)
            ActivityCompat.requestPermissions(this, permissions, 102);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, Setting.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //startService(new Intent(this, BackgroundService.class));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    public void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    /**
     * Adding few albums for testing
     */
    private void prepareAlbums() {
        int[] covers = new int[]{
                R.drawable.message,
                R.drawable.interest,
                R.drawable.sendip,
                R.drawable.rating};

        Album a = new Album("Messages", covers[0]);
        albumList.add(a);

        a = new Album("Interest", covers[1]);
        albumList.add(a);

        a = new Album("Send to IP", covers[2]);
        albumList.add(a);

        a = new Album("Device Ratings", covers[3]);
        albumList.add(a);



        adapter.notifyDataSetChanged();
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public void statusListener(String status) {
    }
}
