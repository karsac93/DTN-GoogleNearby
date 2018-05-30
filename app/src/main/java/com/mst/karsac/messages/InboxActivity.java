package com.mst.karsac.messages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.R;
import com.mst.karsac.RatingsActivity.FinalRatings;
import com.mst.karsac.Utils.LocationHandler;
import com.mst.karsac.ratings.DeviceRating;
import com.mst.karsac.ratings.MessageRatings;
import com.mst.karsac.ratings.RatingsActivity;


import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.model.PredictRequest;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.OkHttpClient;

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
    boolean from_ratings = false;

    @Override
    protected void onResume() {
        super.onResume();
        notifyChange(type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            DeviceRating deviceRating = (DeviceRating) extras.get(FinalRatings.FROM_FINAL_RATING);
            from_ratings = true;
            fab_gal.setVisibility(View.GONE);
            fab_cam.setVisibility(View.GONE);
            List<MessageRatings> messageRatingsList = GlobalApp.dbHelper.getRatingsMessage(null, deviceRating.getDevice_uuid());
            for (MessageRatings ratings : messageRatingsList) {
                Log.d("INBOX", "getting the images");
                messagesList.add(GlobalApp.dbHelper.getSingleMessage(ratings.getMessage_unique_id()));
            }
        }
        msgRecyclerview = findViewById(R.id.recycler_msgs);
        Log.d("INBOX", "No. of images:" + messagesList.size());

        msgAdapter = new MsgAdapter(this, messagesList, from_ratings);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        msgRecyclerview.setLayoutManager(layoutManager);
        msgRecyclerview.setItemAnimator(new DefaultItemAnimator());
        msgRecyclerview.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        msgRecyclerview.setAdapter(msgAdapter);
        msgAdapter.notifyDataSetChanged();
        messageDbHelper = GlobalApp.dbHelper;

        msgRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (from_ratings == false) {
                    if (dy > 0 && (fab_gal.getVisibility() == View.VISIBLE && fab_cam.getVisibility() == View.VISIBLE)) {
                        fab_gal.hide();
                        fab_cam.hide();
                    } else if (dy < 0 && (fab_gal.getVisibility() != View.VISIBLE && fab_cam.getVisibility() != View.VISIBLE)) {
                        fab_gal.show();
                        fab_cam.show();
                    }
                }
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
            if (resultCode != RESULT_CANCELED && data.getData() != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    Log.d("INBOX", "Yeah made it!");
                    handleResult(uri, image);
                }
            }
        }
        if (resultCode != RESULT_CANCELED && requestCode == CHOOSE_FILE_CAM_RESULT_CODE) {
            Log.d("INBOX", "Yeah made it!");
            handleResult(uriSavedImage, image);
            image = null;
        }

    }


    private void handleResult(Uri uri, File image_local) {
        String img_path;
        if (image_local == null)
            img_path = getRealPathFromURI(this, uri);
        else {
            img_path = image_local.getAbsolutePath();
        }
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String timestamp = String.valueOf(sdf.format(file.lastModified()));
        Log.d("timestamp:", timestamp);
        String uuid = GlobalApp.source_mac + getCurrentTimestamp();
        Log.d("InboxActivity", uuid);
        Messages messages = new Messages(uri.toString(), timestamp, tagsForCurrentImg,
                fileName, format, sourceMac, destAddr, rating, 0, size, lat, lon,
                0, 0, 0, uuid);
        messages.imgPath = img_path;
        Log.d("FilePath", messages.imgPath);
        long id = messageDbHelper.insertImageRecord(messages);
        messages.id = String.valueOf(id);
        notifyChange(type);
        myRunnable runnable = new myRunnable(messages, id, this);
        new Thread(runnable).start();

    }

    private String getCurrentTimestamp() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);
        String curTimestamp = String.valueOf(year) + String.valueOf(month) + String.valueOf(day)
                + String.valueOf(hour) + String.valueOf(minute) + String.valueOf(second) +
                String.valueOf(millis);
        return curTimestamp;
    }

    class myRunnable implements Runnable {

        Messages msg;
        long id;
        Activity activity;

        public myRunnable(Messages msg, long id, Activity activity) {
            this.msg = msg;
            this.id = id;
            this.activity = activity;
        }

        @Override
        public void run() {
            try {
                final ClarifaiClient clarifaiClient = new ClarifaiBuilder("ffbca20f1d054a20a223c49c7d4ac4d7")
                        .client(new OkHttpClient()).buildSync();
                Log.d("hello", "Inside run");
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(InboxActivity.this, "Please wait, tags are being added!", Toast.LENGTH_SHORT).show();
                        msgRecyclerview.setClickable(false);
                    }
                });
                Model<Concept> generalModel = clarifaiClient.getDefaultModels().generalModel();
                PredictRequest<Concept> request = generalModel.predict().withInputs(ClarifaiInput.forImage(ClarifaiImage.of(new File(msg.imgPath))));
                List<ClarifaiOutput<Concept>> result = request.executeSync().get();
                Log.d("Inbox///////", result.get(0).data().get(0).name());
                if (result.get(0).data().size() > 0) {
                    String tags = result.get(0).data().get(0).name() + ", " + result.get(0).data().get(1).name();
                    msg.tagsForCurrentImg = tags;
                    GlobalApp.dbHelper.updateMsg(msg);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            notifyChange(0);
                            Toast.makeText(InboxActivity.this, "Tags added", Toast.LENGTH_SHORT).show();
                            msgRecyclerview.setClickable(true);
                        }
                    });
                    DbHelper dbHelper = GlobalApp.dbHelper;
                    msg.tagsForCurrentImg = tags;
                    String[] interests = tags.split(",");
                    for (String intrst : interests)
                        dbHelper.insertInterest(intrst.trim(), 0, 0.5f);
                }
            } catch (Exception e) {
                String tags = "design, illustration";
                DbHelper dbHelper = GlobalApp.dbHelper;
                msg.tagsForCurrentImg = tags;
                GlobalApp.dbHelper.updateMsg(msg);
                String[] interests = tags.split(",");
                for (String intrst : interests)
                    dbHelper.insertInterest(intrst.trim(), 0, 0.5f);
                e.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(InboxActivity.this, "Check the net connection, not able to add tags!", Toast.LENGTH_SHORT).show();
                        msgRecyclerview.setClickable(false);
                    }
                });
            }
        }

    }


    public void notifyChange(int type) {
        if (from_ratings == false) {
            messagesList.clear();
            messagesList.addAll(messageDbHelper.getAllMessages(type));
            Log.d("SIZE", messagesList.size() + " ");
            msgAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (from_ratings == false) {
            getMenuInflater().inflate(R.menu.inbox_menu, menu);

            MenuItem menuItem = menu.findItem(R.id.spinner_inbox);
            Spinner spinner_inbox = (Spinner) menuItem.getActionView();
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.messages_drop, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(R.layout.single_spinner_row);
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
        return false;
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
