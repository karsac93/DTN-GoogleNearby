package com.mst.karsac.messages;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.R;

/**
 * Created by ks2ht on 3/25/2018.
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgViewHolder> {
    public static final String MSG_KEY = "msg";
    Context mContext;
    List<Messages> messagesList = new ArrayList<>();
    MyListener listener;

    public MsgAdapter(Context mContext, List<Messages> messagesList) {
        this.mContext = mContext;
        this.messagesList = messagesList;
        this.listener = (MyListener) mContext;
    }

    @Override
    public MsgViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.msg_single_row, null);
        return new MsgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MsgViewHolder holder, int position) {
        final Messages msg = messagesList.get(position);
        holder.fileName.setText("Filename : " + msg.fileName);
        holder.timestamp.setText("Timestamp : " + msg.timestamp);

        if(msg.type == 0){
            holder.rating.setVisibility(View.GONE);
        }

        holder.rating.setText("Rating : " + String.valueOf(msg.rating));

        Glide.with(mContext).load(new File(msg.imgPath).toString()).into(holder.thumbnail);

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageDetail.class);
                intent.putExtra("msg", msg);
                Bundle bundle = new Bundle();
                bundle.putParcelable(MSG_KEY, msg);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MSGADAPTER", "onclick");
                PopupMenu popupMenu = new PopupMenu(mContext, holder.overflow);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.popup_menu, popupMenu.getMenu());
                Menu menu = popupMenu.getMenu();
                if(msg.type == 1)
                    menu.findItem(R.id.rate).setVisible(false);
                else
                    menu.findItem(R.id.rate).setVisible(true);

                popupMenu.setOnMenuItemClickListener(new MyMenuItemClickListener(msg));
                popupMenu.show();
            }
        });


    }

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        Messages msg;


        public MyMenuItemClickListener(Messages msg) {
            this.msg = msg;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navigate:
                    LatLng closest = new LatLng(msg.lat, msg.lon);
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + closest.latitude + "," + closest.longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    mContext.startActivity(mapIntent);
                    return true;
                case R.id.delete:
                    DbHelper dbHelper = GlobalApp.dbHelper;
                    dbHelper.deleteMsg(msg);
                    listener.callback(msg.type);
                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MsgViewHolder extends RecyclerView.ViewHolder{
        public TextView fileName, timestamp, rating;
        public ImageView thumbnail, overflow;
        public RelativeLayout relativeLayout;

        public MsgViewHolder(View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            overflow = itemView.findViewById(R.id.overflow_icon);
            fileName = itemView.findViewById(R.id.filename);
            timestamp = itemView.findViewById(R.id.timestamp);
            rating = itemView.findViewById(R.id.ratings);
            relativeLayout = itemView.findViewById(R.id.rel_touch);
        }
    }
}
